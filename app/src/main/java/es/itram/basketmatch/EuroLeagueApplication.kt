package es.itram.basketmatch

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EuroLeagueApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("EuroLeagueApp", "🚀 Iniciando aplicación EuroLeague...")
    }
}
