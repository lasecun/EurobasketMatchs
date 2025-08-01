package es.itram.basketmatch.di

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 🔥 Firebase Module - Configuración de dependencias Firebase
 * 
 * Proporciona instancias singleton de:
 * - Firebase Analytics para tracking de eventos y user behavior
 * - Firebase Crashlytics para error reporting y stability monitoring
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    
    @Provides
    @Singleton
    fun provideFirebaseAnalytics(): FirebaseAnalytics {
        return Firebase.analytics.apply {
            // Configuraciones adicionales si es necesario
            setAnalyticsCollectionEnabled(true)
        }
    }
    
    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics {
        return Firebase.crashlytics.apply {
            // Configurar Crashlytics
            setCrashlyticsCollectionEnabled(true)
        }
    }
}
