package com.tasa.authentication.register

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.tasa.domain.ApiError
import com.tasa.domain.user.User
import com.tasa.repository.interfaces.UserRepositoryInterface
import com.tasa.ui.components.ERROR_ALERT
import com.tasa.ui.components.LOADING_VIEW_TAG
import com.tasa.ui.screens.authentication.register.REGISTER_VIEW
import com.tasa.ui.screens.authentication.register.RegisterScreen
import com.tasa.ui.screens.authentication.register.RegisterScreenState
import com.tasa.ui.screens.authentication.register.RegisterScreenViewModel
import com.tasa.ui.screens.authentication.register.SUCCESS_TAG
import com.tasa.utils.Either
import com.tasa.utils.success
import org.junit.Rule
import org.junit.Test

class RegisterScreenTests {
    @get:Rule
    val composeTree = createComposeRule()

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

    private fun createFakeViewModel(screenState: RegisterScreenState): RegisterScreenViewModel =
        RegisterScreenViewModel(
            userRepository = fakeRepo,
            initialState = screenState,
        )

    @Test
    fun when_Initialized_the_Register_view_is_shown() {
        composeTree.setContent {
            RegisterScreen(
                viewModel = createFakeViewModel(screenState = RegisterScreenState.Idle),
                onNavigationBack = {},
                onSubmit = { _, _, _ -> },
                onRegisterSuccessful = {},
            )
        }
        composeTree.onNodeWithTag(REGISTER_VIEW).assertExists()
    }

    @Test
    fun when_Loading_the_Loading_view_is_shown() {
        composeTree.setContent {
            RegisterScreen(
                viewModel = createFakeViewModel(screenState = RegisterScreenState.Loading),
                onNavigationBack = {},
                onSubmit = { _, _, _ -> },
                onRegisterSuccessful = {},
            )
        }
        composeTree.onNodeWithTag(LOADING_VIEW_TAG).assertExists()
    }

    @Test
    fun when_Success_the_successs_view_is_shown() {
        composeTree.setContent {
            RegisterScreen(
                viewModel = createFakeViewModel(screenState = RegisterScreenState.Success(testUserInfo)),
                onNavigationBack = {},
                onSubmit = { _, _, _ -> },
                onRegisterSuccessful = {},
            )
        }
        composeTree.onNodeWithTag(SUCCESS_TAG).assertExists()
    }

    @Test
    fun when_Error_the_error_view_is_shown() {
        composeTree.setContent {
            RegisterScreen(
                viewModel = createFakeViewModel(screenState = RegisterScreenState.Error(ApiError("Test Error"))),
                onNavigationBack = {},
                onSubmit = { _, _, _ -> },
                onRegisterSuccessful = {},
            )
        }
        composeTree.onNodeWithTag(LOADING_VIEW_TAG).assertDoesNotExist()
        composeTree.onNodeWithTag(ERROR_ALERT).assertExists()
    }
}
