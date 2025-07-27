package es.itram.basketmatch.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.entity.MatchStatus
import es.itram.basketmatch.domain.entity.SeasonType
import es.itram.basketmatch.presentation.viewmodel.MatchDetailViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Pantalla de detalle del partido
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    matchId: String,
    onNavigateBack: () -> Unit,
    onTeamClick: (teamTla: String, teamName: String) -> Unit,
    viewModel: MatchDetailViewModel = hiltViewModel()
) {
    val match by viewModel.match.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    // Cargar datos cuando se inicia la pantalla
    LaunchedEffect(matchId) {
        viewModel.loadMatchDetails(matchId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = match?.let { "${it.homeTeamName} vs ${it.awayTeamName}" } ?: "Detalle del Partido",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                error != null -> {
                    ErrorMessage(
                        message = error!!,
                        onRetry = { viewModel.loadMatchDetails(matchId) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                match != null -> {
                    MatchDetailContent(
                        match = match!!,
                        onTeamClick = onTeamClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                else -> {
                    Text(
                        text = "Partido no encontrado",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchDetailContent(
    match: Match,
    onTeamClick: (teamTla: String, teamName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header con equipos y resultado
        MatchHeader(
            match = match,
            onTeamClick = onTeamClick
        )
        
        // Información del partido
        MatchInfo(match = match)
        
        // Estado del partido
        MatchStatusCard(match = match)
        
        // Información adicional
        AdditionalInfo(match = match)
    }
}

@Composable
private fun MatchHeader(
    match: Match,
    onTeamClick: (teamTla: String, teamName: String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Equipos y resultado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Equipo local
                TeamSection(
                    teamName = match.homeTeamName,
                    teamLogo = match.homeTeamLogo,
                    score = match.homeScore,
                    isHome = true,
                    onClick = { onTeamClick(match.homeTeamId, match.homeTeamName) }
                )
                
                // VS y resultado
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "VS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (match.homeScore != null && match.awayScore != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${match.homeScore} - ${match.awayScore}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Equipo visitante
                TeamSection(
                    teamName = match.awayTeamName,
                    teamLogo = match.awayTeamLogo,
                    score = match.awayScore,
                    isHome = false,
                    onClick = { onTeamClick(match.awayTeamId, match.awayTeamName) }
                )
            }
        }
    }
}

@Composable
private fun TeamSection(
    teamName: String,
    teamLogo: String?,
    score: Int?,
    isHome: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(120.dp)
    ) {
        // Logo del equipo (clickeable)
        Card(
            onClick = onClick,
            modifier = Modifier.size(60.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(teamLogo)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Logo de $teamName",
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Nombre del equipo (clickeable)
        TextButton(onClick = onClick) {
            Text(
                text = teamName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
        
        // Marcador individual
        if (score != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = score.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun MatchInfo(match: Match) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Información del Partido",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            InfoRow(
                label = "Fecha",
                value = match.dateTime.format(
                    DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("es", "ES"))
                )
            )
            
            InfoRow(
                label = "Hora",
                value = match.dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            )
            
            InfoRow(
                label = "Jornada",
                value = "Jornada ${match.round}"
            )
            
            if (match.venue.isNotBlank()) {
                InfoRow(
                    label = "Pabellón",
                    value = match.venue
                )
            }
        }
    }
}

@Composable
private fun MatchStatusCard(match: Match) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Estado del Partido",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val (statusText, statusColor) = when (match.status) {
                MatchStatus.SCHEDULED -> "Programado" to MaterialTheme.colorScheme.onSurfaceVariant
                MatchStatus.LIVE -> "En Vivo" to Color(0xFF4CAF50)
                MatchStatus.FINISHED -> "Finalizado" to MaterialTheme.colorScheme.primary
                MatchStatus.POSTPONED -> "Aplazado" to Color(0xFFFF9800)
                MatchStatus.CANCELLED -> "Cancelado" to Color(0xFFF44336)
            }
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = statusColor.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = statusText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun AdditionalInfo(match: Match) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Información Adicional",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            InfoRow(
                label = "Temporada",
                value = "2025-26" // Valor fijo por ahora
            )
            
            InfoRow(
                label = "Tipo",
                value = when (match.seasonType) {
                    SeasonType.REGULAR -> "Temporada Regular"
                    SeasonType.PLAYOFFS -> "Playoffs"
                    SeasonType.FINAL_FOUR -> "Final Four"
                }
            )
            
            InfoRow(
                label = "ID del Partido",
                value = match.id
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(2f)
        )
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}
