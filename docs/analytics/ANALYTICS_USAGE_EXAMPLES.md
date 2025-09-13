# 📊 Guía de Uso de Analytics - BasketMatch

## 🚀 Implementación Completada

He solucionado todos los problemas críticos en tu sistema de analytics:

### ✅ **Problemas Resueltos:**
1. **Archivos vacíos implementados**: `EventDispatcher`, `ScreenTracker`, `AnalyticsEvent`
2. **AnalyticsManager completado**: Agregado método `trackError` y otros métodos faltantes
3. **Error de evento reservado corregido**: Cambié `user_engagement` por `app_engagement`

## 🎯 **Cómo Usar el Sistema**

### 1. **Screen Tracking en Compose**

```kotlin
@Composable
fun TeamDetailScreen(teamCode: String) {
    val screenTracker = hiltViewModel<ScreenTracker>()
    
    screenTracker.TrackScreen(
        screenName = AnalyticsManager.SCREEN_TEAM_DETAIL,
        screenClass = "TeamDetailScreen"
    ) {
        // Tu contenido de pantalla aquí
        TeamDetailContent(teamCode = teamCode)
    }
}

// O usando métodos específicos
@Composable
fun HomeScreen() {
    val screenTracker = hiltViewModel<ScreenTracker>()
    
    screenTracker.TrackHomeScreen {
        HomeContent()
    }
}
```

### 2. **Event Tracking con EventDispatcher**

```kotlin
@Composable
fun MatchCard(match: Match) {
    val eventDispatcher = hiltViewModel<EventDispatcher>()
    
    Card(
        onClick = {
            // Track cuando el usuario ve un partido
            eventDispatcher.trackMatchViewed(
                matchId = match.id,
                homeTeam = match.homeTeam.name,
                awayTeam = match.awayTeam.name,
                isLive = match.isLive
            )
        }
    ) {
        MatchContent(match)
    }
}
```

### 3. **Tracking Directo con AnalyticsManager**

```kotlin
class TeamRepository @Inject constructor(
    private val analyticsManager: AnalyticsManager
) {
    
    suspend fun syncTeamData(): Result<List<Team>> {
        val startTime = System.currentTimeMillis()
        
        analyticsManager.trackDataSyncStarted("teams")
        
        return try {
            val teams = apiService.getTeams()
            val duration = System.currentTimeMillis() - startTime
            
            analyticsManager.trackDataSyncCompleted(
                syncType = "teams",
                durationMs = duration,
                itemsCount = teams.size
            )
            
            Result.success(teams)
        } catch (e: Exception) {
            analyticsManager.trackDataSyncFailed(
                syncType = "teams",
                errorType = "api_error",
                errorMessage = e.message ?: "Unknown error"
            )
            Result.failure(e)
        }
    }
}
```

### 4. **Eventos Tipados con AnalyticsEvent**

```kotlin
// En tu ViewModel
class TeamDetailViewModel @Inject constructor(
    private val eventDispatcher: EventDispatcher
) : ViewModel() {
    
    fun onTeamFavorited(team: Team) {
        eventDispatcher.dispatch(
            AnalyticsEvent.TeamContentEvent(
                action = TeamAction.FAVORITED,
                teamCode = team.code,
                teamName = team.name,
                source = "team_detail"
            )
        )
    }
    
    fun onSearchPerformed(query: String, results: List<Any>) {
        eventDispatcher.dispatch(
            AnalyticsEvent.SearchEvent(
                query = query,
                resultCount = results.size,
                category = "teams"
            )
        )
    }
}
```

### 5. **Performance Tracking**

```kotlin
class ImageLoader @Inject constructor(
    private val analyticsManager: AnalyticsManager
) {
    
    suspend fun loadImage(url: String): Bitmap {
        val startTime = System.currentTimeMillis()
        
        return try {
            val bitmap = loadBitmapFromUrl(url)
            val loadTime = System.currentTimeMillis() - startTime
            
            analyticsManager.trackImageLoadTime(
                imageUrl = url,
                loadTimeMs = loadTime,
                success = true
            )
            
            bitmap
        } catch (e: Exception) {
            val loadTime = System.currentTimeMillis() - startTime
            
            analyticsManager.trackImageLoadTime(
                imageUrl = url,
                loadTimeMs = loadTime,
                success = false
            )
            
            throw e
        }
    }
}
```

## 🎯 **Eventos Disponibles**

### Basketball Content Events:
- `MatchEvent` - Para eventos de partidos
- `TeamContentEvent` - Para eventos de equipos  
- `PlayerEvent` - Para eventos de jugadores

### Discovery Events:
- `SearchEvent` - Para búsquedas
- `FilterEvent` - Para aplicación de filtros

### Data Events:
- `DataSyncEvent` - Para sincronización de datos

### Performance Events:
- `PerformanceEvent` - Para métricas de rendimiento

## 🔧 **Configuración en tu Application**

```kotlin
@HiltAndroidApp
class BasketMatchApplication : Application() {
    
    @Inject
    lateinit var analyticsManager: AnalyticsManager
    
    override fun onCreate() {
        super.onCreate()
        
        // Configurar analytics
        analyticsManager.setAnalyticsCollectionEnabled(true)
        
        // Configurar crash reporting
        analyticsManager.setUserId("user_${Random.nextLong()}")
        
        // Track app startup
        val startupTime = System.currentTimeMillis() - applicationStartTime
        analyticsManager.trackAppStartupTime(startupTime)
    }
}
```

## 🐛 **Testing y Debug**

Para verificar que analytics funciona:

1. **Firebase Debug View**: Habilita debug mode
2. **Logcat**: Busca logs de `FirebaseAnalytics`
3. **Firebase Console**: Verifica eventos en tiempo real

```bash
# Habilitar debug mode
adb shell setprop debug.firebase.analytics.app es.itram.basketmatch.debug

# Ver logs de analytics
adb logcat | grep -i firebase
```

## 📊 **Métricas Importantes a Trackear**

1. **User Journey**: Screen views, navigation patterns
2. **Content Engagement**: Team/player/match views, favorites
3. **Performance**: Load times, sync duration
4. **Errors**: Crashes, sync failures, API errors
5. **Discovery**: Search queries, filter usage

## 🎯 **Próximos Pasos**

1. Implementar tracking en tus pantallas existentes
2. Agregar tracking en operaciones de datos
3. Configurar alertas en Firebase Console
4. Analizar métricas de engagement y performance
