# 🔧 DebugView Troubleshooting Guide - Firebase Analytics

## ✅ **Estado Actual**
- ✅ Firebase Analytics inicializado correctamente
- ✅ Debug mode habilitado 
- ✅ App configurada para DebugView
- ⚠️ **Dispositivo no aparece en DebugView**

## 🎯 **Posibles Causas y Soluciones**

### 1. **Tiempo de Sincronización** ⏱️
**Problema**: Firebase Analytics puede tomar hasta 30 minutos para mostrar en DebugView la primera vez.

**Solución**: 
- Esperar 5-10 minutos más
- Navegar activamente por la app para generar más eventos
- Verificar que los eventos se están enviando en logcat

### 2. **Verificar Región del Proyecto** 🌍
**Problema**: Algunos proyectos en ciertas regiones pueden tener delay.

**Solución**: 
1. Ir a [Firebase Console](https://console.firebase.google.com)
2. Verificar que estás en el proyecto correcto: `basketmatch-79703`
3. Analytics > DebugView
4. Actualizar la página del navegador

### 3. **Network/Conectividad** 📡
**Problema**: El dispositivo puede no estar enviando datos por problemas de red.

**Solución**:
```bash
# Verificar conectividad
adb shell ping -c 3 8.8.8.8

# Reiniciar conexión de red en el dispositivo
adb shell svc wifi disable && adb shell svc wifi enable
```

### 4. **Forzar Envío de Eventos** 🚀
**Problema**: Los eventos pueden estar en buffer local sin enviar.

**Solución**:
```bash
# Forzar envío inmediato (solo en debug mode)
adb shell am start -n es.itram.basketmatch.debug/es.itram.basketmatch.MainActivity \
  -e "com.google.firebase.analytics.deactivate" "false"
```

### 5. **Verificar Configuración del Proyecto** ⚙️
**Problema**: Configuración incorrecta en Firebase Console.

**Verificar**:
- Package name exacto: `es.itram.basketmatch.debug`
- API Key: `[REDACTED - Check google-services.json]`
- Project ID: `basketmatch-79703`

## 🧪 **Steps para Diagnosticar**

### Paso 1: Generar Eventos Manualmente
1. Abrir la app
2. Navegar entre pantallas (Home → Teams → Matches)
3. Hacer pull-to-refresh en varias pantallas
4. Esperar 2-3 minutos

### Paso 2: Verificar Logs en Tiempo Real
```bash
# Ver logs específicos de Firebase
adb logcat -s "FA" | grep -E "(Event|Upload|Debug)"
```

### Paso 3: Test de Conectividad
```bash
# Verificar que el dispositivo puede conectar a Firebase
adb shell ping -c 3 firebaselogging-pa.googleapis.com
```

### Paso 4: Reinicio Completo
```bash
# Limpiar completamente y reiniciar
adb shell setprop debug.firebase.analytics.app .none.
adb shell setprop debug.firebase.analytics.app es.itram.basketmatch.debug
adb uninstall es.itram.basketmatch.debug
./gradlew installDebug
```

## 📊 **Logs Esperados**

### ✅ **Logs Normales** (los que ya vemos):
```
I FA      : App measurement initialized, version: 133001
I FA      : Faster debug mode event logging enabled
```

### ✅ **Logs que Deberíamos Ver** (al usar la app):
```
I FA      : Logging event: screen_view
I FA      : Logging event: session_start  
I FA      : Upload scheduled
I FA      : Uploading events
```

## 🔥 **Siguiente Acción Recomendada**

1. **Esperar 5 minutos** navegando activamente por la app
2. **Actualizar Firebase Console** (F5)
3. **Verificar en Analytics > Realtime** (no solo DebugView)
4. Si no aparece, **reiniciar configuración completa**

---

**💡 NOTA**: El debug mode está funcionando correctamente según los logs. El problema puede ser de timing o sincronización con Firebase Console.
