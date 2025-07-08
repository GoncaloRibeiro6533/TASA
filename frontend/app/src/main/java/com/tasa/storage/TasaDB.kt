package com.tasa.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tasa.storage.converters.Converters
import com.tasa.storage.daos.LocalDao
import com.tasa.storage.daos.RemoteDao
import com.tasa.storage.daos.UserDao
import com.tasa.storage.entities.UserEntity
import com.tasa.storage.entities.localMode.AlarmLocal
import com.tasa.storage.entities.localMode.EventLocal
import com.tasa.storage.entities.localMode.GeofenceLocal
import com.tasa.storage.entities.localMode.LocationLocal
import com.tasa.storage.entities.localMode.RuleEventLocal
import com.tasa.storage.entities.localMode.RuleLocationLocal
import com.tasa.storage.entities.remote.AlarmRemote
import com.tasa.storage.entities.remote.EventRemote
import com.tasa.storage.entities.remote.GeofenceRemote
import com.tasa.storage.entities.remote.LocationRemote
import com.tasa.storage.entities.remote.RuleEventRemote
import com.tasa.storage.entities.remote.RuleLocationRemote

@Database(
    entities = [
        UserEntity::class,
        EventLocal::class,
        EventRemote::class,
        LocationRemote::class,
        LocationLocal::class,
        RuleEventLocal::class,
        RuleEventRemote::class,
        RuleLocationLocal::class,
        RuleLocationRemote::class,
        GeofenceLocal::class,
        GeofenceRemote::class,
        AlarmRemote::class,
        AlarmLocal::class,
    ],
    version = 23,
)
@TypeConverters(Converters::class)
abstract class TasaDB : RoomDatabase() {
    abstract fun localDao(): LocalDao

    abstract fun remoteDao(): RemoteDao

    abstract fun userDao(): UserDao
}
