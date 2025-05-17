package com.tasa.storage.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tasa.storage.entities.ContactExceptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactExceptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContactException(contactException: ContactExceptionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContactExceptions(contactExceptions: List<ContactExceptionEntity>)

    @Query("SELECT * FROM contact_exception WHERE id = :id")
    fun getContactExceptionById(id: Int): Flow<ContactExceptionEntity?>

    @Query("SELECT * FROM contact_exception WHERE name = :name and phoneNumber = :phoneNumber")
    fun getContactExceptionByNameAndPhoneNumber(
        name: String,
        phoneNumber: String,
    ): Flow<ContactExceptionEntity?>

    @Query("SELECT * FROM contact_exception")
    fun getAllContactExceptions(): Flow<List<ContactExceptionEntity>>

    @Query("DELETE FROM contact_exception WHERE id = :id")
    suspend fun deleteContactExceptionById(id: Int)

    @Query("DELETE FROM contact_exception WHERE name = :name and phoneNumber = :phoneNumber")
    suspend fun deleteContactExceptionByNameAndPhoneNumber(
        name: String,
        phoneNumber: String,
    )

    @Query("DELETE FROM contact_exception")
    suspend fun clear()
}
