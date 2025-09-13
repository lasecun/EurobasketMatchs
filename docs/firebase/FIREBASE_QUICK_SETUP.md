# 🎯 Configuración Rápida de Dashboards - BasketMatch

## 🚀 **Enlaces Directos para tu Proyecto**

### **Acceso Rápido a Firebase Console**
- **Dashboard Principal**: https://console.firebase.google.com/project/basketmatch-79703/analytics
- **DebugView**: https://console.firebase.google.com/project/basketmatch-79703/analytics/debugview
- **Realtime**: https://console.firebase.google.com/project/basketmatch-79703/analytics/realtime
- **Custom Reports**: https://console.firebase.google.com/project/basketmatch-79703/analytics/app/android:es.itram.basketmatch/reporting

## 📊 **Configuración Express - 5 Minutos**

### **Dashboard 1: Equipos Más Populares** ⭐ PRIORIDAD ALTA

**Pasos**:
1. Ve a: Analytics → Reports → Create Custom Report
2. **Nombre**: "Top Equipos Visualizados"
3. **Configuración**:
   ```
   Event: team_viewed
   Primary dimension: team_name
   Metric: Event count
   Secondary metric: Users
   Date range: Last 30 days
   Max rows: 20
   Sort by: Event count (descending)
   ```

**Resultado esperado**: Verás una tabla como:
```
Real Madrid     | 1,234 views | 890 users
Barcelona       | 1,120 views | 810 users
Bayern Munich   | 890 views   | 650 users
```

### **Dashboard 2: Pantallas Más Visitadas** ⭐ PRIORIDAD ALTA

**Pasos**:
1. Create Custom Report
2. **Nombre**: "Navegación de Usuarios"
3. **Configuración**:
   ```
   Event: screen_view
   Primary dimension: screen_name
   Metric: Event count
   Secondary metric: Users
   Date range: Last 7 days
   ```

### **Dashboard 3: Performance de la App** ⭐ PRIORIDAD MEDIA

**Pasos**:
1. Create Custom Report
2. **Nombre**: "Métricas de Performance"
3. **Configuración**:
   ```
   Event: app_startup_time
   Primary dimension: Date
   Metric: Average value (load_time_ms)
   Date range: Last 14 days
   Chart type: Line chart
   ```

## 🔧 **Configuración de Alertas Críticas**

### **Alerta 1: Caída en Engagement**
```
Nombre: "Caída en visualizaciones de equipos"
Condición: team_viewed daily count decreases by 25%
Frecuencia: Daily check
Notificación: Email + Push (Firebase mobile app)
```

### **Alerta 2: Errores de Sincronización**
```
Nombre: "Errores de sync críticos"
Condición: data_sync_failed > 20 events per day
Frecuencia: Hourly check
Notificación: Email inmediato
```

## 📱 **Verificación Inmediata**

### **Comando para Activar Debug Mode**
```bash
# Conecta tu dispositivo Android y ejecuta:
adb shell setprop debug.firebase.analytics.app es.itram.basketmatch.debug

# Para desactivar después:
adb shell setprop debug.firebase.analytics.app .none.
```

### **Test de Eventos en tu App**
1. Abre tu app en modo debug
2. Ve a DebugView en Firebase Console
3. Navega por la app:
   - Visita pantalla de equipos → Verás `team_viewed`
   - Ve detalles de partido → Verás `match_viewed`
   - Cambia de pantalla → Verás `screen_view`

## 🎯 **Métricas Clave para Monitorear**

### **Diarias (revisar cada día)**
- Active Users
- Top 3 equipos más vistos
- Crashes & ANRs

### **Semanales (revisar cada lunes)**
- User retention (7-day)
- Popular screens
- Search terms
- Performance metrics

### **Mensuales (revisar cada inicio de mes)**
- Monthly active users
- Top content overall
- Long-term trends
- Feature adoption

## 🚨 **Red Flags - Alertas Inmediatas**

Si ves estas métricas, actuar inmediatamente:

❌ **Crash rate > 1%**
❌ **App startup time > 5 segundos**
❌ **Daily active users drop > 30%**
❌ **Zero events en últimas 2 horas** (posible problema de tracking)

## 🔗 **Integraciones Útiles**

### **Google Sheets (Reportes Automáticos)**
1. Analytics → Export → Google Sheets
2. Programa exportación semanal
3. Comparte con tu equipo

### **Slack/Discord (Alertas)**
1. Usar Zapier para conectar Firebase → Slack
2. Alertas automáticas en canal de desarrollo

## 📊 **Template de Reporte Semanal**

```
📊 REPORTE SEMANAL - BasketMatch Analytics

🏀 EQUIPOS MÁS POPULARES:
1. [Equipo] - [X] visualizaciones
2. [Equipo] - [X] visualizaciones
3. [Equipo] - [X] visualizaciones

📱 ENGAGEMENT:
- Usuarios activos: [X]
- Sesiones promedio: [X]
- Tiempo promedio en app: [X]

⚡ PERFORMANCE:
- Startup time: [X]ms
- Crash rate: [X]%
- API response time: [X]ms

🔍 INSIGHTS:
- [Observación importante]
- [Tendencia detectada]
- [Acción recomendada]
```
