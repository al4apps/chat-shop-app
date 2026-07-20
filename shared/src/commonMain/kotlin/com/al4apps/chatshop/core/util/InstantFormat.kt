package com.al4apps.chatshop.core.util

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Метка времени для списков: сегодня — `14:05`, раньше — `19.07`. */
@OptIn(ExperimentalTime::class)
fun Instant.toListTimestamp(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    now: Instant = Clock.System.now(),
): String {
    val local = toLocalDateTime(timeZone)
    val today = now.toLocalDateTime(timeZone).date
    return if (local.date == today) {
        "${pad(local.hour)}:${pad(local.minute)}"
    } else {
        "${pad(local.day)}.${pad(local.monthNumber)}"
    }
}

private fun pad(value: Int): String = value.toString().padStart(2, '0')
