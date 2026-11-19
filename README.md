# Inteligencia PA

Proyecto académico para explorar emergencias en la Red Vial Nacional del Perú y apoyar la priorización de atención vial. La primera entrega usará un CSV público del MTC, MongoDB y Spring Boot con arquitectura hexagonal.

## Estado

El backend API está implementado y compila. La fuente está descargada en `data/emergencias-red-vial-mtc-2026.csv` y sus 1,362 registros ya fueron importados a Atlas en `inteligencia_pa.emergencias_viales`. El importador reutilizable está en `scripts/importar_emergencias.py`.

El backend Java incluye una interfaz de dashboard HTML/CSS/JS servida por Spring Boot. Requiere Java 21 y usa Maven Wrapper, por lo que no hace falta instalar Maven globalmente.

## Ejecutar la API Java

Con `.env` configurado y acceso de red permitido en Atlas:

```powershell
Set-Location hexagonal-spring
.\mvnw.cmd spring-boot:run
```

Endpoints iniciales:

```text
GET http://localhost:8080/api/v1/emergencias
GET http://localhost:8080/api/v1/emergencias?departamento=JUNIN&estado=INTERRUMPIDO
GET http://localhost:8080/api/v1/emergencias/1
GET http://localhost:8080/api/v1/indicadores/resumen
```

La búsqueda devuelve 20 filas por defecto; `pagina` empieza en 0 y `tamano` acepta valores de 1 a 100. También se puede filtrar por `provincia`, `distrito`, `evento`, `red`, `busqueda`, `desde` y `hasta` (fechas `YYYY-MM-DD`).

El proyecto Java y su ejecutable están en [`hexagonal-spring/`](hexagonal-spring/README.md). Para construir el JAR ejecutable: `Set-Location hexagonal-spring; .\mvnw.cmd -DskipTests package`. Para iniciarlo en Windows, ejecuta `hexagonal-spring/run-api.bat`.

## Documentación para retomar el proyecto

- [Contexto del proyecto](docs/CONTEXTO_PROYECTO.md): objetivo, alcance y decisiones tomadas.
- [Fuente y diccionario de datos](docs/FUENTE_DE_DATOS.md): procedencia, corte, campos y limitaciones.
- [Arquitectura hexagonal](hexagonal-spring/docs/ARQUITECTURA.md): dominio, puertos, adaptadores, casos de uso y contrato REST.

## Importar el CSV a MongoDB

Requiere Python 3.10+ y MongoDB accesible desde la máquina.

```powershell
python -m pip install -r requirements.txt
Copy-Item .env.example .env
# Edita .env y coloca tu URI de MongoDB Atlas en MONGODB_URI.
python scripts/importar_emergencias.py
```

Por defecto se importa a `inteligencia_pa.emergencias_viales`. El proceso normaliza los campos, conserva los identificadores como texto y hace upsert por `id_emergencia`, así que repetir la importación actualiza documentos existentes sin borrar otros. Para validar la lectura del CSV sin conectarse a MongoDB:

```powershell
python scripts/importar_emergencias.py --dry-run
```

El archivo `.env` está excluido de Git. No compartas ni subas ese archivo. Consulta `python scripts/importar_emergencias.py --help` para cambiar archivo, base, colección o URI.

## Fuente

Ministerio de Transportes y Comunicaciones (MTC), [Emergencias en la Red Vial Nacional](https://www.datosabiertos.gob.pe/dataset/emergencias-en-la-red-vial-nacional-ministerio-de-transportes-y-comunicaciones-%E2%80%93-mtc). Licencia indicada en el portal: Open Data Commons Attribution License. Mantener la atribución al reutilizar los datos.
