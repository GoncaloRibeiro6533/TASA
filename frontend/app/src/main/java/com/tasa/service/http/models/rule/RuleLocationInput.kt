package com.tasa.service.http.models.rule

import kotlinx.serialization.Serializable

@Serializable
data class RuleLocationInput(
    val locationId: Int,
)
