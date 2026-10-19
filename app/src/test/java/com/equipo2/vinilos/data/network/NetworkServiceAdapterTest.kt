package com.equipo2.vinilos.data.network

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class NetworkServiceAdapterTest {

    @Test
    fun laFechaSeEnviaEnIsoCompletoAMedianocheUtc() {
        assertEquals("1984-08-01T00:00:00.000Z", NetworkServiceAdapter.toApiDate(LocalDate.of(1984, 8, 1)))
    }

    @Test
    fun laFechaDeLaApiSeLeeComoDia() {
        assertEquals(LocalDate.of(1984, 8, 1), NetworkServiceAdapter.fromApiDate("1984-08-01T00:00:00.000Z"))
        // Varios álbumes del servidor se guardaron a medianoche de Colombia (05:00 UTC): el día no debe cambiar.
        assertEquals(LocalDate.of(1984, 8, 1), NetworkServiceAdapter.fromApiDate("1984-08-01T05:00:00.000Z"))
    }
}
