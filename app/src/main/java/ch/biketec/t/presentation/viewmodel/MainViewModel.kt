package ch.biketec.t.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.biketec.t.domain.entity.Match
import ch.biketec.t.domain.entity.Team
import ch.biketec.t.domain.usecase.GetAllMatchesUseCase
import ch.biketec.t.domain.usecase.GetAllTeamsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getAllMatchesUseCase: GetAllMatchesUseCase,
    private val getAllTeamsUseCase: GetAllTeamsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                combine(
                    getAllMatchesUseCase(),
                    getAllTeamsUseCase()
                ) { matches, teams ->
                    MainUiState(
                        matches = matches,
                        teams = teams,
                        isLoading = false
                    )
                }
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
                .collect { newState ->
                    _uiState.value = newState
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun nextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }

    fun previousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    fun getMatchesForDate(date: LocalDate): List<Match> {
        return _uiState.value.matches
            .filter { it.dateTime.toLocalDate() == date }
            .sortedBy { it.dateTime }
    }

    fun getDatesWithMatches(): Set<LocalDate> {
        return _uiState.value.matches
            .map { it.dateTime.toLocalDate() }
            .toSet()
    }

    fun refreshData() {
        loadData()
    }
}

data class MainUiState(
    val matches: List<Match> = emptyList(),
    val teams: List<Team> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
