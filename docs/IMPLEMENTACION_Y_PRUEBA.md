# UPT Encuentra: funcionamiento implementado

## Acceso
Se registra nombre, correo y contraseña mediante Supabase Auth. No se limita el dominio del correo. Supabase crea automáticamente el perfil con rol usuario. Google está desactivado y no se implementó recuperación de contraseña.

El primer registro e inicio de sesión necesitan internet. Supabase mantiene activa la confirmación por correo: confirma el enlace recibido y vuelve a iniciar sesión en la app. Una sesión ya iniciada se conserva cifrada; no se almacena la contraseña. Las antiguas cuentas Google no se migran: para probar un registro nuevo utiliza otro correo.

## Sin conexión
La pantalla observa Room. Guardar un reporte escribe reporte y pendiente en una transacción, incluso con internet. La foto se copia al almacenamiento privado del dispositivo. WorkManager envía los pendientes cuando hay red; primero sube la foto y después el reporte. La confirmación actualiza Room y retira solo la versión enviada. Un fallo conserva los datos para reintentar.

Las cuentas mantienen sus datos locales separados. Entrar sin internet abre un espacio local independiente: no crea usuarios ni envía sus datos al servidor. La importación de esos reportes a una cuenta requiere la confirmación existente en la pantalla inicial.

## Reportes y coincidencias
Los formularios piden al menos una característica particular: señal, accesorio o inscripción. Los hallazgos requieren foto. Las características publicadas no se editan; se permiten cambios de estado válidos.

El propietario de una pérdida abre Posibles coincidencias desde su detalle. No hay un catálogo de todos los hallazgos. En línea, Supabase compara las características privadas y entrega únicamente categoría, zona, fecha, puntaje y motivos generales. No entrega la descripción privada, la foto ni los detalles identificativos del hallazgo al solicitante.

Coincidir solamente en marca, modelo y color no alcanza. Primero se exige similitud en un campo particular; luego se suman criterios generales. El puntaje ordena candidatos y no demuestra propiedad. El algoritmo es comparación de palabras normalizadas, sin inteligencia artificial ni análisis de imágenes.

En el espacio local, MotorCoincidencias ejecuta la misma regla con los reportes del dispositivo para poder mostrar el flujo sin red. En una cuenta real, buscar candidatos nuevos requiere internet; los resultados ya descargados permanecen en Room.

## Solicitud, recepción y entrega
Desde una coincidencia se guarda una explicación privada de propiedad. Mis solicitudes observa su estado en Room. Sin red queda pendiente de envío; con red se registra y posteriormente se descargan sus cambios.

Solo una cuenta encargada ve Recepción y entrega. Registra el área que recibió físicamente el objeto, revisa el hallazgo y la explicación, valida presencialmente y confirma la devolución. La aplicación registra la validación; no verifica que un teléfono se desbloquee ni decide automáticamente quién es dueño. La entrega cierra ambos reportes y rechaza reclamos incompatibles. Estas acciones requieren conexión.

El rol encargado se asigna desde la administración, nunca desde el registro. Se autorizó y asignó a richardpodesta3@gmail.com. Al comprobarlo, esa cuenta antigua Google no tenía contraseña. No se añadió recuperación: antes de ingresar con correo necesitará definir una contraseña manualmente o utilizar una cuenta nueva con el rol autorizado. Una sesión antigua todavía válida puede seguir funcionando. Para actualizar el rol local, vuelve a iniciar sesión una vez que la cuenta tenga acceso por contraseña.

## Dónde modificar durante la exposición
| Cambio | Archivo o carpeta |
|---|---|
| Textos y distribución de cada pantalla | ui/screens/*Screen.kt |
| Colores y espacios compartidos | ui/theme/Color.kt y Dimensions.kt |
| Mensajes compartidos | res/values/strings.xml |
| Rutas y transiciones | navigation/Routes.kt y AppNavigation.kt |
| Campos obligatorios | domain/validation/ValidarReporte.kt |
| Estado y acciones de pantalla | ui/viewmodel |
| Regla local de comparación | domain/service/MotorCoincidencias.kt |
| Regla en línea de comparación | SQL: privado.puntuar y buscar_coincidencias |
| Guardado local y pendientes | data/repository/OfflineFirstReporteRepository.kt |
| Envío y recepción de cambios | data/sync/SincronizadorReportes.kt |
| Fotos locales y red | data/photos/AlmacenFotos.kt y data/sync/SincronizadorFotos.kt |
| Endpoints y conversión JSON con Gson | data/api/SupabaseApi.kt, data/model y di/NetworkModule.kt |
| Construcción e inyección de dependencias | di/AppModule.kt y NetworkModule.kt |

Se conserva MVVM, Repository, Adapter e inyección de dependencias con Hilt. Los DAO son interfaces que Room implementa; trabajan con entidades concretas. No se agregaron fábricas o clases abstractas sin necesidad. Los reportes encapsulan sus cambios y conservan identidad y versión.

## Prueba breve
1. Registrar e iniciar sesión con dos correos nuevos y confirmar los correos cuando Supabase lo solicite.
2. Cuenta A: publicar una pérdida con una señal específica, por ejemplo «rayón diagonal en esquina».
3. Cuenta B: publicar el hallazgo correspondiente, con esa señal y una foto; usar Lab-A a Lab-E como zonas.
4. Cuenta A: abrir su pérdida, buscar candidatos y enviar una solicitud. No debe ver las pruebas privadas del hallazgo.
5. Con una cuenta encargada: registrar recepción, comprobar físicamente la propiedad y confirmar la entrega.
6. Volver a la cuenta A y sincronizar para ver la solicitud entregada y el reporte recuperado.
7. Sin internet: crear otro reporte, cerrar y abrir la app, comprobar que se conserva y luego reconectar para enviarlo.

Los ocho reportes antiguos se conservaron. Sus detalles particulares permanecen vacíos si antes no existían, por lo que no se inventaron coincidencias para esos registros. En cuentas reales se comparan reportes de personas diferentes.

## Verificación realizada
Compilación debug, 29 pruebas de Kotlin/Room/sincronización/motor y lint Android correctos. La migración se probó en PostgreSQL local con restricciones de acceso, idempotencia y entrega; después se aplicó en Supabase y se comprobó la conservación de datos y el almacenamiento privado. No se realizó todavía una prueba manual completa con dos cuentas reales ni instalación en un emulador: no había dispositivo conectado al comprobarlo.

## Escenario cargado para la exposición
Se añadieron diez reportes vinculados a las dos cuentas registradas, cinco imágenes de referencia privadas, tres solicitudes y una recepción. Consulta `ESCENARIO_EXPOSICION.md` para mostrar el recorrido desde una sola sesión. La aplicación y las pruebas de Android se compilan antes de preparar los archivos para Git; no se publican cambios remotos automáticamente.
