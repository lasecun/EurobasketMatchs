package es.itram.basketmatch.domain.usecase

import es.itram.basketmatch.data.generator.GenerationResult
import es.itram.basketmatch.data.generator.StaticDataGenerator
import es.itram.basketmatch.data.generator.StaticDataInfo
import es.itram.basketmatch.data.sync.SmartSyncManager
import es.itram.basketmatch.data.sync.UpdateCheckResult
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime
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
     * Inicializar datos estáticos (primera vez o cuando no existen)
     */
    suspend fun initializeStaticData(): Result<Unit> {
        return try {
            // Verificar si ya existen datos estáticos
            if (staticDataGenerator.hasStaticData()) {
                // Usar datos estáticos existentes
                smartSyncManager.initializeStaticData()
            } else {
                // Generar datos estáticos desde API por primera vez
                generateStaticDataFromApi()
                // Luego cargar los datos generados
                smartSyncManager.initializeStaticData()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Genera datos estáticos desde la API real
     */
    suspend fun generateStaticDataFromApi(): Result<GenerationResult> {
        return staticDataGenerator.generateAllStaticData()
    }
    
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
     * Sincronización manual (regenera datos estáticos desde API)
     */
    suspend fun syncDynamicData(forceSync: Boolean = false): Result<Unit> {
        return if (forceSync) {
            // Con forceSync = true, regenerar desde API
            val result = refreshStaticDataFromApi()
            if (result.isSuccess) Result.success(Unit) else Result.failure(result.exceptionOrNull()!!)
        } else {
            // Sin force, usar método normal
            smartSyncManager.syncDynamicData(forceSync)
        }
    }
    
    /**
     * Verificar si hay actualizaciones disponibles
     */
    suspend fun checkForUpdates(): Result<UpdateCheckResult> {
        return smartSyncManager.checkForUpdates()
    }
    
    /**
     * Obtener información sobre datos estáticos
     */
    suspend fun getStaticDataInfo(): StaticDataInfo {
        return staticDataGenerator.getStaticDataInfo()
    }
    
    /**
     * Verificar si hay sincronización en progreso
     */
    fun isSyncInProgress(): Boolean {
        return syncState.value.isActive
    }
}
