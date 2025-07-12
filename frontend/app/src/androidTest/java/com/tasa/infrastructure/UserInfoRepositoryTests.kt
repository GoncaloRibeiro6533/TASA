package com.tasa.infrastructure

import com.tasa.domain.user.User
import com.tasa.utils.CleanDataStoreRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class UserInfoRepositoryTests {
    @get:Rule
    val cleanDataStoreRule = CleanDataStoreRule()

    @Test
    fun getUserInfo_returns_null_when_no_userInfo_is_stored() =
        runTest {
            val sut = UserInfoRepo(cleanDataStoreRule.dataStore)
            val userInfo = sut.getUserInfo()
            assert(userInfo == null)
        }

    @Test
    fun updateUserInfo_stores_the_info() =
        runTest {
            val sut = UserInfoRepo(cleanDataStoreRule.dataStore)
            val expectedUserInfo = User(1, "Bob", "bob@mail.com")
            sut.updateUserInfo(expectedUserInfo)
            val storedNick = sut.getUserInfo()
            assert(storedNick == expectedUserInfo)
        }

    @Test
    fun userInfoFlow_emits_null_when_no_info_is_stored() =
        runTest {
            val sut = UserInfoRepo(cleanDataStoreRule.dataStore)
            val userInfo = sut.userInfo.first()
            assert(userInfo == null)
        }

    @Test
    fun userInfoFlow_emits_info_when_it_is_stored() =
        runTest {
            val sut = UserInfoRepo(cleanDataStoreRule.dataStore)
            val expectedUserInfo = User(1, "Alice", "alice@mail.com")
            sut.updateUserInfo(expectedUserInfo)
            val userInfo = sut.userInfo.first()
            assert(userInfo == expectedUserInfo)
        }

    @Test
    fun clearUserInfo_removes_the_info() =
        runTest {
            val sut = UserInfoRepo(cleanDataStoreRule.dataStore)
            val expectedUserInfo = User(1, "Charlie", "charlie@mail.com")
            sut.updateUserInfo(expectedUserInfo)
            sut.clearUserInfo()
            val userInfo = sut.userInfo.first()
            assert(userInfo == null)
        }
}
