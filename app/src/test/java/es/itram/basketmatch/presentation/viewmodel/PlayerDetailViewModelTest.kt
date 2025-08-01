package es.itram.basketmatch.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import es.itram.basketmatch.analytics.AnalyticsManager
import es.itram.basketmatch.domain.model.Player
import es.itram.basketmatch.domain.model.PlayerPosition
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tests para PlayerDetailViewModel - Cobertura de analytics tracking
 */
@ExperimentalCoroutinesApi
class PlayerDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @MockK
    private lateinit var analyticsManager: AnalyticsManager

    private lateinit var viewModel: PlayerDetailViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(Dispatchers.Unconfined)
        
        justRun { analyticsManager.trackScreenView(any(), any()) }
        justRun { analyticsManager.trackPlayerViewed(any(), any(), any()) }
        
        viewModel = PlayerDetailViewModel(analyticsManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `trackScreenView calls analytics manager with correct parameters`() {
        // When
        viewModel.trackScreenView()

        // Then
        verify {
            analyticsManager.trackScreenView(
                AnalyticsManager.SCREEN_PLAYER_DETAIL,
                "PlayerDetailScreen"
            )
        }
    }

    @Test
    fun `trackPlayerViewed calls analytics manager with player data`() {
        // Given
        val player = Player(
            code = "P001234",
            name = "Luka",
            surname = "Doncic",
            fullName = "Luka Doncic",
            jersey = 7,
            position = PlayerPosition.POINT_GUARD,
            height = "201cm",
            weight = "104kg",
            dateOfBirth = "1999-02-28",
            placeOfBirth = "Ljubljana, Slovenia",
            nationality = "Slovenia",
            experience = 5,
            profileImageUrl = null,
            isActive = true,
            isStarter = false,
            isCaptain = false
        )
        val teamCode = "MAD"

        // When
        viewModel.trackPlayerViewed(player, teamCode)

        // Then
        verify {
            analyticsManager.trackPlayerViewed(
                playerCode = "P001234",
                playerName = "Luka Doncic",
                teamCode = "MAD"
            )
        }
    }

    @Test
    fun `trackPlayerViewed with default teamCode uses unknown`() {
        // Given
        val player = Player(
            code = "P001234",
            name = "Luka",
            surname = "Doncic",
            fullName = "Luka Doncic",
            jersey = 7,
            position = PlayerPosition.POINT_GUARD,
            height = "201cm",
            weight = "104kg",
            dateOfBirth = "1999-02-28",
            placeOfBirth = "Ljubljana, Slovenia",
            nationality = "Slovenia",
            experience = 5,
            profileImageUrl = null,
            isActive = true,
            isStarter = false,
            isCaptain = false
        )

        // When
        viewModel.trackPlayerViewed(player)

        // Then
        verify {
            analyticsManager.trackPlayerViewed(
                playerCode = "P001234",
                playerName = "Luka Doncic",
                teamCode = "unknown"
            )
        }
    }

    @Test
    fun `verify analytics methods are called only when explicitly invoked`() {
        // Given - fresh viewModel instance
        val freshViewModel = PlayerDetailViewModel(analyticsManager)

        // When - just creating the viewModel without calling methods
        // (no additional method calls)

        // Then - analytics should not be called automatically
        verify(exactly = 0) { analyticsManager.trackScreenView(any(), any()) }
        verify(exactly = 0) { analyticsManager.trackPlayerViewed(any(), any(), any()) }
    }
}
