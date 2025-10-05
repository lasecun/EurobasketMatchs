package es.itram.basketmatch.presentation.viewmodel

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itram.basketmatch.analytics.AnalyticsManager
import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.entity.Team
import es.itram.basketmatch.domain.usecase.GetAllMatchesUseCase
import es.itram.basketmatch.domain.usecase.GetAllTeamsUseCase
import es.itram.basketmatch.domain.usecase.ManageStaticDataUseCase
import es.itram.basketmatch.domain.service.DataSyncService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * ViewModel para la pantalla principal con sincronización automática
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val getAllMatchesUseCase: GetAllMatchesUseCase,
    private val getAllTeamsUseCase: GetAllTeamsUseCase,
    private val dataSyncService: DataSyncService,
    private val manageStaticDataUseCase: ManageStaticDataUseCase,
    private val analyticsManager: AnalyticsManager,
    val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches.asStateFlow()
    
    // Cache de todos los partidos para filtrado eficiente
    private val _allMatches = MutableStateFlow<List<Match>>(emptyList())

    private val _teams = MutableStateFlow<Map<String, Team>>(emptyMap())
    val teams: StateFlow<Map<String, Team>> = _teams.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    // 📊 Smart Sync - Datos estáticos y sincronización inteligente
    val smartSyncState = manageStaticDataUseCase.syncState
    val lastSyncTime = manageStaticDataUseCase.lastSyncTime

    init {
        Log.d("MainViewModel", "🚀 Inicializando MainViewModel con sistema híbrido de datos...")
        initializeApp()
    }
    
    /**
     * 📊 Analytics: Track main screen view
     */
    fun trackMainScreenView() {
        analyticsManager.trackScreenView(
            screenName = AnalyticsManager.SCREEN_HOME,
            screenClass = "MainViewModel"
        )
    }
    
    /**
     * Verifica si es necesario sincronizar y carga los datos
     */
    private fun checkAndSyncData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {

                
                // Cargar datos desde la base de datos local
                loadLocalData()
                
            } catch (e: Exception) {
                Log.e("MainViewModel", "❌ Error general al cargar datos", e)
                // Solo mostrar error si no hay datos locales disponibles
                tryLoadLocalDataOrShowError(e)
            }
        }
    }
    
    /**
     * Intenta cargar datos locales, si no hay muestra error
     */
    private suspend fun tryLoadLocalDataOrShowError(originalException: Exception) {
        try {
            val teams = getAllTeamsUseCase().first()
            val matches = getAllMatchesUseCase().first()
            
            if (teams.isNotEmpty() || matches.isNotEmpty()) {
                Log.d("MainViewModel", "✅ Usando datos locales disponibles como fallback")
                loadLocalData()
            } else {
                Log.e("MainViewModel", "❌ No hay datos locales disponibles")
                _error.value = "No hay datos disponibles. Verifica tu conexión a internet."
                _isLoading.value = false
                _isSyncing.value = false
            }
        } catch (e: Exception) {
            Log.e("MainViewModel", "❌ Error accediendo a datos locales", e)
            _error.value = "Error al acceder a los datos: ${originalException.message}"
            _isLoading.value = false
            _isSyncing.value = false
        }
    }
    
    /**
     * Carga los datos desde la base de datos local
     */
    private suspend fun loadLocalData() {
        try {
            Log.d("MainViewModel", "📱 Cargando datos completos desde base de datos local...")
            _isLoading.value = true
            
            // Cargar equipos
            val teams = getAllTeamsUseCase().first()
            Log.d("MainViewModel", "✅ Equipos cargados: ${teams.size}")
            _teams.value = teams.associateBy { it.id }
            
            // Cargar partidos
            val matches = getAllMatchesUseCase().first()
            Log.d("MainViewModel", "✅ Partidos cargados: ${matches.size}")
            
            // Guardar todos los partidos en cache para uso del calendario y próximos partidos
            _allMatches.value = matches
            
            // Verificar que tenemos datos
            if (matches.isNotEmpty()) {
                Log.d("MainViewModel", "🏀 Datos disponibles - próximo partido funcionará correctamente")
                val nextMatchDay = findNextMatchDay()
                if (nextMatchDay != null) {
                    Log.d("MainViewModel", "📅 Próximo día con partidos: $nextMatchDay")
                } else {
                    Log.d("MainViewModel", "📅 No hay partidos futuros disponibles")
                }
            } else {
                Log.w("MainViewModel", "⚠️ No hay partidos en la base de datos local")
            }
            
            // Filtrar partidos por la fecha seleccionada (inicialmente es hoy)
            filterMatchesByDate(matches)
            
            _isLoading.value = false
            
        } catch (e: Exception) {
            Log.e("MainViewModel", "❌ Error cargando datos locales", e)
            _error.value = "Error al cargar datos locales: ${e.message}"
            _isLoading.value = false
        }
    }
    
    /**
     * Fuerza la sincronización de datos (regenera desde API real)
     */
    fun refreshData() {
        viewModelScope.launch {
            _isSyncing.value = true
            _error.value = null
            _syncMessage.value = "Obteniendo datos actualizados desde API EuroLeague..."
            
            try {
                Log.d("MainViewModel", "🔄 Iniciando regeneración de datos desde API real...")
                
                // Usar el sistema híbrido para regenerar datos desde API
                val result = manageStaticDataUseCase.refreshStaticDataFromApi()
                
                if (result.isSuccess) {
                    val generationResult = result.getOrNull()!!
                    Log.d("MainViewModel", "✅ Datos regenerados desde API: ${generationResult.teamsGenerated} equipos, ${generationResult.matchesGenerated} partidos")
                    _syncMessage.value = "Datos actualizados desde API: ${generationResult.teamsGenerated} equipos, ${generationResult.matchesGenerated} partidos"
                    
                    // 📊 Analytics: Track successful API data refresh
                    analyticsManager.logCustomEvent("api_data_refreshed", android.os.Bundle().apply {
                        putString("refresh_type", "manual_api_refresh")
                        putString("screen", "home")
                        putInt("teams_generated", generationResult.teamsGenerated)
                        putInt("matches_generated", generationResult.matchesGenerated)
                    })
                    
                    // Recargar datos locales
                    loadLocalData()
                    
                } else {
                    Log.e("MainViewModel", "❌ Error regenerando datos desde API: ${result.exceptionOrNull()?.message}")
                    _error.value = "Error obteniendo datos desde API: ${result.exceptionOrNull()?.message}"
                }
                
            } catch (e: Exception) {
                Log.e("MainViewModel", "❌ Error en refresh desde API", e)
                _error.value = "Error al actualizar desde API: ${e.message}"
            } finally {
                _isSyncing.value = false
            }
        }
    }

    /**
     * Selecciona una fecha específica
     */
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        filterMatchesBySelectedDate()
        
        // 📊 Analytics: Track date navigation
        analyticsManager.logCustomEvent("date_selected", android.os.Bundle().apply {
            putString("selected_date", date.toString())
            putString("screen", "home")
            putInt("total_matches", _matches.value.size)
        })
    }

    /**
     * Navega al día anterior
     */
    fun goToPreviousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
        filterMatchesBySelectedDate()
    }

    /**
     * Navega al día siguiente
     */
    fun goToNextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
        filterMatchesBySelectedDate()
    }

    /**
     * Navega al día de hoy
     */
    fun goToToday() {
        _selectedDate.value = LocalDate.now()
        filterMatchesBySelectedDate()
    }

    /**
     * Establece una fecha específica
     */
    fun setSelectedDate(date: LocalDate) {
        Log.d("MainViewModel", "📅 Estableciendo fecha seleccionada: $date")
        _selectedDate.value = date
        // Filtrar directamente con los datos que ya tenemos
        filterMatchesByDateSync()
    }

    /**
     * Filtra partidos por la fecha seleccionada (versión síncrona)
     */
    private fun filterMatchesByDateSync() {
        try {
            val allMatches = _allMatches.value
            filterMatchesByDate(allMatches)
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error filtrando partidos: ${e.message}", e)
            _error.value = "Error cargando partidos para la fecha seleccionada"
        }
    }

    /**
     * Filtra partidos por la fecha seleccionada (versión async para cargas iniciales)
     */
    private fun filterMatchesBySelectedDate() {
        viewModelScope.launch {
            try {
                val allMatches = getAllMatchesUseCase().first()
                // Guardar todos los partidos para futuros filtros
                _allMatches.value = allMatches
                filterMatchesByDate(allMatches)
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Las CancellationException deben re-lanzarse para mantener el comportamiento de cancelación
                throw e
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error filtrando partidos: ${e.message}", e)
                _error.value = "Error cargando partidos para la fecha seleccionada"
            }
        }
    }

    /**
     * Filtra una lista de partidos por la fecha seleccionada
     */
    private fun filterMatchesByDate(allMatches: List<Match>) {
        val selectedDate = _selectedDate.value
        Log.d("MainViewModel", "🔍 Filtrando partidos - Fecha seleccionada: $selectedDate")
        Log.d("MainViewModel", "🔍 Total partidos disponibles: ${allMatches.size}")
        
        // Mostrar información de rango de fechas para debug
        if (allMatches.isNotEmpty()) {
            val allDates = allMatches.map { it.dateTime.toLocalDate() }.sorted()
            val firstDate = allDates.first()
            val lastDate = allDates.last()
            Log.d("MainViewModel", "🔍 Rango de fechas: desde $firstDate hasta $lastDate")
            
            val sampleDates = allMatches.take(5).map { it.dateTime.toLocalDate() }
            Log.d("MainViewModel", "🔍 Fechas de ejemplo en datos: $sampleDates")
        }
        
        val filteredMatches = allMatches.filter { match ->
            val matchDate = match.dateTime.toLocalDate()
            val matches = matchDate == selectedDate
            if (matches) {
                Log.d("MainViewModel", "✅ Partido encontrado para $selectedDate: ${match.homeTeamName} vs ${match.awayTeamName}")
            }
            matches
        }.sortedBy { it.dateTime }
        
        Log.d("MainViewModel", "🔍 Partidos filtrados para $selectedDate: ${filteredMatches.size}")
        _matches.value = filteredMatches
    }

    /**
     * Obtiene la fecha seleccionada formateada
     */
    fun getFormattedSelectedDate(): String {
        val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")
        return _selectedDate.value.format(formatter)
    }

    /**
     * Obtiene un equipo por ID
     */
    fun getTeamById(teamId: String): Team? {
        return _teams.value[teamId]
    }

    /**
     * Encuentra la próxima fecha que tenga partidos disponibles después de la fecha seleccionada
     */
    fun findNextMatchDay(): LocalDate? {
        val currentDate = _selectedDate.value
        val allMatches = _allMatches.value
        
        return allMatches
            .map { it.dateTime.toLocalDate() }
            .filter { it.isAfter(currentDate) }
            .sorted()
            .firstOrNull()
    }
    
    /**
     * Navega al próximo día que tenga partidos disponibles
     */
    fun goToNextAvailableMatchDay() {
        val nextMatchDay = findNextMatchDay()
        if (nextMatchDay != null) {
            _selectedDate.value = nextMatchDay
            filterMatchesBySelectedDate()
            
            // 📊 Analytics: Track next match day navigation
            analyticsManager.logCustomEvent("next_match_day_navigation", android.os.Bundle().apply {
                putString("next_match_date", nextMatchDay.toString())
                putString("screen", "home")
                putString("action", "go_to_next_match_day")
            })
        }
    }
    
    /**
     * 📊 Analytics: Track calendar navigation from main screen
     */
    fun trackCalendarNavigation() {
        analyticsManager.logCustomEvent("calendar_navigation", android.os.Bundle().apply {
            putString("source_screen", "home")
            putString("selected_date", _selectedDate.value.toString())
            putInt("current_matches", _matches.value.size)
        })
    }
    
    /**
     * 📊 Analytics: Track match click from main screen
     */
    fun trackMatchClicked(match: Match) {
        analyticsManager.trackMatchViewed(
            matchId = match.id,
            homeTeam = match.homeTeamName,
            awayTeam = match.awayTeamName,
            isLive = match.status == es.itram.basketmatch.domain.entity.MatchStatus.LIVE
        )
        
        analyticsManager.logCustomEvent("match_clicked_from_home", android.os.Bundle().apply {
            putString("match_id", match.id)
            putString("home_team", match.homeTeamName)
            putString("away_team", match.awayTeamName)
            putString("match_status", match.status.name)
            putString("source", "home_screen")
            putString("selected_date", _selectedDate.value.toString())
        })
    }
    
    /**
     * 📊 Analytics: Track team click from match cards
     */
    fun trackTeamClickedFromMatch(teamId: String, teamName: String, matchId: String, source: String = "match_card") {
        analyticsManager.trackTeamViewed(
            teamCode = teamId,
            teamName = teamName,
            source = source
        )
        
        analyticsManager.logCustomEvent("team_clicked_from_match", android.os.Bundle().apply {
            putString("team_code", teamId)
            putString("team_name", teamName)
            putString("match_id", matchId)
            putString("source", source)
            putString("selected_date", _selectedDate.value.toString())
        })
    }
    
    /**
     * Verifica si la fecha seleccionada es hoy
     */
    fun isSelectedDateToday(): Boolean {
        return _selectedDate.value == LocalDate.now()
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _error.value = null
    }
    
    // ===== 📊 SMART SYNC - NUEVOS MÉTODOS =====
    
    /**
     * Inicialización de la app con sistema de datos estáticos
     */
    private fun initializeApp() {
        viewModelScope.launch {
            try {
                Log.d("MainViewModel", "🏗️ Inicializando aplicación con carga completa de datos...")
                
                // Inicializar datos estáticos primero
                val staticResult = manageStaticDataUseCase.initializeStaticData()
                if (staticResult.isFailure) {
                    Log.w("MainViewModel", "⚠️ Falló inicialización estática, usando método tradicional")
                    checkAndSyncData()
                    return@launch
                }
                
                Log.d("MainViewModel", "✅ Datos estáticos inicializados")
                
                // CAMBIO: Siempre cargar datos locales completos al inicio
                // Esto asegura que el calendario y "próximo partido" funcionen desde el primer momento
                loadLocalData()
                

            } catch (e: Exception) {
                Log.e("MainViewModel", "❌ Error en inicialización, fallback a método tradicional", e)
                checkAndSyncData()
            }
        }
    }
    

    /**
     * Sincronización manual de datos dinámicos
     */
    fun performManualSync() {
        viewModelScope.launch {
            try {
                Log.d("MainViewModel", "🔄 Iniciando sincronización manual...")
                val result = manageStaticDataUseCase.syncDynamicData(forceSync = true)
                
                if (result.isSuccess) {
                    Log.d("MainViewModel", "✅ Sincronización manual exitosa")
                    loadLocalData() // Recargar datos después de sincronizar
                } else {
                    Log.e("MainViewModel", "❌ Error en sincronización manual: ${result.exceptionOrNull()}")
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "❌ Error en sincronización manual", e)
            }
        }
    }
    
    /**
     * Verificar actualizaciones disponibles
     */
    fun checkForUpdates() {
        viewModelScope.launch {
            try {
                Log.d("MainViewModel", "🔍 Verificando actualizaciones...")
                val result = manageStaticDataUseCase.checkForUpdates()
                
                if (result.isSuccess) {
                    val updateResult = result.getOrNull()!!
                    Log.d("MainViewModel", "✅ Verificación completada: ${updateResult.message}")
                } else {
                    Log.e("MainViewModel", "❌ Error verificando actualizaciones: ${result.exceptionOrNull()}")
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "❌ Error verificando actualizaciones", e)
            }
        }
    }
    
    /**
     * Verifica si hay sincronización en progreso
     */
    fun isSyncInProgress(): Boolean {
        return manageStaticDataUseCase.isSyncInProgress() || _isSyncing.value
    }
}
