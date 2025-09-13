package es.itram.basketmatch.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
class PlayerImageUtil @Inject constructor() {

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
     * @return URL de la imagen desde feeds API o null si no se encuentra
     */
    suspend fun getPlayerImageFromFeeds(): String? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🌐 [FEEDS_API] Obteniendo imagen de jugador desde feeds API...")

            // La imagen se obtiene del roster del equipo que ya incluye URLs de imágenes
            // Esta función ahora es más simple y eficiente
            Log.d(TAG, "💡 [FEEDS_API] Imagen de jugador disponible en roster del equipo")

            // Retornar null ya que las imágenes vienen directamente en los datos del roster
            // Esta función se mantiene por compatibilidad pero la lógica real está en el scraper
            null
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ [FEEDS_API] Error obteniendo imagen desde feeds API", e)
            null
        }
    }
}
