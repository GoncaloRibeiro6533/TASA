package com.tasa.domain

import java.time.LocalDateTime

data class RuleLocation(
    override val id: Int,
    override val startTime: LocalDateTime,
    override val endTime: LocalDateTime,
    val location: Location,
) : Rule(id), TimedRule
