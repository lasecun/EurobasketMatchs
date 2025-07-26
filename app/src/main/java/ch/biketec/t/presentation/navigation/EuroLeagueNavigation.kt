package ch.biketec.t.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ch.biketec.t.presentation.screen.CalendarScreen
import ch.biketec.t.presentation.screen.MainScreen
import ch.biketec.t.presentation.screen.TeamDetailScreen
import java.time.LocalDate

@Composable
fun EuroLeagueNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(
                onTeamClick = { teamId ->
                    navController.navigate("team_detail/$teamId")
                },
                onCalendarClick = {
                    navController.navigate("calendar")
                }
            )
        }
        
        composable("calendar") {
            CalendarScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onDateClick = { date ->
                    // Navigate back to main screen with selected date
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_date", date.toString())
                    navController.popBackStack()
                }
            )
        }
        
        composable("team_detail/{teamId}") { backStackEntry ->
            val teamId = backStackEntry.arguments?.getString("teamId") ?: ""
            TeamDetailScreen(
                teamId = teamId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
