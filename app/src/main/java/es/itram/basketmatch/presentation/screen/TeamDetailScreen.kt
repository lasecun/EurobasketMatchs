package es.itram.basketmatch.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.itram.basketmatch.R
import es.itram.basketmatch.presentation.component.ErrorMessage
import es.itram.basketmatch.presentation.component.EnhancedLoadingIndicator
import es.itram.basketmatch.presentation.component.EnhancedMatchCard
import es.itram.basketmatch.presentation.viewmodel.TeamDetailViewModel

/**
 * Pantalla de detalles del equipo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailScreen(
    teamId: String,
    onNavigateBack: () -> Unit,
    onNavigateToRoster: ((teamTla: String, teamName: String) -> Unit)? = null,
    viewModel: TeamDetailViewModel = hiltViewModel()
) {
    val team by viewModel.team.collectAsStateWithLifecycle()
    val matches by viewModel.matches.collectAsStateWithLifecycle()
    val standing by viewModel.standing.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    // 📊 Analytics: Track screen view
    LaunchedEffect(Unit) {
        viewModel.trackScreenView()
    }

    LaunchedEffect(teamId) {
        viewModel.loadTeamDetails(teamId)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text(team?.name ?: stringResource(R.string.loading)) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            },
            actions = {
                if (team != null) {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) stringResource(R.string.remove_from_favorites) else stringResource(R.string.add_to_favorites),
                            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        )

        when {
            isLoading -> {
                EnhancedLoadingIndicator(
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
                        val currentTeam = team!!
                        TeamInfoCard(
                            team = currentTeam,
                            standing = standing,
                            winPercentage = viewModel.getWinPercentage(),
                            onNavigateToRoster = onNavigateToRoster?.let {
                                {
                                    // 📊 Analytics: Track team roster access from team detail
                                    viewModel.trackRosterAccess(currentTeam.code, currentTeam.name)
                                    it(currentTeam.code, currentTeam.name)
                                }
                            }
                        )
                    }
                    
                    // Próximos partidos
                    item {
                        val upcomingMatches = viewModel.getUpcomingMatches()
                        if (upcomingMatches.isNotEmpty()) {
                            MatchesSection(
                                title = stringResource(R.string.upcoming_matches),
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
                                title = stringResource(R.string.recent_matches),
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
    winPercentage: Double,
    onNavigateToRoster: (() -> Unit)? = null
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
            InfoRow(label = stringResource(R.string.team_info_country), value = team.country)
            InfoRow(label = stringResource(R.string.team_info_city), value = team.city)
            if (team.founded > 0) {
                InfoRow(label = stringResource(R.string.team_info_founded), value = team.founded.toString())
            }
            if (team.coach.isNotBlank()) {
                InfoRow(label = stringResource(R.string.team_info_coach), value = team.coach)
            }
            
            // Estadísticas de la temporada
            if (standing != null) {
                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.team_season_stats),
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
                        label = stringResource(R.string.team_position),
                        value = "${standing.position}º"
                    )
                    
                    StatItem(
                        icon = Icons.Default.Star,
                        label = stringResource(R.string.team_games_played),
                        value = standing.played.toString()
                    )
                    
                    StatItem(
                        icon = Icons.Default.Favorite,
                        label = stringResource(R.string.team_win_percentage),
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
                            text = stringResource(R.string.team_games_won),
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
                            text = stringResource(R.string.team_games_lost),
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
                            text = stringResource(R.string.team_point_difference),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Botón para ver roster del equipo
            if (onNavigateToRoster != null) {
                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = onNavigateToRoster,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.team_view_roster))
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
                EnhancedMatchCard(
                    match = match
                )
                
                if (match != matches.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
