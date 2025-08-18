package com.example.posterr.presentation.common.extensions

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toProfileDate(): String {
    val date = Date(this)
    val formatter = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)
    return formatter.format(date)
}

fun Long.toPostDate(): String {
    val date = Date(this)
    val formatter = SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm", Locale.ENGLISH)
    return formatter.format(date)
}