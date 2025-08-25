package es.itram.basketmatch.presentation.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Indicador de carga mejorado con animaciones fluidas
 */
@Composable
fun EnhancedLoadingIndicator(
    modifier: Modifier = Modifier,
    message: String = "Cargando partidos...",
    showMessage: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    
    // Animación de rotación para el indicador principal
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                easing = LinearEasing
            )
        ), label = "rotation"
    )
    
    // Animación de pulsación para el brillo
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Indicador circular personalizado
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .rotate(rotationAngle),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 8.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    
                    // Círculo de fondo
                    drawCircle(
                        color = Color.Gray.copy(alpha = 0.2f),
                        radius = radius,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    
                    // Arco de progreso con gradiente
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Blue.copy(alpha = pulseAlpha),
                                Color.Cyan.copy(alpha = pulseAlpha),
                                Color.Blue.copy(alpha = pulseAlpha),
                                Color.Transparent
                            )
                        ),
                        startAngle = 0f,
                        sweepAngle = 120f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }
            
            if (showMessage) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = pulseAlpha)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Indicador de puntos animados
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(3) { index ->
                        val delay = index * 200
                        val dotAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(
                                    durationMillis = 600,
                                    delayMillis = delay,
                                    easing = FastOutSlowInEasing
                                ),
                                repeatMode = RepeatMode.Reverse
                            ), label = "dot$index"
                        )
                        
                        Canvas(modifier = Modifier.size(8.dp)) {
                            drawCircle(
                                color = Color.Blue.copy(alpha = dotAlpha),
                                radius = size.minDimension / 2
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Indicador de carga compacto para usar en espacios pequeños
 */
@Composable
fun CompactLoadingIndicator(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 32.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "compact_loading")
    
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            )
        ), label = "compact_rotation"
    )

    Box(
        modifier = modifier
            .size(size)
            .rotate(rotationAngle),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 3.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
