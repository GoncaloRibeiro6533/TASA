package com.tasa.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tasa.storage.converters.Converters
import com.tasa.storage.daos.AlarmDao
import com.tasa.storage.daos.AppExceptionDao
import com.tasa.storage.daos.ContactExceptionDao
import com.tasa.storage.daos.EventDao
import com.tasa.storage.daos.LocationDao
import com.tasa.storage.daos.RuleEventDao
import com.tasa.storage.daos.RuleLocationDao
import com.tasa.storage.daos.UserDao
import com.tasa.storage.entities.AlarmEntity
import com.tasa.storage.entities.AppExceptionEntity
import com.tasa.storage.entities.ContactExceptionEntity
import com.tasa.storage.entities.EventEntity
import com.tasa.storage.entities.LocationEntity
import com.tasa.storage.entities.RuleEventEntity
import com.tasa.storage.entities.RuleLocationEntity
import com.tasa.storage.entities.UserEntity

@Database(
    entities = [
        UserEntity::class,
        EventEntity::class,
        LocationEntity::class,
        RuleEventEntity::class,
        RuleLocationEntity::class,
        AppExceptionEntity::class,
        ContactExceptionEntity::class,
        AlarmEntity::class,
    ],
    version = 1,
)
@TypeConverters(Converters::class)
abstract class TasaDB : RoomDatabase() {
    abstract fun userDao(): UserDao

    abstract fun eventDao(): EventDao

    abstract fun locationDao(): LocationDao

    abstract fun ruleEventDao(): RuleEventDao

    abstract fun ruleLocationDao(): RuleLocationDao

    abstract fun appExceptionDao(): AppExceptionDao

    abstract fun contactExceptionDao(): ContactExceptionDao

    abstract fun alarmDao(): AlarmDao
}
