package es.itram.basketmatch.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itram.basketmatch.analytics.AnalyticsManager
import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.entity.Standing
import es.itram.basketmatch.domain.entity.Team
import es.itram.basketmatch.domain.repository.MatchRepository
import es.itram.basketmatch.domain.repository.StandingRepository
import es.itram.basketmatch.domain.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel para la pantalla de detalles del equipo
 */
@HiltViewModel
class TeamDetailViewModel @Inject constructor(
    private val teamRepository: TeamRepository,
    private val matchRepository: MatchRepository,
    private val standingRepository: StandingRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _team = MutableStateFlow<Team?>(null)
    val team: StateFlow<Team?> = _team.asStateFlow()

    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches.asStateFlow()

    private val _standing = MutableStateFlow<Standing?>(null)
    val standing: StateFlow<Standing?> = _standing.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadTeamDetails(teamId: String) {
        Log.d("TeamDetailViewModel", "Cargando detalles del equipo: $teamId")
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Cargar equipo
                teamRepository.getTeamById(teamId)
                    .catch { e ->
                        _error.value = "Error cargando equipo: ${e.message}"
                    }
                    .collect { team ->
                        _team.value = team
                        _isFavorite.value = team?.isFavorite ?: false
                    }

                // Cargar partidos del equipo
                _team.value?.let { team ->
                    matchRepository.getMatchesByTeam(team.code)
                        .catch { e ->
                            // Error silencioso para partidos
                        }
                        .collect { matches ->
                            _matches.value = matches
                        }

                    // Cargar clasificación
                    standingRepository.getStandingByTeam(team.code)
                        .catch { e ->
                            // Error silencioso para clasificación
                        }
                        .collect { standing ->
                            _standing.value = standing
                        }
                }
            } catch (e: Exception) {
                _error.value = "Error cargando datos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            _team.value?.let { team ->
                val newFavoriteStatus = !_isFavorite.value
                try {
                    teamRepository.updateFavoriteStatus(team.id, newFavoriteStatus)
                    _isFavorite.value = newFavoriteStatus
                } catch (e: Exception) {
                    _error.value = "Error actualizando favorito: ${e.message}"
                }
            }
        }
    }

    fun getWinPercentage(): Double {
        val standing = _standing.value ?: return 0.0
        return if (standing.played > 0) {
            (standing.won.toDouble() / standing.played.toDouble()) * 100
        } else {
            0.0
        }
    }

    fun getUpcomingMatches(): List<Match> {
        val today = LocalDate.now()
        return _matches.value.filter {
            it.dateTime.toLocalDate().isAfter(today)
        }.take(5)
    }

    fun getRecentMatches(): List<Match> {
        val today = LocalDate.now()
        return _matches.value.filter {
            it.dateTime.toLocalDate().isBefore(today) || it.dateTime.toLocalDate().isEqual(today)
        }.sortedByDescending { it.dateTime }.take(5)
    }

    fun clearError() {
        _error.value = null
    }

    /**
     * 📊 Analytics: Track team roster access from team detail
     */
    fun trackRosterAccess(teamCode: String, teamName: String) {
        analyticsManager.logCustomEvent("team_roster_accessed", android.os.Bundle().apply {
            putString("team_code", teamCode)
            putString("team_name", teamName)
            putString("source", "team_detail_screen")
            putString("access_type", "roster_button")
        })
    }

    /**
     * 📊 Analytics: Track screen view
     */
    fun trackScreenView() {
        analyticsManager.trackScreenView(
            screenName = AnalyticsManager.SCREEN_TEAM_DETAIL,
            screenClass = "TeamDetailScreen"
        )
    }
}
