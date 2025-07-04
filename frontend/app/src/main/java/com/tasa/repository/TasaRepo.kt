package com.tasa.repository

import com.tasa.alarm.AlarmScheduler
import com.tasa.domain.UserInfoRepository
import com.tasa.geofence.GeofenceManager
import com.tasa.repository.interfaces.TasaRepoInterface
import com.tasa.service.TasaService
import com.tasa.storage.TasaDB
import com.tasa.utils.QueryCalendarService

class TasaRepo(
    local: TasaDB,
    remote: TasaService,
    userInfoRepository: UserInfoRepository,
    geofenceManager: GeofenceManager,
    ruleScheduler: AlarmScheduler,
    queryCalendarService: QueryCalendarService,
) : TasaRepoInterface {
    override val userRepo = UserRepository(local, remote, userInfoRepository)
    override val locationRepo = LocationRepository(local, remote, userInfoRepository)
    override val eventRepo = EventRepository(local, remote, userInfoRepository, queryCalendarService)
    override val alarmRepo = AlarmRepository(local)
    override val ruleRepo =
        RuleRepository(
            local,
            remote,
            userInfoRepository,
            ruleScheduler,
            geofenceManager = geofenceManager,
            queryCalendarService = queryCalendarService,
        )
    override val geofenceRepo = GeofenceRepository(local)
}
