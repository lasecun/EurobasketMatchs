package es.itram.basketmatch.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.itram.basketmatch.R
import es.itram.basketmatch.domain.entity.SeasonType
import es.itram.basketmatch.presentation.component.EnhancedLoadingIndicator
import es.itram.basketmatch.presentation.component.ErrorMessage
import es.itram.basketmatch.presentation.component.StandingRow
import es.itram.basketmatch.presentation.viewmodel.StandingsUiState
import es.itram.basketmatch.presentation.viewmodel.StandingsViewModel

/**
 * 📊 Pantalla de Clasificación
 *
 * Muestra la tabla de clasificación de la EuroLeague con:
 * - Filtro por tipo de temporada (Regular/Playoffs)
 * - Posiciones con medallas para top 3
 * - Estadísticas de cada equipo
 * - Destacado de equipos favoritos
 * - Navegación a detalles del equipo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandingsScreen(
    onNavigateToTeam: (String, String) -> Unit,
    viewModel: StandingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedSeasonType by viewModel.selectedSeasonType.collectAsStateWithLifecycle()

    // 📊 Analytics: Track screen view
    LaunchedEffect(Unit) {
        viewModel.trackScreenView()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.standings_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.standings_subtitle, "2025-26"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.refresh)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )

            // Filtro de tipo de temporada
            SeasonTypeSelector(
                selectedType = selectedSeasonType,
                onTypeSelected = { viewModel.selectSeasonType(it) }
            )

            // Contenido principal
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "standings_content"
            ) { state ->
                when (state) {
                    is StandingsUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            EnhancedLoadingIndicator(
                                message = stringResource(R.string.loading_standings),
                                showMessage = true
                            )
                        }
                    }

                    is StandingsUiState.Empty -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyStandingsMessage()
                        }
                    }

                    is StandingsUiState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            ErrorMessage(
                                message = state.message,
                                onRetry = { viewModel.refresh() }
                            )
                        }
                    }

                    is StandingsUiState.Success -> {
                        StandingsTable(
                            standings = state.standings,
                            favoriteTeamIds = viewModel.favoriteTeamIds.collectAsStateWithLifecycle().value,
                            onTeamClick = { teamCode, teamName, position ->
                                viewModel.trackTeamTap(teamCode, teamName, position)
                                onNavigateToTeam(teamCode, teamName)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Selector de tipo de temporada
 */
@Composable
private fun SeasonTypeSelector(
    selectedType: SeasonType,
    onTypeSelected: (SeasonType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedType == SeasonType.REGULAR,
            onClick = { onTypeSelected(SeasonType.REGULAR) },
            label = { Text(stringResource(R.string.standings_regular_season)) },
            modifier = Modifier.weight(1f),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        FilterChip(
            selected = selectedType == SeasonType.PLAYOFFS,
            onClick = { onTypeSelected(SeasonType.PLAYOFFS) },
            label = { Text(stringResource(R.string.standings_playoffs)) },
            modifier = Modifier.weight(1f),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            )
        )
    }
}

/**
 * Tabla de clasificación
 */
@Composable
private fun StandingsTable(
    standings: List<es.itram.basketmatch.presentation.viewmodel.StandingWithTeam>,
    favoriteTeamIds: Set<String>,
    onTeamClick: (String, String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Header de la tabla
        StandingsHeader()

        // Lista de equipos
        LazyColumn(
            contentPadding = PaddingValues(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(
                items = standings,
                key = { it.standing.teamId }
            ) { standingWithTeam ->
                val team = standingWithTeam.team
                val standing = standingWithTeam.standing

                if (team != null) {
                    StandingRow(
                        position = standing.position,
                        teamId = team.id,
                        teamName = team.name,
                        teamLogoUrl = team.logoUrl,
                        standing = standing,
                        isFavorite = favoriteTeamIds.contains(team.id),
                        onClick = {
                            onTeamClick(team.code, team.name, standing.position)
                        }
                    )
                }
            }

            // Footer con hint
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.standings_tap_team_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            }
        }
    }
}

/**
 * Header de la tabla con nombres de columnas
 */
@Composable
private fun StandingsHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.standings_position),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(40.dp)
        )

        Text(
            text = stringResource(R.string.standings_team),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f).padding(start = 44.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.standings_played),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.standings_won),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.standings_lost),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.standings_percentage),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Mensaje cuando no hay datos de clasificación
 */
@Composable
private fun EmptyStandingsMessage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📊",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_standings_data),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.no_standings_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

