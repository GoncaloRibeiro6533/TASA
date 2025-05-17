package com.tasa.storage.entities

import androidx.room.Entity

@Entity(
    tableName = "app_exception",
    primaryKeys = ["name"],
)
data class AppExceptionEntity(
    val id: Int? = null,
    val name: String,
)
