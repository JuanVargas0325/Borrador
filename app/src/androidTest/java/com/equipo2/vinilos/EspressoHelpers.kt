package com.equipo2.vinilos

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import org.hamcrest.Matcher

private const val POLL_INTERVAL_MS = 250L

/**
 * Espera a que una vista aparezca. Espresso no sabe cuándo termina una llamada de red, y BackVynils, en Render, puede
 * tardar varios segundos en responder la primera vez.
 */
fun waitForView(matcher: Matcher<View>, timeoutMs: Long = 60_000) {
    val deadline = System.currentTimeMillis() + timeoutMs
    var lastError: Throwable? = null
    while (System.currentTimeMillis() < deadline) {
        try {
            onView(matcher).check(matches(isDisplayed()))
            return
        } catch (e: NoMatchingViewException) {
            lastError = e
        } catch (e: AssertionError) {
            lastError = e
        }
        Thread.sleep(POLL_INTERVAL_MS)
    }
    throw AssertionError("La vista no apareció en ${timeoutMs / 1000} s", lastError)
}

/** Espera a que se cumpla una condición, por ejemplo que la actividad se recree con otro tema. */
fun waitUntil(timeoutMs: Long = 10_000, condition: () -> Boolean) {
    val deadline = System.currentTimeMillis() + timeoutMs
    while (System.currentTimeMillis() < deadline) {
        if (runCatching(condition).getOrDefault(false)) return
        Thread.sleep(POLL_INTERVAL_MS)
    }
    throw AssertionError("La condición no se cumplió en ${timeoutMs / 1000} s")
}
