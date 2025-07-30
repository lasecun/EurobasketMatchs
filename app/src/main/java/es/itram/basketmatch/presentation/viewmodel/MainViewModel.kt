package es.itram.basketmatch.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.entity.Team
import es.itram.basketmatch.domain.usecase.GetAllMatchesUseCase
import es.itram.basketmatch.domain.usecase.GetAllTeamsUseCase
import es.itram.basketmatch.domain.service.DataSyncService
import es.itram.basketmatch.domain.service.SyncResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
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
    
    // Estado del progreso de sincronización
    val syncProgress: StateFlow<DataSyncService.SyncProgress> = dataSyncService.syncProgress
    
    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    init {
        Log.d("MainViewModel", "🚀 Inicializando MainViewModel con sincronización automática...")
        checkAndSyncData()
    }
    
    /**
     * Verifica si es necesario sincronizar y carga los datos
     */
    private fun checkAndSyncData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Verificar si necesitamos sincronizar
                if (dataSyncService.isSyncNeeded()) {
                    Log.d("MainViewModel", "🔄 Sincronización necesaria - obteniendo datos desde API...")
                    _isSyncing.value = true
                    _syncMessage.value = "Obteniendo datos actualizados..."
                    
                    val syncResult = dataSyncService.syncAllData()
                    
                    if (syncResult.isSuccess) {
                        val result = syncResult.getOrNull()!!
                        Log.d("MainViewModel", "✅ Sincronización exitosa: ${result.teamsCount} equipos, ${result.matchesCount} partidos")
                        _syncMessage.value = "Datos actualizados: ${result.teamsCount} equipos, ${result.matchesCount} partidos"
                    } else {
                        Log.w("MainViewModel", "⚠️ Error en sincronización, usando datos locales: ${syncResult.exceptionOrNull()?.message}")
                        // No establecer error aquí, cargar datos locales en su lugar
                    }
                    
                    _isSyncing.value = false
                } else {
                    Log.d("MainViewModel", "✅ Datos actuales - cargando desde base de datos local")
                }
                
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
            Log.d("MainViewModel", "📱 Cargando datos desde base de datos local...")
            
            // Cargar equipos
            val teams = getAllTeamsUseCase().first()
            Log.d("MainViewModel", "✅ Equipos cargados: ${teams.size}")
            _teams.value = teams.associateBy { it.id }
            
            // Cargar partidos
            val matches = getAllMatchesUseCase().first()
            Log.d("MainViewModel", "✅ Partidos cargados: ${matches.size}")
            
            // Guardar todos los partidos en cache
            _allMatches.value = matches
            
            // Filtrar partidos por la fecha seleccionada (inicialmente es hoy)
            filterMatchesByDate(matches)
            
            _isLoading.value = false
            
            // Limpiar mensaje después de un tiempo
            kotlinx.coroutines.delay(3000)
            _syncMessage.value = null
            
        } catch (e: Exception) {
            Log.e("MainViewModel", "❌ Error cargando datos locales", e)
            _error.value = "Error al cargar datos locales: ${e.message}"
            _isLoading.value = false
        }
    }
    
    /**
     * Fuerza la sincronización de datos (llamado desde el botón de refresh)
     */
    fun refreshData() {
        viewModelScope.launch {
            _isSyncing.value = true
            _error.value = null
            _syncMessage.value = "Actualizando datos..."
            
            try {
                val syncResult = dataSyncService.forceSyncData()
                
                if (syncResult.isSuccess) {
                    val result = syncResult.getOrNull()!!
                    Log.d("MainViewModel", "🔄 Sincronización manual exitosa: ${result.teamsCount} equipos, ${result.matchesCount} partidos")
                    _syncMessage.value = "Datos actualizados: ${result.teamsCount} equipos, ${result.matchesCount} partidos"
                    
                    // Recargar datos locales
                    loadLocalData()
                    
                } else {
                    Log.e("MainViewModel", "❌ Error en sincronización manual: ${syncResult.exceptionOrNull()?.message}")
                    _error.value = "Error al actualizar datos: ${syncResult.exceptionOrNull()?.message}"
                }
                
            } catch (e: Exception) {
                Log.e("MainViewModel", "❌ Error en refresh manual", e)
                _error.value = "Error al actualizar: ${e.message}"
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
        }
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
}
