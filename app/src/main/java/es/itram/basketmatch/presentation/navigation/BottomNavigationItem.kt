package es.itram.basketmatch.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 🧭 Bottom Navigation Items
 *
 * Define los elementos de la barra de navegación inferior.
 * Cada item tiene un icono seleccionado y no seleccionado para mejor feedback visual.
 *
 * Iconos usados (disponibles en material-icons-core):
 * - Home: Pantalla principal de partidos
 * - List: Clasificación/tabla (AutoMirrored para RTL)
 * - Star/StarOutline: Favoritos
 * - MoreVert: Más opciones
 */
sealed class BottomNavigationItem(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
) {
    /**
     * 🏀 Partidos - Pantalla principal con los partidos del día
     */
    object Matches : BottomNavigationItem(
        route = NavigationRoutes.MAIN,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        label = "Partidos"
    )

    /**
     * 📊 Clasificación - Tabla de clasificación de la liga
     */
    object Standings : BottomNavigationItem(
        route = NavigationRoutes.STANDINGS,
        selectedIcon = Icons.AutoMirrored.Filled.List,
        unselectedIcon = Icons.AutoMirrored.Outlined.List,
        label = "Clasificación"
    )

    /**
     * ⭐ Favoritos - Equipos marcados como favoritos
     */
    object Favorites : BottomNavigationItem(
        route = NavigationRoutes.FAVORITES,
        selectedIcon = Icons.Filled.Star,
        unselectedIcon = Icons.Filled.Star, // NavigationBarItem manejará la diferenciación visual
        label = "Favoritos"
    )

    /**
     * ⚙️ Más - Configuraciones y opciones adicionales
     */
    object More : BottomNavigationItem(
        route = NavigationRoutes.SETTINGS,
        selectedIcon = Icons.Filled.MoreVert,
        unselectedIcon = Icons.Outlined.MoreVert,
        label = "Más"
    )

    companion object {
        /**
         * Lista de todos los items del bottom navigation
         */
        val items = listOf(
            Matches,
            Standings,
            Favorites,
            More
        )
    }
}

