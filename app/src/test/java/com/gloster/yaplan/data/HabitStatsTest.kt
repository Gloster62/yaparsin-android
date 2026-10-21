package com.gloster.yaplan.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitStatsTest {
    private val today = LocalDate.of(2026, 10, 20)

    private fun days(vararg dayOfMonth: Int) = dayOfMonth.map { LocalDate.of(2026, 10, it) }.toSet()

    @Test
    fun streakCountsBackFromTodayWhenDone() {
        assertEquals(3, currentStreak(days(18, 19, 20), today))
    }

    @Test
    fun unmarkedTodayKeepsYesterdaysChainAlive() {
        assertEquals(2, currentStreak(days(18, 19), today))
        assertTrue(isAtRisk(days(18, 19), today))
    }

    @Test
    fun missedYesterdayBreaksTheChain() {
        assertEquals(0, currentStreak(days(17, 18), today))
        assertFalse(isAtRisk(days(17, 18), today))
        assertEquals(1, currentStreak(days(17, 18, 20), today))
    }

    @Test
    fun longestStreakFindsTheLongestRun() {
        assertEquals(4, longestStreak(days(1, 2, 3, 4, 10, 11, 19, 20)))
        assertEquals(0, longestStreak(emptySet()))
    }

    @Test
    fun onlyTodayAndYesterdayCanBeToggled() {
        assertTrue(canToggle(today, today))
        assertTrue(canToggle(today.minusDays(1), today))
        assertFalse(canToggle(today.minusDays(2), today))
        assertFalse(canToggle(today.plusDays(1), today))
    }

    @Test
    fun monthGridStartsOnMonday() {
        val weeks = monthWeeks(YearMonth.of(2026, 10))
        assertEquals(5, weeks.size)
        assertTrue(weeks.all { it.size == 7 })
        assertEquals(listOf(null, null, null), weeks.first().take(3))
        assertEquals(LocalDate.of(2026, 10, 1), weeks.first()[3])
        assertEquals(LocalDate.of(2026, 10, 31), weeks.last()[5])
        assertNull(weeks.last()[6])
        assertEquals(31, weeks.flatten().count { it != null })
    }

    @Test
    fun chainSplitsAtWeekAndMonthEdges() {
        // 18 Ekim 2026 is a Sunday, 19 a Monday.
        val done = days(17, 18, 19, 20)
        assertEquals(ChainPart.START, chainPart(LocalDate.of(2026, 10, 17), done))
        assertEquals(ChainPart.END, chainPart(LocalDate.of(2026, 10, 18), done))
        assertEquals(ChainPart.START, chainPart(LocalDate.of(2026, 10, 19), done))
        assertEquals(ChainPart.END, chainPart(LocalDate.of(2026, 10, 20), done))
        assertEquals(ChainPart.MIDDLE, chainPart(LocalDate.of(2026, 10, 14), days(13, 14, 15)))
        assertEquals(ChainPart.SINGLE, chainPart(LocalDate.of(2026, 10, 1), days(1) + LocalDate.of(2026, 9, 30)))
    }

    @Test
    fun dayStatusSeparatesMissedFromNotStarted() {
        val start = LocalDate.of(2026, 10, 15)
        val done = days(16, 17)
        assertEquals(DayStatus.DONE, dayStatus(LocalDate.of(2026, 10, 16), done, today, start))
        assertEquals(DayStatus.MISSED, dayStatus(LocalDate.of(2026, 10, 18), done, today, start))
        assertEquals(DayStatus.NOT_STARTED, dayStatus(LocalDate.of(2026, 10, 10), done, today, start))
        assertEquals(DayStatus.TODAY, dayStatus(today, done, today, start))
        assertEquals(DayStatus.FUTURE, dayStatus(today.plusDays(1), done, today, start))
    }

    @Test
    fun monthScoreCountsFromTheStartDay() {
        val start = LocalDate.of(2026, 10, 11)
        assertEquals(MonthScore(done = 3, days = 10), monthScore(YearMonth.of(2026, 10), days(11, 12, 20), today, start))
        assertEquals(MonthScore(done = 1, days = 11), monthScore(YearMonth.of(2026, 10), days(10), today, start))
        assertEquals(MonthScore(0, 0), monthScore(YearMonth.of(2026, 11), emptySet(), today, start))
    }

    @Test
    fun reminderMovesToTomorrowOncePassed() {
        val zone = ZoneOffset.UTC
        fun at(day: Int, hour: Int, minute: Int) = LocalDateTime.of(2026, 10, day, hour, minute).toInstant(zone).toEpochMilli()

        assertEquals(at(20, 21, 0), nextReminderAt(at(20, 10, 0), 21 * 60, zone))
        assertEquals(at(21, 21, 0), nextReminderAt(at(20, 21, 0), 21 * 60, zone))
        assertEquals(at(21, 8, 30), nextReminderAt(at(20, 22, 0), 8 * 60 + 30, zone))
    }

    @Test
    fun chainMoodGetsMoreUrgentTowardsMidnight() {
        val atRisk = days(18, 19)
        fun at(hour: Int) = today.atTime(hour, 0)
        assertEquals(ChainMood.CALM, chainMood(atRisk, at(9)))
        assertEquals(ChainMood.NUDGE, chainMood(atRisk, at(16)))
        assertEquals(ChainMood.URGENT, chainMood(atRisk, at(21)))
        assertEquals(ChainMood.LAST_CALL, chainMood(atRisk, at(22)))
        assertEquals(ChainMood.DONE_TODAY, chainMood(atRisk + today, at(23)))
        assertEquals(ChainMood.BROKEN, chainMood(days(15), at(23)))
        assertEquals(ChainMood.FRESH, chainMood(emptySet(), at(23)))
    }

    @Test
    fun widgetsRefreshAtEachMoodBoundary() {
        val zone = ZoneOffset.UTC
        fun at(day: Int, hour: Int, minute: Int = 0) = LocalDateTime.of(2026, 10, day, hour, minute).toInstant(zone).toEpochMilli()

        assertEquals(at(20, 16), nextMoodBoundaryAt(at(20, 9), zone))
        assertEquals(at(20, 20), nextMoodBoundaryAt(at(20, 16), zone))
        assertEquals(at(20, 22), nextMoodBoundaryAt(at(20, 21, 59), zone))
        assertEquals(at(21, 0), nextMoodBoundaryAt(at(20, 22), zone))
        assertEquals(at(21, 0), nextMidnightAt(at(20, 9), zone))
    }
}
