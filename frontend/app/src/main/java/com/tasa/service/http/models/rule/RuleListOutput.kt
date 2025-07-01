package com.tasa.service.http.models.rule

import com.tasa.domain.Rule

data class RuleListOutput(
    val eventRulesN: Int,
    val eventRules: List<RuleEventOutput>,
    val locationRulesN: Int,
    val locationRules: List<RuleLocationOutput>,
) {
    fun toRules(): List<Rule> {
        return eventRules.map { it.toRuleEvent() } + locationRules.map { it.toRuleLocation() }
    }
}
