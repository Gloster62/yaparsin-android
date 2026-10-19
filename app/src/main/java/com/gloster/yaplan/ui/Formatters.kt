package com.gloster.yaplan.ui

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val turkish = Locale.forLanguageTag("tr")
private val dateTimeFormat = DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", turkish)
private val timeFormat = DateTimeFormatter.ofPattern("HH:mm", turkish)
private val weekdayFormat = DateTimeFormatter.ofPattern("EEEE", turkish)
private val dayMonthFormat = DateTimeFormatter.ofPattern("d MMMM", turkish)
private val dayMonthYearFormat = DateTimeFormatter.ofPattern("d MMMM yyyy", turkish)
private val shortDateFormat = DateTimeFormatter.ofPattern("d MMM", turkish)

private fun String.capitalized() = replaceFirstChar { it.titlecase(turkish) }

fun Long.toLocalDateTime(zone: ZoneId = ZoneId.systemDefault()): LocalDateTime =
    Instant.ofEpochMilli(this).atZone(zone).toLocalDateTime()

fun formatDateTime(millis: Long): String = millis.toLocalDateTime().format(dateTimeFormat)

fun formatTime(time: LocalTime): String = time.format(timeFormat)

/** "Pazartesi, 19 Ekim" */
fun formatHeaderDate(date: LocalDate): String =
    "${date.format(weekdayFormat).capitalized()}, ${date.format(dayMonthFormat)}"

/** "21 Eki" */
fun formatShortDate(date: LocalDate): String = date.format(shortDateFormat)

/** Dün / Bugün / Yarın, gelecek hafta içi gün adı, daha uzağı için tarih. */
fun dayLabel(date: LocalDate, today: LocalDate): String =
    when (val days = ChronoUnit.DAYS.between(today, date)) {
        -1L -> "Dün"
        0L -> "Bugün"
        1L -> "Yarın"
        else -> when {
            days in 2..6 -> date.format(weekdayFormat).capitalized()
            date.year == today.year -> date.format(dayMonthFormat)
            else -> date.format(dayMonthYearFormat)
        }
    }

/** "Bugün 14:00" */
fun dueLabel(dueAt: Long, today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): String {
    val due = dueAt.toLocalDateTime(zone)
    return "${dayLabel(due.toLocalDate(), today)} ${due.format(timeFormat)}"
}
