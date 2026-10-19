package com.equipo2.vinilos.ui.common

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DateFormatsTest {

    @Test
    fun fechaLarga_enEspanolYConElMesEnMinusculas() {
        assertEquals("1 de agosto de 1984", DateFormats.long(LocalDate.of(1984, 8, 1)))
    }

    @Test
    fun milisegundosUtc_idaYVuelta() {
        val date = LocalDate.of(1975, 11, 21)
        assertEquals(date, DateFormats.fromUtcMillis(DateFormats.toUtcMillis(date)))
    }
}
