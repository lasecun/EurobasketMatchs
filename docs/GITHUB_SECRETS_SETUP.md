# 🔐 GitHub Secrets Configuration

## ⚠️ IMPORTANTE: Configuración Requerida para CI/CD

Para que el pipeline funcione correctamente, necesitas configurar el secreto `GOOGLE_SERVICES_JSON` en tu repositorio de GitHub.

## 📋 Pasos para Configurar el Secreto

### 1. Obtener el archivo google-services.json

Si no tienes el archivo, puedes:
- Descargarlo desde la consola de Firebase: https://console.firebase.google.com/
- Ve a tu proyecto → Project Settings → General → Tu app Android → Descargar google-services.json

### 2. Convertir a Base64 (Opcional - Recomendado)

Para mayor seguridad, puedes codificar el archivo en base64:

```bash
# En tu máquina local
base64 -i app/google-services.json
```

### 3. Configurar el Secreto en GitHub

1. Ve a tu repositorio en GitHub
2. Click en **Settings** → **Secrets and variables** → **Actions**
3. Click en **New repository secret**
4. Nombre: `GOOGLE_SERVICES_JSON`
5. Valor: Copia el contenido completo del archivo google-services.json (o la versión base64)

### 4. Si usas Base64, actualiza los workflows

Si codificaste en base64, modifica los workflows para decodificar:

```yaml
- name: 🔥 Setup Firebase Configuration
  run: |
    echo '${{ secrets.GOOGLE_SERVICES_JSON }}' | base64 --decode > app/google-services.json
```

## 🔍 Verificación

Después de configurar el secreto:

1. Haz push de cualquier cambio
2. Verifica que el pipeline pase sin errores
3. El archivo `google-services.json` se creará automáticamente durante la build

## 🚫 Errores Comunes

- **Error**: `File google-services.json is missing`
  - **Solución**: Asegúrate de que el secreto `GOOGLE_SERVICES_JSON` esté configurado correctamente
  - **Debug**: Los workflows ahora muestran información detallada sobre la creación del archivo

- **Error**: `JSON syntax error`
  - **Solución**: Verifica que el contenido del secreto sea válido JSON

- **Error**: `GOOGLE_SERVICES_JSON secret is not set!`
  - **Solución**: El secreto no está configurado en GitHub. Sigue los pasos de configuración arriba

- **Error**: `echo: write error: No space left on device`
  - **Solución**: El contenido del secreto puede estar mal formateado o ser demasiado grande

## 🔍 Debugging

Los workflows ahora incluyen información de debug automática que mostrará:
- Si el secreto está configurado
- Ubicación del archivo creado
- Tamaño del archivo generado
- Listado del archivo para verificar permisos

Revisa los logs del workflow en GitHub Actions para ver esta información de debug.

## 📁 Archivos Afectados

Los siguientes workflows necesitan este secreto:
- `.github/workflows/ci.yml`
- `.github/workflows/pr-validation.yml`
- `.github/workflows/test-suite.yml`

## 🔒 Seguridad

✅ **Buenas prácticas implementadas:**
- El archivo `google-services.json` está en `.gitignore`
- Las claves API están protegidas como secretos de GitHub
- Los workflows solo tienen acceso durante la ejecución
- No se almacenan credenciales en el código fuente

## 📞 Soporte

Si tienes problemas:
1. Verifica que el secreto esté configurado correctamente
2. Comprueba que el JSON sea válido
3. Revisa los logs del workflow para más detalles
