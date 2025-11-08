package es.itram.basketmatch.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import es.itram.basketmatch.analytics.AnalyticsManager

/**
 * EntryPoint para acceder a AnalyticsManager desde Composables
 * sin necesidad de un ViewModel
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface AnalyticsEntryPoint {
    fun analyticsManager(): AnalyticsManager
}

