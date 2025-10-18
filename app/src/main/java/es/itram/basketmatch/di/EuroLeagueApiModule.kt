package es.itram.basketmatch.di

import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import es.itram.basketmatch.data.datasource.remote.dto.api.GameApiDto
import es.itram.basketmatch.data.datasource.remote.dto.api.RoundDto
import es.itram.basketmatch.data.network.EuroLeagueApiService
import es.itram.basketmatch.data.network.GameApiDtoAdapter
import es.itram.basketmatch.data.network.RoundDtoAdapter
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * 🏀 Módulo de Hilt para la API oficial de EuroLeague
 *
 * Configura Retrofit y OkHttpClient para consumir la API oficial
 * https://api-live.euroleague.net/
 */
@Module
@InstallIn(SingletonComponent::class)
object EuroLeagueApiModule {

    private const val EUROLEAGUE_API_BASE_URL = "https://api-live.euroleague.net/"
    private const val TIMEOUT_SECONDS = 30L

    /**
     * Proporciona el cliente HTTP configurado para la API oficial de EuroLeague
     */
    @Provides
    @Singleton
    @Named("EuroLeagueOfficialClient")
    fun provideEuroLeagueOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("User-Agent", "BasketMatch-Android/1.2")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    /**
     * Proporciona la instancia de Retrofit configurada para la API oficial
     */
    @Provides
    @Singleton
    @Named("EuroLeagueOfficialRetrofit")
    fun provideEuroLeagueRetrofit(
        @Named("EuroLeagueOfficialClient") okHttpClient: OkHttpClient
    ): Retrofit {
        // Configurar Gson con adaptadores personalizados
        val gson = GsonBuilder()
            .registerTypeAdapter(RoundDto::class.java, RoundDtoAdapter())
            .registerTypeAdapter(GameApiDto::class.java, GameApiDtoAdapter())
            .create()

        return Retrofit.Builder()
            .baseUrl(EUROLEAGUE_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Proporciona el servicio de la API oficial de EuroLeague
     */
    @Provides
    @Singleton
    fun provideEuroLeagueApiService(
        @Named("EuroLeagueOfficialRetrofit") retrofit: Retrofit
    ): EuroLeagueApiService {
        return retrofit.create(EuroLeagueApiService::class.java)
    }

    /**
     * Proporciona configuración JSON para serialización
     */
    @Provides
    @Singleton
    @Named("EuroLeagueJson")
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = true
        }
    }
}
