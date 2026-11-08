# Seguridad y privacidad

- El token de Firebase se valida en el servidor. Nunca confiar en un UID enviado por el cliente.
- Los registros privados se consultan siempre filtrados por el propietario autenticado.
- No guardar credenciales ni archivos `google-services.json` en Git.
- El acceso a Drive es opcional y separado del inicio de sesión.
- El manifiesto de respaldo es solo una primera pieza: no representa una copia integral ni cifrada.
- No publicar ubicación exacta de domicilio ni teléfono sin consentimiento.
- Las alertas comunitarias necesitan moderación, límites de frecuencia y revisión de abusos antes de producción.
