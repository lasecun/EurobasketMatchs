package ch.biketec.t

import android.app.Application
import ch.biketec.t.data.datasource.local.seed.DatabaseSeeder
import dagger.hilt.android.HiltAndroidApp
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
        
        // Initialize database with sample data
        applicationScope.launch {
            databaseSeeder.seedDatabase()
        }
    }
}
