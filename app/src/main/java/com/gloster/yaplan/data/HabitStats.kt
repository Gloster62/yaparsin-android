package com.gloster.yaplan.data

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/** How a done day joins its neighbours in a calendar week row, to draw one unbroken chain. */
enum class ChainPart { SINGLE, START, MIDDLE, END }

enum class DayStatus { DONE, TODAY, MISSED, FUTURE, NOT_STARTED }

data class MonthScore(val done: Int, val days: Int)

fun Habit.startDay(zone: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(createdAt).atZone(zone).toLocalDate()

/** An unmarked today doesn't break the chain yet: the day isn't over. */
fun currentStreak(done: Set<LocalDate>, today: LocalDate): Int {
    var day = if (today in done) today else today.minusDays(1)
    var count = 0
    while (day in done) {
        count++
        day = day.minusDays(1)
    }
    return count
}

fun longestStreak(done: Set<LocalDate>): Int =
    done.filter { it.minusDays(1) !in done }.maxOfOrNull { start ->
        generateSequence(start) { it.plusDays(1) }.takeWhile { it in done }.count()
    } ?: 0

fun isAtRisk(done: Set<LocalDate>, today: LocalDate): Boolean =
    today !in done && today.minusDays(1) in done

/** Only today and a forgotten yesterday can be changed, so old days can't pad the chain. */
fun canToggle(day: LocalDate, today: LocalDate): Boolean = day == today || day == today.minusDays(1)

/** Monday-first week rows; cells outside the month are null. */
fun monthWeeks(month: YearMonth): List<List<LocalDate?>> {
    val leading = month.atDay(1).dayOfWeek.value - DayOfWeek.MONDAY.value
    val cells = List(leading) { null } + (1..month.lengthOfMonth()).map(month::atDay)
    return cells.chunked(7) { week -> week + List(7 - week.size) { null } }
}

fun chainPart(day: LocalDate, done: Set<LocalDate>): ChainPart {
    val joinsLeft = day.dayOfWeek != DayOfWeek.MONDAY && day.dayOfMonth != 1 && day.minusDays(1) in done
    val joinsRight = day.dayOfWeek != DayOfWeek.SUNDAY && day.dayOfMonth != day.lengthOfMonth() &&
        day.plusDays(1) in done
    return when {
        joinsLeft && joinsRight -> ChainPart.MIDDLE
        joinsLeft -> ChainPart.END
        joinsRight -> ChainPart.START
        else -> ChainPart.SINGLE
    }
}

fun dayStatus(day: LocalDate, done: Set<LocalDate>, today: LocalDate, startDay: LocalDate): DayStatus = when {
    day in done -> DayStatus.DONE
    day == today -> DayStatus.TODAY
    day > today -> DayStatus.FUTURE
    day < startDay -> DayStatus.NOT_STARTED
    else -> DayStatus.MISSED
}

/** Done days out of the days that counted so far this month. */
fun monthScore(month: YearMonth, done: Set<LocalDate>, today: LocalDate, startDay: LocalDate): MonthScore {
    val start = maxOf(month.atDay(1), minOf(startDay, done.minOrNull() ?: startDay))
    val end = minOf(month.atEndOfMonth(), today)
    if (end < start) return MonthScore(0, 0)
    val days = (end.toEpochDay() - start.toEpochDay() + 1).toInt()
    return MonthScore(done.count { it in start..end }, days)
}

/** The next time [minutes] past midnight comes round after [now]. */
fun nextReminderAt(now: Long, minutes: Int, zone: ZoneId): Long = nextRepeatAt(now, EVERY_DAY, minutes, zone)!!
