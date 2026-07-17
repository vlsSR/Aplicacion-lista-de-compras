package com.example.aplicacionlistadecompras.ui.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val formatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")

fun formatCreatedAt(epochMillis: Long): String {
    if (epochMillis <= 0L) return ""
    return Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}