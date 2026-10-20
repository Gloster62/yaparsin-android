package com.gloster.yaplan.ui

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {
    private val today = LocalDate.of(2026, 10, 19)

    @Test
    fun dayLabelsUsePlainTurkish() {
        assertEquals("Dün", dayLabel(today.minusDays(1), today))
        assertEquals("Bugün", dayLabel(today, today))
        assertEquals("Yarın", dayLabel(today.plusDays(1), today))
        assertEquals("Perşembe", dayLabel(today.plusDays(3), today))
        assertEquals("2 Kasım", dayLabel(LocalDate.of(2026, 11, 2), today))
        assertEquals("5 Ocak 2027", dayLabel(LocalDate.of(2027, 1, 5), today))
    }

    @Test
    fun dueLabelCombinesDayAndTime() {
        val dueAt = LocalDateTime.of(2026, 10, 18, 18, 0).toInstant(ZoneOffset.UTC).toEpochMilli()
        assertEquals("Dün 18:00", dueLabel(dueAt, today, ZoneOffset.UTC))
    }

    @Test
    fun headerDate() {
        assertEquals("Pazartesi, 19 Ekim", formatHeaderDate(today))
    }

    @Test
    fun monthTitle() {
        assertEquals("Ekim 2026", formatMonth(YearMonth.of(2026, 10)))
        assertEquals("Şubat 2027", formatMonth(YearMonth.of(2027, 2)))
    }
}
