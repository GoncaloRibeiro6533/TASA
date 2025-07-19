package com.tasa.start

import com.tasa.domain.UserInfoRepository
import com.tasa.domain.user.User
import com.tasa.ui.screens.start.StartScreenState
import com.tasa.ui.screens.start.StartScreenViewModel
import com.tasa.utils.ReplaceMainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class StartScreenViewModelTests {
    @get:Rule
    val replaceMainDispatcherRule = ReplaceMainDispatcherRule()

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
                return "token"
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

    /*
    @Test
    fun initial_state_is_Initialized() {
        val sut =
            StartScreenViewModel(
                repo = fakeRepo,
            )
        val state = sut.state.value
        assert(state is StartScreenState.Idle)
    }

    @Test
    fun load_user_info_updates_state() {
        val sut =
            StartScreenViewModel(
                repo = fakeRepo,
            )
        sut.getSession()
        val state = sut.state.value
        assert(state is StartScreenState.Idle)
    }

    @Test
    fun load_user_info_successful_updates_state_to_Success() {
        runTest(replaceMainDispatcherRule.testDispatcher) {
            val sut =
                StartScreenViewModel(
                    repo = fakeRepo,
                )
            sut.getSession()?.join()
            val state = sut.state.value
            assert(state is StartScreenState.Logged)
        }
    }

    @Test
    fun load_user_info_failure_updates_state_to_Error() {
        runTest(replaceMainDispatcherRule.testDispatcher) {
            val sut =
                StartScreenViewModel(
                    repo =
                        object : UserInfoRepository {
                            override val userInfo: Flow<User?>
                                get() = flow { emit(null) }

                            override suspend fun getUserInfo(): User? {
                                throw Exception("Error")
                            }

                            override suspend fun updateUserInfo(userInfo: User) {}

                            override suspend fun clearUserInfo() {}

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
                        },
                )
            sut.getSession()?.join()
            val state = sut.state.value
            assert(state is StartScreenState.NotLogged)
        }
    }

    @Test
    fun load_user_info_local_transitions_to_logged() {
        runTest(replaceMainDispatcherRule.testDispatcher) {
            val sut =
                StartScreenViewModel(
                    repo =
                        object : UserInfoRepository {
                            override val userInfo: Flow<User?>
                                get() = flow { emit(null) }

                            override suspend fun getUserInfo(): User? {
                                return null
                            }

                            override suspend fun updateUserInfo(userInfo: User) {}

                            override suspend fun clearUserInfo() {}

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
                                return true
                            }
                        },
                )
            sut.getSession()?.join()
            val state = sut.state.value
            assert(state is StartScreenState.Logged)
        }
    }

    @Test
    fun load_when_not_local_transitions_to_not_logged() {
        runTest(replaceMainDispatcherRule.testDispatcher) {
            val sut =
                StartScreenViewModel(
                    repo =
                        object : UserInfoRepository {
                            override val userInfo: Flow<User?>
                                get() = flow { emit(null) }

                            override suspend fun getUserInfo(): User? {
                                return null
                            }

                            override suspend fun updateUserInfo(userInfo: User) {}

                            override suspend fun clearUserInfo() {}

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
                        },
                )
            sut.getSession()?.join()
            val state = sut.state.value
            assert(state is StartScreenState.NotLogged)
        }
    }

     */
}
