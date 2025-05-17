package com.tasa.service.fake

import com.tasa.service.TasaService
import com.tasa.service.UserService

class TasaServiceFake : TasaService {

    override val userService: UserService by lazy {
        UserServiceFake()
    }
}
