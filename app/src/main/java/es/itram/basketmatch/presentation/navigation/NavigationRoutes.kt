package es.itram.basketmatch.presentation.navigation

/**
 * 🧭 Navigation Routes
 *
 * Objeto que define todas las rutas de navegación de la aplicación
 * siguiendo el patrón de navegación de Jetpack Compose.
 */
object NavigationRoutes {

    // 🏠 Main Navigation Routes
    const val MAIN = "main"
    const val CALENDAR = "calendar"
    const val SETTINGS = "settings"
    const val CONTACT = "contact"

    // 🏀 Basketball Content Routes
    const val TEAM_DETAIL = "team_detail/{teamId}"
    const val MATCH_DETAIL = "match_detail/{matchId}"
    const val MATCH_RESULTS = "match_results"
    const val MATCH_RESULTS_BY_DATE = "match_results_by_date"
    const val TEAM_ROSTER = "team_roster/{teamTla}/{teamName}"
    const val PLAYER_DETAIL = "player_detail/{playerCode}/{teamName}"
    
    // ⚙️ Settings Routes
    const val SYNC_SETTINGS = "sync_settings"
    const val FAVORITES = "favorites"
    const val NOTIFICATIONS = "notifications"

    // 🔧 Helper functions for parameterized routes
    fun teamDetail(teamId: String) = "team_detail/$teamId"
    fun matchDetail(matchId: String) = "match_detail/$matchId"
    fun teamRoster(teamTla: String, teamName: String) = "team_roster/$teamTla/$teamName"
    fun playerDetail(playerCode: String, teamName: String) = "player_detail/$playerCode/$teamName"
}
