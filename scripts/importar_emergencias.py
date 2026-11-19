#!/usr/bin/env python3
"""Limpia el CSV público del MTC y hace upsert de sus filas en MongoDB."""

from __future__ import annotations

import argparse
import csv
import os
import re
import sys
from datetime import date, datetime, time, timezone
from pathlib import Path
from typing import Any

from dotenv import load_dotenv


PROJECT_ROOT = Path(__file__).resolve().parents[1]
DEFAULT_CSV = PROJECT_ROOT / "data" / "emergencias-red-vial-mtc-2026.csv"
load_dotenv(PROJECT_ROOT / ".env")

FIELD_MAP = {
    "ID_EMERGENCIA": "id_emergencia",
    "CODIGO_REPORTE": "codigo_reporte",
    "FECHA_OCURRENCIA": "fecha_ocurrencia",
    "CLASE_FENOMENO": "clase_fenomeno",
    "FENOMENO": "fenomeno",
    "EVENTO": "evento",
    "DEPARTEMENTO": "departamento",
    "PROVINCIA": "provincia",
    "DISTRITO": "distrito",
    "AFECTACION": "afectacion",
    "RED_AFECTADA": "red_afectada",
    "TRAMO_AFECADO": "tramo_afectado",
    "SECTOR_AFECTADO": "sector_afectado",
    "HECHOS": "hechos",
    "NOVEDADES": "novedades",
    "FUENTE": "fuente",
    "ESTADO_INICIAL": "estado_inicial",
    "FECHA_CIERRE": "fecha_cierre",
    "ESTADO_ACTUAL": "estado_actual",
    "LATITUD": "latitud",
    "LONGITUD": "longitud",
    "FECHA_CORTE": "fecha_corte",
}

DATE_FORMATS = {
    "FECHA_OCURRENCIA": "%d/%m/%Y",
    "FECHA_CIERRE": "%d/%m/%Y",
    "FECHA_CORTE": "%Y%m%d",
}


def parse_date(value: str, source_field: str) -> datetime | None:
    cleaned = value.strip()
    if not cleaned:
        return None
    parsed = datetime.strptime(cleaned, DATE_FORMATS[source_field]).date()
    return datetime.combine(parsed, time.min, tzinfo=timezone.utc)


def clean_text(value: str | None) -> str | None:
    if value is None:
        return None
    cleaned = re.sub(r"\s+", " ", value).strip()
    return cleaned or None


def parse_coordinate(value: str, field: str, line_number: int) -> float | None:
    cleaned = value.strip()
    if not cleaned:
        return None
    try:
        coordinate = float(cleaned.replace(",", "."))
    except ValueError as exc:
        raise ValueError(f"Línea {line_number}: coordenada inválida en {field}: {value!r}") from exc
    low, high = (-90.0, 90.0) if field == "LATITUD" else (-180.0, 180.0)
    if not low <= coordinate <= high:
        raise ValueError(f"Línea {line_number}: {field} fuera de rango: {coordinate}")
    return coordinate


def clean_record(raw: dict[str, str], line_number: int) -> dict[str, Any]:
    record: dict[str, Any] = {}
    for source_field, target_field in FIELD_MAP.items():
        value = raw.get(source_field, "")
        if source_field in DATE_FORMATS:
            try:
                record[target_field] = parse_date(value or "", source_field)
            except ValueError as exc:
                raise ValueError(
                    f"Línea {line_number}: fecha inválida en {source_field}: {value!r}"
                ) from exc
        elif source_field in ("LATITUD", "LONGITUD"):
            record[target_field] = parse_coordinate(value or "", source_field, line_number)
        else:
            record[target_field] = clean_text(value)

    if not record["id_emergencia"]:
        raise ValueError(f"Línea {line_number}: falta ID_EMERGENCIA")
    return record


def load_records(csv_path: Path) -> list[dict[str, Any]]:
    records: list[dict[str, Any]] = []
    seen_ids: set[str] = set()
    with csv_path.open("r", encoding="cp1252", newline="") as csv_file:
        reader = csv.DictReader(csv_file, delimiter=";")
        headers = set(reader.fieldnames or [])
        missing = sorted(set(FIELD_MAP) - headers)
        if missing:
            raise ValueError("Faltan columnas requeridas: " + ", ".join(missing))

        for line_number, raw in enumerate(reader, start=2):
            if not raw or not any(value and value.strip() for value in raw.values() if value):
                continue
            record = clean_record(raw, line_number)
            identifier = record["id_emergencia"]
            if identifier in seen_ids:
                raise ValueError(f"Línea {line_number}: ID_EMERGENCIA duplicado: {identifier}")
            seen_ids.add(identifier)
            records.append(record)
    return records


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--csv", type=Path, default=DEFAULT_CSV, help="Ruta al CSV del MTC")
    parser.add_argument("--mongo-uri", default=os.getenv("MONGODB_URI"), help="URI MongoDB; también puede definirse en MONGODB_URI")
    parser.add_argument("--database", default="inteligencia_pa", help="Base MongoDB (por defecto: inteligencia_pa)")
    parser.add_argument("--collection", default="emergencias_viales", help="Colección (por defecto: emergencias_viales)")
    parser.add_argument("--dry-run", action="store_true", help="Valida y resume el CSV sin conectarse a MongoDB")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    csv_path = args.csv.expanduser().resolve()
    if not csv_path.is_file():
        print(f"No existe el CSV: {csv_path}", file=sys.stderr)
        return 2

    try:
        records = load_records(csv_path)
    except (OSError, csv.Error, ValueError) as exc:
        print(f"No se pudo leer o limpiar el CSV: {exc}", file=sys.stderr)
        return 2

    if args.dry_run:
        print(f"CSV válido: {len(records)} documentos listos para importar desde {csv_path}")
        return 0

    if not args.mongo_uri:
        print("Configura MONGODB_URI o pasa --mongo-uri para conectarte a MongoDB.", file=sys.stderr)
        return 2

    try:
        from pymongo import MongoClient, ReplaceOne
    except ImportError:
        print("Falta PyMongo. Instálalo con: python -m pip install -r requirements.txt", file=sys.stderr)
        return 2

    try:
        with MongoClient(args.mongo_uri, serverSelectionTimeoutMS=5000) as client:
            client.admin.command("ping")
            collection = client[args.database][args.collection]
            collection.create_index("id_emergencia", unique=True)
            operations = [
                ReplaceOne({"id_emergencia": record["id_emergencia"]}, record, upsert=True)
                for record in records
            ]
            result = collection.bulk_write(operations, ordered=False) if operations else None
            inserted = result.upserted_count if result else 0
            updated = result.modified_count if result else 0
            print(
                f"Importación completa en {args.database}.{args.collection}: "
                f"{inserted} insertados, {updated} actualizados, {len(records)} leídos."
            )
    except Exception as exc:  # Reporta errores del cliente/servidor Mongo de forma legible.
        print(f"Falló la conexión o importación en MongoDB: {exc}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
