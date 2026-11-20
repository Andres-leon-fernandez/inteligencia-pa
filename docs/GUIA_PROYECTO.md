# Guía completa de Inteligencia PA

Esta guía explica qué problema aborda el proyecto, qué contiene la fuente, cómo se transforma y carga en MongoDB, cómo se organiza el código y cómo presentar los indicadores con una interpretación responsable.

## 1. Propósito

Inteligencia PA organiza reportes del MTC sobre emergencias en la Red Vial Nacional del Perú. Permite responder preguntas operativas como:

- ¿En qué departamentos se concentran más reportes durante el periodo elegido?
- ¿Cuántos reportes consignan estado interrumpido, restringido o normal?
- ¿Qué eventos aparecen con más frecuencia?
- ¿Qué tramo y qué información narrativa tiene un reporte concreto?

La aplicación busca ayudar a **encontrar registros que conviene revisar** y a comparar la información por periodo, lugar, evento y estado. No determina por sí sola qué zona es oficialmente peligrosa ni reemplaza la validación de la autoridad vial.

### Qué significa “priorizar” en este proyecto

El dashboard ordena y resume reportes. Por ejemplo, puede señalar que un departamento concentra el mayor número de registros en una selección de fechas. Eso es una **señal descriptiva para orientar una revisión**: se pueden abrir esos registros, leer sus estados y verificar sus novedades.

No se debe describir como predicción, causalidad, evaluación oficial de riesgo ni medida de personas afectadas. La fuente no incluye campos estructurados de población expuesta, daños económicos o severidad normalizada.

## 2. La analogía de una tienda: cómo explicar un indicador

En una tienda, una frase responsable sería: “En mayo, el producto A registró 120 ventas, más que los demás productos del periodo. Por eso aparece primero en el resumen. El dato muestra volumen de ventas, pero no demuestra por qué se vendió más; para explicar la causa habría que revisar precio, stock, promoción u otra información”.

En Inteligencia PA se aplica el mismo razonamiento:

> “En el periodo seleccionado, Cajamarca reúne 134 de los 1,362 reportes (9.8%), el mayor conteo entre departamentos. Se muestra primero porque tiene más registros en esta copia. Conviene revisar los estados y detalles de esos reportes. El conteo no demuestra que Cajamarca tenga mayor riesgo ni explica por qué se generaron los reportes”.

La cifra anterior ilustra la copia documentada con corte al 20 de agosto de 2026. El resultado del dashboard puede cambiar según el rango de fechas, los filtros y la versión del CSV cargada.

### Fórmula de la concentración

`porcentaje del departamento = reportes del departamento / reportes del periodo × 100`

El porcentaje describe la distribución de reportes dentro del periodo consultado; no es una probabilidad ni una tasa de riesgo.

## 3. Cómo leer el dashboard

### Panel general

- **Reportes registrados:** cantidad de registros que cumplen el rango de fechas seleccionado.
- **Interrumpido / Restringido:** cantidad y proporción de reportes cuyo `estado_actual` coincide con ese estado en la copia cargada.
- **Último estado reportado:** distribución de los estados presentes en los registros consultados.
- **Departamentos con más registros:** ranking por conteo de reportes, no por población, longitud vial ni nivel de peligro.
- **Zona con más reportes:** explicación automática del departamento con el mayor conteo en el conjunto consultado y de por qué aparece destacado.

La zona destacada se calcula con `porDepartamento`, ordenado por la cantidad que devuelve la API. El texto indica el nombre, el conteo, el total del periodo y su proporción. Si dos zonas empatan, la primera que entrega la API aparece primero.

### Página de emergencias y tabla

La tabla permite buscar por texto y filtrar por departamento, estado y evento. Sus controles permiten ordenar por fecha, estado, departamento o evento, cambiar ascendente/descendente, elegir 10, 20, 50 o 100 filas, ir a la primera o última página y saltar a una página concreta.

