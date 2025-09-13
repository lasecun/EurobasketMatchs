package es.itram.basketmatch.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.itram.basketmatch.analytics.AnalyticsManager
import es.itram.basketmatch.data.model.ContactInfo
import es.itram.basketmatch.domain.usecase.GetContactInfoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 📧 Contact ViewModel
 *
 * ViewModel para la pantalla de contacto siguiendo MVVM y clean architecture.
 * Maneja el estado de la información de contacto y analytics.
 */
@HiltViewModel
class ContactViewModel @Inject constructor(
    private val getContactInfoUseCase: GetContactInfoUseCase,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _contactInfo = MutableStateFlow(ContactInfo.getDefaultContactInfo())
    val contactInfo: StateFlow<ContactInfo> = _contactInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadContactInfo()
        trackScreenView()
    }

    private fun loadContactInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val info = getContactInfoUseCase()
                _contactInfo.value = info
            } catch (e: Exception) {
                // En caso de error, mantener la info por defecto
                analyticsManager.trackError(
                    errorType = "contact_info_load_error",
                    errorMessage = e.message ?: "Unknown error loading contact info"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun trackScreenView() {
        analyticsManager.trackScreenView(
            screenName = "contact",
            screenClass = "ContactScreen"
        )
    }

    fun onEmailClicked() {
        analyticsManager.logCustomEvent("contact_email_clicked", android.os.Bundle().apply {
            putString("email", _contactInfo.value.email)
            putString("action", "open_email_client")
        })
    }

    fun onGithubIssuesClicked() {
        analyticsManager.logCustomEvent("contact_github_clicked", android.os.Bundle().apply {
            putString("url", _contactInfo.value.githubIssuesUrl)
            putString("action", "open_github_issues")
        })
    }

    fun onContactMethodUsed(method: String) {
        analyticsManager.logCustomEvent("contact_method_used", android.os.Bundle().apply {
            putString("method", method)
            putLong("timestamp", System.currentTimeMillis())
        })
    }
}
