package es.itram.basketmatch.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import es.itram.basketmatch.R
import es.itram.basketmatch.domain.entity.Standing

/**
 * 📊 Componente que muestra una fila de la clasificación
 *
 * Muestra la posición, logo del equipo, nombre y estadísticas principales.
 */
@Composable
fun StandingRow(
    position: Int,
    teamId: String,
    teamName: String,
    teamLogoUrl: String?,
    standing: Standing,
    isFavorite: Boolean,
    isVisible: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            initialOffsetY = { it * (position) }
        ) + fadeIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { onClick() },
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp // Misma elevation para todos (sin sombra exagerada)
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isFavorite) {
                    // Tinte muy sutil para favoritos (8% de alpha en lugar de 30%)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Primera fila: Posición, Logo, Nombre y Estadísticas principales
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Posición con medalla para top 3
                    PositionIndicator(
                        position = position,
                        modifier = Modifier.width(40.dp)
                    )

                    // Logo del equipo
                    AsyncImage(
                        model = teamLogoUrl,
                        contentDescription = stringResource(R.string.team_logo, teamName),
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Fit
                    )

                    // Nombre del equipo
                    Text(
                        text = teamName,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isFavorite) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Estadísticas compactas
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatsChip(
                            label = stringResource(R.string.standings_played),
                            value = standing.played.toString()
                        )
                        StatsChip(
                            label = stringResource(R.string.standings_won),
                            value = standing.won.toString(),
                            color = MaterialTheme.colorScheme.primary
                        )
                        StatsChip(
                            label = stringResource(R.string.standings_lost),
                            value = standing.lost.toString(),
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "${standing.winPercentage.toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Segunda fila: Diferencia de puntos
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Spacer(Modifier.width(40.dp))
                    Spacer(Modifier.width(32.dp))
                    Spacer(Modifier.width(12.dp))

                    Text(
                        text = stringResource(
                            R.string.standings_difference_with_sign,
                            standing.pointsDifference
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            standing.pointsDifference > 0 -> Color(0xFF4CAF50)
                            standing.pointsDifference < 0 -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Indicador de posición con medallas para top 3
 */
@Composable
private fun PositionIndicator(
    position: Int,
    modifier: Modifier = Modifier
) {
    val medal = when (position) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> null
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (medal != null) {
            Text(
                text = medal,
                fontSize = 20.sp
            )
        } else {
            Text(
                text = position.toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Chip de estadística compacto
 */
@Composable
private fun StatsChip(
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontSize = 9.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

