package es.itram.basketmatch.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.itram.basketmatch.R
import es.itram.basketmatch.presentation.viewmodel.MainViewModel
import es.itram.basketmatch.presentation.component.MatchCard
import es.itram.basketmatch.presentation.component.LoadingIndicator
import es.itram.basketmatch.presentation.component.ErrorMessage

/**
 * Pantalla principal que muestra los partidos del día seleccionado
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToCalendar: () -> Unit,
    onNavigateToTeamDetail: (String) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val matches by viewModel.matches.collectAsStateWithLifecycle()
    val teams by viewModel.teams.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header con navegación de fechas
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.goToPreviousDay() }) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Día anterior"
                        )
                    }

                    Text(
                        text = viewModel.getFormattedSelectedDate(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = { viewModel.goToNextDay() }) {
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = "Día siguiente"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = { viewModel.goToToday() }
                    ) {
                        Text("Hoy")
                    }

                    Button(
                        onClick = onNavigateToCalendar
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Calendario")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contenido principal
        when {
            isLoading -> {
                LoadingIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            error != null -> {
                val currentError = error
                ErrorMessage(
                    message = currentError!!,
                    onRetry = { viewModel.clearError() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            matches.isEmpty() -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay partidos programados para este día",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(matches) { match ->
                        val homeTeam = teams[match.homeTeamId]
                        val awayTeam = teams[match.awayTeamId]
                        
                        MatchCard(
                            match = match,
                            homeTeam = homeTeam,
                            awayTeam = awayTeam,
                            onTeamClick = onNavigateToTeamDetail
                        )
                    }
                }
            }
        }
    }
}
