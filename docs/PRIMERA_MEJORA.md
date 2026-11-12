# Primera mejora: reportes locales reales

## Lo que funciona

Crear reportes perdidos y encontrados, validar sus campos, conservar borradores al recrear la pantalla,
guardar en Room, listar, consultar detalle y cancelar. El detalle ya no lleva a coincidencias simuladas.
Los reportes siguen disponibles al cerrar la aplicación. No se solicitan permisos de ubicación ni internet.

Esta etapa es una demostración local explícita, no un inicio de sesión. Los reportes pertenecen a esta
instalación; no existe aislamiento por usuario todavía. No usarla para información confidencial.

## Cómo se reparte el trabajo

| Carpeta | Responsabilidad |
|---|---|
| `domain/model` | Reporte, tipos y objetos de valor; sin dependencias de Android |
| `domain/validation` | Formulario y errores identificados por campo |
| `domain/repository` | Contrato para observar, guardar y cancelar |
| `data/local/entity` | Clases concretas que Room almacena |
| `data/local/dao` | Interfaces de acceso a esas entidades; Room genera su implementación |
| `data/model` | DTO y conversión entre dominio, Room y JSON |
| `data/repository` | Coordina la transacción de reporte y pendiente |
| `ui/viewmodel` | Estado y acciones de cada pantalla |
| `ui/screens` | Compose: campos, botones, lista y detalle |
| `di` | Hilt construye y conecta las dependencias |

`Reporte` es una clase con identidad e invariantes. No tiene setters: `cancelar()` produce una nueva
versión con el mismo ID, sin cambiar la descripción. Los objetos de valor usan `data class` e invariantes.
`ReporteEntity`, `PendienteEntity` y los DTO son datos de persistencia/intercambio, no entidades de negocio.
No hay una interfaz por cada clase: el contrato importante es `ReporteRepository`.

## Flujo offline first

Pantalla → ViewModel → ReporteRepository → OfflineFirstReporteRepository → Room.

Una transacción guarda ReporteEntity y PendienteEntity. Si falla el pendiente, tampoco se conserva el
reporte. Gson transforma el DTO en JSON para la cola. Por ahora nadie envía esa cola por internet.
Se conserva una instantánea pendiente por reporte; al cancelar se reemplaza por su nueva versión.
La confirmación exige ID y versión para no borrar una actualización posterior. El contrato remoto
deberá aceptar estas instantáneas de forma idempotente antes de habilitar el sincronizador.

La UI observa Room mediante Flow y StateFlow. Al modificar la base, recibe el nuevo estado.
No elige entre una base local y otra remota según la conexión.

## Patrones y principios aplicados en esta etapa

- MVVM: UI, estado de pantalla y datos separados.
- Repository: la UI desconoce las consultas SQL y la cola de pendientes.
- Observación reactiva: Flow/StateFlow notifican cambios; sin crear un Observer manual.
- Inyección de dependencias: Hilt suministra repositorio y base de datos; Hilt es la herramienta.
- Encapsulamiento y SOLID proporcional: responsabilidades pequeñas, contrato independiente,
  reglas en dominio y dependencias concretas resueltas en infraestructura.
- DDD proporcional: identidad del reporte, objetos de valor y comportamiento de cancelación.

DDD, SOLID e inyección de dependencias no son patrones GoF. No se agregan clases abstractas,
factories, decorators o Composite solo para aumentar la cantidad de patrones.

## Cambios rápidos durante la exposición

| Cambio solicitado | Archivo principal |
|---|---|
| Título, etiqueta o mensaje | `res/values/strings.xml` |
| Colores | `ui/theme/Color.kt` y `Theme.kt` |
| Espaciados | `ui/theme/Dimensions.kt` |
| Campos o distribución del formulario | `ui/screens/ReportFormScreen.kt` |
| Restricción de un campo | `domain/validation/ValidarReporte.kt` y la invariante correspondiente |
| Acción al guardar | `ui/viewmodel/ReportFormViewModel.kt` |
| Reglas de cancelación | `domain/model/Reporte.kt` |
| Consultas locales | `data/local/dao/ReporteDao.kt` |
| Rutas entre pantallas | `navigation/AppNavigation.kt` |

Los comentarios de los nuevos métodos no superan una línea. Las pantallas existentes se conservaron;
coincidencias, verificación y recuperación todavía son prototipos y no están conectados al detalle real.

## Pendiente para las siguientes etapas

1. Configurar Supabase Auth con Google para cualquier cuenta admitida, sin restricción institucional.
2. Asociar reportes a usuarios reales y proteger el acceso mediante políticas del servidor.
3. Retrofit + Gson para la API REST de Supabase; URL y clave pública configurables, nunca service_role.
4. Adapter remoto, sincronizador con reintentos y pruebas de conflictos e idempotencia.
5. Coincidencias con características flexibles JSONB y evidencia privada separada de la información pública.
6. Custodia en un área universitaria, solicitud de recuperación y entrega validada presencialmente.
7. Evaluar Composite con operaciones individuales/grupos reales; todavía no se implementa.

No hay Firebase ni mapas en esta implementación. El área escrita por el usuario es una intención de
entrega, no una confirmación de recepción. La aplicación no comprueba físicamente la propiedad.

## Validación

Pruebas unitarias de invariantes, validaciones y conversiones Gson/Room. Pruebas Android de persistencia
al reabrir la base, reversión de transacciones y confirmación de pendientes por versión.
Ejecutar `:app:testDebugUnitTest`, `:app:assembleDebug` y, con dispositivo, `:app:connectedDebugAndroidTest`.


Las transacciones también se prueban en la computadora con Robolectric (Android 35), sin emulador.
El borrador conserva su ID y el guardado es idempotente: reintentar el mismo reporte no duplica datos.


## Resultado de la verificación (11 de noviembre de 2026)

- APK de depuración generado correctamente.
- 14 pruebas locales aprobadas: 8 de dominio, 1 de conversiones, 4 de Room y 1 original del proyecto.
- Análisis lint aprobado sin errores; quedan avisos de versiones y recursos del proyecto.
- Pruebas Android instrumentadas compiladas, pero no ejecutadas: no había dispositivo conectado
  y el emulador no pudo iniciar por falta de aceleración de hardware disponible.
- La interfaz todavía requiere comprobación en un dispositivo Android.

Al conectar cuentas reales, se deberá decidir explícitamente si se asocian los datos de demostración
al usuario autenticado; no se enviarán automáticamente como si ya tuvieran propietario verificado.
