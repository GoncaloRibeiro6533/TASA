package com.tasa.storage.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tasa.storage.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(vararg users: UserEntity)

    @Query("SELECT * FROM user WHERE id = :id")
    fun getUserById(id: Int): Flow<UserEntity>

    @Query("SELECT * FROM user WHERE username = :username")
    fun getUserByUsername(username: String): Flow<UserEntity>

    @Query("Update user SET username = :username WHERE id = :id")
    suspend fun updateUsername(
        id: Int,
        username: String,
    )

    @Query("SELECT * FROM user")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("DELETE FROM user")
    suspend fun clear()
}
