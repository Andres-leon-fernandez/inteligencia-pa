# Arquitectura del backend

## Idea central

Inteligencia PA es una aplicación Spring Boot pequeña, organizada en arquitectura hexagonal. Expone una API de consulta y un dashboard para explorar reportes de emergencias viales del MTC guardados en MongoDB. La arquitectura separa las reglas y casos de uso de las tecnologías de entrada y almacenamiento.

```text
CSV MTC → importador Python → MongoDB Atlas
                                  ↑
Dashboard → HTTP/JSON → REST → puerto de entrada → caso de uso
                                          → puerto de salida → adaptador Mongo
```

La carga del CSV ocurre antes e independiente de la API: el importador no es un adaptador de escritura en tiempo real. La API de esta fase consulta los documentos importados.

## Capas y responsabilidades

| Parte | Ubicación | Responsabilidad |
|---|---|---|
| Dominio | `domain/model` | Representa `EmergenciaVial`, `UbicacionVial` y `Coordenadas`; no conoce Spring, HTTP ni MongoDB. |
| Aplicación | `application/service`, `application/model` | Ejecuta buscar, obtener detalle y resumir indicadores; aplica validaciones y coordina puertos. |
| Puerto de entrada | `application/port/in` | Contrato de lo que se puede pedir: búsqueda, detalle e indicadores. |
| Puerto de salida | `application/port/out` | Contrato de los datos que la aplicación necesita consultar. |
| Adaptador REST | `adapter/in/rest` | Convierte parámetros HTTP a modelos de consulta y el resultado a JSON DTO. |
| Adaptador Mongo | `adapter/out/mongodb` | Traduce filtros y agregaciones a MongoTemplate; mapea documentos Mongo a dominio. |
| Configuración | `config/UseCaseConfiguration.java` | Conecta los servicios de aplicación con las implementaciones de puertos. |

Dirección de dependencias: los adaptadores conocen los contratos del núcleo; el dominio no depende de los adaptadores. Por eso se puede reemplazar REST o Mongo sin mover la lógica de los casos de uso.

## Modelos: por qué hay varios

- `EmergenciaVial`, `UbicacionVial`, `Coordenadas`: conceptos del dominio, expresados en términos del problema vial.
- `EmergenciaMongoDocument`: forma persistida, con nombres y tipos propios del esquema Mongo. `id_emergencia` es la clave funcional; `_id` es el ID técnico.
- `FiltroEmergencias`, `FiltroIndicadores`, `PaginaResultado`, `ResumenIndicadores`, `IndicadorItem`: entradas y resultados de consultas de aplicación.
- DTO REST: contrato JSON de la API. Evita exponer directamente el documento Mongo.

No son copias arbitrarias: cada modelo protege un límite. El mapper adapta entre Mongo y dominio; el mapper REST adapta entre dominio/aplicación y HTTP.

## Recorrido de una consulta

Ejemplo: filtrar por `estado=RESTRINGIDO`.

1. El navegador envía `GET /api/v1/emergencias?estado=RESTRINGIDO`.
2. `EmergenciaController` construye `FiltroEmergencias` y llama a `BuscarEmergenciasUseCase`.
3. `BuscarEmergenciasService` valida paginación y delega en `EmergenciaVialQueryPort`.
4. `EmergenciaMongoAdapter` implementa el puerto, arma el criterio Mongo y obtiene documentos.
5. `EmergenciaMongoMapper` convierte los documentos a `EmergenciaVial`.
6. El controlador entrega JSON paginado; el dashboard actualiza tabla o visualizaciones.

Indicadores recorren el mismo límite: `IndicadoresController` → `ConsultarIndicadoresUseCase` → `IndicadoresQueryPort` → agregaciones Mongo. El resumen acepta fechas y filtros de departamento, estado y evento.

## Cómo encaja la importación de datos

`scripts/importar_emergencias.py` es el proceso ETL inicial, fuera del backend Java:

1. **Extrae/lee:** CSV del MTC, con codificación y separador conocidos.
2. **Transforma:** valida columnas e IDs, normaliza encabezados/texto, convierte fechas y coordenadas, y revisa duplicados.
3. **Carga:** conecta con `MONGODB_URI`, crea índice único por `id_emergencia` y hace upsert por lotes en `inteligencia_pa.emergencias_viales`.
4. **Consulta:** la API lee esa colección a través del puerto de salida y el adaptador Mongo.

La hexagonal gobierna las consultas de la aplicación; el importador resuelve la preparación y carga de la fuente. Separarlos permite repetir la importación sin acoplar el caso de uso al formato del CSV. El upsert inserta o reemplaza IDs presentes; no borra automáticamente IDs ausentes en una descarga nueva.

## Ejecutar y verificar

Desde Windows, en la raíz: configurar `MONGODB_URI` en `.env`, validar primero con `python scripts/importar_emergencias.py --dry-run`; importar con `python scripts/importar_emergencias.py`. Luego ejecutar desde `hexagonal-spring` con `..\run-api.bat` o `..\mvnw.cmd spring-boot:run` según la consola actual. Abrir `http://localhost:8080/`.

Rutas principales: `GET /api/v1/emergencias`, `GET /api/v1/emergencias/{idEmergencia}` y `GET /api/v1/indicadores/resumen`. Swagger no está incluido en esta fase.

## Límite de interpretación

Los conteos describen los registros de la copia importada; no son conteos de personas, predicciones ni una clasificación oficial de riesgo. `estado_actual` significa último estado anotado en la fuente y puede no ser el estado vigente. Atribuir los datos al MTC y mencionar siempre el corte.
