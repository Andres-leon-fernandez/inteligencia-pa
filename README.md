# Inteligencia PA

Proyecto académico de análisis descriptivo de emergencias en la Red Vial Nacional del Perú. Combina datos abiertos del MTC, un importador Python, MongoDB y una API/dashboard Java con arquitectura hexagonal.

## Empezar rápido

Requisitos: Java 21, Python 3.10+ para importar y una URI válida de MongoDB en `.env` en la raíz del repositorio. Consulta la [guía completa del proyecto](docs/GUIA_PROYECTO.md) para entender el problema, base de datos, normalización, arquitectura e interpretación de resultados.

Desde la raíz, instala dependencias de importación y prepara el archivo local de configuración:

```powershell
python -m pip install -r requirements.txt
Copy-Item .env.example .env
```

Edita `.env` localmente para definir `MONGODB_URI`. No lo subas a Git ni compartas sus credenciales.

Valida el CSV sin conectar a MongoDB y luego impórtalo:

```powershell
python scripts/importar_emergencias.py --dry-run
python scripts/importar_emergencias.py
```

Inicia la API y el dashboard:

```powershell
Set-Location hexagonal-spring
.\mvnw.cmd spring-boot:run
```

Abre `http://localhost:8080/`. Consulta [hexagonal-spring/README.md](hexagonal-spring/README.md) para requisitos y empaquetado.

## Qué resuelve y qué no

Permite explorar dónde se concentran los reportes del MTC, qué estados y eventos aparecen y revisar el detalle de cada emergencia. Los conteos describen registros de una copia del conjunto; **no son predicciones, niveles oficiales de riesgo, ni confirmaciones del estado vial en tiempo real**. Para presentar resultados y entender sus límites, revisa la sección de interpretación en la [guía](docs/GUIA_PROYECTO.md).

## API principal

- `GET /api/v1/emergencias`: búsqueda, filtros, paginación y ordenamiento. Filtros: `departamento`, `provincia`, `distrito`, `estado`, `evento`, `red`, `busqueda`, `desde` y `hasta`. Paginación: `pagina` (base cero), `tamano` (1–100). Orden: `ordenarPor=fecha|estado|departamento|evento` y `direccion=asc|desc`.
- `GET /api/v1/emergencias/{idEmergencia}`: detalle por identificador.
- `GET /api/v1/indicadores/resumen?desde=YYYY-MM-DD&hasta=YYYY-MM-DD`: total, conteos por estado/departamento/evento y fechas mínima/máxima disponibles.

Las fechas usan `YYYY-MM-DD`. El archivo fuente incluido contiene 1,362 registros y tiene corte documentado al 20 de agosto de 2026; consulta [FUENTE_DE_DATOS.md](docs/FUENTE_DE_DATOS.md) antes de reutilizarlo como dato actual.

## Documentación

- [Guía completa del proyecto](docs/GUIA_PROYECTO.md)
- [Contexto, alcance y usuarios](docs/CONTEXTO_PROYECTO.md)
- [Fuente y diccionario de datos](docs/FUENTE_DE_DATOS.md)
- [Arquitectura hexagonal](hexagonal-spring/docs/ARQUITECTURA.md)

## Fuente y atribución

Ministerio de Transportes y Comunicaciones, [Emergencias en la Red Vial Nacional](https://www.datosabiertos.gob.pe/dataset/emergencias-en-la-red-vial-nacional-ministerio-de-transportes-y-comunicaciones-%E2%80%93-mtc). La ficha indica licencia Open Data Commons Attribution License; atribuye al MTC y enlaza la ficha cuando se redistribuyan o presenten estos datos.
