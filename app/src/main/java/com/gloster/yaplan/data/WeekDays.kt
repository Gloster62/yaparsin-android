package com.gloster.yaplan.data

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

// Chosen weekdays are stored as one Int column: bit 0 is Monday … bit 6 is Sunday.
const val EVERY_DAY = 0b111_1111
const val WEEKDAYS = 0b001_1111
const val WEEKEND = 0b110_0000

private val DayOfWeek.bit: Int get() = 1 shl ordinal

fun Int.hasDay(day: DayOfWeek): Boolean = this and day.bit != 0

fun Int.toggleDay(day: DayOfWeek): Int = this xor day.bit

fun Int.days(): List<DayOfWeek> = DayOfWeek.entries.filter(::hasDay)

fun timeOfDay(minutes: Int): LocalTime = LocalTime.of(minutes / 60, minutes % 60)

/** The first chosen day at [minutes] past midnight strictly after [now]; null when no day is chosen. */
fun nextRepeatAt(now: Long, days: Int, minutes: Int, zone: ZoneId): Long? {
    if (days and EVERY_DAY == 0) return null
    val current = Instant.ofEpochMilli(now).atZone(zone)
    val time = timeOfDay(minutes)
    // Up to a week ahead: today's slot may already have passed and today is the only chosen day.
    return (0L..7L).asSequence()
        .map { current.toLocalDate().plusDays(it) }
        .filter { days.hasDay(it.dayOfWeek) }
        .map { it.atTime(time).atZone(zone) }
        .first { it.isAfter(current) }
        .toInstant()
        .toEpochMilli()
}
