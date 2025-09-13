# 📊 Guía Completa: Dashboard de Firebase Analytics para BasketMatch

## 🎯 Configuración de Dashboards Personalizados

### 📱 **Información del Proyecto**
- **Project ID**: basketmatch-79703
- **App Package**: es.itram.basketmatch
- **Debug Package**: es.itram.basketmatch.debug

---

## 🚀 **Paso 1: Acceso a Firebase Console**

1. **Abrir Firebase Console**:
   ```
   https://console.firebase.google.com/project/basketmatch-79703
   ```

2. **Navegar a Analytics**:
   - En el menú lateral → **Analytics** → **Dashboard**
   - O directamente: `https://console.firebase.google.com/project/basketmatch-79703/analytics`

---

## 📊 **Paso 2: Dashboards Recomendados para BasketMatch**

### 🏀 **Dashboard 1: Engagement de Equipos**

**Propósito**: Monitorear qué equipos son más populares entre los usuarios

**Configuración**:
1. **Crear nuevo dashboard**:
   - Click en "+" o "Create Dashboard"
   - Nombre: "📊 Engagement de Equipos"

2. **Métricas a incluir**:

   **Gráfico 1: Top Equipos Visualizados**
   ```
   Tipo: Table/Tabla
   Evento: team_viewed
   Dimensión: team_name
   Métrica: Event count
   Filtro: Últimos 30 días
   Ordenar por: Event count (DESC)
   ```

   **Gráfico 2: Tendencia de Visualizaciones por Equipo**
   ```
   Tipo: Line Chart
   Evento: team_viewed
   Dimensión: Date + team_name (top 5)
   Métrica: Event count
   Período: Últimos 7 días
   ```

   **Gráfico 3: Fuentes de Tráfico a Equipos**
   ```
   Tipo: Pie Chart
   Evento: team_viewed
   Dimensión: source
   Métrica: Event count
   ```

### 🏆 **Dashboard 2: Engagement de Partidos**

**Propósito**: Analizar el interés en partidos y contenido deportivo

**Configuración**:
1. **Crear dashboard**: "🏆 Engagement de Partidos"

2. **Métricas**:

   **Gráfico 1: Partidos Más Vistos**
   ```
   Tipo: Table
   Evento: match_viewed
   Dimensiones: home_team, away_team
   Métrica: Event count + Unique users
   ```

   **Gráfico 2: Partidos en Vivo vs Diferidos**
   ```
   Tipo: Bar Chart
   Evento: match_viewed
   Dimensión: is_live
   Métrica: Event count
   ```

   **Gráfico 3: Engagement por Hora del Día**
   ```
   Tipo: Heatmap
   Evento: match_viewed
   Dimensión: Hour of day
   Métrica: Event count
   ```

### 📱 **Dashboard 3: Navegación y UX**

**Propósito**: Entender cómo los usuarios navegan por la app

**Configuración**:
1. **Crear dashboard**: "📱 Navegación y UX"

2. **Métricas**:

   **Gráfico 1: Pantallas Más Visitadas**
   ```
   Tipo: Bar Chart
   Evento: screen_view
   Dimensión: screen_name
   Métrica: Event count + Unique users
   ```

   **Gráfico 2: Tiempo de Permanencia en Pantallas**
   ```
   Tipo: Table
   Evento: screen_engagement
   Dimensión: screen_name
   Métrica: Average time_spent_seconds
   ```

   **Gráfico 3: Funnel de Usuario**
   ```
   Tipo: Funnel
   Eventos secuenciales:
   1. screen_view (home)
   2. team_viewed
   3. match_viewed
   ```

### 🔍 **Dashboard 4: Búsquedas y Descubrimiento**

**Propósito**: Analizar qué buscan los usuarios y cómo descubren contenido

**Configuración**:
1. **Crear dashboard**: "🔍 Búsquedas y Descubrimiento"

2. **Métricas**:

   **Gráfico 1: Términos de Búsqueda Más Populares**
   ```
   Tipo: Table
   Evento: search_performed
   Dimensión: search_term
   Métrica: Event count + result_count (promedio)
   ```

   **Gráfico 2: Efectividad de Búsquedas**
   ```
   Tipo: Line Chart
   Evento: search_performed
   X-axis: Date
   Y-axis: Average result_count
   ```

### ⚡ **Dashboard 5: Performance y Errores**

**Propósito**: Monitorear el rendimiento de la app y detectar problemas

**Configuración**:
1. **Crear dashboard**: "⚡ Performance y Errores"

2. **Métricas**:

   **Gráfico 1: Tiempo de Startup**
   ```
   Tipo: Line Chart
   Evento: app_startup_time
   Métrica: Average load_time_ms
   Tendencia: Últimos 7 días
   ```

   **Gráfico 2: Errores de Sincronización**
   ```
   Tipo: Bar Chart
   Evento: data_sync_failed
   Dimensión: sync_type
   Métrica: Event count
   ```

   **Gráfico 3: Tiempo de Carga de APIs**
   ```
   Tipo: Table
   Evento: api_response_time
   Dimensión: endpoint
   Métrica: Average load_time_ms
   Ordenar por: Tiempo promedio (DESC)
   ```

---

## 🔧 **Paso 3: Configuración Avanzada**

### 📊 **Eventos Personalizados Configurados**

