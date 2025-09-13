package es.itram.basketmatch.domain.repository

import es.itram.basketmatch.data.model.ContactInfo

/**
 * 📧 Contact Repository Interface
 *
 * Define el contrato para obtener información de contacto siguiendo clean architecture.
 */
interface ContactRepository {
    fun getContactInfo(): ContactInfo
}
