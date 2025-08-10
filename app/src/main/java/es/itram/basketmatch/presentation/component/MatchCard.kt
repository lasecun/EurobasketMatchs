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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import es.itram.basketmatch.R
import es.itram.basketmatch.domain.entity.Match
import es.itram.basketmatch.domain.entity.MatchStatus
import java.time.format.DateTimeFormatter

/**
 * Componente para mostrar la información de un partido usando datos directamente del Match
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchCard(
    modifier: Modifier = Modifier,
    match: Match,
    onMatchClick: (String) -> Unit = {}
) {
    Card(
        onClick = { onMatchClick(match.id) },
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
                    teamName = match.homeTeamName,
                    teamLogo = match.homeTeamLogo,
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
                            text = stringResource(R.string.vs),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Equipo visitante
                TeamInfo(
                    teamName = match.awayTeamName,
                    teamLogo = match.awayTeamLogo,
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
    teamName: String,
    teamLogo: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo arriba (sin click)
        TeamLogo(logoUrl = teamLogo)
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Nombre abajo (sin click)
        Text(
            text = teamName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(4.dp)
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
            contentDescription = stringResource(R.string.team_logo_content_description),
            modifier = modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Fit
        )
    } else {
        // Fallback icon cuando no hay logo
        Icon(
            Icons.Default.Person,
            contentDescription = stringResource(R.string.team_logo_content_description),
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
        MatchStatus.SCHEDULED -> stringResource(R.string.match_status_scheduled) to MaterialTheme.colorScheme.secondaryContainer
        MatchStatus.LIVE -> stringResource(R.string.match_status_live) to MaterialTheme.colorScheme.primaryContainer
        MatchStatus.FINISHED -> stringResource(R.string.match_status_finished) to MaterialTheme.colorScheme.tertiaryContainer
        MatchStatus.POSTPONED -> stringResource(R.string.match_status_postponed) to MaterialTheme.colorScheme.errorContainer
        MatchStatus.CANCELLED -> stringResource(R.string.match_status_cancelled) to MaterialTheme.colorScheme.errorContainer
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
