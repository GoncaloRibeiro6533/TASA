package com.tasa.authentication.login

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.tasa.domain.ApiError
import com.tasa.domain.user.User
import com.tasa.repository.interfaces.UserRepositoryInterface
import com.tasa.ui.components.LOADING_VIEW_TAG
import com.tasa.ui.components.NavigateBackTestTag
import com.tasa.ui.screens.authentication.login.LOGIN_VIEW
import com.tasa.ui.screens.authentication.login.LoginScreen
import com.tasa.ui.screens.authentication.login.LoginScreenState
import com.tasa.ui.screens.authentication.login.LoginScreenViewModel
import com.tasa.utils.Either
import com.tasa.utils.StringResourceResolver
import com.tasa.utils.success
import org.junit.Rule
import org.junit.Test

class LoginScreenTests {
    @get:Rule
    val composeTree = createComposeRule()

    /*private val testUserInfo = User(1, "test", "test@example.com")
    private val fakeRepo = object : UserInfoRepository {
        override val userInfo: Flow<User?>
            get() = flow { emit(null) }
        override suspend fun getUserInfo(): User? { delay(1000); return testUserInfo }
        override suspend fun updateUserInfo(userInfo: User) { }
        override suspend fun clearUserInfo() { }
        override val lastActivity: Flow<Int?>
            get() = flow { emit(null) }

        override suspend fun writeLastActivity(activity: Int) {}
        override suspend fun getLastActivity(): Int? { return null}
        override val lastActivityTransition: Flow<Int?> = flow { emit(null) }
        override val notifiedOfNoLocation: Flow<Boolean>
            get() = flow { emit(false) }
        override suspend fun setNotifiedOfNoLocation(notified: Boolean) {}
        override val locationStatus: Flow<Boolean>
            get() = flow { emit(false) }
        override suspend fun setLocationStatus(enabled: Boolean) {}

        override suspend fun getLocationStatus(): Boolean? {return null }
        override suspend fun getToken(): String? { return null}
        override suspend fun setToken(token: String) {}
        override suspend fun saveRefreshToken(token: String) {}
        override suspend fun getRefreshToken(): String? {return null}
        override suspend fun getSessionExpiration(): LocalDateTime? {return null}
        override suspend fun setSessionExpiration(expiration: LocalDateTime) {}
        override suspend fun setLocal(local: Boolean) {}
        override suspend fun isLocal(): Boolean {return false}
    }*/

    private val testUserInfo = User(1, "test", "test@example.com")
    private val fakeRepo =
        object : UserRepositoryInterface {
            override suspend fun createUser(
                username: String,
                email: String,
                password: String,
            ): Either<ApiError, User> {
                return success(testUserInfo)
            }

            override suspend fun clear() {}

            override suspend fun createToken(
                email: String,
                password: String,
            ): Either<ApiError, User> {
                return success(testUserInfo)
            }

            override suspend fun logout(): Either<ApiError, Unit> {
                return success(Unit)
            }

            override suspend fun refreshSession(): Either<ApiError, String> {
                return success("test")
            }
        }

    private fun createFakeViewModel(screenState: LoginScreenState): LoginScreenViewModel =
        LoginScreenViewModel(
            userRepository = fakeRepo,
            initialState = screenState,
        )

    @Test
    fun when_Initialized_the_Login_view_is_shown() {
        composeTree.setContent {
            LoginScreen(
                viewModel = createFakeViewModel(screenState = LoginScreenState.Idle),
                onLoginSuccess = {},
                onNavigationBack = {},
                onRegisterRequested = {},
            )
        }
        composeTree.onNodeWithTag(LOGIN_VIEW).assertExists()
    }

    @Test
    fun when_Loading_the_Loading_view_is_shown() {
        composeTree.setContent {
            LoginScreen(
                viewModel = createFakeViewModel(screenState = LoginScreenState.Loading),
                onLoginSuccess = {},
                onNavigationBack = {},
                onRegisterRequested = {},
            )
        }
        composeTree.onNodeWithTag(LOADING_VIEW_TAG).assertExists()
    }

    @Test
    fun when_successful_the_LoginScreen_is_not_shown() {
        composeTree.setContent {
            LoginScreen(
                viewModel = createFakeViewModel(screenState = LoginScreenState.Success(testUserInfo)),
                onLoginSuccess = {},
                onNavigationBack = {},
                onRegisterRequested = {},
            )
        }
        composeTree.onNodeWithTag(LOGIN_VIEW).assertDoesNotExist()
    }

    @Test
    fun when_back_pressed_calls_onBackPressed() {
        var backPressed = false
        composeTree.setContent {
            LoginScreen(
                viewModel = createFakeViewModel(screenState = LoginScreenState.Idle),
                onLoginSuccess = {},
                onNavigationBack = { backPressed = true },
                onRegisterRequested = {},
            )
        }
        composeTree.onNodeWithTag(NavigateBackTestTag).performClick()
        assert(backPressed) { "onBackPressed was not called" }
    }
}
