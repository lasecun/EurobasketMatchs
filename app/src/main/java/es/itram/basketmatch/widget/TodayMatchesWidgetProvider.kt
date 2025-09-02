package es.itram.basketmatch.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import dagger.hilt.android.EntryPointAccessors
import es.itram.basketmatch.MainActivity
import es.itram.basketmatch.R
import es.itram.basketmatch.di.RepositoryEntryPoint
import es.itram.basketmatch.domain.repository.MatchRepository
import es.itram.basketmatch.domain.repository.TeamRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Widget que muestra los partidos de hoy con énfasis en equipos favoritos
 */
class TodayMatchesWidgetProvider : AppWidgetProvider() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val TAG = "TodayMatchesWidget"
        private const val ACTION_UPDATE_WIDGET = "es.itram.basketmatch.widget.UPDATE_WIDGET"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "🔄 Actualizando widget - ${appWidgetIds.size} widgets")

        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_UPDATE_WIDGET -> {
                Log.d(TAG, "📡 Recibida acción de actualización manual")
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    android.content.ComponentName(context, TodayMatchesWidgetProvider::class.java)
                )
                onUpdate(context, appWidgetManager, appWidgetIds)
            }
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        Log.d(TAG, "🎯 Actualizando widget ID: $appWidgetId")

        coroutineScope.launch {
            try {
                // Obtener repositorios usando EntryPoint
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    RepositoryEntryPoint::class.java
                )
                val matchRepository = entryPoint.matchRepository()
                val teamRepository = entryPoint.teamRepository()

                // Obtener partidos de hoy
                val today = java.time.LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0)
                val todayMatches = matchRepository.getMatchesByDate(today).first()

                // Obtener equipos favoritos
                val favoriteTeams = teamRepository.getFavoriteTeams().first()
                val favoriteTeamCodes = favoriteTeams.map { it.code }.toSet()

                // Filtrar partidos con equipos favoritos
                val favoriteMatches = todayMatches.filter { match ->
                    favoriteTeamCodes.contains(match.homeTeamId) ||
                    favoriteTeamCodes.contains(match.awayTeamId)
                }

                Log.d(TAG, "📊 Encontrados ${todayMatches.size} partidos hoy, ${favoriteMatches.size} con equipos favoritos")

                // Crear las vistas del widget
                val views = createWidgetViews(context, todayMatches, favoriteMatches)

                // Actualizar el widget en el hilo principal
                launch(Dispatchers.Main) {
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                    Log.d(TAG, "✅ Widget actualizado exitosamente")
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error actualizando widget", e)

                // Mostrar vista de error
                val errorViews = createErrorViews(context)
                launch(Dispatchers.Main) {
                    appWidgetManager.updateAppWidget(appWidgetId, errorViews)
                }
            }
        }
    }

    private fun createWidgetViews(
        context: Context,
        allMatches: List<es.itram.basketmatch.domain.entity.Match>,
        favoriteMatches: List<es.itram.basketmatch.domain.entity.Match>
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_today_matches)

        // Configurar título con fecha actual
        val dateFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())
        val todayFormatted = dateFormatter.format(Date())
        views.setTextViewText(R.id.widget_title, "Partidos $todayFormatted")

        // Configurar contador de partidos
        val matchCountText = when {
            favoriteMatches.isNotEmpty() -> "${favoriteMatches.size} favoritos"
            allMatches.isNotEmpty() -> "${allMatches.size} partidos"
            else -> "Sin partidos"
        }
        views.setTextViewText(R.id.widget_match_count, matchCountText)

        // Mostrar próximo partido (priorizar favoritos)
        val nextMatch = favoriteMatches.firstOrNull() ?: allMatches.firstOrNull()

        if (nextMatch != null) {
            // Configurar equipos
            views.setTextViewText(R.id.widget_home_team, nextMatch.homeTeamName)
            views.setTextViewText(R.id.widget_away_team, nextMatch.awayTeamName)

            // Configurar hora
            val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            val matchDateTime = java.time.ZonedDateTime.of(nextMatch.dateTime, java.time.ZoneId.systemDefault())
            val matchTime = timeFormatter.format(Date.from(matchDateTime.toInstant()))
            views.setTextViewText(R.id.widget_match_time, matchTime)

            // Marcar si es favorito
            val isFavoriteMatch = favoriteMatches.contains(nextMatch)
            views.setViewVisibility(
                R.id.widget_favorite_indicator,
                if (isFavoriteMatch) android.view.View.VISIBLE else android.view.View.GONE
            )

            // Ocultar vista vacía
            views.setViewVisibility(R.id.widget_no_matches_container, android.view.View.GONE)
            views.setViewVisibility(R.id.widget_match_container, android.view.View.VISIBLE)

        } else {
            // No hay partidos
            views.setViewVisibility(R.id.widget_match_container, android.view.View.GONE)
            views.setViewVisibility(R.id.widget_no_matches_container, android.view.View.VISIBLE)
            views.setTextViewText(R.id.widget_no_matches_text, "No hay partidos hoy")
        }

        // Configurar click para abrir la app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

        // Configurar botón de actualización
        val updateIntent = Intent(context, TodayMatchesWidgetProvider::class.java).apply {
            action = ACTION_UPDATE_WIDGET
        }
        val updatePendingIntent = PendingIntent.getBroadcast(
            context, 0, updateIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_refresh_button, updatePendingIntent)

        return views
    }

    private fun createErrorViews(context: Context): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_today_matches)

        views.setTextViewText(R.id.widget_title, "EuroLeague")
        views.setTextViewText(R.id.widget_match_count, "Error")
        views.setViewVisibility(R.id.widget_match_container, android.view.View.GONE)
        views.setViewVisibility(R.id.widget_no_matches_container, android.view.View.VISIBLE)
        views.setTextViewText(R.id.widget_no_matches_text, "Error al cargar")

        return views
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        coroutineScope.cancel()
        Log.d(TAG, "🛑 Widget deshabilitado")
    }
}
