package com.gloster.yaplan.ui

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateTimeFormat = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.forLanguageTag("tr"))

fun formatDateTime(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(dateTimeFormat)
