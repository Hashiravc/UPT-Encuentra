# UPT Encuentra

Aplicación móvil Android para reportar, buscar y recuperar objetos perdidos en las instalaciones de la **Universidad Privada de Tacna (UPT)**.

> Proyecto académico · **Soluciones Móviles I y Patrones de Software**

## Descripción

Cuando un estudiante pierde un objeto en la UPT, no sabe si alguien lo encontró ni con quién comunicarse. **UPT Encuentra** conecta los objetos perdidos con los encontrados y da seguimiento al proceso completo:

```text
reportar → buscar coincidencias → verificar → recuperar
```

## Funcionalidades (MVP)

- Registro e inicio de sesión con correo y contraseña mediante Supabase Auth
- Sesión persistida y espacio independiente para entrar sin internet
- Reporte de objetos perdidos y encontrados
- Lista de reportes y detalle con estado
- Coincidencias entre reportes mediante reglas básicas (sin IA)
- Solicitudes privadas, recepción universitaria y validación presencial de la entrega
- Fotos privadas en Supabase Storage y sincronización offline first

## Tecnologías

Kotlin · Jetpack Compose · Material 3 · Navigation Compose · MVVM · Hilt · ViewModel · StateFlow · Room · Coroutines · WorkManager · Retrofit · Gson · Coil · Supabase

## Arquitectura

```text
UI (Compose) → ViewModel → Repository → Room + pendientes
WorkManager → Sincronizador → Retrofit/Gson → Supabase → Room
```

## Ejecución

1. Clonar el repositorio: `git clone <URL-del-repositorio>`
2. Abrir la carpeta en Android Studio y esperar la sincronización de Gradle.
3. Completar `supabase.url` y `supabase.publishableKey` en `local.properties` usando `local.properties.example` como referencia. Android Studio configura su propia ruta `sdk.dir`.
4. Ejecutar en un emulador o dispositivo con Android 8.0 (API 26) o superior. El primer registro requiere red; no se pide confirmación del correo por enlace.

## Base de datos y prueba

- La migración está en `supabase/migrations/20261112_cuentas_fotos_recuperacion.sql`.
- `supabase/seed_exposicion.sql` prepara un escenario adicional para las dos cuentas existentes indicadas en el script. No crea credenciales ni concede roles. Antes se deben subir las imágenes de referencia de `supabase/fixtures`, conservando sus subcarpetas, al bucket privado `fotos-reportes`.
- Instrucciones: [Funcionamiento y archivos](docs/IMPLEMENTACION_Y_PRUEBA.md) y [Escenario de exposición](docs/ESCENARIO_EXPOSICION.md).
- No se incluyen contraseñas, tokens, claves administrativas ni `local.properties` en Git. La aplicación usa únicamente la clave publicable del proyecto y los permisos del usuario autenticado.

## Documentación

| Documento | Enlace |
|---|---|
| Estudio de factibilidad | [Ver documento](#) |
| Misión y visión | [Ver documento](#) |
| SRS (Especificación de requisitos) | [Ver documento](#) |
| SAD (Arquitectura del software) | [Ver documento](#) |
| Diccionario de datos | [Ver documento](#) |
| Informe final | [Ver documento](#) |

## Equipo

| Integrante | Código |
|---|---|
| Ariana Byanca Sofía Espinoza Castañeda | 2022073904 |
| Hashira Belén Vargas Candia | 2022075480 |
| Richard Podesta | — |
| Nombre Apellido | — |

**Docente:** ING. ALBERTO JOHNATAN FLOR RODRIGUEZ · **Universidad Privada de Tacna**

## Licencia

Proyecto desarrollado con fines académicos.
