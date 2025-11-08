package es.itram.basketmatch.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.entity.Team
import es.itram.basketmatch.domain.usecase.GetAllMatchesUseCase
import es.itram.basketmatch.domain.usecase.GetAllTeamsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel para la pantalla principal con sincronización automática
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val getAllMatchesUseCase: GetAllMatchesUseCase,
    private val getAllTeamsUseCase: GetAllTeamsUseCase,
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches.asStateFlow()

    private val _allMatches = MutableStateFlow<List<Match>>(emptyList())

    private val _teams = MutableStateFlow<Map<String, Team>>(emptyMap())
    val teams: StateFlow<Map<String, Team>> = _teams.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        Log.d("MainViewModel", "🚀 Inicializando MainViewModel con sistema híbrido de datos...")
        initializeApp()
    }

    /**
     * Init App. Check if we have in local and if not load from remote
     */
    private fun initializeApp() {
        viewModelScope.launch {
            try {
                Log.d("MainViewModel", "🏗️ Inicializando aplicación con carga completa de datos...")
                loadData()
            } catch (e: Exception) {
                Log.e(
                    "MainViewModel",
                    "❌ Error en inicialización",
                    e
                )
            }
        }
    }

    /**
     * Carga los datos desde la base de datos local
     */
    private suspend fun loadData() {
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

            // Filtrar partidos por la fecha seleccionada (inicialmente es hoy)
            filterMatchesByDate()

            _isLoading.value = false

        } catch (e: Exception) {
            Log.e("MainViewModel", "❌ Error cargando datos locales", e)
            _error.value = "Error al cargar datos locales: ${e.message}"
            _isLoading.value = false
        }
    }

    /**
     * Selecciona una fecha específica
     */
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        filterMatchesByDate()
    }

    /**
     * Navega al día anterior
     */
    fun goToPreviousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
        filterMatchesByDate()
    }

    /**
     * Navega al día siguiente
     */
    fun goToNextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
        filterMatchesByDate()
    }

    /**
     * Filtra la lista de partidos cacheados por la fecha seleccionada
     * Usa los datos ya cargados en memoria para mejor rendimiento
     */
    private fun filterMatchesByDate() {
        val selectedDate = _selectedDate.value
        val allMatches = _allMatches.value

        Log.d(
            "MainViewModel",
            "📅 Filtrando partidos para fecha: $selectedDate (Total: ${allMatches.size})"
        )

        val filteredMatches = allMatches
            .filter { it.dateTime.toLocalDate() == selectedDate }
            .sortedBy { it.dateTime }

        Log.d("MainViewModel", "✅ Partidos encontrados: ${filteredMatches.size}")
        _matches.value = filteredMatches
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
            .filter { it.isAfter(currentDate) }.minOrNull()
    }

    /**
     * Navega al próximo día que tenga partidos disponibles
     */
    fun goToNextAvailableMatchDay() {
        val nextMatchDay = findNextMatchDay()
        if (nextMatchDay != null) {
            _selectedDate.value = nextMatchDay
            filterMatchesByDate()
        }
    }

    /**
     * Verifica si la fecha seleccionada es hoy
     */
    fun isSelectedDateToday(): Boolean {
        return _selectedDate.value == LocalDate.now()
    }
}
