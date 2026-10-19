package com.equipo2.vinilos

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.equipo2.vinilos.data.model.ThemeMode
import com.equipo2.vinilos.ui.common.ThemeModes
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas E2E de HU07 · Crear un álbum: un escenario por cada criterio de aceptación.
 * Usan BackVynils real, salvo el error al guardar, que reemplaza el repositorio por uno que falla.
 */
@RunWith(AndroidJUnit4::class)
class HU07CrearAlbumTest {

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        runBlocking { ServiceLocator.preferencesRepository.setThemeMode(ThemeMode.LIGHT) }
        InstrumentationRegistry.getInstrumentation().runOnMainSync { ThemeModes.apply(ThemeMode.LIGHT) }
        ServiceLocator.albumRepositoryOverride = null
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
        ServiceLocator.albumRepositoryOverride = null
    }

    // Criterio 1: al abrir la app se muestra la selección de rol y al elegir un rol se entra a Álbumes.
    @Test
    fun alAbrir_muestraLaSeleccionDeRol_yAlElegirEntraAAlbumes() {
        launch()
        onView(withText(R.string.role_select_question)).check(matches(isDisplayed()))
        onView(withId(R.id.btnCollector)).check(matches(isDisplayed()))
        onView(withId(R.id.btnVisitor)).perform(click())
        onView(toolbarTitle(R.string.album_list_title)).check(matches(isDisplayed()))
        onView(withId(R.id.bottomNav)).check(matches(isDisplayed()))
    }

    // Criterio 2: desde el chip de rol se puede cambiar de rol.
    @Test
    fun chipDeRol_cambiaAVisitante_avisa_yOcultaElBoton() {
        launch()
        enterAsCollector()
        onView(withId(R.id.roleChip)).perform(click())
        onView(withText("MODO ACTUAL: COLECCIONISTA")).inRoot(isPlatformPopup()).check(matches(isDisplayed()))
        onView(withText(R.string.role_visitor)).inRoot(isPlatformPopup()).perform(click())
        onView(withText(R.string.snackbar_role_visitor)).check(matches(isDisplayed()))
        onView(withId(R.id.fabCreate)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    // Criterio 2: desde el chip de rol se activa el modo oscuro, el menú sigue abierto y la preferencia se recuerda.
    @Test
    fun chipDeRol_activaElModoOscuro_yLaPreferenciaSeRecuerda() {
        launch()
        enterAsCollector()
        onView(withId(R.id.roleChip)).perform(click())
        onView(withId(R.id.switchDarkMode)).inRoot(isPlatformPopup()).perform(click())
        waitUntil { ThemeModes.isDark(currentActivity()) }
        waitForView(allOf(withId(R.id.switchDarkMode), isChecked()))
        pressBack()

        scenario.close()
        launch()
        scenario.onActivity { assertTrue(ThemeModes.isDark(it)) }

        onView(withId(R.id.btnTheme)).perform(click())
        waitUntil { !ThemeModes.isDark(currentActivity()) }
        scenario.onActivity { assertFalse(ThemeModes.isDark(it)) }
    }

    // Criterio 2: desde el chip de rol se puede volver al inicio.
    @Test
    fun chipDeRol_volverAlInicio_muestraLaSeleccionDeRol() {
        launch()
        enterAsCollector()
        onView(withId(R.id.roleChip)).perform(click())
        onView(withText(R.string.role_menu_go_home)).inRoot(isPlatformPopup()).perform(click())
        onView(withText(R.string.role_select_question)).check(matches(isDisplayed()))
    }

    // Criterio 3: solo el coleccionista ve el botón + en Álbumes.
    @Test
    fun coleccionista_veElBotonCrear() {
        launch()
        enterAsCollector()
        onView(withId(R.id.fabCreate)).check(matches(isDisplayed()))
    }

    @Test
    fun visitante_noVeElBotonCrear() {
        launch()
        onView(withId(R.id.btnVisitor)).perform(click())
        waitForView(toolbarTitle(R.string.album_list_title))
        onView(withId(R.id.fabCreate)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    // Criterio 4: guardar con campos vacíos muestra el error debajo de cada campo.
    @Test
    fun guardarVacio_muestraElErrorDeCadaCampo() {
        launch()
        openCreateForm()
        onView(withId(R.id.btnSave)).perform(click())
        onView(withText(R.string.error_album_name_required)).check(matches(isDisplayed()))
        onView(withText(R.string.error_cover_required)).check(matches(isDisplayed()))
        onView(withText(R.string.error_date_required)).check(matches(isDisplayed()))
        onView(withText(R.string.error_genre_required)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(R.string.error_label_required)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(R.string.error_description_required)).perform(scrollTo()).check(matches(isDisplayed()))
    }

    // Criterio 4: guardar con un campo inválido muestra su error.
    @Test
    fun urlInvalida_muestraElErrorDeFormato() {
        launch()
        openCreateForm()
        onView(withId(R.id.etCover)).perform(replaceText("portada.jpg"), closeSoftKeyboard())
        onView(withId(R.id.btnSave)).perform(click())
        onView(withText(R.string.error_cover_invalid)).check(matches(isDisplayed()))
    }

    // Criterio 5: al guardar se vuelve a Álbumes con el aviso Álbum creado (BackVynils real).
    @Test
    fun crearAlbumValido_vuelveAAlbumesConElAviso() {
        launch()
        openCreateForm()
        fillForm("E2E HU07 ${System.currentTimeMillis()}")
        onView(withId(R.id.btnSave)).perform(click())
        waitForView(withText(R.string.snackbar_album_created))
        onView(toolbarTitle(R.string.album_list_title)).check(matches(isDisplayed()))
    }

    // Criterio 6: si el guardado falla, el formulario conserva los datos y muestra un aviso de error.
    @Test
    fun siFallaElGuardado_conservaLosDatos_yMuestraElAviso() {
        ServiceLocator.albumRepositoryOverride = FailingAlbumRepository()
        launch()
        openCreateForm()
        fillForm("Álbum que no se guarda")
        onView(withId(R.id.btnSave)).perform(click())
        waitForView(withText(R.string.error_album_create_failed))
        onView(toolbarTitle(R.string.album_create_title)).check(matches(isDisplayed()))
        onView(withId(R.id.etName)).perform(scrollTo()).check(matches(withText("Álbum que no se guarda")))
    }

    @Test
    fun cancelarConDatos_pideConfirmacion_yDescartarVuelveAAlbumes() {
        launch()
        openCreateForm()
        onView(withId(R.id.etName)).perform(replaceText("Borrador"), closeSoftKeyboard())
        onView(withId(R.id.btnCancel)).perform(click())
        onView(withText(R.string.discard_title)).check(matches(isDisplayed()))
        onView(withText(R.string.discard_confirm)).perform(click())
        onView(toolbarTitle(R.string.album_list_title)).check(matches(isDisplayed()))
    }

    @Test
    fun atrasConElFormularioVacio_vuelveSinPreguntar() {
        launch()
        openCreateForm()
        pressBack()
        onView(toolbarTitle(R.string.album_list_title)).check(matches(isDisplayed()))
    }

    private fun launch() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    private fun currentActivity(): MainActivity {
        lateinit var activity: MainActivity
        scenario.onActivity { activity = it }
        return activity
    }

    private fun toolbarTitle(title: Int) = allOf(withText(title), isDescendantOfA(withId(R.id.toolbar)))

    private fun enterAsCollector() {
        onView(withId(R.id.btnCollector)).perform(click())
        waitForView(withId(R.id.fabCreate))
    }

    private fun openCreateForm() {
        enterAsCollector()
        onView(withId(R.id.fabCreate)).perform(click())
        waitForView(toolbarTitle(R.string.album_create_title))
    }

    private fun fillForm(name: String) {
        onView(withId(R.id.etName)).perform(replaceText(name), closeSoftKeyboard())
        onView(withId(R.id.etCover)).perform(replaceText("https://i.imgur.com/vinilo.jpg"), closeSoftKeyboard())
        onView(withId(R.id.etDate)).perform(click())
        onView(withText(R.string.action_accept)).perform(click())
        onView(withId(R.id.acGenre)).perform(scrollTo(), click())
        onView(withText("Rock")).inRoot(isPlatformPopup()).perform(click())
        onView(withId(R.id.acLabel)).perform(scrollTo(), click())
        onView(withText("EMI")).inRoot(isPlatformPopup()).perform(click())
        onView(withId(R.id.etDescription)).perform(scrollTo(), replaceText("Álbum creado por la prueba E2E de HU07."), closeSoftKeyboard())
    }
}
