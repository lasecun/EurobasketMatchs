package ch.biketec.t.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.biketec.t.domain.entity.Team

@Composable
fun TeamComponent(
    team: Team,
    score: Int? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAway: Boolean = false
) {
    Surface(
        modifier = modifier
            .clip(CircleShape)
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Column(
            horizontalAlignment = if (isAway) Alignment.End else Alignment.Start,
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (isAway) Arrangement.End else Arrangement.Start
            ) {
                if (!isAway) {
                    TeamLogo(team = team)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                Column(
                    horizontalAlignment = if (isAway) Alignment.End else Alignment.Start
                ) {
                    Text(
                        text = team.shortName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = if (isAway) TextAlign.End else TextAlign.Start
                    )
                    Text(
                        text = team.city,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = if (isAway) TextAlign.End else TextAlign.Start
                    )
                }
                
                if (isAway) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TeamLogo(team = team)
                }
            }
            
            if (team.isFavorite) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = if (isAway) Arrangement.End else Arrangement.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Equipo favorito",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamLogo(team: Team) {
    // Placeholder for team logo - in a real app you'd load from team.logoUrl
    Surface(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = try {
            Color(android.graphics.Color.parseColor(team.primaryColor))
        } catch (e: Exception) {
            MaterialTheme.colorScheme.primary
        }
    ) {
        // For now, show the first letter of the team short name
        // In a real app, you'd use an image loading library like Coil
        Text(
            text = team.shortName.firstOrNull()?.toString() ?: "?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp)
        )
    }
}
