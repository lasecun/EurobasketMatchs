package es.itram.basketmatch.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itram.basketmatch.domain.model.TeamRoster
import es.itram.basketmatch.domain.usecase.GetTeamRosterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla de roster del equipo
 */
@HiltViewModel
class TeamRosterViewModel @Inject constructor(
    private val getTeamRosterUseCase: GetTeamRosterUseCase
) : ViewModel() {
    
    companion object {
        private const val TAG = "TeamRosterViewModel"
    }
    
    private val _uiState = MutableStateFlow(TeamRosterUiState())
    val uiState: StateFlow<TeamRosterUiState> = _uiState.asStateFlow()
    
    /**
     * Carga el roster de un equipo por su código TLA
     */
    fun loadTeamRoster(teamTla: String) {
        Log.d(TAG, "🔍 Cargando roster para equipo: $teamTla")
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            
            getTeamRosterUseCase(teamTla).fold(
                onSuccess = { teamRoster ->
                    Log.d(TAG, "✅ Roster cargado exitosamente: ${teamRoster.players.size} jugadores")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        teamRoster = teamRoster,
                        error = null
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ Error cargando roster", error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Error desconocido cargando roster"
                    )
                }
            )
        }
    }
    
    /**
     * Refresca el roster forzando la descarga desde la API
     */
    fun refreshTeamRoster(teamTla: String) {
        Log.d(TAG, "🔄 Refrescando roster para equipo: $teamTla")
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRefreshing = true,
                error = null
            )
            
            getTeamRosterUseCase.refresh(teamTla).fold(
                onSuccess = { teamRoster ->
                    Log.d(TAG, "✅ Roster refrescado exitosamente")
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        teamRoster = teamRoster,
                        error = null
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ Error refrescando roster", error)
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        error = error.message ?: "Error refrescando roster"
                    )
                }
            )
        }
    }
    
    /**
     * Limpia el error del estado
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * Estado de la UI de la pantalla de roster
 */
data class TeamRosterUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val teamRoster: TeamRoster? = null,
    val error: String? = null
)
