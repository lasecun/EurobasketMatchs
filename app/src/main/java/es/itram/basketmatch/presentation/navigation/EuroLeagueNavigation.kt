package es.itram.basketmatch.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import es.itram.basketmatch.presentation.screen.MainScreen
import es.itram.basketmatch.presentation.screen.CalendarScreen
import es.itram.basketmatch.presentation.screen.TeamDetailScreen

/**
 * Configuración de navegación de la aplicación
 */
@Composable
fun EuroLeagueNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.MAIN
    ) {
        composable(NavigationRoutes.MAIN) {
            MainScreen(
                onNavigateToCalendar = {
                    navController.navigate(NavigationRoutes.CALENDAR)
                },
                onNavigateToTeamDetail = { teamId ->
                    navController.navigate(NavigationRoutes.teamDetail(teamId))
                }
            )
        }
        
        composable(NavigationRoutes.CALENDAR) {
            CalendarScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToTeamDetail = { teamId ->
                    navController.navigate(NavigationRoutes.teamDetail(teamId))
                }
            )
        }
        
        composable(
            route = NavigationRoutes.TEAM_DETAIL,
            arguments = listOf(
                navArgument("teamId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getString("teamId") ?: ""
            TeamDetailScreen(
                teamId = teamId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
