package com.project.minlishapp.presentation.dashboard

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.Date

class DashboardStreakTest {

    @Test
    fun `dashboard keeps streak for activity today`() {
        val today = date(2026, Calendar.JUNE, 2)

        assertEquals(4, calculateEffectiveStreak(4, today, today))
    }

    @Test
    fun `dashboard keeps streak for activity yesterday`() {
        assertEquals(
            4,
            calculateEffectiveStreak(
                currentStreak = 4,
                lastLearnedDate = date(2026, Calendar.JUNE, 1),
                now = date(2026, Calendar.JUNE, 2)
            )
        )
    }

    @Test
    fun `dashboard resets streak after missed day`() {
        assertEquals(
            0,
            calculateEffectiveStreak(
                currentStreak = 4,
                lastLearnedDate = date(2026, Calendar.MAY, 31),
                now = date(2026, Calendar.JUNE, 2)
            )
        )
    }

    @Test
    fun `dashboard rejects future activity date`() {
        assertEquals(
            0,
            calculateEffectiveStreak(
                currentStreak = 4,
                lastLearnedDate = date(2026, Calendar.JUNE, 3),
                now = date(2026, Calendar.JUNE, 2)
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
