package com.equipo2.vinilos.ui.session

import com.equipo2.vinilos.FakePreferencesRepository
import com.equipo2.vinilos.MainDispatcherRule
import com.equipo2.vinilos.data.model.Role
import com.equipo2.vinilos.data.model.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SessionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val preferences = FakePreferencesRepository()
    private val viewModel by lazy { SessionViewModel(preferences) }

    @Test
    fun alEntrarConUnRol_seGuardaElRol() {
        viewModel.enterAs(Role.COLLECTOR)
        assertEquals(Role.COLLECTOR, viewModel.role.value)
        assertEquals(Role.COLLECTOR, preferences.role.value)
    }

    @Test
    fun cambiarDeRol_avisaDelCambio() = runTest {
        viewModel.enterAs(Role.COLLECTOR)
        viewModel.changeRole(Role.VISITOR)
        assertEquals(SessionEvent.RoleChanged(Role.VISITOR), viewModel.events.first())
        assertEquals(Role.VISITOR, preferences.role.value)
    }

    @Test
    fun elegirElMismoRol_noHaceNada() {
        viewModel.enterAs(Role.VISITOR)
        viewModel.changeRole(Role.VISITOR)
        viewModel.changeRole(Role.COLLECTOR)
        assertEquals(Role.COLLECTOR, viewModel.role.value)
    }

    @Test
    fun elModoOscuro_seRecuerda() {
        viewModel.setDarkMode(true)
        assertEquals(ThemeMode.DARK, preferences.themeMode.value)
        viewModel.setDarkMode(false)
        assertEquals(ThemeMode.LIGHT, preferences.themeMode.value)
    }

    @Test
    fun alCrearse_leeLoGuardado() {
        preferences.role.value = Role.COLLECTOR
        preferences.themeMode.value = ThemeMode.DARK
        val restored = SessionViewModel(preferences)
        assertEquals(Role.COLLECTOR, restored.role.value)
        assertEquals(ThemeMode.DARK, restored.themeMode.value)
    }
}
