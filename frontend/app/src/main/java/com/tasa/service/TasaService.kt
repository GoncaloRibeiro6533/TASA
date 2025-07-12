package com.tasa.service

import com.tasa.service.interfaces.EventService
import com.tasa.service.interfaces.LocationService
import com.tasa.service.interfaces.RuleService
import com.tasa.service.interfaces.UserService

/**
 * TasaService is the main service interface that aggregates all other services.
 * It provides access to remote data.
 */
interface TasaService {
    val userService: UserService
    val eventService: EventService
    val locationService: LocationService
    val ruleService: RuleService
}
