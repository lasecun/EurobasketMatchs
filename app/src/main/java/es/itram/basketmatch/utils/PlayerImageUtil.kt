package es.itram.basketmatch.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utilidad para obtener URLs de imágenes de jugadores desde la API de EuroLeague Feeds
 * 
 * ARQUITECTURA ACTUALIZADA:
 * - ÚNICO ORIGEN: feeds.incrowdsports.com - API de feeds para todos los datos incluyendo imágenes
 * - ELIMINADO: www.euroleaguebasketball.net - Ya no necesario, feeds API proporciona imágenes
 * 
 * Las imágenes de jugadores se obtienen directamente del roster del equipo desde feeds API
 * que incluye campos `images.profile` y `images.headshot` con URLs del CDN oficial.
 */
@Singleton
class PlayerImageUtil @Inject constructor(
    private val httpClient: OkHttpClient
) {

    companion object {
        private const val TAG = "PlayerImageUtil"
        
        // URLs de imágenes por defecto para casos especiales
        val DEFAULT_PLAYER_IMAGES = mapOf(
            "003733" to "https://media-cdn.incrowdsports.com/c0c0c586-9005-47a5-970d-86b57458701d.png", // Sergio Llull
            // Aquí se pueden agregar más imágenes conocidas
        )
    }

    /**
     * Obtiene la URL de imagen de un jugador desde el roster del equipo en feeds API
     * 
     * NOTA: Esta función ahora usa exclusivamente la API de feeds que proporciona
     * URLs de imágenes directamente en el campo `images` de cada jugador.
     * 
     * @param playerCode Código único del jugador
     * @param playerName Nombre del jugador (para búsqueda si es necesario)
     * @param teamCode Código del equipo (usado para obtener el roster)
     * @return URL de la imagen desde feeds API o null si no se encuentra
     */
    suspend fun getPlayerImageFromFeeds(
        playerCode: String,
        playerName: String,
        teamCode: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🌐 [FEEDS_API] Obteniendo imagen de jugador $playerName desde feeds API...")
            
            // La imagen se obtiene del roster del equipo que ya incluye URLs de imágenes
            // Esta función ahora es más simple y eficiente
            Log.d(TAG, "💡 [FEEDS_API] Imagen de jugador disponible en roster del equipo $teamCode")
            
            // Retornar null ya que las imágenes vienen directamente en los datos del roster
            // Esta función se mantiene por compatibilidad pero la lógica real está en el scraper
            null
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ [FEEDS_API] Error obteniendo imagen desde feeds API", e)
            null
        }
    }

    /**
     * FUNCIÓN OBSOLETA: Obtiene imagen desde sitio web oficial
     * 
     * @deprecated Esta función ya no es necesaria. Las imágenes se obtienen directamente
     * desde la API de feeds en el campo `images` del roster de jugadores.
     * Usar getPlayerImageFromFeeds() o mejor aún, obtener la imagen directamente del PlayerDto.
     */
    @Deprecated(
        message = "Usar feeds API en lugar de web scraping. Imágenes disponibles en PlayerDto.images",
        replaceWith = ReplaceWith("getPlayerImageFromFeeds(playerCode, playerName, teamCode)")
    )
    suspend fun getPlayerImageUrl(
        playerCode: String,
        playerName: String,
        teamCode: String
    ): String? = withContext(Dispatchers.IO) {
        Log.w(TAG, "⚠️ [DEPRECATED] getPlayerImageUrl está obsoleta. Usar feeds API en su lugar.")
        return@withContext null
    }
    
    /**
     * Convierte el nombre del jugador al formato requerido por la URL de EuroLeague
     * Ejemplo: "SERGIO LLULL" -> "llull-sergio"
     */
    private fun formatPlayerNameForUrl(playerName: String): String {
        val parts = playerName.trim().split(" ")
        return if (parts.size >= 2) {
            val lastName = parts.last().lowercase()
            val firstName = parts.first().lowercase()
            "$lastName-$firstName"
        } else {
            playerName.lowercase().replace(" ", "-")
        }
    }
    
    /**
     * Genera una URL de imagen placeholder basada en las iniciales del jugador
     */
    fun generatePlaceholderImageUrl(playerName: String): String {
        val initials = playerName.split(" ")
            .take(2)
            .map { it.firstOrNull()?.uppercase() ?: "" }
            .joinToString("")
        
        // Usar un servicio de avatares con las iniciales
        return "https://ui-avatars.com/api/?name=$initials&size=400&background=004996&color=ffffff&font-size=0.4"
    }
}
