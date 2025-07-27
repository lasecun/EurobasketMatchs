package es.itram.basketmatch.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.itram.basketmatch.presentation.viewmodel.TeamDetailViewModel
import es.itram.basketmatch.presentation.component.MatchCard
import es.itram.basketmatch.presentation.component.LoadingIndicator
import es.itram.basketmatch.presentation.component.ErrorMessage

/**
 * Pantalla de detalles del equipo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailScreen(
    teamId: String,
    onNavigateBack: () -> Unit,
    viewModel: TeamDetailViewModel = hiltViewModel()
) {
    val team by viewModel.team.collectAsStateWithLifecycle()
    val matches by viewModel.matches.collectAsStateWithLifecycle()
    val standing by viewModel.standing.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    LaunchedEffect(teamId) {
        viewModel.loadTeamDetails(teamId)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text(team?.name ?: "Cargando...") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }
            },
            actions = {
                if (team != null) {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
                            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        )

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
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
            }
            
            team != null -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Información del equipo
                    item {
                        TeamInfoCard(
                            team = team!!,
                            standing = standing,
                            winPercentage = viewModel.getWinPercentage()
                        )
                    }
                    
                    // Próximos partidos
                    item {
                        val upcomingMatches = viewModel.getUpcomingMatches()
                        if (upcomingMatches.isNotEmpty()) {
                            MatchesSection(
                                title = "Próximos partidos",
                                matches = upcomingMatches,
                                onTeamClick = { /* No navegamos al mismo equipo */ }
                            )
                        }
                    }
                    
                    // Partidos recientes
                    item {
                        val recentMatches = viewModel.getRecentMatches()
                        if (recentMatches.isNotEmpty()) {
                            MatchesSection(
                                title = "Partidos recientes",
                                matches = recentMatches,
                                onTeamClick = { /* No navegamos al mismo equipo */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamInfoCard(
    team: es.itram.basketmatch.domain.entity.Team,
    standing: es.itram.basketmatch.domain.entity.Standing?,
    winPercentage: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Nombre y código del equipo
            Text(
                text = team.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = team.shortName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Información básica
            InfoRow(label = "País", value = team.country)
            InfoRow(label = "Ciudad", value = team.city)
            if (team.founded > 0) {
                InfoRow(label = "Fundado", value = team.founded.toString())
            }
            if (team.coach.isNotBlank()) {
                InfoRow(label = "Entrenador", value = team.coach)
            }
            
            // Estadísticas de la temporada
            if (standing != null) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Divider()
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Estadísticas de la temporada",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        icon = Icons.Default.Info,
                        label = "Posición",
                        value = "${standing.position}º"
                    )
                    
                    StatItem(
                        icon = Icons.Default.Star,
                        label = "Jugados",
                        value = standing.played.toString()
                    )
                    
                    StatItem(
                        icon = Icons.Default.Favorite,
                        label = "% Victoria",
                        value = "${winPercentage.toInt()}%"
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = standing.won.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Ganados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = standing.lost.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Perdidos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = standing.pointsDifference.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (standing.pointsDifference >= 0) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Diferencia",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MatchesSection(
    title: String,
    matches: List<es.itram.basketmatch.domain.entity.Match>,
    onTeamClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            matches.forEach { match ->
                MatchCard(
                    match = match,
                    onTeamClick = onTeamClick
                )
                
                if (match != matches.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
