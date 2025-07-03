package pt.isel.models.rule

data class RuleEventListOutput(
    val nRules: Int,
    val rules: List<RuleEventOutput>,
)
