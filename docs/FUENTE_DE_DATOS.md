# Fuente de datos y diccionario

## Procedencia

- **Título:** Emergencias en la Red Vial Nacional.
- **Entidad:** Ministerio de Transportes y Comunicaciones (MTC).
- **Portal:** [Plataforma Nacional de Datos Abiertos del Perú](https://www.datosabiertos.gob.pe/dataset/emergencias-en-la-red-vial-nacional-ministerio-de-transportes-y-comunicaciones-%E2%80%93-mtc).
- **Descripción del portal:** eventos que interrumpen o restringen la transitabilidad por daños en vías, puentes u otra infraestructura; incluye tipo de peligro, ruta, tramo, estado, ámbito geográfico y coordenadas.
- **Frecuencia de actualización indicada:** semanal.
- **Licencia indicada:** Open Data Commons Attribution License. Atribuir al MTC y enlazar la ficha al redistribuir o presentar análisis.
- **CSV descargado:** `data/emergencias-red-vial-mtc-2026.csv`.
- **Recurso de esta copia:** `Reporte Emergencias 01.01 al 20.08.2026 COES-MTC.csv`.
- **Fecha de corte informada por el CSV:** 20 de agosto de 2026 (`FECHA_CORTE`).
- **Tamaño:** 926,716 bytes (aprox. 927 KB).
- **Filas de datos:** 1,362.
- **Codificación:** Windows-1252 (cp1252).
- **Separador:** punto y coma (`;`).
- **Encabezados:** 22. Los nombres fuente `DEPARTEMENTO` y `TRAMO_AFECADO` aparecen así escritos en el CSV; el importador los normaliza como `departamento` y `tramo_afectado`.

La ficha del portal señala actualización semanal, pero la copia descargada tiene fecha de corte fija. `FECHA_CORTE` es el corte de la extracción y no la fecha del evento. Para mostrar actualidad, consultar la fecha de corte y volver a descargar el recurso cuando corresponda.

## Diccionario de campos

| Campo fuente | Campo Mongo sugerido | Significado / tratamiento |
|---|---|---|
| `ID_EMERGENCIA` | `id_emergencia` | Identificador de la emergencia. Guardar como texto, no como número. Clave de upsert. |
| `CODIGO_REPORTE` | `codigo_reporte` | Código del reporte. |
| `FECHA_OCURRENCIA` | `fecha_ocurrencia` | Fecha del evento en `dd/MM/yyyy`; convertir a fecha MongoDB. |
| `CLASE_FENOMENO` | `clase_fenomeno` | Clasificación amplia del fenómeno. |
| `FENOMENO` | `fenomeno` | Fenómeno reportado. |
| `EVENTO` | `evento` | Evento observado, por ejemplo huaico o accidente. |
| `DEPARTEMENTO` | `departamento` | Departamento; typo fuente preservado solo en CSV. |
| `PROVINCIA` | `provincia` | Provincia. |
| `DISTRITO` | `distrito` | Distrito. |
| `AFECTACION` | `afectacion` | Texto de la afectación reportada. |
| `RED_AFECTADA` | `red_afectada` | Código o ruta vial afectada. |
| `TRAMO_AFECADO` | `tramo_afectado` | Tramo vial; typo fuente corregido en el documento Mongo. |
| `SECTOR_AFECTADO` | `sector_afectado` | Sector reportado. |
| `HECHOS` | `hechos` | Descripción del evento y sus efectos. |
| `NOVEDADES` | `novedades` | Actualizaciones narrativas de atención. |
| `FUENTE` | `fuente` | Entidad que reporta o proporciona la información. |
| `ESTADO_INICIAL` | `estado_inicial` | Estado inicial de transitabilidad reportado. |
| `FECHA_CIERRE` | `fecha_cierre` | Fecha de cierre; vacío se importa como `null`. |
| `ESTADO_ACTUAL` | `estado_actual` | Último estado reportado en esta copia: por ejemplo `INTERRUMPIDO`, `RESTRINGIDO`, `NORMAL`. No implica estado actual a la fecha de consulta. |
| `LATITUD` | `latitud` | Coordenada decimal convertida a número. |
| `LONGITUD` | `longitud` | Coordenada decimal convertida a número. |
| `FECHA_CORTE` | `fecha_corte` | Fecha de corte `yyyyMMdd`; convertir a fecha MongoDB. |

El CSV contiene texto libre extenso en `HECHOS` y `NOVEDADES`. Para una primera versión basta almacenarlo y mostrarlo como detalle; no hace falta procesarlo con IA.

## Notas de calidad y límites de interpretación

- La copia descargada trae 1,362 IDs distintos y coordenadas no vacías en todas las filas.
- Las coordenadas describen localizaciones asociadas a los reportes; validar el significado exacto antes de usarlas como punto cartográfico preciso.
- Hay tres estados observados en esta copia: `INTERRUMPIDO`, `RESTRINGIDO` y `NORMAL`.
- El CSV documenta emergencias y afectaciones viales, no un mapa oficial de peligros futuros.
- No incluye nivel socioeconómico ni número de residentes afectados como variables estructuradas.
- No crear un `nivel_riesgo` que se presente como oficial. Si se define una puntuación del proyecto, especificar sus reglas y llamarla indicador de priorización.

## Límites departamentales del mapa

- **Fuente cartográfica:** [Instituto Geográfico Nacional (IGN), capa de Límites Departamentales](https://www.datosabiertos.gob.pe/dataset/limites-departamentales), publicada en el servicio cartográfico del [GeoServidor del MINAM](https://geoservidorperu.minam.gob.pe/arcgis/rest/services/ServicioCFOI/MapServer/15).
- **Escala referencial:** 1:100 000. Los límites se usan para orientar el mapa y permitir la selección de departamentos; no son límites político-administrativos para trámites ni decisiones legales.
- **Archivo web:** `hexagonal-spring/src/main/resources/static/data/peru-departamentos.geojson`, convertido a GeoJSON WGS84 y simplificado para carga en navegador.
- **Atribución:** IGN / MINAM. Los puntos del MTC se dibujan sobre esta referencia cartográfica; seleccionar un departamento solo filtra los reportes de esta aplicación.
