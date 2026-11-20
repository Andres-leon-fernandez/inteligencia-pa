# API de Inteligencia PA

Backend Java de solo lectura con Spring Boot y arquitectura hexagonal, con dashboard HTML/CSS/JavaScript servido desde la misma aplicación. Consulta la colección `inteligencia_pa.emergencias_viales` en MongoDB Atlas.

Para la guía de usuario, interpretación de indicadores, carga y normalización del CSV, consulta [la guía completa del proyecto](../docs/GUIA_PROYECTO.md).

## Requisitos

- Java 21.
- URI MongoDB en el archivo `.env` del directorio padre (raíz del repositorio), bajo la clave `MONGODB_URI`.
- La IP del equipo permitida en Network Access de Atlas.

No copies la URI ni las credenciales a este directorio. Spring carga `../.env`; ese archivo permanece ignorado por Git.

## Ejecución en desarrollo

En Windows, ejecuta `run-api.bat` o desde esta carpeta:

```powershell
.\mvnw.cmd spring-boot:run
```

El modo de desarrollo carga Spring Boot DevTools para reiniciar la aplicación cuando cambian clases. Maven agrega directamente `src/main/resources` al classpath, así los cambios en HTML, CSS, JavaScript y GeoJSON se reflejan sin empaquetar ni recompilar. La URI MongoDB sigue leyéndose desde el archivo `.env` ubicado en la raíz del repositorio. Abre `http://localhost:8080/` cuando Spring indique que inició.

## JAR distribuible

Construye el JAR ejecutable con:

```powershell
Set-Location hexagonal-spring
.\mvnw.cmd -DskipTests package
```

Maven genera `target/inteligencia-pa-api-0.0.1-SNAPSHOT.jar`; el JAR distribuible incluido en el repositorio está en `dist/inteligencia-pa-api.jar`. El archivo `run-api.bat` siempre inicia la aplicación con Maven en modo desarrollo.

## Endpoints

- `GET http://localhost:8080/api/v1/emergencias`
- `GET http://localhost:8080/api/v1/emergencias?departamento=JUNIN&estado=INTERRUMPIDO`
- `GET http://localhost:8080/api/v1/emergencias/1`
- `GET http://localhost:8080/api/v1/indicadores/resumen`
- `GET http://localhost:8080/api/v1/indicadores/resumen?departamento=JUNIN&estado=RESTRINGIDO`

La búsqueda acepta los filtros `departamento`, `provincia`, `distrito`, `estado`, `evento`, `red`, `busqueda`, `desde`, `hasta`, `pagina`, `tamano`, `ordenarPor` (`fecha`, `estado`, `departamento`, `evento`) y `direccion` (`asc`, `desc`). `busqueda` busca coincidencias en el ID, ubicación, vía y descripción. Las fechas usan `YYYY-MM-DD`; `pagina` comienza en cero y `tamano` permite de 1 a 100.

El resumen de indicadores acepta filtros de fecha, `departamento`, `estado` y `evento`; incluye `fechaInicialDatos` y `fechaMaximaDatos` para informar el rango de fechas disponible en toda la colección.

La estructura hexagonal y responsabilidades se describen en [`docs/ARQUITECTURA.md`](docs/ARQUITECTURA.md).
