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
