package es.itram.basketmatch.presentation.viewmodel

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itram.basketmatch.analytics.AnalyticsManager
import es.itram.basketmatch.domain.model.TeamRoster
import es.itram.basketmatch.domain.repository.TeamRepository
import es.itram.basketmatch.domain.usecase.GetTeamRosterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla de roster del equipo
 */
@HiltViewModel
class TeamRosterViewModel @Inject constructor(
    private val getTeamRosterUseCase: GetTeamRosterUseCase,
    private val teamRepository: TeamRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {
    
    companion object {
        private const val TAG = "TeamRosterViewModel"
    }
    
    private val _uiState = MutableStateFlow(TeamRosterUiState())
    val uiState: StateFlow<TeamRosterUiState> = _uiState.asStateFlow()
    
    // Variable para mantener el jugador seleccionado
    private var selectedPlayer: es.itram.basketmatch.domain.model.Player? = null
    
    /**
     * Carga el roster de un equipo por su código TLA
     */
    fun loadTeamRoster(teamTla: String) {
        Log.d(TAG, "🔍 Cargando roster para equipo: $teamTla")
        
        // Verificar si ya tenemos el roster de este equipo cargado
        val currentRoster = _uiState.value.teamRoster
        if (currentRoster?.teamCode == teamTla && !_uiState.value.isLoading) {
            Log.d(TAG, "✅ Roster ya cargado para equipo: $teamTla, evitando recarga")
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null,
                    successMessage = null,
                    loadingProgress = LoadingProgress(
                        current = 1,
                        total = 4,
                        message = "Conectando con la API"
                    )
                )
                
                // Simular progreso paso a paso durante la carga real
                kotlinx.coroutines.delay(500)
                
                _uiState.value = _uiState.value.copy(
                    loadingProgress = LoadingProgress(
                        current = 2,
                        total = 4,
                        message = "Obteniendo datos del equipo"
                    )
                )
                
                getTeamRosterUseCase(teamTla).fold(
                    onSuccess = { teamRoster ->
                        Log.d(TAG, "✅ Roster cargado exitosamente: ${teamRoster.players.size} jugadores")
                        Log.d(TAG, "🖼️ DEBUG_LOGO_URL: '${teamRoster.logoUrl}' para equipo: '${teamRoster.teamName}'")
                        
                        // 📊 Analytics: Track roster viewed successfully
                        analyticsManager.trackRosterViewed(
                            teamCode = teamTla,
                            teamName = teamRoster.teamName,
                            playerCount = teamRoster.players.size
                        )
                        
                        // Paso 3: Procesando datos
                        _uiState.value = _uiState.value.copy(
                            loadingProgress = LoadingProgress(
                                current = 3,
                                total = 4,
                                message = "Procesando ${teamRoster.players.size} jugadores"
                            )
                        )
                        kotlinx.coroutines.delay(300)
                        
                        // Paso 4: Finalizando
                        _uiState.value = _uiState.value.copy(
                            loadingProgress = LoadingProgress(
                                current = 4,
                                total = 4,
                                message = "Finalizando carga"
                            )
                        )
                        kotlinx.coroutines.delay(200)
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            teamRoster = teamRoster,
                            error = null,
                            successMessage = null,
                            loadingProgress = null
                        )
                        
                        // Cargar estado de favorito después de cargar el roster
                        loadFavoriteStatus(teamTla)
                    },
                    onFailure = { error ->
                        Log.e(TAG, "❌ Error cargando roster", error)
                        
                        // 📊 Analytics: Track roster loading error
                        analyticsManager.logCustomEvent("roster_load_error", android.os.Bundle().apply {
                            putString("team_code", teamTla)
                            putString("error_message", error.message)
                            putString("error_class", error.javaClass.simpleName)
                        })
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Error desconocido cargando roster",
                            successMessage = null,
                            loadingProgress = null
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error inesperado cargando roster para $teamTla", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error inesperado: ${e.message}",
                    successMessage = null,
                    loadingProgress = null
                )
            }
        }
    }
    
    /**
     * Resetea el estado de error y mensaje de éxito
     */
    fun resetMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }
    
    /**
     * Refresca el roster forzando la descarga desde la API
     */
    fun refreshTeamRoster(teamTla: String) {
        Log.d(TAG, "🔄 Refrescando roster para equipo: $teamTla")
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = true,
                    error = null,
                    successMessage = null,
                    loadingProgress = LoadingProgress(
                        current = 1,
                        total = 4,
                        message = "Conectando con la API"
                    )
                )
                
                kotlinx.coroutines.delay(500)
                
                _uiState.value = _uiState.value.copy(
                    loadingProgress = LoadingProgress(
                        current = 2,
                        total = 4,
                        message = "Actualizando datos del equipo"
                    )
                )
                
                getTeamRosterUseCase.refresh(teamTla).fold(
                    onSuccess = { teamRoster ->
                        Log.d(TAG, "✅ Roster refrescado exitosamente: ${teamRoster.players.size} jugadores")
                        
                        // 📊 Analytics: Track roster refreshed
                        analyticsManager.logCustomEvent("roster_refreshed", android.os.Bundle().apply {
                            putString("team_code", teamTla)
                            putString("team_name", teamRoster.teamName)
                            putInt("player_count", teamRoster.players.size)
                        })
                        
                        // Paso 3: Procesando datos
                        _uiState.value = _uiState.value.copy(
                            loadingProgress = LoadingProgress(
                                current = 3,
                                total = 4,
                                message = "Procesando ${teamRoster.players.size} jugadores"
                            )
                        )
                        kotlinx.coroutines.delay(300)
                        
                        // Paso 4: Finalizando
                        _uiState.value = _uiState.value.copy(
                            loadingProgress = LoadingProgress(
                                current = 4,
                                total = 4,
                                message = "Finalizando actualización"
                            )
                        )
                        kotlinx.coroutines.delay(200)
                        
                        _uiState.value = _uiState.value.copy(
                            isRefreshing = false,
                            teamRoster = teamRoster,
                            error = null,
                            successMessage = null,
                            loadingProgress = null
                        )
                    },
                    onFailure = { error ->
                        Log.e(TAG, "❌ Error refrescando roster", error)
                        _uiState.value = _uiState.value.copy(
                            isRefreshing = false,
                            error = error.message ?: "Error refrescando roster",
                            successMessage = null,
                            loadingProgress = null
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error inesperado refrescando roster para $teamTla", e)
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = "Error inesperado: ${e.message}",
                    successMessage = null,
                    loadingProgress = null
                )
            }
        }
    }
    
    /**
     * Limpia el error del estado
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Limpia el mensaje de éxito del estado
     */
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
    
    /**
     * Obtiene un jugador por su código
     */
    fun getPlayerById(playerId: String): es.itram.basketmatch.domain.model.Player? {
        val player = _uiState.value.teamRoster?.players?.find { it.code == playerId }
        selectedPlayer = player // Guardar referencia
        return player
    }
    
    /**
     * Selecciona un jugador para navegación
     */
    fun selectPlayer(player: es.itram.basketmatch.domain.model.Player) {
        selectedPlayer = player
        
        // 📊 Analytics: Track player selection from roster
        analyticsManager.trackPlayerViewed(
            playerCode = player.code,
            playerName = player.name,
            teamCode = _uiState.value.teamRoster?.teamCode ?: ""
        )
    }
    
    /**
     * Obtiene el jugador seleccionado
     */
    fun getSelectedPlayer(): es.itram.basketmatch.domain.model.Player? {
        return selectedPlayer
    }
    
    /**
     * Carga el estado de favorito de un equipo
     */
    private suspend fun loadFavoriteStatus(teamCode: String) {
        try {
            val team = teamRepository.getTeamByCode(teamCode).first()
            _uiState.value = _uiState.value.copy(
                isFavorite = team?.isFavorite ?: false
            )
            Log.d(TAG, "✅ Estado de favorito cargado para $teamCode: ${team?.isFavorite ?: false}")
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando estado de favorito para equipo $teamCode", e)
            // En caso de error, establecer como no favorito por defecto
            _uiState.value = _uiState.value.copy(isFavorite = false)
        }
    }
    
    /**
     * Función pública para cargar el estado de favoritos desde la UI
     */
    fun loadFavoriteStatusForTeam(teamCode: String) {
        viewModelScope.launch {
            loadFavoriteStatus(teamCode)
        }
    }
    
    /**
     * Alterna el estado de favorito del equipo actual
     */
    fun toggleFavorite() {
        val currentRoster = _uiState.value.teamRoster ?: return
        val currentFavoriteStatus = _uiState.value.isFavorite
        val newFavoriteStatus = !currentFavoriteStatus
        
        viewModelScope.launch {
            try {
                // Actualizar en la base de datos usando el código del equipo
                teamRepository.updateFavoriteStatusByCode(currentRoster.teamCode, newFavoriteStatus)
                
                // Actualizar el estado local
                _uiState.value = _uiState.value.copy(
                    isFavorite = newFavoriteStatus
                )
                
                // 📊 Analytics: Track favorite action
                analyticsManager.trackFavoriteAdded(
                    contentType = "team",
                    contentId = currentRoster.teamCode
                )
                
                Log.d(TAG, "💖 Estado de favorito actualizado para ${currentRoster.teamName}: $newFavoriteStatus")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error actualizando estado de favorito", e)
                _uiState.value = _uiState.value.copy(
                    error = "Error al actualizar favorito"
                )
            }
        }
    }
    
    /**
     * 📊 Track screen view for analytics
     */
    fun trackScreenView() {
        analyticsManager.trackScreenView(
            screenName = AnalyticsManager.SCREEN_TEAM_ROSTER,
            screenClass = "TeamRosterScreen"
        )
    }
}

/**
 * Estado de la UI de la pantalla de roster
 */
data class TeamRosterUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val teamRoster: TeamRoster? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val loadingProgress: LoadingProgress? = null,
    val isFavorite: Boolean = false
)

/**
 * Progreso de carga con contador
 */
data class LoadingProgress(
    val current: Int,
    val total: Int,
    val message: String = "Cargando roster"
) {
    val progressText: String
        get() = "$message $current/$total"
}
