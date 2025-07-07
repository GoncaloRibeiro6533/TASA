package com.tasa.repository

import com.tasa.alarm.AlarmScheduler
import com.tasa.domain.UserInfoRepository
import com.tasa.geofence.GeofenceManager
import com.tasa.repository.interfaces.TasaRepoInterface
import com.tasa.service.TasaService
import com.tasa.storage.TasaDB
import com.tasa.utils.NetworkChecker
import com.tasa.utils.QueryCalendarService

class TasaRepo(
    local: TasaDB,
    remote: TasaService,
    userInfoRepository: UserInfoRepository,
    geofenceManager: GeofenceManager,
    ruleScheduler: AlarmScheduler,
    networkChecker: NetworkChecker,
    queryCalendarService: QueryCalendarService,
) : TasaRepoInterface {
    override val userRepo = UserRepository(local, remote, userInfoRepository, networkChecker)
    override val locationRepo = LocationRepository(local, remote, userInfoRepository, networkChecker, userRepo)
    override val eventRepo = EventRepository(local, remote, userInfoRepository, queryCalendarService, networkChecker, userRepo)
    override val alarmRepo = AlarmRepository(local, userInfoRepository)
    override val ruleRepo =
        RuleRepository(
            local,
            remote,
            userInfoRepository,
            ruleScheduler,
            geofenceManager = geofenceManager,
            queryCalendarService = queryCalendarService,
            networkChecker = networkChecker,
            userRepo,
        )
    override val geofenceRepo = GeofenceRepository(local, userInfoRepository)
}
