# Matriz de funciones — Mi Mascota

> Estado por inspección de implementación. El código subido no equivale a un APK verificado. Consultar **GitHub Actions** para resultados de pruebas.

| Función | Código disponible | Requisitos pendientes |
|---|---|---|
| Perfiles completos | Registro, edición de identidad/raza/peso/esterilización/microchip, selección, galería | QA en dispositivo y fotos elegidas |
| Vacunas y recordatorios | Formularios, historial, próxima dosis, estado | Pruebas de fechas y migraciones |
| Avisos locales | WorkManager y permiso Android 13+ | Verificar notificaciones y restricciones de batería |
| Álbum | Miniaturas URI, imágenes privadas restauradas, visor | Captura de cámara integrada, sincronización remota |
| Respaldo | JSON v1-v3, ZIP con imagen, importación con límites y confirmación | Cifrado, pruebas grandes y resolución avanzada de conflictos |
| Google Sign-In | Pantalla y clase Credential Manager/Firebase; plugin condicional | Archivo Firebase, OAuth SHA-1/SHA-256, pruebas |
| API comunitaria | Leer y crear posts; contenido pendiente de aprobación | Backend HTTPS y moderación operativa |
| Mascotas perdidas | Leer y publicar alertas, sincronizar mascota propietaria, marcar encontrada | Fotos remotas, geolocalización de eventos, push |
| Adopciones | Listados, solicitudes, estados y cierre | Contacto seguro, revisión de asociaciones, seguimiento |
| Moderación | Cola protegida por allowlist UID y decisiones | Configurar MODERATOR_UIDS y equipo responsable |
| Veterinarias | OpenStreetMap embebido, ubicación aproximada opcional, mapas externos | Directorio local confiable y validado |
| Compañero virtual | Estados persistentes, descanso, juego y huella oculta | Más animaciones y pruebas de usabilidad |
| Navegación | Menú inferior global y enlaces de todas las pantallas | Pruebas Compose, accesibilidad y tablets |
| CI/APK | Prisma valida y migra contra PostgreSQL temporal, smoke tests, Android debug y upload-artifact | Comprobar último run y pruebas de instalación |
| Google Drive | Guardar/abrir con Storage Access Framework, si Drive aparece como proveedor | Sin sincronización OAuth automática |
| Notificaciones push | No | Firebase Cloud Messaging + token de dispositivo + backend de envío |
| Cifrado de respaldos | No | AES-GCM + gestión de contraseña/clave |

## Riesgos a validar
- Permisos por dispositivo (fotos, ubicación, notificaciones), ejecución con la pantalla apagada.
- Lectura de fotos grandes, restauración ZIP y disponibilidad de URI originales.
- Respuesta ante API caída o lentitud, conexión a Firebase inexistente.
- Protección de datos personales, borrado de cuenta, protección contra abuso y moderación humana.
- Compatibilidad con modo oscuro, accesibilidad, distintos tamaños de letra y tablets.

La aplicación usa datos reales; no inserta anuncios de adopción ficticios.
