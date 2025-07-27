package es.itram.basketmatch.presentation.navigation

/**
 * Rutas de navegación de la aplicación
 */
object NavigationRoutes {
    const val MAIN = "main"
    const val CALENDAR = "calendar"
    const val TEAM_DETAIL = "team_detail/{teamId}"
    const val MATCH_DETAIL = "match_detail/{matchId}"
    
    fun teamDetail(teamId: String) = "team_detail/$teamId"
    fun matchDetail(matchId: String) = "match_detail/$matchId"
}
