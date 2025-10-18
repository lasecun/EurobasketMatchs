package es.itram.basketmatch.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import es.itram.basketmatch.domain.entity.Match
import java.time.format.DateTimeFormatter

/**
 * 🏀 Componente para mostrar el resultado de un partido de Euroliga
 * ACTUALIZADO: Ahora es clickeable para navegar al detalle del partido y muestra estados claros
 */
@Composable
fun MatchResultCard(
    match: Match,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else Modifier
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con fecha y estado del partido
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Estado del partido con colores distintivos
                val (statusText, statusColor, statusBackgroundColor) = when (match.status) {
                    es.itram.basketmatch.domain.entity.MatchStatus.FINISHED -> Triple(
                        "FINALIZADO",
                        MaterialTheme.colorScheme.onPrimary,
                        MaterialTheme.colorScheme.primary
                    )
                    es.itram.basketmatch.domain.entity.MatchStatus.LIVE -> Triple(
                        "EN VIVO",
                        MaterialTheme.colorScheme.onError,
                        MaterialTheme.colorScheme.error
                    )
                    es.itram.basketmatch.domain.entity.MatchStatus.SCHEDULED -> Triple(
                        "PROGRAMADO",
                        MaterialTheme.colorScheme.onSecondary,
                        MaterialTheme.colorScheme.secondary
                    )
                    else -> Triple(
                        "PROGRAMADO",
                        MaterialTheme.colorScheme.onSecondary,
                        MaterialTheme.colorScheme.secondary
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = statusBackgroundColor),
                    modifier = Modifier.padding(0.dp)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Equipos y resultado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Equipo local
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = match.homeTeamName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Local",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Resultado o VS - MEJORADO: Centrado verticalmente
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .width(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (match.status == es.itram.basketmatch.domain.entity.MatchStatus.FINISHED &&
                        match.homeScore != null && match.awayScore != null) {
                        // Mostrar resultado final - CENTRADO
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = match.homeScore.toString(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (match.homeScore > match.awayScore) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = " - ",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(horizontal = 4.dp),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = match.awayScore.toString(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (match.awayScore > match.homeScore) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // Mostrar VS para partidos programados - CENTRADO
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "VS",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            if (match.status == es.itram.basketmatch.domain.entity.MatchStatus.SCHEDULED) {
                                Text(
                                    text = match.dateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Equipo visitante
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = match.awayTeamName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = "Visitante",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End
                    )
                }
            }

            // Información adicional
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📍 ${match.venue}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Jornada ${match.round}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Indicador visual de que es clickeable
            if (onClick != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "👆 Toca para ver detalles",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
