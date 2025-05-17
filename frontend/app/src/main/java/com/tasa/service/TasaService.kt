package com.tasa.service

interface TasaService {
    val userService: UserService
    val eventService: EventService
    val locationService: LocationService
    val ruleService: RuleService
}
