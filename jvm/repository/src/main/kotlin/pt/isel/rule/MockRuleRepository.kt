package pt.isel.rule

import kotlinx.datetime.Instant
import pt.isel.Event
import pt.isel.Location
import pt.isel.Rule
import pt.isel.RuleEvent
import pt.isel.RuleLocation
import pt.isel.User

class MockRuleRepository : RuleRepository {
    private var eventRuleId = 0
    private var locationRuleId = 0

    // <UserId,Rule>
    private val eventRules = mutableMapOf<Int, MutableList<RuleEvent>>()
    private val locationRules = mutableMapOf<Int, MutableList<RuleLocation>>()

    override fun createEventRule(
        event: Event,
        user: User,
        startTime: Instant,
        endTime: Instant,
    ): RuleEvent {
        val rule =
            RuleEvent(
                id = eventRuleId++,
                startTime = startTime,
                endTime = endTime,
                event = event,
            )
        eventRules.computeIfAbsent(user.id) { mutableListOf() }
            .add(rule)
        return rule
    }

    override fun createLocationRule(
        location: Location,
        user: User,
        startTime: Instant,
        endTime: Instant,
    ): RuleLocation {
        val rule =
            RuleLocation(
                id = locationRuleId++,
                startTime = startTime,
                endTime = endTime,
                location = location,
            )
        locationRules.computeIfAbsent(user.id) { mutableListOf() }
            .add(rule)
        return rule
    }

    override fun findAll(): List<Rule> {
        return eventRules.values.flatten() + locationRules.values.flatten()
    }

    override fun findRuleEventById(id: Int): RuleEvent? {
        return eventRules.values.flatten().find { it.id == id }
    }

    override fun findRuleLocationById(id: Int): RuleLocation? {
        return locationRules.values.flatten().find { it.id == id }
    }

    override fun findByUserId(user: User): List<Rule> {
        val location = locationRules[user.id] ?: emptyList<Rule>()
        val event = eventRules[user.id] ?: emptyList<Rule>()
        return location + event
    }

    override fun updateRuleEvent(
        rule: RuleEvent,
        startTime: Instant,
        endTime: Instant,
    ): RuleEvent {
        val key = eventRules.entries.find { it.value.contains(rule) }?.key
        val updatedRule =
            RuleEvent(
                id = rule.id,
                startTime = startTime,
                endTime = endTime,
                event = rule.event,
            )
        eventRules[key]?.removeIf { it.id == rule.id }
        eventRules[key]?.add(updatedRule)
        return updatedRule
    }

    override fun updateRuleLocation(
        rule: RuleLocation,
        startTime: Instant,
        endTime: Instant,
    ): RuleLocation {
        val key = locationRules.entries.find { it.value.contains(rule) }?.key
        locationRules[key]?.removeIf { it.id == rule.id }
        val updatedRule =
            RuleLocation(
                id = rule.id,
                startTime = startTime,
                endTime = endTime,
                location = rule.location,
            )
        locationRules[key]?.add(
            updatedRule,
        )
        return updatedRule
    }

    override fun deleteRuleEvent(rule: RuleEvent): Boolean {
        val key = eventRules.entries.find { it.value.contains(rule) }?.key
        eventRules[key]?.remove(rule)
        return true
    }

    override fun deleteLocationEvent(rule: RuleLocation): Boolean {
        val key = locationRules.entries.find { it.value.contains(rule) }?.key
        locationRules[key]?.remove(rule)
        return true
    }

    override fun clear() {
        locationRules.clear()
        eventRules.clear()
        locationRuleId = 0
        eventRuleId = 0
    }
}
