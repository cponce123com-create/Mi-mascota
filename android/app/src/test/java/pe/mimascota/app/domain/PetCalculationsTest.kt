package pe.mimascota.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class PetCalculationsTest {
    @Test fun ageAccountsForCalendar() {
        assertEquals(2, PetCalculations.age(LocalDate.parse("2024-01-01"), LocalDate.parse("2026-02-01")).years)
    }
    @Test fun remindersCanBeOverdue() {
        assertEquals(-2L, PetCalculations.daysUntil(LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-03")))
    }
    @Test fun sumsCurrencyWithoutFloatingPoint() {
        assertEquals(301L, PetCalculations.sumCents(listOf(100L, 201L)))
    }
}
