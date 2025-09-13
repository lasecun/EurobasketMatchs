package es.itram.basketmatch.data.model

/**
 * 📧 Contact Information Data Model
 *
 * Representa la información de contacto de la aplicación siguiendo el patrón de datos inmutables.
 */
data class ContactInfo(
    val email: String,
    val githubIssuesUrl: String,
    val appName: String,
    val version: String,
    val description: String
) {
    companion object {
        fun getDefaultContactInfo(): ContactInfo {
            return ContactInfo(
                email = "itramgames@gmail.com",
                githubIssuesUrl = "https://github.com/lasecun/EurobasketMatchs/issues",
                appName = "BasketMatch",
                version = "1.1",
                description = "Aplicación oficial para seguir la EuroLeague Basketball"
            )
        }
    }
}
