package es.itram.basketmatch.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itram.basketmatch.analytics.events.AnalyticsEvent
import es.itram.basketmatch.analytics.events.DataAction
import es.itram.basketmatch.analytics.events.ErrorSeverity
import es.itram.basketmatch.analytics.events.PerformanceType
import es.itram.basketmatch.analytics.events.PlayerAction
import es.itram.basketmatch.analytics.events.TeamAction
import es.itram.basketmatch.analytics.tracking.EventDispatcher
import es.itram.basketmatch.domain.model.Player
import es.itram.basketmatch.domain.model.TeamRoster
import es.itram.basketmatch.domain.usecase.GetTeamRosterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 🏀 Enhanced Team Roster ViewModel with Analytics
 * 
 * ViewModel mejorado para la pantalla de roster del equipo con:
 * - 📊 Analytics tracking completo
 * - ⚡ Performance monitoring
 * - 🔍 User interaction tracking
 * - 🚨 Error tracking y reporting
 */
@HiltViewModel
class EnhancedTeamRosterViewModel @Inject constructor(
    private val getTeamRosterUseCase: GetTeamRosterUseCase,
    private val eventDispatcher: EventDispatcher
) : ViewModel() {
    
    companion object {
        private const val TAG = "TeamRosterViewModel"
    }
    
    private val _uiState = MutableStateFlow(TeamRosterUiState())
    val uiState: StateFlow<TeamRosterUiState> = _uiState.asStateFlow()
    
    private var selectedPlayer: Player? = null
    private var currentTeamCode: String? = null
    private var loadStartTime: Long = 0L
    
    /**
     * 🎯 Carga el roster de un equipo con analytics tracking completo
     */
    fun loadTeamRoster(teamTla: String, source: String = "navigation") {
        Log.d(TAG, "🔍 Cargando roster para equipo: $teamTla")
        
        // Track data sync start
        loadStartTime = System.currentTimeMillis()
        eventDispatcher.dispatch(
            AnalyticsEvent.DataSyncEvent(
                action = DataAction.SYNC_STARTED,
                syncType = "team_roster"
            )
        )
        
        // Verificar si ya tenemos el roster de este equipo cargado
        val currentRoster = _uiState.value.teamRoster
        if (currentRoster?.teamCode == teamTla && !_uiState.value.isLoading) {
            Log.d(TAG, "📋 Roster ya cargado para $teamTla, usando cache")
            
            // Track cache hit
            eventDispatcher.dispatch(
                AnalyticsEvent.DataSyncEvent(
                    action = DataAction.CACHE_HIT,
                    syncType = "team_roster"
                )
            )
            
            // Aún así, track que se accedió al roster
            trackRosterAccess(currentRoster, source)
            return
        }
        
        currentTeamCode = teamTla
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "📡 Obteniendo roster desde repository...")
                val result = getTeamRosterUseCase(teamTla)
                
                val loadDuration = System.currentTimeMillis() - loadStartTime
                
                if (result.isSuccess) {
                    val roster = result.getOrNull()!!
                    
                    Log.d(TAG, "✅ Roster cargado exitosamente: ${roster.players.size} jugadores")
                    
                    _uiState.value = _uiState.value.copy(
                        teamRoster = roster,
                        isLoading = false,
                        error = null
                    )
                    
                    // Track successful data sync
                    eventDispatcher.dispatch(
                        AnalyticsEvent.DataSyncEvent(
                            action = DataAction.SYNC_COMPLETED,
                            syncType = "team_roster",
                            durationMs = loadDuration,
                            itemsCount = roster.players.size
                        )
                    )
                    
                    // Track performance
                    eventDispatcher.dispatch(
                        AnalyticsEvent.PerformanceEvent(
                            type = PerformanceType.SCREEN_LOAD,
                            durationMs = loadDuration,
                            success = true,
                            details = mapOf(
                                "screen" to "team_roster",
                                "team_code" to teamTla,
                                "player_count" to roster.players.size
                            )
                        )
                    )
                    
                    // Track roster access
                    trackRosterAccess(roster, source)
                    
                } else {
                    val error = result.exceptionOrNull()
                    val errorMessage = error?.message ?: "Error desconocido cargando roster"
                    
                    Log.e(TAG, "❌ Error cargando roster: $errorMessage", error)
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                    
                    // Track failed data sync
                    eventDispatcher.dispatch(
                        AnalyticsEvent.DataSyncEvent(
                            action = DataAction.SYNC_FAILED,
                            syncType = "team_roster",
                            errorMessage = errorMessage
                        )
                    )
                    
                    // Track error
                    eventDispatcher.dispatch(
                        AnalyticsEvent.ErrorEvent(
                            errorType = error?.javaClass?.simpleName ?: "UnknownError",
                            errorMessage = errorMessage,
                            screen = "team_roster",
                            action = "load_roster",
                            severity = ErrorSeverity.HIGH
                        )
                    )
                }
                
            } catch (e: Exception) {
                val loadDuration = System.currentTimeMillis() - loadStartTime
                val errorMessage = "Error inesperado: ${e.message}"
                
                Log.e(TAG, "💥 Error inesperado cargando roster", e)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
                
                // Track performance failure
                eventDispatcher.dispatch(
                    AnalyticsEvent.PerformanceEvent(
                        type = PerformanceType.SCREEN_LOAD,
                        durationMs = loadDuration,
                        success = false,
                        details = mapOf(
                            "screen" to "team_roster",
                            "team_code" to teamTla,
                            "error_type" to e.javaClass.simpleName
                        )
                    )
                )
                
                // Track critical error
                eventDispatcher.dispatch(
                    AnalyticsEvent.ErrorEvent(
                        errorType = e.javaClass.simpleName,
                        errorMessage = errorMessage,
                        screen = "team_roster",
                        action = "load_roster",
                        severity = ErrorSeverity.CRITICAL
                    )
                )
            }
        }
    }
    
    /**
     * 🏃 Track cuando un usuario selecciona un jugador
     */
    fun onPlayerSelected(player: Player, source: String = "roster_list") {
        selectedPlayer = player
        
        Log.d(TAG, "👤 Jugador seleccionado: ${player.name}")
        
        // Track player interaction
        eventDispatcher.dispatch(
            AnalyticsEvent.PlayerContentEvent(
                action = PlayerAction.VIEWED,
                playerCode = player.code,
                playerName = player.fullName,
                teamCode = currentTeamCode ?: "unknown",
                position = player.position?.name,
                source = source
            )
        )
        
        // Track user interaction
        eventDispatcher.dispatch(
            AnalyticsEvent.UserInteractionEvent(
                action = "player_selected",
                element = "player_card",
                screen = "team_roster",
                value = player.code
            )
        )
    }
    
    /**
     * 🔄 Refresh del roster con tracking
     */
    fun refreshRoster(teamTla: String) {
        Log.d(TAG, "🔄 Refresh manual del roster")
        
        // Track user-initiated refresh
        eventDispatcher.dispatch(
            AnalyticsEvent.UserInteractionEvent(
                action = "refresh_triggered",
                element = "refresh_button",
                screen = "team_roster"
            )
        )
        
        loadTeamRoster(teamTla, source = "refresh")
    }
    
    /**
     * 🎯 Limpiar error state con tracking
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
        
        eventDispatcher.dispatch(
            AnalyticsEvent.UserInteractionEvent(
                action = "error_dismissed",
                element = "error_dialog",
                screen = "team_roster"
            )
        )
    }
    
    /**
     * 📊 Track acceso al roster (interno)
     */
    private fun trackRosterAccess(roster: TeamRoster, source: String) {
        // Track team roster viewed
        eventDispatcher.dispatch(
            AnalyticsEvent.TeamContentEvent(
                action = TeamAction.ROSTER_ACCESSED,
                teamCode = roster.teamCode,
                teamName = roster.teamName,
                source = source,
                context = "roster_screen"
            )
        )
        
        // Track detailed roster metrics
        eventDispatcher.dispatch(
            AnalyticsEvent.UserInteractionEvent(
                action = "roster_viewed",
                element = "roster_list",
                screen = "team_roster",
                value = "${roster.players.size}_players"
            )
        )
    }
    
    /**
     * 💝 Track cuando se añade equipo a favoritos
     */
    fun addTeamToFavorites() {
        val roster = _uiState.value.teamRoster
        if (roster != null) {
            eventDispatcher.dispatch(
                AnalyticsEvent.FavoriteEvent(
                    action = es.itram.basketmatch.analytics.events.FavoriteAction.ADDED,
                    contentType = "team",
                    contentId = roster.teamCode,
                    contentName = roster.teamName
                )
            )
        }
    }
    
    /**
     * 📤 Track cuando se comparte el roster
     */
    fun shareRoster(method: String) {
        val roster = _uiState.value.teamRoster
        if (roster != null) {
            eventDispatcher.dispatch(
                AnalyticsEvent.ShareEvent(
                    contentType = "team_roster",
                    contentId = roster.teamCode,
                    shareMethod = method,
                    contentTitle = "${roster.teamName} Roster"
                )
            )
        }
    }
    
    /**
     * 🔍 Track búsqueda de jugadores en el roster
     */
    fun searchPlayers(query: String) {
        val roster = _uiState.value.teamRoster
        if (roster != null) {
            val filteredPlayers = roster.players.filter { 
                it.fullName.contains(query, ignoreCase = true) ||
                it.position?.name?.contains(query, ignoreCase = true) == true
            }
            
            eventDispatcher.dispatch(
                AnalyticsEvent.SearchEvent(
                    query = query,
                    category = "players",
                    resultCount = filteredPlayers.size
                )
            )
        }
    }
    
    /**
     * 🔧 Track filtrado por posición
     */
    fun filterByPosition(position: String) {
        eventDispatcher.dispatch(
            AnalyticsEvent.FilterEvent(
                filterType = "position",
                filterValue = position,
                resultCount = _uiState.value.teamRoster?.players?.count { 
                    it.position?.name == position 
                } ?: 0,
                screen = "team_roster"
            )
        )
    }
}

/**
 * 🎯 UI State para Team Roster con analytics metadata
 */
data class TeamRosterUiState(
    val teamRoster: TeamRoster? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFilter: String? = null,
    val searchQuery: String = "",
    val sortBy: RosterSortOption = RosterSortOption.JERSEY_NUMBER
)

enum class RosterSortOption(val displayName: String) {
    JERSEY_NUMBER("Número"),
    NAME("Nombre"),
    POSITION("Posición"),
    HEIGHT("Altura"),
    WEIGHT("Peso")
}
