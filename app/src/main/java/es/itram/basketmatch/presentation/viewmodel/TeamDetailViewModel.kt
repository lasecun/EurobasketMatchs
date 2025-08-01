package es.itram.basketmatch.presentation.viewmodel

import android.os.Bundle
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
            try {
                Log.d("TeamDetailViewModel", "Obteniendo datos del equipo...")
                // Cargar datos del equipo de manera simple
                val team = teamRepository.getTeamById(teamId).first()
                Log.d("TeamDetailViewModel", "Equipo cargado: ${team?.name}")
                
                val matches = matchRepository.getMatchesByTeam(teamId).first()
                Log.d("TeamDetailViewModel", "Partidos cargados: ${matches.size}")
                
                val standing = standingRepository.getStandingByTeam(teamId).first()
                Log.d("TeamDetailViewModel", "Clasificación cargada: ${standing?.position}")
                
                _team.value = team
                _matches.value = matches.sortedBy { it.dateTime }
                _standing.value = standing
                _isFavorite.value = team?.isFavorite ?: false
                _error.value = null
                
                // 📊 Analytics: Track team viewed
                team?.let { teamData ->
                    analyticsManager.trackTeamViewed(
                        teamCode = teamData.code,
                        teamName = teamData.name,
                        source = "team_detail_screen"
                    )
                }
                
                Log.d("TeamDetailViewModel", "Carga de detalles completada")
            } catch (e: Exception) {
                Log.e("TeamDetailViewModel", "Error cargando detalles: ${e.message}", e)
                _error.value = e.message ?: "Error desconocido"
                
                // 📊 Analytics: Track team loading error
                analyticsManager.logCustomEvent("team_load_error", android.os.Bundle().apply {
                    putString("team_id", teamId)
                    putString("error_message", e.message)
                    putString("error_class", e.javaClass.simpleName)
                })
                
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite() {
        val currentTeam = _team.value ?: return
        
        viewModelScope.launch {
            try {
                val updatedTeam = currentTeam.copy(isFavorite = !currentTeam.isFavorite)
                teamRepository.updateTeam(updatedTeam)
                _isFavorite.value = updatedTeam.isFavorite
                _team.value = updatedTeam
                
                // 📊 Analytics: Track favorite toggle
                analyticsManager.trackFavoriteAdded(
                    contentType = "team",
                    contentId = currentTeam.code
                )
                
            } catch (e: Exception) {
                _error.value = "Error al actualizar favorito: ${e.message}"
            }
        }
    }

    fun getUpcomingMatches(): List<Match> {
        return _matches.value.filter { match ->
            match.dateTime.isAfter(java.time.LocalDateTime.now())
        }.take(5)
    }

    fun getRecentMatches(): List<Match> {
        return _matches.value.filter { match ->
            match.dateTime.isBefore(java.time.LocalDateTime.now())
        }.sortedByDescending { it.dateTime }.take(5)
    }

    fun getWinPercentage(): Double {
        val standing = _standing.value ?: return 0.0
        return if (standing.played > 0) {
            (standing.won.toDouble() / standing.played.toDouble()) * 100
        } else {
            0.0
        }
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
