package com.tasa.storage.entities

import androidx.room.Entity

@Entity(
    tableName = "contact_exception",
    primaryKeys = ["name", "phoneNumber"],
)
data class ContactExceptionEntity(
    val id: Int? = null,
    val name: String,
    val phoneNumber: String,
)
