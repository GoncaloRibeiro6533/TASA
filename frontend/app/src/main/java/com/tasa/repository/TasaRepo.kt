package com.tasa.repository

import com.tasa.repository.interfaces.AlarmRepository
import com.tasa.repository.interfaces.TasaRepoInterface
import com.tasa.service.TasaService
import com.tasa.storage.TasaDB

class TasaRepo(
    local: TasaDB,
    remote: TasaService,
) : TasaRepoInterface {
    override val userRepo = UserRepository(local, remote)
    override val locationRepo = LocationRepository(local, remote)
    override val eventRepo = EventRepository(local, remote)
    override val alarmRepo = AlarmRepository(local)
}
