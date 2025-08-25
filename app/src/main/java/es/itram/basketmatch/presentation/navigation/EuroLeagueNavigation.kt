package es.itram.basketmatch.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import es.itram.basketmatch.presentation.screen.settings.SettingsScreen
import es.itram.basketmatch.presentation.screen.settings.SyncSettingsScreen
import es.itram.basketmatch.presentation.viewmodel.MainViewModel
import es.itram.basketmatch.presentation.viewmodel.SettingsViewModel

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
                onNavigateToMatchDetail = { matchId ->
                    navController.navigate(NavigationRoutes.matchDetail(matchId))
                },
                onNavigateToSettings = {
                    navController.navigate(NavigationRoutes.SETTINGS)
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
                    
                    // 📊 Analytics: Track team click from match detail
                    mainViewModel.trackTeamClickedFromMatch(
                        teamId = teamTla,
                        teamName = teamName, 
                        matchId = matchId,
                        source = "match_detail_screen"
                    )
                    
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
        
        composable(NavigationRoutes.SETTINGS) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSyncSettingsClick = {
                    navController.navigate(NavigationRoutes.SYNC_SETTINGS)
                },
                onFavoritesClick = {
                    navController.navigate(NavigationRoutes.FAVORITES)
                }
            )
        }
        
        composable(NavigationRoutes.FAVORITES) {
            es.itram.basketmatch.presentation.screen.settings.FavoritesScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onTeamClick = { teamId ->
                    // Navegar directamente al roster del equipo en lugar de team detail
                    // Primero necesitamos obtener el equipo para tener su TLA y nombre
                    val team = mainViewModel.getTeamById(teamId)
                    if (team != null) {
                        // 📊 Analytics: Track team click from favorites
                        mainViewModel.trackTeamClickedFromMatch(
                            teamId = team.code,
                            teamName = team.name,
                            matchId = "favorites_screen",
                            source = "favorites_screen"
                        )
                        
                        navController.navigate(NavigationRoutes.teamRoster(team.code, team.name))
                    } else {
                        // Fallback a team detail si no encontramos el equipo
                        navController.navigate(NavigationRoutes.teamDetail(teamId))
                    }
                }
            )
        }
        
        composable(NavigationRoutes.SYNC_SETTINGS) {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val lastSyncTime by settingsViewModel.lastSyncTime.collectAsStateWithLifecycle()
            val isSyncing by settingsViewModel.isSyncing.collectAsStateWithLifecycle()
            val isVerifying by settingsViewModel.isVerifying.collectAsStateWithLifecycle()
            
            // Track sync settings access
            LaunchedEffect(Unit) {
                settingsViewModel.trackSyncSettingsAccess()
            }
            
            SyncSettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSyncClick = {
                    settingsViewModel.performManualSync()
                },
                onVerifyClick = {
                    settingsViewModel.performVerification()
                },
                lastSyncTime = lastSyncTime?.atZone(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
                isLoading = isSyncing || isVerifying,
                isSyncing = isSyncing,
                isVerifying = isVerifying
            )
        }
    }
}
