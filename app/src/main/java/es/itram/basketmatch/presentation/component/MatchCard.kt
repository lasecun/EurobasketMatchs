package es.itram.basketmatch.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.entity.MatchStatus
import java.time.format.DateTimeFormatter

/**
 * Componente para mostrar la información de un partido usando datos directamente del Match
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchCard(
    match: Match,
    onTeamClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con hora y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = match.dateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                MatchStatusChip(status = match.status)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Equipos y marcador
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Equipo local
                TeamInfo(
                    teamId = match.homeTeamId,
                    teamName = match.homeTeamName,
                    teamLogo = match.homeTeamLogo,
                    score = match.homeScore,
                    isHome = true,
                    onTeamClick = onTeamClick,
                    modifier = Modifier.weight(1f)
                )
                
                // VS o marcador
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    if (match.status == MatchStatus.FINISHED && match.homeScore != null && match.awayScore != null) {
                        Text(
                            text = "${match.homeScore} - ${match.awayScore}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = "VS",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Equipo visitante
                TeamInfo(
                    teamId = match.awayTeamId,
                    teamName = match.awayTeamName,
                    teamLogo = match.awayTeamLogo,
                    score = match.awayScore,
                    isHome = false,
                    onTeamClick = onTeamClick,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Información adicional
            if (match.venue.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = match.venue,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamInfo(
    teamId: String,
    teamName: String,
    teamLogo: String?,
    score: Int?,
    isHome: Boolean,
    onTeamClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onTeamClick(teamId) }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo arriba
        TeamLogo(logoUrl = teamLogo)
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Nombre abajo
        Text(
            text = teamName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TeamLogo(
    logoUrl: String?,
    modifier: Modifier = Modifier
) {
    if (!logoUrl.isNullOrBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(logoUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Logo del equipo",
            modifier = modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Fit
        )
    } else {
        // Fallback icon cuando no hay logo
        Icon(
            Icons.Default.Person,
            contentDescription = "Logo del equipo",
            modifier = modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun MatchStatusChip(
    status: MatchStatus,
    modifier: Modifier = Modifier
) {
    val (text, containerColor) = when (status) {
        MatchStatus.SCHEDULED -> "Programado" to MaterialTheme.colorScheme.secondaryContainer
        MatchStatus.LIVE -> "En vivo" to MaterialTheme.colorScheme.primaryContainer
        MatchStatus.FINISHED -> "Finalizado" to MaterialTheme.colorScheme.tertiaryContainer
        MatchStatus.POSTPONED -> "Aplazado" to MaterialTheme.colorScheme.errorContainer
        MatchStatus.CANCELLED -> "Cancelado" to MaterialTheme.colorScheme.errorContainer
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = containerColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}
