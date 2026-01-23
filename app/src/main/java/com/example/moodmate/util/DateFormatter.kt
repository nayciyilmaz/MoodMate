package com.example.moodmate.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatDate(isoDate: String): String {
    return try {
        val dateTime = LocalDateTime.parse(isoDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        dateTime.format(
            DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", Locale("tr"))
        )
    } catch (e: Exception) {
        isoDate
    }
}