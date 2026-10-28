package com.therapist.assistant

import com.therapist.assistant.util.JalaliDate
import org.junit.Assert.assertEquals
import org.junit.Test

class JalaliDateTest {
    @Test
    fun nowruz1403() {
        val j = JalaliDate.gregorianToJalali(2024, 3, 20)
        assertEquals(1403, j.year)
        assertEquals(1, j.month)
        assertEquals(1, j.day)
    }

    @Test
    fun nowruz1404() {
        val j = JalaliDate.gregorianToJalali(2025, 3, 21)
        assertEquals(1404, j.year)
        assertEquals(1, j.month)
        assertEquals(1, j.day)
    }
}
