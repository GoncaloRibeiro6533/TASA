package com.tasa.domain

import java.time.LocalDateTime
import java.util.Calendar

data class TriggerTime(
    val value: Long,
) {
    private fun getCalendar(): Calendar {
        return Calendar.getInstance().apply { timeInMillis = value }
    }

    fun toLocalDateTime(): LocalDateTime {
        val calendar = getCalendar()
        return LocalDateTime.of(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1, // Ajuste para base 1
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND),
        )
    }

    val year: Int
        get() = getCalendar().get(Calendar.YEAR)

    val month: Int
        get() = getCalendar().get(Calendar.MONTH)

    val day: Int
        get() = getCalendar().get(Calendar.DAY_OF_MONTH)

    val hour: Int
        get() = getCalendar().get(Calendar.HOUR_OF_DAY)

    val minute: Int
        get() = getCalendar().get(Calendar.MINUTE)

    val second: Int
        get() = getCalendar().get(Calendar.SECOND)
}

fun Long.toTriggerTime(): TriggerTime {
    return TriggerTime(this)
}
