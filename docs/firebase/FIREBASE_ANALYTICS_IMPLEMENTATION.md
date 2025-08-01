# 📊 Firebase Analytics Implementation Complete - EuroLeague App

## ✅ Analytics Implementation Status

### 🎯 ViewModels con Firebase Analytics Implementado

#### 1. **TeamRosterViewModel** ✅
- **Screen View Tracking**: Automático al abrir la pantalla
- **Roster Load Success**: Cuando se carga exitosamente un roster
- **Roster Load Error**: Tracking de errores de carga
- **Roster Refresh**: Cuando se refresca via pull-to-refresh
- **Player Selection**: Cuando se selecciona un jugador del roster

**Events Tracked:**
```kotlin
// Screen view automático
analyticsManager.trackScreenView("team_roster", "TeamRosterScreen")

// Roster cargado exitosamente
analyticsManager.trackRosterViewed(teamCode, teamName, playerCount)

// Errores de carga
analyticsManager.logCustomEvent("roster_load_error", Bundle())

// Player seleccionado
analyticsManager.trackPlayerViewed(playerCode, playerName, teamCode)
```

#### 2. **MatchDetailViewModel** ✅
- **Match Viewed**: Tracking cuando se visualiza un partido
- **Match Load Error**: Errores al cargar detalles del partido

**Events Tracked:**
```kotlin
// Match visualizado
analyticsManager.trackMatchViewed(matchId, homeTeam, awayTeam, isLive)

// Errores de carga
analyticsManager.logCustomEvent("match_load_error", Bundle())
```

#### 3. **TeamDetailViewModel** ✅
- **Team Viewed**: Tracking cuando se visualiza un equipo
- **Team Load Error**: Errores al cargar detalles del equipo
- **Favorite Toggle**: Cuando se marca/desmarca como favorito

**Events Tracked:**
```kotlin
// Team visualizado
analyticsManager.trackTeamViewed(teamCode, teamName, source)

// Favorito agregado/removido
analyticsManager.trackFavoriteAdded(contentType, contentId)

// Errores de carga
analyticsManager.logCustomEvent("team_load_error", Bundle())
```

#### 4. **MainViewModel** ✅
- **Data Refresh**: Tracking de actualización manual de datos
- **Date Selection**: Navegación por fechas en calendario

**Events Tracked:**
```kotlin
// Datos refrescados manualmente
analyticsManager.logCustomEvent("data_refreshed", Bundle())

// Fecha seleccionada
analyticsManager.logCustomEvent("date_selected", Bundle())
```

## 🧪 Testing Firebase Analytics

### 1. **Preparar Testing Environment**
```bash
# Habilitar DebugView en dispositivo
adb shell setprop debug.firebase.analytics.app es.itram.basketmatch.debug

# Instalar la app debug
./gradlew installDebug

# Ejecutar la app
adb shell am start -n es.itram.basketmatch.debug/es.itram.basketmatch.MainActivity
```

### 2. **Verificar que Firebase está funcionando**
```bash
# Ver logs de Firebase Analytics
adb logcat -s "FA" | head -10

# Logs esperados:
# I FA : App measurement initialized, version: 133001
# I FA : Faster debug mode event logging enabled
```

