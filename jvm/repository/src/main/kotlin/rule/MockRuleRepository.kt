package rule

import kotlinx.datetime.Instant
import pt.isel.Event
import pt.isel.Rule
import pt.isel.User

class MockRuleRepository : RuleRepository {
    private var ruleId = 0
    private val rules = mutableListOf<Rule>()
    private val userRules = mutableMapOf<Int, MutableList<Int>>()
    private val eventRules = mutableMapOf<Int, MutableList<Int>>()
    private val locationRules = mutableMapOf<Int, MutableList<Int>>()

    override fun createEventRule(
        event: Event,
        user: User,
        startTime: Instant,
        endTime: Instant,
    ): Rule {
        val rule =
            Rule(
                id = ruleId++,
                startTime = startTime,
                endTime = endTime,
            )
        rules.add(rule)
        userRules.computeIfAbsent(user.id) { mutableListOf() }
            .add(rule.id)
        eventRules.computeIfAbsent(event.id.toInt()) { mutableListOf() }
            .add(rule.id)
        return rule
    }

    override fun createLocationRule(
        locationId: Int,
        user: User,
        startTime: Instant,
        endTime: Instant,
    ): Rule {
        val rule =
            Rule(
                id = ruleId++,
                startTime = startTime,
                endTime = endTime,
            )
        rules.add(rule)
        userRules.computeIfAbsent(user.id) { mutableListOf() }
            .add(rule.id)
        locationRules.computeIfAbsent(locationId) { mutableListOf() }
            .add(rule.id)
        return rule
    }

    override fun findAll(): List<Rule> {
        return rules
    }

    override fun findById(id: Int): Rule? {
        return rules.find { it.id == id }
    }

    override fun findByUserId(user: User): List<Rule> {
        userRules[user.id]?.let { ruleIds ->
            return rules.filter { it.id in ruleIds }
        } ?: return emptyList()
    }

    override fun update(
        rule: Rule,
        startTime: Instant,
        endTime: Instant,
    ): Rule {
        val updatedRule = rule.copy(startTime = startTime, endTime = endTime)
        rules[rules.indexOf(rule)] = updatedRule
        return updatedRule
    }

    override fun delete(rule: Rule): Boolean {
        return rules.remove(rule)
    }

    override fun clear() {
        rules.clear()
    }
}
