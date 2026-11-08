# Lista de verificación de calidad

## Antes de publicar
- [ ] Android assembleDebug y testDebugUnitTest en GitHub Actions: comprobar estado verde.
- [ ] Verificar migración Room 1→2→3 con base real y borrado en cascada.
- [ ] Probar dispositivos Android 8 a 16, rotación, pantalla pequeña y accesibilidad.
- [ ] Configurar Firebase; probar inicio/cierre de sesión, errores y tokens caducados.
- [ ] Revisar CORS, límites por usuario, moderación, reportes y eliminación de contenido.
- [ ] Generar migraciones Prisma versionadas y comprobar deploy con Neon.
- [ ] Pruebas de restauración; exportación actual es parcial, no backup completo.
- [ ] Permisos mínimos, política de privacidad, términos y eliminación de cuenta.
- [ ] Comprobar que fotos de galería sigan disponibles tras reinicio y cambio de dispositivo.
- [ ] Carga de 5000 usuarios concurrentes y límites de servicios gratuitos.
