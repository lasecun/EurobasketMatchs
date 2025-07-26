package ch.biketec.t.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.biketec.t.domain.entity.Standing
import ch.biketec.t.domain.entity.Team
import ch.biketec.t.domain.repository.TeamRepository
import ch.biketec.t.domain.usecase.GetAllTeamsUseCase
import ch.biketec.t.domain.usecase.GetCurrentStandingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamDetailViewModel @Inject constructor(
    private val getAllTeamsUseCase: GetAllTeamsUseCase,
    private val getCurrentStandingsUseCase: GetCurrentStandingsUseCase,
    private val teamRepository: TeamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamDetailUiState())
    val uiState: StateFlow<TeamDetailUiState> = _uiState.asStateFlow()

    private val _selectedTeamId = MutableStateFlow<String?>(null)

    fun loadTeamDetail(teamId: String) {
        _selectedTeamId.value = teamId
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                combine(
                    getAllTeamsUseCase(),
                    getCurrentStandingsUseCase()
                ) { teams, standings ->
                    val team = teams.find { it.id == teamId }
                    val teamStanding = standings.find { it.team.id == teamId }
                    
                    TeamDetailUiState(
                        team = team,
                        standing = teamStanding,
                        allStandings = standings,
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

    fun toggleFavorite() {
        val team = _uiState.value.team ?: return
        
        viewModelScope.launch {
            try {
                if (team.isFavorite) {
                    teamRepository.removeFromFavorites(team.id)
                } else {
                    teamRepository.addToFavorites(team.id)
                }
                
                // Reload team data
                _selectedTeamId.value?.let { teamId ->
                    loadTeamDetail(teamId)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message
                )
            }
        }
    }
}

data class TeamDetailUiState(
    val team: Team? = null,
    val standing: Standing? = null,
    val allStandings: List<Standing> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
