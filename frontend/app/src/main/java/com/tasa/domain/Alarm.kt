package com.tasa.domain

data class Alarm(
    val id: Int,
    val triggerTime: Long,
    val action: Action,
    val ruleId: Int,
)
