package es.itram.basketmatch.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itram.basketmatch.analytics.AnalyticsManager
import es.itram.basketmatch.domain.model.Player
import javax.inject.Inject

/**
 * ViewModel para la pantalla de detalle del jugador
 */
@HiltViewModel
class PlayerDetailViewModel @Inject constructor(
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    /**
     * 📊 Analytics: Track screen view
     */
    fun trackScreenView() {
        analyticsManager.trackScreenView(
            screenName = AnalyticsManager.SCREEN_PLAYER_DETAIL,
            screenClass = "PlayerDetailScreen"
        )
    }

    /**
     * 📊 Analytics: Track when player details are viewed
     */
    fun trackPlayerViewed(player: Player, teamCode: String = "unknown") {
        analyticsManager.trackPlayerViewed(
            playerCode = player.code,
            playerName = player.fullName,
            teamCode = teamCode
        )
    }
}
