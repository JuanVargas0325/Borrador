package com.equipo2.vinilos.ui.common

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormats {

    private val spanish = Locale.forLanguageTag("es")
    private val longFormat = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", spanish)

    /** «1 de junio de 1992». En Android 5 el mes llega con mayúscula inicial, por eso se pasa a minúsculas. */
    fun long(date: LocalDate): String = longFormat.format(date).lowercase(spanish)

    /** Los selectores de fecha de Material trabajan en milisegundos UTC. */
    fun toUtcMillis(date: LocalDate): Long = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    fun fromUtcMillis(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
}
