# Contexto del proyecto

## Idea

**Inteligencia PA** es una API con dashboard para explorar reportes de emergencias en la Red Vial Nacional del Perú. Permite consultar registros por ubicación, tipo de evento, estado de transitabilidad y periodo, y revisar su detalle.

El nombre del proyecto se mantiene como `Inteligencia PA`. Repositorio: <https://github.com/Andres-leon-fernandez/inteligencia-pa>.

## Pregunta que busca responder

¿Qué emergencias viales registradas requieren más atención según su ubicación, tipo de evento y estado de transitabilidad?

La aplicación resume registros de una copia del dataset y estados reportados a su fecha de corte. **No es un sistema de predicción de desastres ni declara oficialmente que una zona sea peligrosa.** El ranking por departamento indica concentración de reportes, no una clasificación oficial de riesgo.

## Decisiones ya tomadas

- Usar una fuente peruana de la Plataforma Nacional de Datos Abiertos.
- Fuente inicial: dataset de emergencias de la Red Vial Nacional publicado por el MTC.
- Persistencia: MongoDB.
- Backend: Spring Boot.
- Diseño: arquitectura hexagonal (puertos y adaptadores).
- Mantener el alcance pequeño: una fuente, consultas e indicadores descriptivos; sin Big Data.
- CSV inicial guardado en `data/emergencias-red-vial-mtc-2026.csv`.

## Datos disponibles en el CSV

El archivo inicial contiene 1,362 filas, 22 columnas y ocupa alrededor de 927 KB. Está separado por punto y coma y codificado en Windows-1252. El identificador de emergencia es único en esta copia. Hay coordenadas para los 1,362 registros. La fecha de corte del archivo es 2026-08-20; la fecha del evento debe leerse como día/mes/año. Consultar `FUENTE_DE_DATOS.md` para campos y limitaciones.

La fuente se publica con frecuencia semanal, por lo que el CSV local es una copia de una fecha concreta. No asumir que describe el estado vial en tiempo real. Antes de una entrega final, volver a descargar la versión vigente y actualizar la documentación de corte.

## Usuarios y decisiones de ejemplo

- Un operador consulta las emergencias activas con estado `INTERRUMPIDO` o `RESTRINGIDO`.
- Un responsable compara incidentes por departamento, ruta o tipo de evento.
- Un analista identifica meses y zonas con más reportes para orientar inspecciones o recursos.

Los indicadores describen lo que aparece en los reportes. La fuente no contiene población afectada ni nivel socioeconómico de los residentes; no inferir esos atributos. Si se desea incorporarlos más adelante, documentar y justificar una segunda fuente y una forma confiable de enlazarla.

## Alcance inicial sugerido

1. Cargar los registros del CSV a MongoDB.
2. Exponer endpoints REST para listar y filtrar emergencias por departamento, estado, evento y rango de fechas.
3. Exponer resúmenes: recuento por estado, departamento, tipo de evento y periodo.
4. Permitir obtener el detalle de una emergencia por su identificador.
5. Mantener la fuente, las limitaciones y la atribución visibles en el README.

## Fuera de alcance por ahora

- Predicción de huaicos, inundaciones u otros eventos.
- Afirmar un nivel de riesgo oficial o socioeconómico no publicado por la fuente.
- Ingesta automática semanal o conexión con sistemas del MTC.
- Autenticación, microservicios, streaming o arquitectura Big Data.
- Enriquecer con múltiples fuentes geográficas o demográficas.

## Cómo continuar si otro agente retoma el trabajo

1. Leer este archivo, `FUENTE_DE_DATOS.md` y `../hexagonal-spring/docs/ARQUITECTURA.md` antes de cambiar el alcance.
2. Revisar el CSV local y el importador. No asumir separador, codificación ni nombres de columnas convencionales.
3. Preguntar al usuario antes de conectar con una instancia que no sea local o de incorporar credenciales; nunca guardar secretos en Git.
4. Implementar una primera versión monolítica Spring Boot siguiendo los límites de la arquitectura propuesta.
5. Distinguir los reportes históricos y el último estado registrado de una predicción o estado vial en tiempo real.
