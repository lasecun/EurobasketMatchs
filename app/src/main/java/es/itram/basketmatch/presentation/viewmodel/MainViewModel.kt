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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * ViewModel para la pantalla principal
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val getAllMatchesUseCase: GetAllMatchesUseCase,
    private val getAllTeamsUseCase: GetAllTeamsUseCase
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches.asStateFlow()

    private val _teams = MutableStateFlow<Map<String, Team>>(emptyMap())
    val teams: StateFlow<Map<String, Team>> = _teams.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        Log.d("MainViewModel", "Inicializando MainViewModel...")
        loadData()
    }

    private fun loadData() {
        Log.d("MainViewModel", "Iniciando carga de datos...")
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                Log.d("MainViewModel", "Cargando equipos...")
                // Cargar equipos primero - solo el primer valor
                val teams = getAllTeamsUseCase().first()
                Log.d("MainViewModel", "Equipos cargados: ${teams.size}")
                _teams.value = teams.associateBy { it.id }
                
                Log.d("MainViewModel", "Cargando partidos...")
                // Luego cargar partidos - solo el primer valor
                val matches = getAllMatchesUseCase().first()
                Log.d("MainViewModel", "Partidos cargados: ${matches.size}")
                filterMatchesByDate(matches)
                _isLoading.value = false
                
                Log.d("MainViewModel", "Carga de datos completada")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error cargando datos: ${e.message}", e)
                _error.value = e.message ?: "Error desconocido"
                _isLoading.value = false
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        filterMatchesBySelectedDate()
    }

    fun goToPreviousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
        filterMatchesBySelectedDate()
    }

    fun goToNextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
        filterMatchesBySelectedDate()
    }

    fun goToToday() {
        _selectedDate.value = LocalDate.now()
        filterMatchesBySelectedDate()
    }

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

    private fun filterMatchesByDate(allMatches: List<Match>) {
        val selectedDate = _selectedDate.value
        val filteredMatches = allMatches.filter { match ->
            match.dateTime.toLocalDate() == selectedDate
        }.sortedBy { it.dateTime }
        _matches.value = filteredMatches
    }

    fun getFormattedSelectedDate(): String {
        val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")
        return _selectedDate.value.format(formatter)
    }

    fun getTeamById(teamId: String): Team? {
        return _teams.value[teamId]
    }

    fun clearError() {
        _error.value = null
    }
}
