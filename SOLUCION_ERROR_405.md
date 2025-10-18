# 🔧 SOLUCIÓN ERROR 405 - API v3 vs v2

## ❌ Problema Detectado

La app estaba llamando a:
```
GET https://api-live.euroleague.net/v3/competitions/E/seasons/E2026/games
--> 405 (Method Not Allowed)
```

**Dos problemas:**
1. ❌ **API v3** no existe (debe ser v2)
2. ❌ **E2026** no existe (debe ser E2025 - temporada actual 2025-2026)

## ✅ SOLUCIÓN INMEDIATA

### Paso 1: Clean Build
En Android Studio:
```
Build → Clean Project
```
Espera a que termine (verás el progreso abajo).

### Paso 2: Rebuild
```
Build → Rebuild Project
```
Espera a que compile completamente.

### Paso 3: Desinstalar App
En el dispositivo/emulador:
```
Settings → Apps → Eurobasket → Uninstall
```
O desde Android Studio:
```
Run → Run 'app' (selecciona "Uninstall and reinstall")
```

### Paso 4: Ejecutar de Nuevo
```
Run → Run 'app'
```

## 📊 Qué Deberías Ver Ahora

Después del rebuild, en Logcat verás:
```
🏀 Obteniendo partidos temporada E2025 usando API v2...
--> GET https://api-live.euroleague.net/v2/competitions/E/seasons/E2025/games
<-- 200 OK
✅ Partidos obtenidos desde API: XXX
📊 Estados de partidos: {SCHEDULED=XXX, FINISHED=YY}
```

Nota los cambios: **v2** en lugar de v3, y **E2025** en lugar de E2026.

## 🎯 Verificación

Si aún ves `/v3/` en los logs después del rebuild, entonces hay un problema de caché más profundo.

En ese caso, ejecuta desde terminal:
```bash
cd /Users/juanjomarti/Proyectos/Eurobasket
./gradlew clean
./gradlew assembleDebug
./gradlew installDebug
```

## 💡 Por Qué Pasa Esto

Android Studio a veces cachea las clases compiladas y no recompila todo cuando hacemos cambios. 
El Clean + Rebuild fuerza una recompilación completa desde cero.

## ⚠️ Nomenclatura Correcta de Euroleague

La nomenclatura de Euroleague es: **E20XX = Temporada 20XX-20XX+1**

Por ejemplo:
- **E2025** = Temporada 2025-2026 (octubre 2025 - mayo 2026) ← **✅ TEMPORADA ACTUAL**
- **E2024** = Temporada 2024-2025 (octubre 2024 - mayo 2025)

**Resumen:** El código **E20XX** representa la temporada que **empieza** en octubre de 20XX y **termina** en mayo de 20XX+1.

Estamos en **octubre 2025**, por lo tanto la temporada actual es **E2025** (2025-2026).
