package es.itram.basketmatch.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itram.basketmatch.analytics.AnalyticsManager
import es.itram.basketmatch.domain.entity.SeasonType
import es.itram.basketmatch.domain.entity.Standing
import es.itram.basketmatch.domain.entity.Team
import es.itram.basketmatch.domain.repository.TeamRepository
import es.itram.basketmatch.domain.usecase.GetStandingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 📊 ViewModel para la pantalla de clasificación
 *
 * Gestiona el estado de la clasificación de la EuroLeague,
 * permitiendo filtrar por tipo de temporada y acceder a
 * información detallada de los equipos.
 */
@HiltViewModel
class StandingsViewModel @Inject constructor(
    private val getStandingsUseCase: GetStandingsUseCase,
    private val calculateStandingsUseCase: es.itram.basketmatch.domain.usecase.CalculateStandingsUseCase,
    private val teamRepository: TeamRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    // Estado de la UI
    private val _uiState = MutableStateFlow<StandingsUiState>(StandingsUiState.Loading)
    val uiState: StateFlow<StandingsUiState> = _uiState.asStateFlow()

    // Tipo de temporada seleccionado
    private val _selectedSeasonType = MutableStateFlow(SeasonType.REGULAR)
    val selectedSeasonType: StateFlow<SeasonType> = _selectedSeasonType.asStateFlow()

    // IDs de equipos favoritos
    private val _favoriteTeamIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteTeamIds: StateFlow<Set<String>> = _favoriteTeamIds.asStateFlow()

    init {
        loadStandings()
        loadFavoriteTeams()
    }

    /**
     * Carga la clasificación según el tipo de temporada seleccionado
     */
    private fun loadStandings() {
        viewModelScope.launch {
            combine(
                getStandingsUseCase(_selectedSeasonType.value),
                teamRepository.getAllTeams()
            ) { standings, teams ->
                StandingsData(standings, teams)
            }
                .catch { error ->
                    _uiState.value = StandingsUiState.Error(
                        error.message ?: "Error desconocido al cargar la clasificación"
                    )
                }
                .collect { data ->
                    if (data.standings.isEmpty()) {
                        _uiState.value = StandingsUiState.Empty
                    } else {
                        // Combinar standings con información de equipos
                        val enrichedStandings = data.standings.map { standing ->
                            val team = data.teams.find { it.id == standing.teamId }
                            StandingWithTeam(standing, team)
                        }.sortedBy { it.standing.position }

                        _uiState.value = StandingsUiState.Success(enrichedStandings)
                    }
                }
        }
    }

    /**
     * Carga los IDs de los equipos favoritos
     */
    private fun loadFavoriteTeams() {
        viewModelScope.launch {
            teamRepository.getFavoriteTeams()
                .collect { favoriteTeams ->
                    _favoriteTeamIds.value = favoriteTeams.map { it.id }.toSet()
                }
        }
    }

    /**
     * Cambia el tipo de temporada y recarga los datos
     */
    fun selectSeasonType(seasonType: SeasonType) {
        if (_selectedSeasonType.value != seasonType) {
            _selectedSeasonType.value = seasonType
            _uiState.value = StandingsUiState.Loading
            loadStandings()

            // 📊 Analytics: Track season type change
            analyticsManager.logCustomEvent("standings_season_type_changed", android.os.Bundle().apply {
                putString("season_type", seasonType.name)
            })
        }
    }

    /**
     * Recarga la clasificación (recalcula desde partidos)
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = StandingsUiState.Loading

            try {
                // Recalcular standings desde resultados de partidos
                calculateStandingsUseCase.calculateAndSave(_selectedSeasonType.value)
                    .getOrThrow()

                // Recargar datos actualizados
                loadStandings()

                // 📊 Analytics: Track manual refresh
                analyticsManager.logCustomEvent("standings_refreshed", android.os.Bundle().apply {
                    putString("season_type", _selectedSeasonType.value.toString())
                    putBoolean("recalculated", true)
                })
            } catch (e: Exception) {
                _uiState.value = StandingsUiState.Error(
                    "Error al recalcular clasificación: ${e.message}"
                )
            }
        }
    }

    /**
     * Tracking de visualización de pantalla
     */
    fun trackScreenView() {
        analyticsManager.trackScreenView(
            screenName = "Standings",
            screenClass = "StandingsScreen"
        )
        analyticsManager.trackStandingsViewed(season = "2025-26")
    }

    /**
     * Tracking de tap en equipo
     */
    fun trackTeamTap(teamCode: String, teamName: String, position: Int) {
        analyticsManager.trackTeamViewed(
            teamCode = teamCode,
            teamName = teamName,
            source = "standings_screen"
        )

        analyticsManager.logCustomEvent("standings_team_tapped", android.os.Bundle().apply {
            putString("team_code", teamCode)
            putString("team_name", teamName)
            putInt("position", position)
            putString("season_type", _selectedSeasonType.value.toString())
        })
    }

    /**
     * Verifica si un equipo es favorito
     */
    fun isTeamFavorite(teamId: String): Boolean {
        return _favoriteTeamIds.value.contains(teamId)
    }
}

/**
 * Estados de la UI para la pantalla de clasificación
 */
sealed class StandingsUiState {
    object Loading : StandingsUiState()
    object Empty : StandingsUiState()
    data class Success(val standings: List<StandingWithTeam>) : StandingsUiState()
    data class Error(val message: String) : StandingsUiState()
}

/**
 * Datos combinados de clasificación y equipos
 */
private data class StandingsData(
    val standings: List<Standing>,
    val teams: List<Team>
)

/**
 * Standing enriquecido con información del equipo
 */
data class StandingWithTeam(
    val standing: Standing,
    val team: Team?
)

