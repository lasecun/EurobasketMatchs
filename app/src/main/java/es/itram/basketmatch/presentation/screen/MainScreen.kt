package es.itram.basketmatch.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.itram.basketmatch.presentation.component.ErrorMessage
import es.itram.basketmatch.presentation.component.LoadingIndicator
import es.itram.basketmatch.presentation.component.MatchCard
import es.itram.basketmatch.presentation.component.HeaderDateSelector
import es.itram.basketmatch.presentation.component.SyncProgressIndicator
import es.itram.basketmatch.presentation.component.NoMatchesTodayCard
import es.itram.basketmatch.presentation.viewmodel.MainViewModel

/**
 * Pantalla principal con la lista de partidos filtrada por fecha
 */
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToCalendar: () -> Unit,
    onNavigateToTeamDetail: (String) -> Unit,
    onNavigateToMatchDetail: (String) -> Unit
) {
    val matches by viewModel.matches.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val syncProgress by viewModel.syncProgress.collectAsStateWithLifecycle()

    // 📊 Analytics: Track screen view
    LaunchedEffect(Unit) {
        viewModel.trackMainScreenView()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // Header con navegación de fechas
        HeaderDateSelector(
            selectedDate = selectedDate,
            onDateClick = {
                // 📊 Analytics: Track calendar navigation from main screen
                viewModel.trackCalendarNavigation()
                onNavigateToCalendar()
            },
            onPreviousDay = { viewModel.goToPreviousDay() },
            onNextDay = { viewModel.goToNextDay() }
        )

        // Contenido principal
        when {
            syncProgress.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    SyncProgressIndicator(syncProgress = syncProgress)
                }
            }
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorMessage(
                        message = error ?: "Error desconocido",
                        onRetry = { /* viewModel.retryLoading() */ }
                    )
                }
            }
            matches.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    NoMatchesTodayCard(
                        isToday = viewModel.isSelectedDateToday(),
                        nextMatchDay = viewModel.findNextMatchDay(),
                        onNavigateToNextMatchDay = { viewModel.goToNextAvailableMatchDay() },
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(matches) { match ->
                        MatchCard(
                            match = match,
                            onMatchClick = { matchId ->
                                // 📊 Analytics: Track match selection from main screen
                                viewModel.trackMatchClicked(match)
                                onNavigateToMatchDetail(matchId)
                            }
                        )
                    }
                }
            }
        }
    }
}
