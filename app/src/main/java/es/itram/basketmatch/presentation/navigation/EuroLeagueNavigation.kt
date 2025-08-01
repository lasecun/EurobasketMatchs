package es.itram.basketmatch.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
                    android.util.Log.d("Navigation", "🔙 MatchDetailScreen: navigating back")
                    navController.popBackStack()
                },
                onTeamClick = { teamTla, teamName ->
                    android.util.Log.d("Navigation", "🏀 MatchDetailScreen: navigating to team roster $teamTla")
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
                    android.util.Log.d("Navigation", "🔙 TeamRosterScreen: navigating back")
                    navController.popBackStack()
                },
                onPlayerClick = { player ->
                    // Guardar el jugador seleccionado para acceso en la pantalla de detalle
                    android.util.Log.d("Navigation", "👤 TeamRosterScreen: navigating to player ${player.fullName}")
                    PlayerNavigationHelper.setSelectedPlayer(player, teamName)
                    // Navegar con parámetros para una navegación más robusta
                    navController.navigate(NavigationRoutes.playerDetail(player.code, teamName))
                },
                viewModel = teamRosterViewModel
            )
        }
        
        composable(
            route = NavigationRoutes.PLAYER_DETAIL,
            arguments = listOf(
                navArgument("playerCode") { type = NavType.StringType },
                navArgument("teamName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val playerCode = backStackEntry.arguments?.getString("playerCode") ?: ""
            val teamName = backStackEntry.arguments?.getString("teamName") ?: ""
            
            // Variable para controlar si ya se navegó hacia atrás
            var hasNavigatedBack by remember { mutableStateOf(false) }
            
            // Obtener el jugador desde el helper
            val player = PlayerNavigationHelper.getSelectedPlayer()
            
            if (player != null && player.code == playerCode) {
                PlayerDetailScreen(
                    player = player,
                    teamName = teamName,
                    onNavigateBack = {
                        if (!hasNavigatedBack) {
                            android.util.Log.d("Navigation", "🔙 PlayerDetailScreen: User pressed back button")
                            hasNavigatedBack = true
                            PlayerNavigationHelper.clearSelection()
                            navController.popBackStack()
                        }
                    }
                )
            } else if (!hasNavigatedBack) {
                // Player not found - navigate back only once
                LaunchedEffect(Unit) {
                    android.util.Log.d("Navigation", "⚠️ Player not found for code: $playerCode, navigating back")
                    hasNavigatedBack = true
                    navController.popBackStack()
                }
            }
        }
    }
}
