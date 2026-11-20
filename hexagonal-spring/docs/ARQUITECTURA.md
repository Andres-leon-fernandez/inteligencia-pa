# Arquitectura hexagonal implementada

## Alcance de esta fase

API REST de solo lectura y dashboard HTML/CSS/JavaScript para consultar emergencias viales del MTC almacenadas en MongoDB. El CSV se valida, normaliza e importa con `scripts/importar_emergencias.py` desde la raíz del repositorio.

La aplicación es un monolito modular Spring Boot. El dominio no depende de Spring, MongoDB, HTTP ni CSV. Las dependencias apuntan al núcleo:

```text
Cliente HTTP → Adaptador REST → Puerto de entrada → Caso de uso → Puerto de salida → Adaptador Mongo → MongoDB
```

## Estructura Java

```text
pe.inteligenciapa
├── domain.model
│   ├── EmergenciaVial.java
│   ├── UbicacionVial.java
│   └── Coordenadas.java
├── application
│   ├── model
│   │   ├── FiltroEmergencias.java
│   │   ├── PaginaResultado.java
│   │   ├── IndicadorItem.java
│   │   └── ResumenIndicadores.java
│   ├── port.in
│   │   ├── BuscarEmergenciasUseCase.java
│   │   ├── ObtenerEmergenciaUseCase.java
│   │   └── ConsultarIndicadoresUseCase.java
│   ├── port.out
│   │   ├── EmergenciaVialQueryPort.java
│   │   └── IndicadoresQueryPort.java
│   └── service
├── adapter.in.rest
│   ├── EmergenciaController.java
│   ├── IndicadoresController.java
│   ├── ApiExceptionHandler.java
│   ├── dto
│   └── mapper
├── adapter.out.mongodb
│   ├── EmergenciaMongoAdapter.java
│   ├── document/EmergenciaMongoDocument.java
│   └── mapper/EmergenciaMongoMapper.java
└── config/UseCaseConfiguration.java
```

### Modelos

- `EmergenciaVial`: reporte del MTC, con ID, fechas, evento, fenómeno, afectación, carretera, estado, fuente y ubicación.
- `UbicacionVial`: departamento, provincia, distrito y coordenadas del reporte.
- `Coordenadas`: latitud y longitud, validadas dentro de sus rangos geográficos.
- `EmergenciaMongoDocument`: forma concreta del documento Mongo. Mantiene los nombres snake_case existentes y el `_id` técnico separado de `id_emergencia`.
- DTOs REST: forma pública del JSON; no se filtra el documento Mongo ni se expone el `_id` interno.
- Modelos de consulta: filtro, página y resultados agregados usados por la aplicación.

## Puertos y adaptadores

**Puertos de entrada** son contratos que describen qué puede pedir un cliente:

- `BuscarEmergenciasUseCase`: consulta filtrada y paginada.
- `ObtenerEmergenciaUseCase`: detalle por `id_emergencia`.
- `ConsultarIndicadoresUseCase`: resumen por periodo.

Los servicios de aplicación implementan estos contratos y coordinan la operación. No tienen anotaciones Spring.

**Puertos de salida** son contratos que expresan qué datos necesita la aplicación:

- `EmergenciaVialQueryPort`: búsqueda y detalle.
- `IndicadoresQueryPort`: total y agrupaciones de reportes.

`EmergenciaMongoAdapter` implementa los dos puertos mediante `MongoTemplate`. `EmergenciaMongoMapper` traduce Mongo ↔ dominio. `UseCaseConfiguration` registra los casos de uso como beans y conecta las implementaciones.

El controlador REST recibe HTTP, crea modelos de consulta y llama a un puerto de entrada. Nunca accede directamente a Mongo. El adaptador Mongo traduce las consultas a criterios/aggregations de MongoDB y devuelve modelos de dominio o resultados de aplicación.

## Endpoints implementados

- `GET /api/v1/emergencias`: búsqueda paginada. Filtros opcionales: `departamento`, `provincia`, `distrito`, `estado`, `evento`, `red`, `busqueda`, `desde` y `hasta`. `busqueda` busca por ID, evento, ubicación, vía y descripciones. Parámetros de página: `pagina` (inicia en 0) y `tamano` (1 a 100, por defecto 20). Orden: `ordenarPor` acepta `fecha`, `estado`, `departamento` o `evento`; `direccion` acepta `asc` o `desc`. Valores por defecto: `fecha` y `desc`. El adaptador usa una lista permitida de campos de Mongo.
- `GET /api/v1/emergencias/{idEmergencia}`: detalle por ID; devuelve 404 si no existe.
- `GET /api/v1/indicadores/resumen?desde=YYYY-MM-DD&hasta=YYYY-MM-DD`: total de reportes y conteos por estado, departamento y evento. Las fechas son opcionales. También devuelve `fechaInicialDatos` y `fechaMaximaDatos`, calculadas sobre la colección completa para que el dashboard inicialice el filtro de fechas con el rango realmente disponible.

Los indicadores cuentan reportes, no personas. El campo `estadoActual` es el último estado del archivo al `fechaCorte`, no necesariamente el estado vial actual. El ranking geográfico representa concentración de reportes y no mide el riesgo. No se predicen eventos.

## MongoDB y configuración

- Base: `inteligencia_pa`.
- Colección: `emergencias_viales`.
- La URI se lee de `MONGODB_URI`, que Spring carga desde el `.env` de la raíz (`spring.config.import=optional:file:../.env[.properties]` al ejecutar desde `hexagonal-spring`). `.env` está ignorado por Git.
- La colección tiene un índice único sobre `id_emergencia`, creado por el importador.

Spring Boot 4.1.2 usa Java 21 y Spring Data MongoDB para la persistencia. Spring Boot configura el cliente Mongo mediante `spring.mongodb.uri`; ver la [documentación oficial de MongoDB en Spring Boot](https://docs.spring.io/spring-boot/reference/data/nosql.html).

## Estado

Se implementaron el dominio, aplicación, adaptadores REST y Mongo, dashboard y configuración. El dashboard separa panel general y tabla de emergencias, permite paginar, ordenar, elegir el tamaño de página y alternar tema claro/oscuro. El JAR se puede construir con Maven Wrapper; el proyecto no incluye pruebas automatizadas en esta fase.