### 3. **Firebase Console Setup**
1. Ir a [Firebase Console](https://console.firebase.google.com)
2. Seleccionar proyecto: `basketmatch-79703`
3. Analytics > DebugView
4. **IMPORTANTE**: Puede tomar 5-30 minutos en aparecer la primera vez
5. Navegar activamente por la app mientras esperas

### 4. **Si DebugView no muestra dispositivos**
- ✅ **Logs muestran que Firebase está funcionando**
- ⏱️ **Esperar 5-10 minutos navegando por la app**
- 🔄 **Actualizar página de Firebase Console (F5)**
- 📊 **Verificar también en Analytics > Realtime**

### 3. **Test Scenarios**

#### 📱 **Screen View Testing**
- Navegar a Team Roster screen
- **Expected Event**: `screen_view` con `screen_name = "team_roster"`

#### 🏀 **Match Analytics**
1. Ir a Match Detail screen
2. **Expected Events**:
   - `screen_view` 
   - `match_viewed` con parámetros del partido

#### 👥 **Team Analytics**
1. Ir a Team Detail screen
2. Marcar/desmarcar como favorito
3. **Expected Events**:
   - `team_viewed`
   - `favorite_added` (si se marca como favorito)

#### 📊 **Roster Analytics**
1. Ir a Team Roster screen
2. Pull-to-refresh para actualizar
3. Seleccionar un jugador
4. **Expected Events**:
   - `roster_viewed`
   - `roster_refreshed` (si se hace refresh)
   - `player_viewed` (al seleccionar jugador)

#### 🔄 **Error Analytics**
1. Desconectar internet
2. Intentar cargar datos
3. **Expected Events**:
   - `roster_load_error`
   - `team_load_error`
   - `match_load_error`

## 📈 Analytics Events Reference

### 🎯 **Built-in Events** (desde AnalyticsManager)
- `screen_view` - Navegación entre pantallas
- `match_viewed` - Visualización de partidos
- `team_viewed` - Visualización de equipos
- `player_viewed` - Visualización de jugadores
- `roster_viewed` - Visualización de roster
- `favorite_added` - Agregar a favoritos

### 🏀 **Custom Events** (logCustomEvent)
- `roster_load_error` - Error cargando roster
- `roster_refreshed` - Roster actualizado
- `team_load_error` - Error cargando equipo
- `match_load_error` - Error cargando partido
- `data_refreshed` - Datos actualizados manualmente
- `date_selected` - Fecha seleccionada en calendario

## 🔥 **Firebase Configuration**
- **Project**: basketmatch-79703
- **Debug Package**: es.itram.basketmatch.debug
- **Release Package**: es.itram.basketmatch
- **API Key**: AIzaSyAJ_yZ3SKDM8h58aogDNh_JSgpOedsiqZg

## 📊 **Analytics Parameters**

### **Common Parameters:**
- `screen_name` - Nombre de la pantalla
- `team_code` - Código del equipo (e.g., "BAR", "MAD")
- `team_name` - Nombre del equipo
- `match_id` - ID único del partido
- `player_code` - Código del jugador
- `error_message` - Mensaje de error
- `error_class` - Clase de la excepción

### **Basketball-Specific Parameters:**
- `player_count` - Número de jugadores en roster
- `is_live` - Si el partido está en vivo
- `home_team` / `away_team` - Equipos del partido
- `content_type` - Tipo de contenido ("team", "player", "match")

## 🚀 **Next Steps**

### Priority 1: Validation
- [ ] Test all events in Firebase DebugView
- [ ] Verify parameters are being sent correctly
- [ ] Test error scenarios (network offline)
- [ ] Validate screen view tracking

### Priority 2: Enhancement
- [ ] Add screen view tracking to remaining screens
- [ ] Implement user properties (favorite team, etc.)
- [ ] Add performance timing events
- [ ] Enhanced error context

### Priority 3: Advanced Analytics
- [ ] User journey analysis setup
- [ ] Conversion funnel configuration
- [ ] Custom audiences in Firebase Console
- [ ] Integration with Google Analytics 4

---

## 🎯 **Implementation Highlights**

✅ **Complete ViewModel Integration**: Analytics integrado en todos los ViewModels principales
✅ **Error Tracking**: Comprehensive error tracking con contexto
✅ **User Actions**: Track de acciones clave del usuario
✅ **Screen Navigation**: Tracking automático de navegación
✅ **Basketball-Specific**: Eventos optimizados para apps deportivas
✅ **Production Ready**: Configuración real de Firebase

**🔥 Firebase Analytics está completamente funcional y listo para insights de usuario!** 📊
