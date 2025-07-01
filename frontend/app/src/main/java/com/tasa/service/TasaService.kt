package com.tasa.service

import com.tasa.service.interfaces.EventService
import com.tasa.service.interfaces.LocationService
import com.tasa.service.interfaces.RuleService
import com.tasa.service.interfaces.UserService

interface TasaService {
    val userService: UserService
    val eventService: EventService
    val locationService: LocationService
    val ruleService: RuleService
}
