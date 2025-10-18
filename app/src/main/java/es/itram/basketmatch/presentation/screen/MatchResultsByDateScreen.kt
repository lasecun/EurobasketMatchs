package es.itram.basketmatch.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.presentation.components.MatchResultCard
import es.itram.basketmatch.presentation.viewmodel.MatchResultsByDateViewModel
import java.time.format.DateTimeFormatter

/**
 * 🏀 Pantalla para mostrar resultados de partidos del 30 de septiembre de 2025
 *
 * Esta pantalla muestra los resultados específicos de partidos de Euroliga
 * para la fecha solicitada usando la API oficial v3
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchResultsByDateScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToMatchDetail: (String) -> Unit = {},
    viewModel: MatchResultsByDateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Resultados del 30 de Septiembre 2025",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.retry() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingContent()
                }

                uiState.errorMessage != null -> {
                    ErrorContent(
                        errorMessage = uiState.errorMessage!!,
                        onRetry = { viewModel.retry() }
                    )
                }

                uiState.matches.isEmpty() -> {
                    EmptyContent()
                }

                else -> {
                    MatchResultsList(
                        matches = uiState.matches,
                        selectedDate = uiState.selectedDate,
                        onNavigateToMatchDetail = onNavigateToMatchDetail
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Cargando resultados...",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Obteniendo datos de la API oficial de Euroleague",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "❌ Error",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry
            ) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "🏀",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = "No hay partidos",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "No se encontraron partidos para el 30 de septiembre de 2025",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MatchResultsList(
    matches: List<Match>,
    selectedDate: java.time.LocalDateTime?,
    onNavigateToMatchDetail: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DateHeader(selectedDate = selectedDate)
        }

        item {
            SummaryCard(matchCount = matches.size, matches = matches)
        }

        items(matches) { match ->
            MatchResultCard(
                match = match,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigateToMatchDetail(match.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DateHeader(selectedDate: java.time.LocalDateTime?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📅 Resultados de Euroliga",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            selectedDate?.let { date ->
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy")),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(matchCount: Int, matches: List<Match>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "📊 Resumen del día",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Estadísticas detalladas
            val finishedMatches = matches.filter { it.status == es.itram.basketmatch.domain.entity.MatchStatus.FINISHED }
            val scheduledMatches = matches.filter { it.status == es.itram.basketmatch.domain.entity.MatchStatus.SCHEDULED }
            val liveMatches = matches.filter { it.status == es.itram.basketmatch.domain.entity.MatchStatus.LIVE }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    icon = "✅",
                    count = finishedMatches.size,
                    label = "Finalizados",
                    color = MaterialTheme.colorScheme.primary
                )

                if (liveMatches.isNotEmpty()) {
                    StatisticItem(
                        icon = "🔴",
                        count = liveMatches.size,
                        label = "En vivo",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (scheduledMatches.isNotEmpty()) {
                    StatisticItem(
                        icon = "⏰",
                        count = scheduledMatches.size,
                        label = "Programados",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Datos obtenidos de la API oficial de Euroleague",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (finishedMatches.isNotEmpty()) {
                Text(
                    text = "Toca cualquier partido finalizado para ver estadísticas completas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun StatisticItem(
    icon: String,
    count: Int,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
