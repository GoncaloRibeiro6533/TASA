package com.tasa.service.fake

import com.tasa.service.EventService
import com.tasa.service.LocationService
import com.tasa.service.RuleService
import com.tasa.service.TasaService
import com.tasa.service.UserService

class TasaServiceFake : TasaService {
    override val userService: UserService by lazy {
        UserServiceFake()
    }
    override val eventService: EventService by lazy {
        EventServiceFake()
    }
    override val locationService: LocationService by lazy {
        LocationServiceFake()
    }
    override val ruleService: RuleService by lazy {
        RuleServiceFake()
    }
}