El orden inicial es por fecha de ocurrencia descendente (más reciente primero). El paginado comienza en página `0` en la API, aunque en pantalla se presenta desde página `1`.

### Ficha de detalle: diferencia entre A y B

- **A · ¿Qué parte de la vía está afectada?** Corresponde a `AFECTACION`. Describe la infraestructura, carretera, tramo o sector señalado en el reporte. Responde principalmente **dónde y qué componente**.
- **B · ¿Qué ocurrió y qué impacto tuvo?** Corresponde a `HECHOS`. Resume el evento narrado y su consecuencia para la transitabilidad. Responde **qué pasó y qué efecto se reportó**.

Ejemplo de la ficha de la captura: A identifica la Red Vial Nacional PE-30A, el tramo Dv. Nazca–Pampa Chiri y el sector Km 24+800–Km 24+820. B explica que, por condiciones climáticas, se deslizó material del talud y se afectó la transitabilidad. La sección “Novedades” informa después que se realizaron trabajos de limpieza y se recuperó la transitabilidad, según la entidad que reportó.

Estos campos son texto fuente y pueden repetir ubicación o usar redacciones distintas; la ficha conserva su sentido, no los interpreta como hechos verificados independientemente.

## 4. Fuente y alcance de los datos

- **Entidad:** Ministerio de Transportes y Comunicaciones (MTC).
- **Dataset:** [Emergencias en la Red Vial Nacional](https://www.datosabiertos.gob.pe/dataset/emergencias-en-la-red-vial-nacional-ministerio-de-transportes-y-comunicaciones-%E2%80%93-mtc), publicado en la Plataforma Nacional de Datos Abiertos del Perú.
- **Copia incluida:** `data/emergencias-red-vial-mtc-2026.csv`.
- **Corte documentado:** 20 de agosto de 2026; la copia contiene 1,362 filas y 22 columnas.
- **Formato:** CSV separado por punto y coma, codificación Windows-1252 (cp1252).
- **Actualización del portal:** la ficha indica frecuencia semanal. El archivo dentro del repositorio es una instantánea, no una conexión en vivo.
- **Licencia indicada en el portal:** Open Data Commons Attribution License. Mantener atribución al MTC y enlace a la ficha.

La fecha de ocurrencia, la fecha de cierre y la fecha de corte tienen significados distintos. `FECHA_CORTE` describe la extracción; no convierte `ESTADO_ACTUAL` en el estado vigente hoy.

Para el diccionario de los 22 campos fuente, tratamiento y límites, consulta [FUENTE_DE_DATOS.md](FUENTE_DE_DATOS.md).

## 5. MongoDB: base, colección y credenciales

- Base: `inteligencia_pa`.
- Colección: `emergencias_viales`.
- Clave funcional única: `id_emergencia` (cadena de texto).
- El campo `_id` es el identificador técnico que MongoDB administra; no se expone como identificador de negocio en la API.
- El importador crea un índice único en `id_emergencia` para evitar duplicar la misma emergencia.

La aplicación Java lee `MONGODB_URI` del archivo `.env` de la raíz. Al ejecutar desde `hexagonal-spring`, Spring importa `../.env`, y usa `spring.mongodb.database=inteligencia_pa`. El importador Python también carga el `.env` de la raíz. El archivo `.env` está ignorado por Git: no subirlo, copiarlo a documentación ni pegar credenciales en capturas o mensajes.

Una URI de Atlas normalmente comienza con `mongodb+srv://`; una conexión local normalmente comienza con `mongodb://`. Debe ser la URI real del clúster y su usuario requiere permisos en la base. Atlas también debe permitir la IP del equipo en Network Access. No incluyas usuario o contraseña en los argumentos de comandos que puedan quedar en el historial.

## 6. Carga y normalización del CSV

El importador reutilizable está en `scripts/importar_emergencias.py`. Sus pasos son:

1. Lee el archivo como `cp1252` y usa `;` como delimitador.
2. Comprueba que están las 22 columnas fuente esperadas.
3. Omite filas completamente vacías.
4. Traduce nombres fuente a `snake_case` en español. Corrige los errores tipográficos del origen `DEPARTEMENTO` → `departamento` y `TRAMO_AFECADO` → `tramo_afectado` en el documento importado.
5. Recorta espacios y colapsa secuencias de espacios/saltos de línea. Texto vacío se convierte en `null`.
6. Convierte `FECHA_OCURRENCIA` y `FECHA_CIERRE` desde `dd/MM/yyyy`; convierte `FECHA_CORTE` desde `yyyyMMdd`. Las fechas se guardan como fecha/hora UTC a medianoche. Las fechas vacías quedan como `null`.
7. Convierte latitud y longitud en números decimales, acepta coma decimal y valida rangos geográficos: latitud entre -90 y 90, longitud entre -180 y 180.
8. Exige que `ID_EMERGENCIA` exista y no se repita dentro del mismo CSV. Conserva el identificador como texto.
9. En modo normal, conecta a MongoDB, comprueba la conexión con `ping`, crea el índice único y ejecuta `ReplaceOne(..., upsert=True)` en lote no ordenado.

**Upsert** significa que una clave nueva se inserta y una existente se reemplaza con la versión de la fila del archivo. El proceso no elimina de MongoDB registros que ya existían y dejaron de aparecer en un nuevo CSV. Por ello, si la fuente publica una extracción que ya no incluye emergencias anteriores, esta rutina por sí sola no sincroniza eliminaciones.

El conteo informado como “actualizados” es `modified_count`: documentos existentes cuyo contenido cambió. Un documento encontrado pero idéntico puede no sumar como modificado.

### Importar en Windows PowerShell

Desde la raíz del repositorio:

```powershell
python -m pip install -r requirements.txt
Copy-Item .env.example .env
```

Edita `.env` localmente y coloca la URI en `MONGODB_URI`. Luego valida el CSV sin conectar a MongoDB:

```powershell
python scripts/importar_emergencias.py --dry-run
```

Si la validación es correcta y Atlas está accesible, importa:

```powershell
python scripts/importar_emergencias.py
```

Para consultar opciones:

```powershell
python scripts/importar_emergencias.py --help
```

Se pueden cambiar el archivo, base y colección con `--csv`, `--database` y `--collection`. El script necesita `MONGODB_URI` o la opción `--mongo-uri`.

## 7. Ejecutar la API y el dashboard

Requisitos: Java 21, PowerShell en Windows, `.env` válido y conexión permitida a Atlas.

```powershell
Set-Location hexagonal-spring
.\mvnw.cmd spring-boot:run
```

Abre `http://localhost:8080/`. Para detener el servidor, presiona `Ctrl+C` en la consola donde se está ejecutando.

Para generar el JAR ejecutable sin ejecutar pruebas:

```powershell
.\mvnw.cmd -DskipTests package
```

El JAR de compilación queda en `hexagonal-spring/target/`. `hexagonal-spring/run-api.bat` selecciona el artefacto local de `target` si está disponible; si no, usa `dist/inteligencia-pa-api.jar`.

### Configurar Java 21 si Maven ve Java 17

`java -version` y `mvnw -version` pueden mostrar instalaciones diferentes si `JAVA_HOME` apunta a otro JDK. En PowerShell, se puede alinear la sesión usando el `javac` que ya está en el `PATH`:

```powershell
$javacPath = (Get-Command javac).Source
$env:JAVA_HOME = Split-Path (Split-Path $javacPath -Parent) -Parent
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd -version
```

Verifica que Maven muestre Java 21 y vuelve a ejecutar `.\mvnw.cmd spring-boot:run` desde esa misma ventana.

## 8. API REST

### Listar, filtrar, paginar y ordenar emergencias

`GET /api/v1/emergencias`

Parámetros opcionales:

| Parámetro | Uso | Ejemplo |
|---|---|---|
| `departamento` | Coincidencia por departamento | `departamento=JUNIN` |
| `provincia` / `distrito` | Filtrar ubicación | `provincia=SATIPO` |
| `estado` | Estado reportado | `estado=RESTRINGIDO` |
| `evento` | Tipo de evento | `evento=Derrumbe` |
| `red` | Código o nombre de carretera | `red=PE-30A` |
| `busqueda` | Busca en ID, evento, ubicación, vía y texto descriptivo | `busqueda=puente` |
| `desde` / `hasta` | Rango inclusivo por fecha de ocurrencia, formato `YYYY-MM-DD` | `desde=2026-05-01&hasta=2026-08-20` |
| `pagina` | Página base cero; predeterminado `0` | `pagina=1` |
| `tamano` | Filas por página; predeterminado `20`, rango `1–100` | `tamano=50` |
| `ordenarPor` | `fecha`, `estado`, `departamento` o `evento`; predeterminado `fecha` | `ordenarPor=estado` |
| `direccion` | `asc` o `desc`; predeterminado `desc` | `direccion=asc` |

Ejemplo:

```text
GET /api/v1/emergencias?departamento=JUNIN&estado=RESTRINGIDO&desde=2026-05-01&hasta=2026-08-20&pagina=0&tamano=20&ordenarPor=fecha&direccion=desc
```

Para fechas, desde/hasta son inclusivas por día. Los campos de orden se validan con una lista permitida para que el cliente no pueda ordenar por campos arbitrarios.

### Detalle de una emergencia

`GET /api/v1/emergencias/{idEmergencia}`

Busca por `id_emergencia`; devuelve 404 si no existe.

### Resumen de indicadores y rango disponible

`GET /api/v1/indicadores/resumen?desde=YYYY-MM-DD&hasta=YYYY-MM-DD`

Devuelve el total y las agrupaciones por estado, departamento y evento dentro del rango opcional. También informa `fechaInicialDatos` y `fechaMaximaDatos`: fecha mínima y máxima de ocurrencia presentes en toda la colección. La interfaz utiliza esos dos valores para inicializar y limitar los campos “Desde” y “Hasta”.

Los nombres de categorías se devuelven en `porEstado`, `porDepartamento` y `porEvento`, cada uno con `categoria` y `cantidad`. Son conteos de documentos, no de personas.

## 9. Estructura del repositorio

```text
inteligencia-pa/
├── data/                         CSV de la fuente MTC
├── docs/                         Contexto, fuente y guía completa
├── scripts/importar_emergencias.py  Validación, normalización e importación
├── requirements.txt              Dependencias del importador
├── .env.example                  Plantilla de variables; no contiene secretos
├── README.md                     Inicio rápido
└── hexagonal-spring/
    ├── src/main/java/pe/inteligenciapa/
    │   ├── domain/               Modelo de dominio independiente
    │   ├── application/          Casos de uso, puertos y modelos de consulta
    │   ├── adapter/in/rest/      Controladores HTTP, DTO y mapeadores
    │   ├── adapter/out/mongodb/  Consultas Mongo, documentos y mapeadores
    │   └── config/               Conexión de casos de uso
    ├── src/main/resources/
    │   ├── application.properties
    │   └── static/               Dashboard HTML, CSS y JavaScript
    ├── docs/ARQUITECTURA.md      Detalle de capas y responsabilidades
    ├── pom.xml                   Dependencias y configuración Maven
    ├── mvnw.cmd                  Maven Wrapper para Windows
    └── run-api.bat               Inicio del JAR disponible
```

## 10. Arquitectura y recorrido de una petición

El backend es un monolito modular Spring Boot con arquitectura hexagonal. El dominio no depende de Spring, MongoDB ni HTTP. El recorrido principal es:

```text
Navegador/cliente → Controlador REST → Puerto de entrada → Servicio de aplicación
                  → Puerto de salida → Adaptador MongoTemplate → MongoDB Atlas
```

- **Dominio:** `EmergenciaVial`, `UbicacionVial` y `Coordenadas` representan el problema vial.
- **Aplicación:** servicios de búsqueda, detalle e indicadores coordinan casos de uso.
- **Puertos de entrada:** describen las acciones que ofrecen la aplicación.
- **Puertos de salida:** describen los datos que necesita el núcleo.
- **Adaptador REST:** traduce query params/JSON y no accede directamente a MongoDB.
- **Adaptador MongoDB:** convierte filtros en criterios Mongo y agregaciones, y mapea documentos al dominio.
- **Dashboard:** HTML/CSS/JavaScript estático servido por Spring Boot; consume los mismos endpoints REST.

Para el mapa de paquetes y contratos técnicos, consulta [ARQUITECTURA.md](../hexagonal-spring/docs/ARQUITECTURA.md).

## 11. Cómo comunicar los resultados

Una presentación clara de un indicador debe decir:

1. **Qué se contó:** registros de emergencia, no habitantes ni daños.
2. **Dónde:** departamento, provincia, distrito, carretera o tramo disponible.
3. **Cuándo:** rango consultado y fecha de corte de la fuente.
4. **Qué estado/evento:** según los campos reportados.
5. **Por qué aparece resaltado:** regla visible, por ejemplo mayor número de reportes del periodo.
6. **Qué acción sugiere:** revisar las fichas, estados y novedades de esos reportes.
7. **Qué no demuestra:** no atribuir causa, probabilidad de futuros eventos ni riesgo oficial sin otra evidencia.

### Ejemplos de redacción

**Correcto:** “Entre las fechas seleccionadas, Cajamarca registra 134 reportes, la cifra más alta por departamento. El tablero la resalta por volumen de registros. Conviene revisar cuáles aparecen interrumpidos o restringidos y consultar las novedades de cada ficha. La cifra no establece que el departamento tenga mayor riesgo”.

**Evitar:** “Cajamarca es el departamento más peligroso porque tiene más emergencias”. El conjunto no compara exposición vial, número de usuarios, kilómetros de carretera, severidad ni subregistro; por tanto, ese salto no está sustentado.

**Correcto sobre el estado:** “En esta copia, 116 reportes aparecen con estado restringido al corte del 20 de agosto de 2026”.

**Evitar:** “Hay 116 vías restringidas hoy”. El campo refleja lo reportado en la copia y puede quedar desactualizado.

## 12. Límites y futuras mejoras

- El dashboard presenta una instantánea de la fuente y solo cambia cuando se reemplaza/importa otra copia.
- No existe ingesta automática semanal ni consulta al MTC en tiempo real.
- Un conteo por departamento depende del número de reportes registrados; no ajusta por longitud de red vial ni exposición.
- Las coordenadas están asociadas a reportes; antes de presentarlas como ubicación cartográfica precisa se debe validar su semántica.
- Los textos `HECHOS` y `NOVEDADES` son narrativos y pueden variar en detalle y estilo.
- Para estimar riesgo o priorización más formal se necesitarían variables, reglas explícitas y validación con especialistas; habría que denominarlo indicador del proyecto, no clasificación oficial.
- La rutina de importación actual hace upsert, pero no elimina documentos ausentes de un archivo posterior; decidir y documentar una política de sincronización antes de automatizar actualizaciones.

## 13. Referencias internas

- [README del repositorio](../README.md): acceso rápido e instrucciones resumidas.
- [Contexto del proyecto](CONTEXTO_PROYECTO.md): objetivos, usuarios, decisiones y alcance.
- [Fuente y diccionario de datos](FUENTE_DE_DATOS.md): origen, campos y calidad.
- [Arquitectura hexagonal](../hexagonal-spring/docs/ARQUITECTURA.md): capas, puertos y adaptadores.
