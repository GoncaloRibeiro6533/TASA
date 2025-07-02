package com.tasa.repository

import com.tasa.domain.UserInfoRepository
import com.tasa.repository.interfaces.TasaRepoInterface
import com.tasa.service.TasaService
import com.tasa.storage.TasaDB

class TasaRepo(
    local: TasaDB,
    remote: TasaService,
    userInfoRepository: UserInfoRepository,
) : TasaRepoInterface {
    override val userRepo = UserRepository(local, remote, userInfoRepository)
    override val locationRepo = LocationRepository(local, remote, userInfoRepository)
    override val eventRepo = EventRepository(local, remote, userInfoRepository)
    override val alarmRepo = AlarmRepository(local)
    override val ruleRepo = RuleRepository(local, remote, userInfoRepository)
    override val geofenceRepo = GeofenceRepository(local)
}
