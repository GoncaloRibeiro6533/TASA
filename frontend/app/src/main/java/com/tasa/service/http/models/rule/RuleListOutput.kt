package com.tasa.service.http.models.rule

import kotlinx.serialization.Serializable

@Serializable
data class RuleListOutput(
    val eventRulesN: Int,
    val eventRules: List<RuleEventOutput>,
    val locationRulesN: Int,
    val locationRules: List<RuleLocationOutput>,
)
