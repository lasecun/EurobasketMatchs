package es.itram.basketmatch.data.repository

import es.itram.basketmatch.data.model.ContactInfo
import es.itram.basketmatch.domain.repository.ContactRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 📧 Contact Repository Implementation
 *
 * Implementación del repositorio de contacto que proporciona información estática
 * de contacto de la aplicación.
 */
@Singleton
class ContactRepositoryImpl @Inject constructor() : ContactRepository {

    override fun getContactInfo(): ContactInfo {
        return ContactInfo.getDefaultContactInfo()
    }
}
