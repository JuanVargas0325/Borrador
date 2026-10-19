package com.equipo2.vinilos.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class ResultOfTest {

    @Test
    fun siElBloqueTermina_devuelveElValor() = runTest {
        assertEquals(Result.success(5), resultOf { 5 })
    }

    @Test
    fun siElBloqueFalla_devuelveElError() = runTest {
        val error = IOException("Sin conexión")
        assertEquals(Result.failure<Int>(error), resultOf<Int> { throw error })
    }

    @Test
    fun laCancelacion_noSeConvierteEnError() = runTest {
        val cancellation = CancellationException("El usuario salió de la pantalla")
        val thrown = runCatching { resultOf { throw cancellation } }.exceptionOrNull()
        assertEquals(cancellation, thrown)
    }
}
