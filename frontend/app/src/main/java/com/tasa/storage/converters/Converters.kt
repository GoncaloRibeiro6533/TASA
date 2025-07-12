package com.tasa.storage.converters

import androidx.room.TypeConverter
import java.time.LocalDateTime

/**
 * Converters for Room database to handle LocalDateTime serialization and deserialization.
 * This is necessary because Room does not support LocalDateTime natively.
 */
class Converters {
    /**
     * Converts LocalDateTime to String for storage in the database.
     * @param value The LocalDateTime value to convert.
     * @return The String representation of the LocalDateTime.
     */
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime): String {
        return value.toString()
    }

    /**
     * Converts String back to LocalDateTime when retrieving from the database.
     * @param value The String representation of the LocalDateTime.
     * @return The LocalDateTime object.
     */
    @TypeConverter
    fun toLocalDateTime(value: String): LocalDateTime {
        return LocalDateTime.parse(value)
    }
}
