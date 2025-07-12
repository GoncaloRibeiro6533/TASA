package com.tasa.authentication.register

import com.tasa.domain.ApiError
import com.tasa.domain.user.User
import com.tasa.repository.interfaces.UserRepositoryInterface
import com.tasa.ui.screens.authentication.register.RegisterScreenState
import com.tasa.ui.screens.authentication.register.RegisterScreenViewModel
import com.tasa.utils.Either
import com.tasa.utils.ReplaceMainDispatcherRule
import com.tasa.utils.failure
import com.tasa.utils.success
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class RegisterViewModelTests {
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
            RegisterScreenViewModel(
                userRepository = fakeRepo,
            )
        val state = sut.state.value
        assert(state is RegisterScreenState.Idle)
    }

    @Test
    fun register_success_updates_state() {
        val sut =
            RegisterScreenViewModel(
                userRepository = fakeRepo,
            )
        sut.registerUser(testUserInfo.username, "password", testUserInfo.email)
        val state = sut.state.value
        assert(state is RegisterScreenState.Loading)
    }

    @Test
    fun register_success_returns_success_state() {
        runTest(replaceMainDispatcherRule.testDispatcher) {
            val sut =
                RegisterScreenViewModel(
                    userRepository = fakeRepo,
                )
            sut.registerUser(testUserInfo.username, "password", testUserInfo.email)?.join()
            val state = sut.state.value
            assert(state is RegisterScreenState.Success)
        }
    }

    @Test
    fun register_error_returns_error_state() {
        runTest(replaceMainDispatcherRule.testDispatcher) {
            val sut =
                RegisterScreenViewModel(
                    userRepository =
                        object : UserRepositoryInterface {
                            override suspend fun createUser(
                                username: String,
                                email: String,
                                password: String,
                            ): Either<ApiError, User> {
                                return failure(ApiError("Registration failed"))
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
                        },
                )
            sut.registerUser(testUserInfo.username, "password", testUserInfo.email)?.join()
            val state = sut.state.value
            assert(state is RegisterScreenState.Error)
        }
    }
}
