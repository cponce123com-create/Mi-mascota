# Auditoría integral — Mi Mascota — 2026-11-08

## Alcance inspeccionado
Android: MainActivity, PetsScreen, HealthScreen, CommunityScreen, ExpenseScreen, PhotoScreen, BackupScreen, Room y Gradle.
Backend: Fastify, rutas de mascotas, salud y comunidad, Prisma y CI.

## Hallazgos y acciones

| Severidad | Hallazgo | Acción |
|---|---|---|
| Crítica | Navegación por strings y selectedPet!! | Sustituida por destinos tipados y manejo del botón Atrás |
| Alta | Comunidad era solo texto estático | Cliente HTTPS con consulta de publicaciones y alertas, errores y recarga |
| Alta | Acciones de mascota apretadas en móvil | Tarjetas con botones distribuidos y lista única desplazable |
| Alta | Doble pulsación en guardar mascota | Estado saving y botón deshabilitado durante escritura |
| Alta | Borrado de mascota accidental | Confirmación antes de eliminar |
| Alta | Backend sin limitación de tráfico | Rate limit inicial (no distribuido) |
| Alta | Prisma inválido | Esquema reformateado y migración inicial versionada |
| Media | API sin URL de entorno Android | BuildConfig API_BASE_URL, por defecto sin conexión |
| Media | Sin permiso INTERNET | Añadido |
| Media | Reutilización externa sin control de licencias | Política de evaluación, sin copiar código no verificado |

## Cableado verificado por inspección estática
- Inicio -> registro/listado -> Room -> Flow -> Compose
- Inicio -> salud -> HealthDao -> Room
- Inicio -> fotos -> selector Android -> PhotoEntity
- Inicio -> gastos -> ExpenseDao -> Room
- Inicio -> exportación -> selector SAF -> BackupExporter
- Inicio -> veterinarias -> app externa de mapas
- Inicio -> compañero virtual -> estado local
- Inicio -> comunidad -> CommunityApi -> endpoints GET HTTPS (requiere configurar API_BASE_URL)

## No implementado / no verificado
- Compilación de Android y backend, pruebas instrumentadas y APK.
- Inicio de sesión Google funcional en la interfaz y despliegue Firebase.
- Creación de publicaciones y alertas desde Android; API POST existe pero requiere autenticación.
- Moderación, bloqueo de abusos, eliminación de cuentas, notificaciones y adopciones.
- Respaldo integral cifrado con restauración y fotos.
- Mapas integrados y juegos open source importados.
- Pruebas de carga y compatibilidad de versiones.
- Verificación de licencias de repositorios externos. No se ha clonado código externo.

## Riesgos y próximos controles
1. Ejecutar CI en GitHub y revisar logs; corregir todos los fallos antes de release.
2. Configurar Firebase y Render/Neon mediante secretos y HTTPS.
3. Añadir pruebas Room de migración, navegación Compose y API Fastify con base de prueba.
4. Agregar moderación y límites por cuenta antes de abrir escritura comunitaria.
5. Validar privacidad, seguridad de respaldos y accesibilidad con usuarios reales.

**Nota:** Auditoría estática; no se afirma que los binarios compilen ni que el cableado online funcione en un servidor desplegado.

## Correcciones adicionales de esta iteración
- Pantalla de cuenta y acceso con Google opcional conectada desde el inicio.
- Identificador de cliente OAuth configurable mediante propiedad Gradle.
- Guardado de vacunas y recordatorios con bloqueo de doble pulsación y errores visibles.
- Se mantiene como pendiente la verificación del build en GitHub Actions, la conexión Firebase real y la integración completa de escritura comunitaria.
