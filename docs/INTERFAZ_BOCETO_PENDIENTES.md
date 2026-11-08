# Implementación visual y pendientes — Mi Mascota

## Interfaz implementada
- Inicio con encabezado degradado turquesa, selector horizontal de mascotas, tarjetas de funciones, banner de compañero virtual y barra inferior.
- Tema global Material 3 menta/turquesa.
- Alta de mascotas en modal; selección desde barra inferior; eliminación con confirmación.
- Ventanas de salud, recordatorios, gastos, álbum, comunidad, cuenta, veterinarias, exportación y compañero virtual enlazadas.
- Nuevas ventanas específicas de adopciones y mascotas perdidas con navegación real hacia comunidad y veterinarias.

## Pendientes para equivalencia visual exacta
1. Comparación píxel a píxel con el boceto original (no hay assets ni medidas de referencia versionados en este repositorio).
2. Fotos de perfil y portada reales: Room solo contiene metadatos de mascotas, no URI de foto de portada; requiere entidad/campo y migración.
3. Iconografía vectorial coherente, ilustraciones y animaciones de cada pantalla.
4. Fichas detalladas y formularios para adopciones, solicitudes y reportes de pérdida.
5. Navegación inferior persistente en pantallas secundarias; hoy solo está en el inicio.
6. Estados de carga, error, vacíos y accesibilidad completos en todas las pantallas.
7. Adaptación a tablet, pantallas pequeñas, modo oscuro y tamaños de fuente ampliados.

## Funcionalidad pendiente
- Crear, editar y moderar publicaciones de comunidad desde Android.
- Solicitudes de adopción, filtros, chat/contacto seguro y verificación de organizaciones.
- Crear/editar alertas de mascotas perdidas con geolocalización aproximada, fotos y seguimiento.
- Inicio de sesión Firebase probado en dispositivo real y autorización API extremo a extremo.
- Notificaciones locales/push y calendario.
- Backup cifrado, fotos incluidas, restauración con manejo de conflictos y sincronización.
- Veterinarias dentro de la app y geolocalización con permisos.
- Persistencia del compañero virtual y minijuegos completos.
- Pruebas Compose, Room, accesibilidad y ejecución real de APK.

## Nota de honestidad
Las pantallas informativas de adopción y perdidos no simulan resultados ni solicitudes. Sus botones abren funciones existentes; la escritura remota queda pendiente. Los cambios están guardados en código, no se afirma compilación o equivalencia visual exacta sin capturas comparativas y pruebas en dispositivo.

## Actualización de implementación
- Edición real de perfiles de mascota (nombre, especie y nacimiento) desde la pantalla de inicio.
- La opción «Otro» ahora se acepta también en la capa de datos.
- Álbum con miniaturas obtenidas de URI locales (no se duplican los archivos).
- Comunidad con presentación visual mejorada y aviso explícito sobre publicación aún no disponible.

## Bloqueadores externos para terminar todo
- Firebase: proyecto, configuración Android y credenciales de servicio para el backend.
- API HTTPS desplegada con PostgreSQL operativo.
- Servicios de notificaciones, almacenamiento de imágenes y moderación configurados.
- Verificación del boceto original mediante capturas y pruebas visuales en un dispositivo.
- Pruebas y validación del APK actual mediante GitHub Actions.

Las funciones que necesitan estos servicios **no deben presentarse como operativas** hasta completar su integración y las pruebas.

## Revisión adicional (2026-11-08)
El repositorio contiene ya implementaciones posteriores a la primera lista: perfiles ampliados, restauración de respaldos JSON/ZIP cifrado, notificaciones locales con WorkManager, mensajería push, chat de adopciones, moderación, mapa OpenStreetMap, minijuego persistente y API de adopciones. La presencia de código no certifica su funcionamiento en producción.

Correcciones recientes:
- Los recordatorios pendientes conservan el trabajo programado al refrescar Compose; ya no se reemplaza continuamente la tarea.
- Se simplificó la solicitud de permiso de ubicación aproximada en el mapa.

Pendientes verificables:
1. Obtener y conservar una ejecución de CI verde de Android **y** backend para el commit más reciente; probar APK en dispositivo.
2. Ejecutar pruebas de migración Room 1→4 y restauración JSON/ZIP, incluyendo archivos maliciosos, URIs revocadas y datos duplicados.
3. Configurar Firebase, credenciales OAuth, base PostgreSQL, despliegue HTTPS, almacenamiento y push; comprobar extremo a extremo.
4. Revisar políticas de privacidad, permisos, accesibilidad, seguridad del WebView, retención y moderación.
5. Comparar todas las pantallas con el boceto original en distintos tamaños; faltan pruebas visuales automatizadas.
6. Validar notificaciones con cambios de zona horaria, permisos, actualizaciones y reinicios.
7. Pruebas de carga, costes, seguridad y publicación firmada en Play Store.

No declarar estas tareas completas solo por existir ventanas o endpoints.
