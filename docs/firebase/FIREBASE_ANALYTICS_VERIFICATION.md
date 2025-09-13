# 🔍 Verificación de Analytics - BasketMatch

## ✅ **Checklist de Verificación Pre-Dashboard**

### **1. Verificar Eventos Implementados**

Tu aplicación ya tiene estos eventos críticos implementados:

```kotlin
// ✅ EVENTOS CONFIRMADOS EN TU CÓDIGO:
- team_viewed          → Para trackear visualizaciones de equipos (ej: Real Madrid)
- match_viewed         → Para trackear visualizaciones de partidos
- player_viewed        → Para trackear visualizaciones de jugadores
- screen_view          → Para trackear navegación entre pantallas
- search_performed     → Para trackear búsquedas de usuarios
- data_sync_started    → Para trackear inicio de sincronización
- data_sync_completed  → Para trackear sync exitoso
- data_sync_failed     → Para trackear errores de sync
- app_startup_time     → Para trackear performance de inicio
- api_response_time    → Para trackear performance de APIs
```

### **2. Test de Verificación Inmediata**

**Ejecuta estos comandos para probar**:

```bash
# 1. Activar modo debug
adb shell setprop debug.firebase.analytics.app es.itram.basketmatch.debug

# 2. Verificar que está activo
adb shell getprop debug.firebase.analytics.app
# Debe mostrar: es.itram.basketmatch.debug
```

**Luego en tu app**:
1. Abre la app
2. Ve a Firebase Console → DebugView
3. Navega a un equipo (ej: Real Madrid)
4. Deberías ver aparecer inmediatamente:
   ```
   ✅ screen_view (team_detail)
   ✅ team_viewed (team_name: "Real Madrid")
   ```

### **3. Verificación de Parámetros Clave**

**Para el evento `team_viewed` deberías ver**:
```json
{
  "event_name": "team_viewed",
  "parameters": {
    "team_code": "MAD",           // ✅ Código del equipo
    "team_name": "Real Madrid",   // ✅ Nombre completo
    "source": "team_detail_screen", // ✅ Origen de la vista
    "content_type": "team"        // ✅ Tipo de contenido
  }
}
```

## 🚨 **Problemas Comunes y Soluciones**

### **Problema 1: No aparecen eventos en DebugView**

**Soluciones**:
```bash
# Verificar que debug esté activo
adb shell getprop debug.firebase.analytics.app

# Reiniciar la app completamente
adb shell am force-stop es.itram.basketmatch.debug
adb shell am start es.itram.basketmatch.debug/.MainActivity

# Verificar logs en tiempo real
adb logcat | grep -i firebase
```

### **Problema 2: Eventos aparecen pero sin parámetros**

**Verificar en tu código**:
- Los parámetros se están enviando correctamente
- No hay errores en la implementación de Bundle

### **Problema 3: App no está en modo debug**

**Asegurar que estés usando**:
- Package: `es.itram.basketmatch.debug` (no release)
- Build variant: Debug

## 🎯 **Test Específico para Equipos Populares**

### **Script de Prueba Real Madrid**

1. **Abrir tu app**
2. **Ir a equipo Real Madrid** (desde cualquier pantalla)
3. **En DebugView deberías ver**:
   ```
   Event: team_viewed
   team_name: Real Madrid
   team_code: MAD
   source: navigation (o team_detail_screen)
   ```

4. **Repetir con otros equipos** para generar datos de prueba:
   - Barcelona
   - Bayern Munich  
   - Panathinaikos
   - etc.

### **Verificar Agregación de Datos**

**Después de 1-2 horas** (Firebase tiene delay):
1. Ve a Analytics → Realtime
2. Busca eventos `team_viewed`
3. Deberías ver contadores incrementándose

**Después de 24 horas**:
1. Ve a Analytics → Events
2. Busca `team_viewed` en la lista
3. Click para ver detalles y parámetros

## 📊 **Configuración de Dashboard Real Madrid**

### **Dashboard Específico: "Popularidad Real Madrid"**

**Una vez verificado que los eventos funcionan**:

1. **Custom Report en Firebase**:
   ```
   Nombre: "Real Madrid Analytics"
   Event: team_viewed
   Filter: team_name = "Real Madrid"
   Dimensions: source, date
   Metrics: Event count, Users
   Date range: Last 30 days
   ```

2. **Métricas esperadas**:
   - Cuántos usuarios únicos vieron Real Madrid
   - Desde qué pantallas llegaron
   - Tendencia temporal de visualizaciones
   - Comparación con otros equipos

### **Query de Verificación en BigQuery** (si exportas datos)

```sql
SELECT 
  event_date,
  COUNT(*) as total_views,
  COUNT(DISTINCT user_pseudo_id) as unique_users
FROM `basketmatch-79703.analytics_[TABLE_SUFFIX]`
WHERE event_name = 'team_viewed'
  AND (SELECT value.string_value FROM UNNEST(event_params) WHERE key = 'team_name') = 'Real Madrid'
GROUP BY event_date
ORDER BY event_date DESC
LIMIT 30
```

## 🔧 **Comandos de Debug Útiles**

```bash
# Ver todos los eventos de Firebase en tiempo real
adb logcat | grep -E "(FirebaseAnalytics|team_viewed|Analytics)"

# Verificar configuración de Firebase
adb logcat | grep -i "firebase.*config"

# Monitor específico de analytics
adb logcat | grep "AnalyticsManager"
```

## ✅ **Checklist Final Pre-Dashboard**

Antes de configurar dashboards, verificar:

- [ ] DebugView muestra eventos en tiempo real
- [ ] Evento `team_viewed` aparece con parámetros correctos
- [ ] Al menos 5 equipos diferentes han sido vistos
- [ ] Eventos aparecen en Analytics → Realtime (después de 1 hora)
- [ ] No hay errores en logcat relacionados con Firebase

**Una vez completado este checklist**, los dashboards mostrarán datos reales y útiles.

## 🎯 **Resultado Esperado**

Con esta verificación completada, tu dashboard de Firebase podrá responder preguntas como:

✅ **¿Cuántos usuarios vieron Real Madrid esta semana?**
✅ **¿Qué equipo es más popular entre los usuarios?**
✅ **¿Desde qué pantalla llegan más a ver equipos?**
✅ **¿Cuál es la tendencia de visualizaciones por equipo?**
✅ **¿Qué usuarios ven más equipos diferentes?**
