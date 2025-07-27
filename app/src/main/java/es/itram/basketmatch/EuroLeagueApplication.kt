package es.itram.basketmatch

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase Application de la aplicación EuroLeague 2026
 * Configurada con Hilt para inyección de dependencias
 */
@HiltAndroidApp
class EuroLeagueApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d("EuroLeagueApp", "🚀 Iniciando aplicación EuroLeague...")
        
        // NOTA: El seeding de datos ahora se maneja a través de DataSyncService 
        // en MainViewModel para mejor control y progreso en tiempo real
        Log.d("EuroLeagueApp", "✅ Aplicación iniciada - datos se cargarán vía DataSyncService")
    }
}
