package com.tasa.repository.interfaces

interface TasaRepoInterface {
    val userRepo: UserRepositoryInterface
    val alarmRepo: AlarmRepositoryInterface
    val locationRepo: LocationRepositoryInterface
    val eventRepo: EventRepositoryInterface
    val ruleRepo: RuleRepositoryInterface
    val geofenceRepo: GeofenceRepositoryInterface
}
