package com.example.posterr.presentation.common.extensions

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class DataExtensionsTest {

    private lateinit var originalTimeZone: TimeZone
    private lateinit var originalLocale: Locale

    @Before
    fun setUp() {
        originalTimeZone = TimeZone.getDefault()
        originalLocale = Locale.getDefault()
        // Force UTC to make time-based formatting deterministic
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        // Change default locale to ensure functions use Locale.ENGLISH internally
        Locale.setDefault(Locale("pt", "BR"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalTimeZone)
        Locale.setDefault(originalLocale)
    }

    @Test
    fun `toProfileDate should format as 'March 25, 2021' in English`() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, 2021)
            set(Calendar.MONTH, Calendar.MARCH)
            set(Calendar.DAY_OF_MONTH, 25)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val millis = calendar.timeInMillis
        val result = millis.toProfileDate()

        assertEquals("March 25, 2021", result)
    }

    @Test
    fun `toPostDate should format as 'March 25, 2021 at 14_30' in English 24h`() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, 2021)
            set(Calendar.MONTH, Calendar.MARCH)
            set(Calendar.DAY_OF_MONTH, 25)
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val millis = calendar.timeInMillis
        val result = millis.toPostDate()

        assertEquals("March 25, 2021 at 14:30", result)
    }
}


