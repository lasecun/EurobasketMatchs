package es.itram.basketmatch.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itram.basketmatch.analytics.AnalyticsManager
import es.itram.basketmatch.domain.entity.Team
import es.itram.basketmatch.domain.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla de equipos favoritos
 */
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val teamRepository: TeamRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _favoriteTeams = MutableStateFlow<List<Team>>(emptyList())
    val favoriteTeams: StateFlow<List<Team>> = _favoriteTeams.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        Log.d("FavoritesViewModel", "🌟 Cargando equipos favoritos...")
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            teamRepository.getFavoriteTeams()
                .catch { e ->
                    Log.e("FavoritesViewModel", "❌ Error cargando favoritos", e)
                    _error.value = "Error cargando equipos favoritos: ${e.message}"
                    _isLoading.value = false
                }
                .collect { teams ->
                    Log.d("FavoritesViewModel", "✅ Favoritos cargados: ${teams.size} equipos")
                    _favoriteTeams.value = teams
                    _isLoading.value = false
                }
        }
    }

    fun toggleFavorite(teamId: String) {
        viewModelScope.launch {
            try {
                val currentTeams = _favoriteTeams.value
                val team = currentTeams.find { it.id == teamId }
                
                if (team != null) {
                    val newFavoriteStatus = !team.isFavorite
                    Log.d("FavoritesViewModel", "🔄 Cambiando estado de favorito para ${team.name}: $newFavoriteStatus")
                    
                    teamRepository.updateFavoriteStatus(teamId, newFavoriteStatus)
                    
                    // 📊 Analytics: Track favorite toggle
                    analyticsManager.logCustomEvent("favorite_toggled_from_favorites", android.os.Bundle().apply {
                        putString("team_id", teamId)
                        putString("team_name", team.name)
                        putBoolean("is_favorite", newFavoriteStatus)
                        putString("source", "favorites_screen")
                    })
                    
                    if (!newFavoriteStatus) {
                        // Si se quitó de favoritos, también hacer tracking específico
                        analyticsManager.logCustomEvent("favorite_removed_from_list", android.os.Bundle().apply {
                            putString("team_id", teamId)
                            putString("team_name", team.name)
                            putString("removal_source", "favorites_screen")
                        })
                    }
                }
            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "❌ Error actualizando favorito", e)
                _error.value = "Error actualizando favorito: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    /**
     * 📊 Analytics: Track favorites screen view
     */
    fun trackScreenView() {
        analyticsManager.trackScreenView(
            screenName = "favorites",
            screenClass = "FavoritesScreen"
        )
        
        // También hacer tracking del número de favoritos
        analyticsManager.logCustomEvent("favorites_screen_viewed", android.os.Bundle().apply {
            putInt("favorite_teams_count", _favoriteTeams.value.size)
            putString("screen", "favorites")
        })
    }
}
