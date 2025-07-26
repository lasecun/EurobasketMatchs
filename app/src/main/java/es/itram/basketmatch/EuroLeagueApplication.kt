package es.itram.basketmatch

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import es.itram.basketmatch.data.datasource.local.seed.DatabaseSeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Clase Application de la aplicación EuroLeague 2026
 * Configurada con Hilt para inyección de dependencias
 */
@HiltAndroidApp
class EuroLeagueApplication : Application() {
    
    @Inject
    lateinit var databaseSeeder: DatabaseSeeder
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d("EuroLeagueApp", "Iniciando aplicación...")
        
        // Initialize database with sample data
        applicationScope.launch {
            try {
                Log.d("EuroLeagueApp", "Iniciando poblado de base de datos...")
                databaseSeeder.seedDatabase()
                Log.d("EuroLeagueApp", "Base de datos poblada exitosamente")
            } catch (e: Exception) {
                Log.e("EuroLeagueApp", "Error poblando base de datos: ${e.message}", e)
            }
        }
    }
}
