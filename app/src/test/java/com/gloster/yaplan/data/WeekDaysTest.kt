package com.gloster.yaplan.data

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeekDaysTest {
    private val zone = ZoneOffset.UTC

    // 19 October 2026 is a Monday.
    private fun at(day: Int, hour: Int, minute: Int = 0) =
        LocalDateTime.of(2026, 10, day, hour, minute).toInstant(zone).toEpochMilli()

    private fun daysOf(vararg days: DayOfWeek) = days.fold(0) { mask, day -> mask.toggleDay(day) }

    @Test
    fun maskKeepsChosenDays() {
        val mondayThursday = daysOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY)
        assertTrue(mondayThursday.hasDay(DayOfWeek.MONDAY))
        assertFalse(mondayThursday.hasDay(DayOfWeek.TUESDAY))
        assertEquals(listOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY), mondayThursday.days())
        assertEquals(0, mondayThursday.toggleDay(DayOfWeek.MONDAY).toggleDay(DayOfWeek.THURSDAY))
        assertEquals(WEEKDAYS, daysOf(*DayOfWeek.entries.take(5).toTypedArray()))
    }

    @Test
    fun remindsLaterTodayWhenTodayIsChosen() {
        val days = daysOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY)
        assertEquals(at(19, 9), nextRepeatAt(at(19, 8), days, 9 * 60, zone))
    }

    @Test
    fun skipsToTheNextChosenDayOncePassed() {
        val days = daysOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY)
        assertEquals(at(22, 9), nextRepeatAt(at(19, 9), days, 9 * 60, zone))
        assertEquals(at(26, 9), nextRepeatAt(at(22, 10), days, 9 * 60, zone))
    }

    @Test
    fun onlyTodayChosenWaitsAWeek() {
        assertEquals(at(26, 9), nextRepeatAt(at(19, 9, 30), daysOf(DayOfWeek.MONDAY), 9 * 60, zone))
    }

    @Test
    fun noDaysMeansNoReminder() {
        assertNull(nextRepeatAt(at(19, 8), 0, 9 * 60, zone))
    }
}
