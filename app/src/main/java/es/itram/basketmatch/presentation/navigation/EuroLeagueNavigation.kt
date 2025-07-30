package es.itram.basketmatch.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import es.itram.basketmatch.presentation.screen.MainScreen
import es.itram.basketmatch.presentation.screen.CalendarScreen
import es.itram.basketmatch.presentation.screen.TeamDetailScreen
import es.itram.basketmatch.presentation.screen.MatchDetailScreen
import es.itram.basketmatch.presentation.screen.TeamRosterScreen
import es.itram.basketmatch.presentation.screen.PlayerDetailScreen
import es.itram.basketmatch.presentation.viewmodel.MainViewModel

/**
 * Configuración de navegación de la aplicación
 */
@Composable
fun EuroLeagueNavigation(navController: NavHostController) {
    // ViewModel compartido a nivel de navegación para evitar recreaciones
    val mainViewModel: MainViewModel = hiltViewModel()
    
    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.MAIN
    ) {
        composable(NavigationRoutes.MAIN) {
            MainScreen(
                viewModel = mainViewModel,
                onNavigateToCalendar = {
                    navController.navigate(NavigationRoutes.CALENDAR)
                },
                onNavigateToTeamDetail = { teamId ->
                    navController.navigate(NavigationRoutes.teamDetail(teamId))
                },
                onNavigateToMatchDetail = { matchId ->
                    navController.navigate(NavigationRoutes.matchDetail(matchId))
                }
            )
        }
        
        composable(NavigationRoutes.CALENDAR) {
            CalendarScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onDateSelected = { selectedDate ->
                    // Usar el ViewModel compartido directamente
                    mainViewModel.setSelectedDate(selectedDate)
                    navController.popBackStack()
                },
                onNavigateToTeamDetail = { teamId ->
                    navController.navigate(NavigationRoutes.teamDetail(teamId))
                },
                onNavigateToMatchDetail = { matchId ->
                    navController.navigate(NavigationRoutes.matchDetail(matchId))
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
        
        composable(
            route = NavigationRoutes.MATCH_DETAIL,
            arguments = listOf(
                navArgument("matchId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
            MatchDetailScreen(
                matchId = matchId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onTeamClick = { teamTla, teamName ->
                    navController.navigate(NavigationRoutes.teamRoster(teamTla, teamName))
                }
            )
        }
        
        composable(
            route = NavigationRoutes.TEAM_ROSTER,
            arguments = listOf(
                navArgument("teamTla") { type = NavType.StringType },
                navArgument("teamName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val teamTla = backStackEntry.arguments?.getString("teamTla") ?: ""
            val teamName = backStackEntry.arguments?.getString("teamName") ?: ""
            TeamRosterScreen(
                teamTla = teamTla,
                teamName = teamName,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPlayerClick = { player ->
                    navController.navigate(NavigationRoutes.playerDetail(player.code, teamName))
                }
            )
        }
        
        composable(
            route = NavigationRoutes.PLAYER_DETAIL,
            arguments = listOf(
                navArgument("playerId") { type = NavType.StringType },
                navArgument("teamName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val playerId = backStackEntry.arguments?.getString("playerId") ?: ""
            val teamName = backStackEntry.arguments?.getString("teamName") ?: ""
            // Necesitamos obtener el jugador del ViewModel
            val teamRosterViewModel: es.itram.basketmatch.presentation.viewmodel.TeamRosterViewModel = hiltViewModel()
            val player = teamRosterViewModel.getPlayerById(playerId)
            
            if (player != null) {
                PlayerDetailScreen(
                    player = player,
                    teamName = teamName,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            } else {
                // Si no se encuentra el jugador, volver atrás
                navController.popBackStack()
            }
        }
    }
}
