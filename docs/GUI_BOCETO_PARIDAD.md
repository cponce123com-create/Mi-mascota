# Mi Mascota — Correspondencia con el boceto aprobado
**Revisión: 8 de noviembre de 2026 · Referencia: composición de 7 pantallas generada durante la conversación.**

La interfaz aprobada tiene una identidad visual de encabezados verde petróleo/turquesa, fondos celeste pálido, tarjetas blancas redondeadas, acentos verdes y fotografías de mascotas. Este documento describe **código implementado**, **integración real** y **diferencias pendientes**. No afirma identidad píxel a píxel sin probar un APK sobre el teléfono.

## Pantallas y cableado

| Pantalla del boceto | Cambios en Android | Pendiente para equivalencia funcional y visual |
| --- | --- | --- |
| Bienvenida | Nueva pantalla con marca, imagen de mascota si ya existe o ilustración, acceso Google, correo e invitado | Fotografías originales optimizadas para Android, tipografía/ondas exactamente iguales; Google y correo requieren Firebase |
| Inicio | Header turquesa, selector circular real con fotos del álbum, cuadrícula 3×3, tarjeta verde de consejos, acceso a juegos y veterinarias, navegación inferior con iconos vectoriales | Foto de portada/banners originales, badge de notificaciones basado en datos reales, revisión píxel a píxel en tamaños distintos |
| Ficha de mascota | Foto de álbum como portada, atajos Ficha/Salud/Vacunas/Gastos, edición de raza/peso/esterilización/microchip, metadatos organizados | Campo sexo/género (boceto muestra símbolo masculino), foto de portada elegible de forma independiente, opción de editar nombre y fecha directamente desde ficha (actualmente desde Inicio) |
| Vacunas | Vista diferenciada, próxima vacuna, fecha y días restantes, historial, alta y eliminación | Tipos de vacuna/estados y adjuntos; validación veterinaria |
| Recordatorios | Tarjetas, fechas, interruptores, alta, eliminación, WorkManager y permisos | Categorías e iconos específicos de cada tarea, alertas de hora personalizada, más pruebas en dispositivos |
| Comunidad | Cabecera, filtros Todo/Adopción/Perdidos/Consejos, publicaciones, comentarios y botón + | Feed multimedia: fotos, perfil/autores, favoritos, compartir, contador de interacciones, geolocalización e integración/moderación desplegada |
| Cuenta | Cabecera turquesa, avatar local modificable, Google o correo opcionales, respaldos, configuración, privacidad y Acerca de | Foto remota del perfil, sincronización automática real, identidad del usuario desde servidor, personalización de cuenta |

## Otras ventanas refinadas
- **Galería:** cuadrícula adaptable con fotografías reales, fechas, ver y quitar con confirmación.
- **Gastos:** total en PEN, historial compacto, alta y borrado con confirmación.
- **Consejos:** guía offline con botones funcionales hacia salud y mapa.
- **Configuración / Acerca de:** permisos, respaldos, información de la versión y soporte.
- **Adopciones / perdidos:** formularios y consulta API existentes, requieren servidor, autenticación y moderación.
- **Juegos:** minijuego offline con persistencia; requiere diseños e ilustraciones para igualar un producto terminado.

## Diferencias que requieren recursos/servicios
1. **Fotografías y artes:** el boceto es una imagen compuesta, no un conjunto de fotos originales recortables en alta calidad. Crear y versionar imágenes maestras del perro/gato, banner de consejos, avatares de demostración y fondos sin texto incrustado. En perfiles reales se utilizarán fotos elegidas por el usuario.
2. **Identidad:** configurar proyecto Firebase Android, OAuth web, correo/contraseña habilitados y credenciales seguras del servidor. Sin esto, la bienvenida ofrece modo invitado funcional y el resto muestra avisos.
3. **Datos comunitarios:** desplegar la API Fastify/Prisma/PostgreSQL por HTTPS; moderación, almacenamiento multimedia, reportes, adopciones, alertas y contacto seguro necesitan validación real.
4. **Sincronización:** copias JSON/ZIP con contraseña existen, pero no equivalen a sincronización automática entre dispositivos. Falta flujo bidireccional/conflictos y consentimiento explícito.
5. **Notificaciones:** WorkManager funciona con condiciones del SO; badge, push y actualización en tiempo real necesitan permisos, servicio Firebase y pruebas.
6. **Ajustes finos:** capturas del APK en el teléfono y comparación con el boceto; probar pantalla pequeña, teclado abierto, texto grande, orientación, contraste, lectores de pantalla y modo sin conexión.

## Checklist mínimo para aceptar un APK visual
- [ ] Bienvenida, sin superposición y con entrada como invitado.
- [ ] Inicio con 9 tarjetas 3×3 sin recorte de títulos.
- [ ] Barra inferior legible y botones funcionales.
- [ ] Crear, editar, seleccionar y eliminar mascotas con confirmación.
- [ ] Fotos reales en avatar, portada y galería después de elegir una imagen.
- [ ] Perfil con valores de hasta 80 caracteres sin superposición.
- [ ] Vacunas, salud y recordatorios como tres vistas distintas.
- [ ] Comunidad sin publicaciones ficticias; modos sin servidor claros.
- [ ] Cuenta con foto local, exportación y rutas de ajustes/acerca.
- [ ] Compilación Android + pruebas + publicación de APK en verde.

### Regla de producto
Nunca simular sincronización, publicaciones reales, calificaciones, notificaciones recibidas ni sesiones iniciadas. Los datos del boceto eran ilustrativos, no registros del usuario. La aplicación debe presentar estados vacíos reales.
