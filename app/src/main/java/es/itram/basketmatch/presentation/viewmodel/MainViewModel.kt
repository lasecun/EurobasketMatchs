package es.itram.basketmatch.presentation.viewmodel

import android.util.Log
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
    private val dataSyncService: DataSyncService
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches.asStateFlow()

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
                        Log.e("MainViewModel", "❌ Error en sincronización: ${syncResult.exceptionOrNull()?.message}")
                        _error.value = "Error al sincronizar datos: ${syncResult.exceptionOrNull()?.message}"
                    }
                    
                    _isSyncing.value = false
                } else {
                    Log.d("MainViewModel", "✅ Datos actuales - cargando desde base de datos local")
                }
                
                // Cargar datos desde la base de datos local
                loadLocalData()
                
            } catch (e: Exception) {
                Log.e("MainViewModel", "❌ Error general al cargar datos", e)
                _error.value = "Error al cargar datos: ${e.message}"
                _isLoading.value = false
                _isSyncing.value = false
            }
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
     * Filtra partidos por la fecha seleccionada
     */
    private fun filterMatchesBySelectedDate() {
        viewModelScope.launch {
            try {
                val allMatches = getAllMatchesUseCase().first()
                filterMatchesByDate(allMatches)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error filtrando partidos: ${e.message}", e)
            }
        }
    }

    /**
     * Filtra una lista de partidos por la fecha seleccionada
     */
    private fun filterMatchesByDate(allMatches: List<Match>) {
        val selectedDate = _selectedDate.value
        val filteredMatches = allMatches.filter { match ->
            match.dateTime.toLocalDate() == selectedDate
        }.sortedBy { it.dateTime }
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
     * Limpia el mensaje de error
     */
    fun clearError() {
        _error.value = null
    }
}
