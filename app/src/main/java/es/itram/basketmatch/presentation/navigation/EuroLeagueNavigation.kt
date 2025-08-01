package es.itram.basketmatch.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
                },
                onNavigateToRoster = { teamTla, teamName ->
                    navController.navigate(NavigationRoutes.teamRoster(teamTla, teamName))
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
            
            // Crear ViewModel aquí para mantener el estado
            val teamRosterViewModel: es.itram.basketmatch.presentation.viewmodel.TeamRosterViewModel = hiltViewModel()
            
            TeamRosterScreen(
                teamTla = teamTla,
                teamName = teamName,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPlayerClick = { player ->
                    // Guardar el jugador seleccionado
                    PlayerNavigationHelper.setSelectedPlayer(player, teamName)
                    navController.navigate(NavigationRoutes.playerDetail())
                },
                viewModel = teamRosterViewModel
            )
        }
        
        composable(NavigationRoutes.PLAYER_DETAIL) {
            val player = PlayerNavigationHelper.getSelectedPlayer()
            val teamName = PlayerNavigationHelper.getSelectedTeamName()
            
            if (player != null) {
                PlayerDetailScreen(
                    player = player,
                    teamName = teamName,
                    onNavigateBack = {
                        PlayerNavigationHelper.clearSelection()
                        navController.popBackStack()
                    }
                )
            } else {
                // Si no se encuentra el jugador, volver atrás
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }
    }
}
