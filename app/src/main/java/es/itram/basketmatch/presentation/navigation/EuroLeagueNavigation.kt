package es.itram.basketmatch.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dagger.hilt.android.EntryPointAccessors
import es.itram.basketmatch.analytics.AnalyticsManager
import es.itram.basketmatch.di.AnalyticsEntryPoint
import es.itram.basketmatch.presentation.screen.CalendarScreen
import es.itram.basketmatch.presentation.screen.ContactScreen
import es.itram.basketmatch.presentation.screen.MainScreen
import es.itram.basketmatch.presentation.screen.MatchDetailScreen
import es.itram.basketmatch.presentation.screen.NotificationSettingsScreen
import es.itram.basketmatch.presentation.screen.PlayerDetailScreen
import es.itram.basketmatch.presentation.screen.StandingsScreen
import es.itram.basketmatch.presentation.screen.TeamDetailScreen
import es.itram.basketmatch.presentation.screen.TeamRosterScreen
import es.itram.basketmatch.presentation.screen.settings.SettingsScreen
import es.itram.basketmatch.presentation.screen.settings.SyncSettingsScreen
import es.itram.basketmatch.presentation.results.MatchResultsScreen
import es.itram.basketmatch.presentation.viewmodel.MainViewModel
import es.itram.basketmatch.presentation.viewmodel.SettingsViewModel

/**
 * Configuración de navegación de la aplicación
 */
@Composable
fun EuroLeagueNavigation(navController: NavHostController) {
    // ViewModel compartido a nivel de navegación para evitar recreaciones
    val mainViewModel: MainViewModel = hiltViewModel()

    // Obtener AnalyticsManager desde Hilt usando EntryPoint
    val context = LocalContext.current
    val analyticsManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AnalyticsEntryPoint::class.java
        ).analyticsManager()
    }

    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.MAIN
    ) {
        composable(NavigationRoutes.MAIN) {
            MainScreen(
                viewModel = mainViewModel,
                analyticsManager = analyticsManager,
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
                    mainViewModel.selectDate(selectedDate)
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
                    analyticsManager.trackTeamViewed(
                        teamCode = teamTla,
                        teamName = teamName,
                        source = "match_detail_screen"
                    )
                    
                    navController.navigate(NavigationRoutes.teamRoster(teamTla, teamName))
                }
            )
        }
        
        // 📊 Match Results Screen
        composable(NavigationRoutes.MATCH_RESULTS) {
            MatchResultsScreen()
        }

        // 📅 Match Results By Date Screen (30 de Septiembre 2025)
        composable(NavigationRoutes.MATCH_RESULTS_BY_DATE) {
            es.itram.basketmatch.presentation.screen.MatchResultsByDateScreen(
                onNavigateBack = {
                    android.util.Log.d("Navigation", "🔙 MatchResultsByDateScreen: navigating back")
                    navController.popBackStack()
                },
                onNavigateToMatchDetail = { matchId ->
                    android.util.Log.d("Navigation", "🏀 MatchResultsByDateScreen: navigating to match detail $matchId")
                    navController.navigate(NavigationRoutes.matchDetail(matchId))
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
                onSyncSettingsClick = {
                    navController.navigate(NavigationRoutes.SYNC_SETTINGS)
                },
                onFavoritesClick = {
                    navController.navigate(NavigationRoutes.FAVORITES)
                },
                onNotificationsClick = {
                    navController.navigate(NavigationRoutes.NOTIFICATIONS)
                },
                onContactClick = {
                    navController.navigate(NavigationRoutes.CONTACT)
                }
            )
        }

        // 📧 Contact Screen
        composable(NavigationRoutes.CONTACT) {
            ContactScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // ⭐ Favorites Screen
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
                        analyticsManager.trackTeamViewed(
                            teamCode = team.code,
                            teamName = team.name,
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
        
        // 📊 Standings Screen
        composable(NavigationRoutes.STANDINGS) {
            StandingsScreen(
                onNavigateToTeam = { teamCode, teamName ->
                    android.util.Log.d("Navigation", "🏀 StandingsScreen: navigating to team roster $teamCode")
                    navController.navigate(NavigationRoutes.teamRoster(teamCode, teamName))
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
        
        composable(NavigationRoutes.NOTIFICATIONS) {
            NotificationSettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
