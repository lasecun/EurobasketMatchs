# 🚀 Plan de Implementación - Datos Reales EuroLeague

## 📋 Fases de Implementación

### **Fase 1: Configuración de Red (1-2 días)**

#### 1.1 Agregar dependencias de red en `build.gradle.kts`:
```kotlin
// Networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Coroutines para async calls
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

#### 1.2 Crear configuración de red
- `NetworkModule.kt` (Hilt/Dagger)
- `ApiKeyInterceptor.kt`
- `LoggingInterceptor.kt`

### **Fase 2: API Remote DataSource (2-3 días)**

#### 2.1 Investigar y elegir API
**Opciones evaluadas:**
- ✅ **API-Sports Basketball** (RapidAPI) - Recomendado
- ✅ **SportRadar** - Datos profesionales
- ✅ **Custom scraping de euroleague.net**

#### 2.2 Crear DTOs (Data Transfer Objects)
```kotlin
data class TeamApiDto(
    val id: Int,
    val name: String,
    val country: String,
    val logo: String,
    // ... otros campos de la API
)

data class MatchApiDto(
    val id: Int,
    val homeTeam: TeamApiDto,
    val awayTeam: TeamApiDto,
    val date: String,
    val status: String,
    val scores: ScoreDto?
)
```

#### 2.3 Crear API Service
```kotlin
interface EuroLeagueApiService {
    @GET("teams")
    suspend fun getTeams(@Query("league") league: String): ApiResponse<List<TeamApiDto>>
    
    @GET("games")
    suspend fun getMatches(
        @Query("league") league: String,
        @Query("season") season: String
    ): ApiResponse<List<MatchApiDto>>
}
```

#### 2.4 Implementar RemoteDataSource
```kotlin
@Singleton
class TeamRemoteDataSource @Inject constructor(
    private val apiService: EuroLeagueApiService
) {
    suspend fun getAllTeams(): List<TeamApiDto> = 
        apiService.getTeams("euroleague").data
}
```

### **Fase 3: Mappers API → Domain (1 día)**

#### 3.1 Crear mappers para DTOs
```kotlin
object TeamApiMapper {
    fun toDomain(dto: TeamApiDto): Team {
        return Team(
            id = dto.id.toString(),
            name = dto.name,
            country = dto.country,
            logoUrl = dto.logo,
            isFavorite = false // default
        )
    }
    
    fun toDomainList(dtos: List<TeamApiDto>): List<Team> = 
        dtos.map { toDomain(it) }
}
```

### **Fase 4: Repository con Sync Logic (2-3 días)**

#### 4.1 Actualizar Repository para usar ambas fuentes
```kotlin
@Singleton
class TeamRepositoryImpl @Inject constructor(
    private val teamDao: TeamDao,
    private val remoteDataSource: TeamRemoteDataSource,
    private val networkManager: NetworkManager
) : TeamRepository {

    override fun getAllTeams(): Flow<List<Team>> {
        return teamDao.getAllTeams().map { entities ->
            TeamMapper.toDomainList(entities)
        }.onStart {
            // Sync from remote if needed
            refreshTeamsIfNeeded()
        }
    }
    
    private suspend fun refreshTeamsIfNeeded() {
        if (networkManager.isConnected() && shouldRefresh()) {
            try {
                val remoteTeams = remoteDataSource.getAllTeams()
                val domainTeams = TeamApiMapper.toDomainList(remoteTeams)
                val entities = TeamMapper.fromDomainList(domainTeams)
                teamDao.insertTeams(entities)
            } catch (e: Exception) {
                // Log error, continue with local data
                Log.e("TeamRepository", "Error syncing teams", e)
            }
        }
    }
    
    private fun shouldRefresh(): Boolean {
        // Logic to determine if we need to refresh
        // e.g., last sync was > 24 hours ago
        return true
    }
}
```

### **Fase 5: Testing de Integración (1-2 días)**

#### 5.1 Tests para RemoteDataSource
```kotlin
@Test
fun `when api returns teams, then teams are mapped correctly`() = runTest {
    // Given
    val apiTeams = listOf(
        TeamApiDto(1, "Real Madrid", "Spain", "logo_url")
    )
    every { apiService.getTeams("euroleague") } returns 
        ApiResponse(data = apiTeams)
    
    // When
    val result = remoteDataSource.getAllTeams()
    
    // Then
    assertThat(result).hasSize(1)
    assertThat(result[0].name).isEqualTo("Real Madrid")
}
```

#### 5.2 Tests para Repository con sync
```kotlin
@Test
fun `when network available, then data is synced from remote`() = runTest {
    // Test sync logic
}

@Test
fun `when network unavailable, then local data is used`() = runTest {
    // Test offline behavior
}
```

### **Fase 6: UI Indicators (1 día)**

#### 6.1 Agregar estados de sync al ViewModel
```kotlin
data class MainUiState(
    val teams: List<Team> = emptyList(),
    val matches: List<Match> = emptyList(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false, // 🆕 NUEVO
    val error: String? = null,
    val lastSyncTime: String? = null // 🆕 NUEVO
)
```

#### 6.2 Indicadores en UI
- Loading spinner durante sync
- "Last updated" timestamp
- Offline indicator
- Pull-to-refresh

## 🎯 **Resultados Esperados**

### **Después de la implementación:**
✅ **Datos reales** de equipos y partidos de EuroLeague  
✅ **Sync automático** en background  
✅ **Offline-first** - app funciona sin internet  
✅ **Cache inteligente** - reduce llamadas a API  
✅ **Tests completos** para toda la funcionalidad  
✅ **UX mejorada** con indicadores de estado  

### **Beneficios técnicos:**
- **Clean Architecture** mantenida
- **Single Source of Truth** (Room DB)
- **Error handling** robusto
- **Performance optimizada** 
- **Testing coverage** completo

## 🔄 **Orden de Implementación Recomendado**

1. **Elegir API** → Investigar y decidir fuente de datos
2. **Configurar network** → Retrofit + OkHttp + Interceptors  
3. **Crear DTOs** → Modelar respuestas de API
4. **Implement RemoteDataSource** → Llamadas a API
5. **Crear mappers** → API DTO → Domain entities
6. **Actualizar Repository** → Sync logic + offline-first
7. **Testing completo** → Unit + Integration tests
8. **UI improvements** → Loading states + UX

## ⚡ **Quick Start - Primeros Pasos**

**¿Empezamos con la Fase 1?** 
1. Agregar dependencias de red
2. Investigar APIs disponibles
3. Configurar estructura básica de network

**¿Cuál fase te interesa más comenzar?**
