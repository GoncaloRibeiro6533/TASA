package com.tasa.storage.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tasa.storage.entities.AppExceptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppExceptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertException(exceptionApp: AppExceptionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExceptions(exceptionsApp: List<AppExceptionEntity>)

    @Query("SELECT * FROM app_exception")
    fun getAppExceptions(): Flow<List<AppExceptionEntity>>

    @Query("SELECT * FROM app_exception WHERE id = :id")
    fun getAppExceptionById(id: Int): Flow<AppExceptionEntity?>

    @Query("SELECT * FROM app_exception WHERE name = :name")
    fun getAppExceptionByName(name: String): Flow<AppExceptionEntity?>

    @Query("DELETE FROM app_exception WHERE id = :id")
    suspend fun deleteAppExceptionById(id: Int)

    @Query("DELETE FROM app_exception WHERE name = :name")
    suspend fun deleteAppExceptionByName(name: String)

    @Query("DELETE FROM app_exception")
    suspend fun clear()
}
