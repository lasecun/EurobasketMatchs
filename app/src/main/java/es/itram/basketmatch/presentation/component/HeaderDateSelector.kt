package es.itram.basketmatch.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.itram.basketmatch.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Header with date selector for navigating matches by date.
 * Versión mejorada con gradientes y mejor diseño visual
 */
@Composable
fun HeaderDateSelector(
    selectedDate: LocalDate?,
    onDateClick: () -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isToday = selectedDate == LocalDate.now()
    
    // Animación de color para resaltar cuando es hoy
    val cardBackgroundColor by animateColorAsState(
        targetValue = if (isToday) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            MaterialTheme.colorScheme.surface,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ), label = "cardBackground"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = if (isToday) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent
                            )
                        )
                    }
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousDay,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = if (isToday) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.previous_day),
                    modifier = Modifier.size(28.dp)
                )
            }

            OutlinedButton(
                onClick = onDateClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isToday) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.primary,
                    containerColor = if (isToday)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                    else
                        Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selectedDate?.let { date ->
                        when {
                            date == LocalDate.now() -> "Hoy"
                            date == LocalDate.now().minusDays(1) -> "Ayer"
                            date == LocalDate.now().plusDays(1) -> "Mañana"
                            else -> date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                        }
                    } ?: stringResource(R.string.select_date),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
                    )
                )
            }

            IconButton(
                onClick = onNextDay,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = if (isToday) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.next_day),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
