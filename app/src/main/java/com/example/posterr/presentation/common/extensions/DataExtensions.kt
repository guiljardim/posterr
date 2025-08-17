package com.example.posterr.presentation.common.extensions

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun Long.toFormattedDate(pattern: String = "dd/MM/yyyy HH:mm"): String {
    val date = Date(this)
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(date)
}