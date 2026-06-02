package com.project.minlishapp.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar
import java.util.Date

class PracticeStreakTest {

    @Test
    fun `first completed practice starts streak`() {
        assertEquals(1, calculateNextStreak(0, null, date(2026, Calendar.JUNE, 2)))
    }

    @Test
    fun `additional practice on same day does not change streak`() {
        val today = date(2026, Calendar.JUNE, 2)

        assertNull(calculateNextStreak(4, today, today))
    }

    @Test
    fun `practice on following day increments streak`() {
        assertEquals(
            5,
            calculateNextStreak(
                currentStreak = 4,
                lastLearnedDate = date(2026, Calendar.JUNE, 1),
                answeredAt = date(2026, Calendar.JUNE, 2)
            )
        )
    }

    @Test
    fun `practice after missed day restarts streak`() {
        assertEquals(
            1,
            calculateNextStreak(
                currentStreak = 4,
                lastLearnedDate = date(2026, Calendar.MAY, 31),
                answeredAt = date(2026, Calendar.JUNE, 2)
            )
        )
    }

    private fun date(year: Int, month: Int, day: Int): Date {
        return Calendar.getInstance().apply {
            clear()
            set(year, month, day, 12, 0, 0)
        }.time
    }
}
