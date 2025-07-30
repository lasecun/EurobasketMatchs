package es.itram.basketmatch.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Componente que muestra una tarjeta cuando no hay partidos en el día seleccionado (especialmente hoy)
 * con opción de navegar al próximo día con partidos
 */
@Composable
fun NoMatchesTodayCard(
    isToday: Boolean,
    nextMatchDay: LocalDate?,
    onNavigateToNextMatchDay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cara triste emoji
            Text(
                text = "😢",
                fontSize = 48.sp,
                textAlign = TextAlign.Center
            )
            
            // Mensaje principal
            Text(
                text = if (isToday) "Hoy no hay partido" else "No hay partidos para la fecha seleccionada",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Mensaje secundario si es para hoy
            if (isToday) {
                Text(
                    text = "No te preocupes, siempre hay más baloncesto por venir",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
            
            // Botón para ir al próximo día con partidos (solo si hay uno disponible)
            if (nextMatchDay != null) {
                Button(
                    onClick = onNavigateToNextMatchDay,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ver próximo partido",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = nextMatchDay.format(
                                DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("es"))
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}
