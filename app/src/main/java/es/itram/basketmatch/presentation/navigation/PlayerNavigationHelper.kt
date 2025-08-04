package es.itram.basketmatch.presentation.navigation

import es.itram.basketmatch.domain.model.Player

/**
 * Objeto temporal para manejar la navegación de jugadores
 * Mantiene una referencia temporal del jugador seleccionado para la navegación
 */
object PlayerNavigationHelper {
    private var selectedPlayer: Player? = null
    private var selectedTeamName: String? = null
    
    fun setSelectedPlayer(player: Player, teamName: String) {
        selectedPlayer = player
        selectedTeamName = teamName
    }
    
    fun getSelectedPlayer(): Player? = selectedPlayer
    
    fun getSelectedTeamName(): String? = selectedTeamName
    
    fun clearSelection() {
        selectedPlayer = null
        selectedTeamName = null
    }
}
