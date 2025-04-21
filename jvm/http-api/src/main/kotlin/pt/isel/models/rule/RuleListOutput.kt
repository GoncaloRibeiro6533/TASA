package pt.isel.models.rule

data class RuleListOutput(
    val eventRulesN: Int,
    val eventRules: List<RuleEventOutput>,
    val locationRulesN: Int,
    val locationRules: List<RuleLocationOutput>,
)
