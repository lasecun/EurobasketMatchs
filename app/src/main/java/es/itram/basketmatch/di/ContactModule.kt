package es.itram.basketmatch.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import es.itram.basketmatch.data.repository.ContactRepositoryImpl
import es.itram.basketmatch.domain.repository.ContactRepository
import javax.inject.Singleton

/**
 * 📧 Contact Module
 *
 * Módulo de Hilt para la inyección de dependencias relacionadas con la funcionalidad de contacto.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ContactModule {

    @Binds
    @Singleton
    abstract fun bindContactRepository(
        contactRepositoryImpl: ContactRepositoryImpl
    ): ContactRepository
}
