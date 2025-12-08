package es.itram.basketmatch.domain.usecase

import es.itram.basketmatch.data.generator.GenerationResult
import es.itram.basketmatch.data.generator.StaticDataGenerator
import es.itram.basketmatch.data.sync.SmartSyncManager
import es.itram.basketmatch.data.sync.UpdateCheckResult
import javax.inject.Inject

/**
 * Use case para gestión de datos estáticos y sincronización inteligente
 * 
 * Estrategia híbrida:
 * 1. Usa datos estáticos por defecto (carga rápida)
 * 2. Genera datos estáticos desde API real cuando es necesario
 * 3. Actualiza datos estáticos solo con refresh manual
 */
class ManageStaticDataUseCase @Inject constructor(
    private val smartSyncManager: SmartSyncManager,
    private val staticDataGenerator: StaticDataGenerator
) {
    
    /**
     * Estado de sincronización observable
     */
    val syncState = smartSyncManager.syncState
    
    /**
     * Última sincronización
     */
    val lastSyncTime = smartSyncManager.lastSyncTime
    
    /**
     * Actualiza datos estáticos desde API (refresh manual)
     */
    suspend fun refreshStaticDataFromApi(): Result<GenerationResult> {
        return try {
            // 1. Generar nuevos datos desde API
            val generationResult = staticDataGenerator.generateAllStaticData()
            
            if (generationResult.isSuccess) {
                // 2. Recargar datos estáticos en memoria
                smartSyncManager.reloadStaticData()
                generationResult
            } else {
                generationResult
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Verificar si hay actualizaciones disponibles
     */
    suspend fun checkForUpdates(): Result<UpdateCheckResult> {
        return smartSyncManager.checkForUpdates()
    }
    
    /**
     * Verificar si hay sincronización en progreso
     */
    fun isSyncInProgress(): Boolean {
        return syncState.value.isActive
    }
}
