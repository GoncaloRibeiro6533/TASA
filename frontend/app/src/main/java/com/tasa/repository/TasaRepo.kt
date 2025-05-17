package com.tasa.repository

import com.tasa.repository.interfaces.TasaRepoInterface
import com.tasa.service.TasaService
import com.tasa.storage.TasaDB

class TasaRepo(
    local: TasaDB,
    remote: TasaService,
) : TasaRepoInterface {
    override val userRepo = UserRepository(local, remote)
}
