# Escenario para exponer desde una sesión

Se usa la cuenta personal del expositor como usuario y encargado. La cuenta institucional existente aporta registros ajenos. No necesitas iniciar sesión con ella para mostrar los candidatos. Son dos identidades registradas en Auth; los reportes conservan su autor. No se alteran contraseñas ni se abre acceso público.

| Caso | Qué mostrar |
|---|---|
| Celular perdido, Lab-A | Encuentra un celular ajeno con señales particulares compatibles. Crear la solicitud en vivo. |
| Otro Samsung negro | Comparte marca/modelo/color, pero tiene señales distintas: no debe aparecer como candidato. |
| Mochila perdida, Lab-B | Tiene una solicitud pendiente para mostrar el seguimiento. |
| Cuaderno perdido, Lab-C | Tiene recepción y solicitud aprobada para demostrar la entrega. |
| Audífonos perdidos, Lab-E | No tiene candidatos. |
| Llaves halladas, Lab-D | Reportadas por el expositor; otra identidad las reclama. Permite revisar una solicitud ajena desde el rol encargado. |

Se añaden diez reportes, tres solicitudes y una recepción. No se entrega ningún objeto automáticamente. Los ocho reportes anteriores se conservan. Las cinco imágenes son ilustraciones de referencia preparadas para estas pruebas, no fotografías de objetos reales.

## Orden sugerido
1. Entrar con la cuenta personal y sincronizar.
2. Abrir la pérdida del celular de Lab-A y buscar coincidencias.
3. Enviar una explicación privada y comprobar Mis solicitudes.
4. Abrir Recepción y entrega para recibir el celular y validar presencialmente el reclamo.
5. Mostrar el cuaderno ya aprobado y confirmar su devolución.
6. Sincronizar para ver el reporte recuperado y el seguimiento actualizado.
7. Desactivar la red y crear un reporte: debe quedar local y pendiente hasta reconectar.

El acceso con correo necesita contraseña. La cuenta antigua Google todavía requiere definirla manualmente; este escenario no agrega recuperación ni guarda credenciales en archivos.

## Repetir la carga
El script conserva los identificadores y no restablece estados ni borra resultados de la exposición. Las fechas de reportes ya creados se conservan. Para una exposición nueva se debe preparar otro conjunto de identificadores, no borrar datos de otros usuarios.

## Resultado de la carga en Supabase
Comprobado el 12/11/2026: 18 reportes totales (8 anteriores + 10 nuevos), 5 imágenes vinculadas y disponibles, 3 candidatos del expositor, 3 solicitudes y 1 recepción inicial. La comprobación con el rol autenticado de la cuenta institucional no permitió leer reportes, pruebas ni fotos ajenas, ni buscar candidatos de una pérdida ajena. Esto verifica los permisos en la base real; no reemplaza la prueba manual de inicio de sesión en Android.

La carga usa las mismas funciones de publicación y recuperación que consume Retrofit; no inserta coincidencias forzadas. No crea usuarios ficticios porque ya existen las dos cuentas requeridas. Puedes consultar las cantidades con `supabase/verificar_exposicion.sql`.
