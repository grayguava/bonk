package com.grayguava.bonk.data

import org.junit.Assert.assertEquals
import org.junit.Test

class DurationTest {
    private fun millis(days: Long, hours: Long, minutes: Long, seconds: Long): Long =
        ((days * 86_400 + hours * 3_600 + minutes * 60 + seconds) * 1000)

    @Test
    fun `the days are not repeated`() {
        assertEquals(
            "7 hours 8 mins 44 secs",
            formatElapsed(millis(2, 7, 8, 44)),
        )
    }

    @Test
    fun `the hours are the hours inside the day`() {
        assertEquals(
            "1 hour 0 mins 0 secs",
            formatElapsed(millis(1, 1, 0, 0)),
        )
    }

    @Test
    fun `a fresh streak still shows the seconds`() {
        assertEquals("0 hours 0 mins 5 secs", formatElapsed(5_000))
    }

    @Test
    fun `a negative duration is nothing`() {
        assertEquals("0 hours 0 mins 0 secs", formatElapsed(-10_000))
    }
}
