package es.itram.basketmatch.di

import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import es.itram.basketmatch.data.datasource.remote.dto.api.RoundDto
import es.itram.basketmatch.data.network.EuroLeagueApiService
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
            // Nivel BODY para ver el JSON completo y verificar la estructura de images
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Interceptor personalizado para loggear images específicamente
        val imagesDebugInterceptor = okhttp3.Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)

            // Solo loggear para el endpoint de games
            if (request.url.encodedPath.contains("/games")) {
                val responseBody = response.body
                val source = responseBody?.source()
                source?.request(Long.MAX_VALUE)
                val buffer = source?.buffer
                val responseBodyString = buffer?.clone()?.readString(Charsets.UTF_8)

                // Buscar "images" y "crest" en el JSON para confirmar la estructura
                if (responseBodyString?.contains("\"images\"") == true) {
                    android.util.Log.d("ImagesDebug", "✅ La API SÍ devuelve 'images' en el JSON")
                    // Verificar si tiene el campo 'crest'
                    if (responseBodyString.contains("\"crest\"")) {
                        android.util.Log.d("ImagesDebug", "✅ La API SÍ devuelve 'crest' dentro de images")
                        // Mostrar un fragmento del JSON con images.crest
                        val crestIndex = responseBodyString.indexOf("\"crest\"")
                        val fragment = responseBodyString.substring(
                            maxOf(0, crestIndex - 100),
                            minOf(responseBodyString.length, crestIndex + 200)
                        )
                        android.util.Log.d("ImagesDebug", "Fragmento JSON: $fragment")
                    }
                } else {
                    android.util.Log.w("ImagesDebug", "❌ La API NO devuelve 'images' en el JSON")
                }
            }

            response
        }

        return OkHttpClient.Builder()
            .addInterceptor(imagesDebugInterceptor)
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
        // Configurar Gson con adaptador personalizado solo para RoundDto
        // IMPORTANTE: NO usar GameApiDtoAdapter porque impide que Gson use las anotaciones @SerializedName
        val gson = GsonBuilder()
            .registerTypeAdapter(RoundDto::class.java, RoundDtoAdapter())
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
