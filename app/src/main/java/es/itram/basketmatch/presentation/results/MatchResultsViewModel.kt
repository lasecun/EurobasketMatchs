package es.itram.basketmatch.presentation.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.usecase.GetMatchResultsUseCase
import es.itram.basketmatch.domain.usecase.GetMatchResultsByDateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * 🏀 ViewModel para la pantalla de resultados de partidos de Euroliga
 */
@HiltViewModel
class MatchResultsViewModel @Inject constructor(
    private val getMatchResultsUseCase: GetMatchResultsUseCase,
    private val getMatchResultsByDateUseCase: GetMatchResultsByDateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchResultsUiState())
    val uiState: StateFlow<MatchResultsUiState> = _uiState.asStateFlow()

    init {
        loadLatestResults()
    }

    /**
     * Carga los últimos resultados de partidos
     */
    fun loadLatestResults() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            getMatchResultsUseCase.getLatestResults(20)
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error al cargar resultados: ${error.message}"
                    )
                }
                .collect { matches ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        matches = matches,
                        error = null
                    )
                }
        }
    }

    /**
     * Carga resultados para una fecha específica
     */
    fun loadResultsForDate(date: LocalDateTime) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            getMatchResultsByDateUseCase(date)
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error al cargar resultados para la fecha: ${error.message}"
                    )
                }
                .collect { matches ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        matches = matches,
                        error = null
                    )
                }
        }
    }

    /**
     * Carga resultados para un equipo específico
     */
    fun loadResultsForTeam(teamId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            getMatchResultsUseCase.getResultsForTeam(teamId)
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error al cargar resultados del equipo: ${error.message}"
                    )
                }
                .collect { matches ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        matches = matches,
                        error = null
                    )
                }
        }
    }

    /**
     * Refresca los datos
     */
    fun refresh() {
        loadLatestResults()
    }
}

/**
 * Estado de la UI para los resultados de partidos
 */
data class MatchResultsUiState(
    val isLoading: Boolean = false,
    val matches: List<Match> = emptyList(),
    val error: String? = null
)
