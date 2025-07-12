package com.tasa.start

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.tasa.domain.UserInfoRepository
import com.tasa.domain.user.User
import com.tasa.ui.components.LOADING_VIEW_TAG
import com.tasa.ui.screens.start.START_SCREEN
import com.tasa.ui.screens.start.START_VIEW
import com.tasa.ui.screens.start.StartScreen
import com.tasa.ui.screens.start.StartScreenState
import com.tasa.ui.screens.start.StartScreenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

class StartScreenTests {
    @get:Rule
    val composeTree = createComposeRule()

    private val testUserInfo = User(1, "test", "test@example.com")
    private val fakeRepo =
        object : UserInfoRepository {
            override val userInfo: Flow<User?>
                get() = flow { emit(null) }

            override suspend fun getUserInfo(): User? {
                delay(1000)
                return testUserInfo
            }

            override suspend fun updateUserInfo(userInfo: User) { }

            override suspend fun clearUserInfo() { }

            override val lastActivity: Flow<Int?>
                get() = flow { emit(null) }

            override suspend fun writeLastActivity(activity: Int) {}

            override suspend fun getLastActivity(): Int? {
                return null
            }

            override val lastActivityTransition: Flow<Int?> = flow { emit(null) }
            override val notifiedOfNoLocation: Flow<Boolean>
                get() = flow { emit(false) }

            override suspend fun setNotifiedOfNoLocation(notified: Boolean) {}

            override val locationStatus: Flow<Boolean>
                get() = flow { emit(false) }

            override suspend fun setLocationStatus(enabled: Boolean) {}

            override suspend fun getLocationStatus(): Boolean? {
                return null
            }

            override suspend fun getToken(): String? {
                return null
            }

            override suspend fun setToken(token: String) {}

            override suspend fun saveRefreshToken(token: String) {}

            override suspend fun getRefreshToken(): String? {
                return null
            }

            override suspend fun getSessionExpiration(): LocalDateTime? {
                return null
            }

            override suspend fun setSessionExpiration(expiration: LocalDateTime) {}

            override suspend fun setLocal(local: Boolean) {}

            override suspend fun isLocal(): Boolean {
                return false
            }
        }

    private fun createFakeViewModel(screenState: StartScreenState): StartScreenViewModel =
        StartScreenViewModel(
            repo = fakeRepo,
            initialState = screenState,
        )

    @Test
    fun when_Initialized_the_Start_view_is_shown() {
        composeTree.setContent {
            StartScreen(
                viewModel = createFakeViewModel(screenState = StartScreenState.Idle),
                onAboutRequested = {},
                onLoginRequested = { },
                onRegisterRequested = {},
                onLoggedIntent = {},
            )
        }
        composeTree.onNodeWithTag(START_SCREEN).assertExists()
        composeTree.onNodeWithTag(START_VIEW).assertExists()
    }

    @Test
    fun when_Loading_the_Loading_view_is_shown() {
        composeTree.setContent {
            StartScreen(
                viewModel = createFakeViewModel(screenState = StartScreenState.Saving),
                onAboutRequested = {},
                onLoginRequested = { },
                onRegisterRequested = {},
                onLoggedIntent = {},
            )
        }
        composeTree.onNodeWithTag(LOADING_VIEW_TAG).assertExists()
    }

    @Test
    fun when_not_logged_show_start_view() {
        composeTree.setContent {
            StartScreen(
                viewModel = createFakeViewModel(screenState = StartScreenState.NotLogged),
                onAboutRequested = {},
                onLoginRequested = { },
                onRegisterRequested = {},
                onLoggedIntent = {},
            )
        }
        composeTree.onNodeWithTag(START_VIEW).assertExists()
    }
}
