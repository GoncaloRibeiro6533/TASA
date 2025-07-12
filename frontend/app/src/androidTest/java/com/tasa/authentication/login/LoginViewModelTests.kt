package com.tasa.authentication.login

import com.tasa.domain.ApiError
import com.tasa.domain.user.User
import com.tasa.repository.interfaces.UserRepositoryInterface
import com.tasa.ui.screens.authentication.login.LoginScreenState
import com.tasa.ui.screens.authentication.login.LoginScreenViewModel
import com.tasa.utils.Either
import com.tasa.utils.ReplaceMainDispatcherRule
import com.tasa.utils.success
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTests {
    @get:Rule
    val replaceMainDispatcherRule = ReplaceMainDispatcherRule()

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

    @Test
    fun initial_state_is_Initialized() {
        val sut =
            LoginScreenViewModel(
                userRepository = fakeRepo,
            )
        val state = sut.state.value
        assert(state is LoginScreenState.Idle)
    }

    @Test
    fun login_success_updates_state() {
        val sut =
            LoginScreenViewModel(
                userRepository = fakeRepo,
            )
        sut.login(testUserInfo.username, "password")
        val state = sut.state.value
        assert(state is LoginScreenState.Loading)
    }

    @Test
    fun login_successful_updates_state_to_Success() {
        runTest(replaceMainDispatcherRule.testDispatcher) {
            val sut =
                LoginScreenViewModel(
                    userRepository = fakeRepo,
                )
            sut.login(testUserInfo.username, "password")?.join()
            val state = sut.state.value
            assert(state is LoginScreenState.Success)
        }
    }
}
