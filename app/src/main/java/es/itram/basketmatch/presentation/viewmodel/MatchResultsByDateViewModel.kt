package es.itram.basketmatch.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.entity.MatchStatus
import es.itram.basketmatch.domain.usecase.GetMatchResultsByDateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * 🏀 ViewModel para mostrar resultados de partidos por fecha
 *
 * Permite ver los resultados de partidos de Euroliga para fechas específicas,
 * especialmente el 10 de octubre de 2024
 */
@HiltViewModel
class MatchResultsByDateViewModel @Inject constructor(
    private val getMatchResultsByDateUseCase: GetMatchResultsByDateUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "MatchResultsByDateVM"
    }

    data class UiState(
        val matches: List<Match> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val selectedDate: LocalDateTime? = null,
        val finishedMatchesCount: Int = 0,
        val scheduledMatchesCount: Int = 0,
        val liveMatchesCount: Int = 0
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        // Cargar automáticamente los resultados del 30 de septiembre de 2025 (fecha con partidos confirmados)
        loadResultsForSeptember30th2025()
    }

    /**
     * Carga los resultados específicos del 30 de septiembre de 2025 (fecha con partidos reales)
     */
    fun loadResultsForSeptember30th2025() {
        val targetDate = LocalDateTime.of(2025, 9, 30, 0, 0)
        loadResultsForDate(targetDate)
    }

    /**
     * Carga los resultados para una fecha específica
     */
    fun loadResultsForDate(date: LocalDateTime) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
            selectedDate = date
        )

        viewModelScope.launch {
            getMatchResultsByDateUseCase(date)
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error cargando resultados: ${exception.message}"
                    )
                }
                .collect { matches ->
                    // Calcular estadísticas
                    val finishedCount = matches.count { it.status == MatchStatus.FINISHED }
                    val scheduledCount = matches.count { it.status == MatchStatus.SCHEDULED }
                    val liveCount = matches.count { it.status == MatchStatus.LIVE }

                    _uiState.value = _uiState.value.copy(
                        matches = matches.sortedWith(
                            compareByDescending<Match> { it.status == MatchStatus.FINISHED }
                                .thenByDescending { it.status == MatchStatus.LIVE }
                                .thenBy { it.dateTime }
                        ),
                        isLoading = false,
                        errorMessage = null,
                        finishedMatchesCount = finishedCount,
                        scheduledMatchesCount = scheduledCount,
                        liveMatchesCount = liveCount
                    )
                }
        }
    }

    /**
     * Reintenta cargar los resultados para la fecha actualmente seleccionada
     */
    fun retry() {
        _uiState.value.selectedDate?.let { date ->
            loadResultsForDate(date)
        } ?: loadResultsForSeptember30th2025()
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
