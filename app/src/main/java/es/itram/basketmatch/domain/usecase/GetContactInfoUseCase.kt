package es.itram.basketmatch.domain.usecase

import es.itram.basketmatch.data.model.ContactInfo
import es.itram.basketmatch.domain.repository.ContactRepository
import javax.inject.Inject

/**
 * 📧 Get Contact Info Use Case
 *
 * Caso de uso para obtener la información de contacto de la aplicación.
 * Encapsula la lógica de negocio para obtener datos de contacto.
 */
class GetContactInfoUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {

    operator fun invoke(): ContactInfo {
        return contactRepository.getContactInfo()
    }
}
