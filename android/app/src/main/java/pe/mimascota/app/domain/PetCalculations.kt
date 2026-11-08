package pe.mimascota.app.domain

import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit

object PetCalculations {
    fun age(birthDate: LocalDate, today: LocalDate = LocalDate.now()): Period {
        require(!birthDate.isAfter(today))
        return Period.between(birthDate, today)
    }
    fun daysUntil(dueDate: LocalDate, today: LocalDate = LocalDate.now()): Long =
        ChronoUnit.DAYS.between(today, dueDate)
    fun sumCents(amounts: Iterable<Long>): Long = amounts.fold(0L, Math::addExact)
}