Tu app ya tiene estos eventos implementados:
- `team_viewed` - Visualización de equipos
- `match_viewed` - Visualización de partidos  
- `player_viewed` - Visualización de jugadores
- `screen_view` - Navegación entre pantallas
- `search_performed` - Búsquedas de usuarios
- `data_sync_started/completed/failed` - Sincronización
- `app_startup_time` - Performance de inicio
- `api_response_time` - Performance de APIs

### 🎯 **Audiencias Personalizadas**

Crea estas audiencias para segmentación avanzada:

1. **Usuarios Activos de Equipos**:
   ```
   Condición: team_viewed en últimos 7 días
   Mínimo: 3 eventos
   ```

2. **Fans del Real Madrid**:
   ```
   Condición: team_viewed 
   Parámetro: team_name = "Real Madrid"
   Frecuencia: Últimos 30 días
   ```

3. **Usuarios de Partidos en Vivo**:
   ```
   Condición: match_viewed
   Parámetro: is_live = true
   Frecuencia: Últimos 7 días
   ```

### 🚨 **Alertas Inteligentes**

Configura alertas para monitoreo proactivo:

1. **Caída en Engagement**:
   ```
   Métrica: team_viewed (daily)
   Condición: Decrease > 20% vs previous week
   ```

2. **Errores de Sincronización**:
   ```
   Métrica: data_sync_failed
   Condición: > 10 events per hour
   ```

3. **Performance Degradado**:
   ```
   Métrica: app_startup_time (average)
   Condición: > 3000ms daily average
   ```

---

## 📱 **Paso 4: Dashboard Mobile**

### **Firebase Mobile App**

1. **Descargar Firebase Mobile App**:
   - iOS: App Store
   - Android: Google Play Store

2. **Configurar Proyecto**:
   - Login con tu cuenta de Google
   - Seleccionar proyecto: basketmatch-79703
   - Activar notificaciones para alertas

3. **Widgets Recomendados**:
   - Active users (today)
   - Top events (team_viewed, match_viewed)
   - Crashes & ANRs
   - Revenue (si implementas monetización)

---

## 🎯 **Paso 5: Métricas Clave de Éxito (KPIs)**

### **KPIs Principales para BasketMatch**

1. **Engagement de Contenido**:
   - Top 5 equipos más vistos
   - Promedio de partidos por usuario
   - Tiempo promedio en pantallas

2. **Retención de Usuarios**:
   - DAU (Daily Active Users)
   - Retention Rate (1, 7, 30 días)
   - Session Duration

3. **Performance de App**:
   - Crash-free rate > 99.5%
   - App startup time < 2s
   - API response time < 1s

4. **Descubrimiento de Contenido**:
   - Search success rate > 80%
   - Click-through rate en recomendaciones
   - Diversidad de equipos visitados por usuario

---

## 🚀 **Paso 6: Implementación Práctica**

### **Script de Configuración Rápida**

He preparado las métricas más importantes que puedes configurar inmediatamente:

1. **Accede a**: `https://console.firebase.google.com/project/basketmatch-79703/analytics/app/android:es.itram.basketmatch/overview`

2. **Crear estos reportes Custom**:

   **Reporte: "Top Equipos"**
   ```
   Dimensions: team_name
   Metrics: Event count (team_viewed)
   Date range: Last 30 days
   Max rows: 20
   ```

   **Reporte: "Engagement por Pantalla"**
   ```
   Dimensions: screen_name
   Metrics: Event count (screen_view), Unique users
   Date range: Last 7 days
   ```

   **Reporte: "Performance Overview"**
   ```
   Dimensions: Date
   Metrics: Average value (app_startup_time)
   Date range: Last 30 days
   ```

### **Exportación de Datos**

Para análisis avanzado, configura exportación a:
- **Google Sheets**: Para reportes automáticos
- **BigQuery**: Para análisis de datos complejos
- **Data Studio**: Para dashboards públicos

---

## 🔍 **Paso 7: Testing y Validación**

### **Verificar que los Datos Fluyen**

1. **Debug View**:
   ```bash
   adb shell setprop debug.firebase.analytics.app es.itram.basketmatch.debug
   ```

2. **Eventos de Prueba**:
   - Navegar por la app
   - Ver equipos diferentes
   - Realizar búsquedas
   - Verificar en DebugView que aparecen los eventos

3. **Realtime Dashboard**:
   - Ir a Analytics → Realtime
   - Verificar que aparecen usuarios activos
   - Confirmar que los eventos se registran

---

## 📊 **Resultado Esperado**

Con esta configuración tendrás:

✅ **5 dashboards especializados** para diferentes aspectos de tu app
✅ **Métricas en tiempo real** de engagement de equipos
✅ **Alertas automáticas** para problemas de performance  
✅ **Audiencias segmentadas** para marketing dirigido
✅ **KPIs claros** para medir el éxito de la app
✅ **Monitoreo mobile** desde cualquier lugar

---

## 🎯 **Próximos Pasos**

1. **Implementar dashboards** siguiendo esta guía
2. **Configurar alertas** para monitoreo proactivo
3. **Exportar datos** a herramientas de análisis
4. **Crear reportes semanales** automatizados
5. **Optimizar la app** basándose en insights

¿Necesitas ayuda con algún paso específico de la configuración?
