package com.tasa.repository.interfaces

import com.tasa.domain.Action
import com.tasa.domain.Alarm

interface AlarmRepositoryInterface {
    /**
     * Creates an alarm with the given trigger time, action, and rule ID.
     * @param triggerTime The time at which the alarm should be triggered.
     * @param action The action to be performed when the alarm is triggered.
     * @param ruleId The ID of the rule associated with this alarm.
     * @return The ID of the created alarm.
     */
    suspend fun createAlarm(
        triggerTime: Long,
        action: Action,
        ruleId: Int,
    ): Int

    /**
     * Retrieves an alarm by its trigger time.
     * @param currentTime The trigger time of the alarm to be retrieved.
     * @return The Alarm object if found, or null if not found.
     */
    suspend fun getAlarmByTriggerTime(currentTime: Long): Alarm?

    /**
     * Retrieves an alarm by its ID.
     * @param id The ID of the alarm to be retrieved.
     * @return The Alarm object if found, or null if not found.
     */
    suspend fun getAlarmById(id: Int): Alarm?

    /**
     * Retrieves all alarms.
     * @return A list of all Alarm objects.
     */
    suspend fun getAllAlarms(): List<Alarm>

    /**
     * Updates an existing alarm with a new trigger time and action.
     * @param triggerTime The new trigger time for the alarm.
     * @param action The new action to be performed when the alarm is triggered.
     * @param id The ID of the alarm to be updated.
     */
    suspend fun updateAlarm(
        triggerTime: Long,
        action: Action,
        id: Int,
    )

    /**
     * Deletes an alarm by its ID.
     * @param id The ID of the alarm to be deleted.
     */
    suspend fun deleteAlarm(id: Int)

    /**
     * Clears all alarms from the repository.
     */
    suspend fun clear()

    /**
     * Clears alarms that are older than the specified time.
     * @param now The current time in milliseconds. Alarms older than this will be cleared.
     */
    suspend fun clearOlderAlarms(now: Long)
}
