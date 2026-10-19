# Vinilos · Paso 9 de 9: T8 · Pruebas de la historia

Tarea de la HU07: **«Realizar las pruebas de la historia»**.

Con este paso termina la HU07 y queda lista la base de la app. Se agregan:

- **28 pruebas unitarias** (se ejecutan en el computador): las reglas del formulario, el ViewModel de Crear álbum, el
  de sesión (rol y tema), `resultOf` y el formato de fechas para la API y para la pantalla.
- **12 pruebas E2E con Espresso** (se ejecutan en el emulador o el teléfono, sobre la app completa): al menos un
  escenario por cada criterio de aceptación, contra BackVynils real.
- Se borran las dos pruebas de ejemplo de la plantilla.

| Paso | Commit |
|---|---|
| 1 | Proyecto base de Android Studio |
| 2 | T1 · Configuración, capa de red y estructura MVVM |
| 3 | T2 · Tema claro y oscuro y navegación |
| 4 | T3 · Componentes de carga, error, vacío y avisos |
| 5 | T4 · Selección de rol y menú de rol |
| 6 | T5 · Botón + en Álbumes |
| 7 | T6 · Formulario de crear álbum con validaciones |
| 8 | T7 · Creación del álbum en la API |
| **9** | **T8 · Pruebas de la historia** |

## Resultados

| Dispositivo | Pruebas E2E |
|---|---|
| Emulador Android 5.0.2 (API 21) | 12 de 12 |
| Emulador Android 9 (API 28) | 12 de 12 |
| Emulador Android 17 (API 37) | 12 de 12 |
| Pruebas unitarias (JVM) | 28 de 28 |

Lint: 0 errores (las advertencias que quedan son las esperadas del paso 4).

Las pruebas E2E encontraron un error que solo pasaba en Android 5.0: la app se cerraba al mostrar el marcador de la
vista previa de la portada. Se corrigió en el paso 7, donde nació ese marcador (su README explica el porqué).

## Criterios de aceptación y sus pruebas E2E

| Criterio de HU07 | Prueba E2E (`HU07CrearAlbumTest`) |
|---|---|
| Al abrir la app se muestra la selección de rol y al elegir un rol se entra a Álbumes | `alAbrir_muestraLaSeleccionDeRol_yAlElegirEntraAAlbumes` |
| Desde el chip de rol se puede cambiar de rol, activar el modo oscuro o volver al inicio, y las preferencias se recuerdan | `chipDeRol_cambiaAVisitante_avisa_yOcultaElBoton`, `chipDeRol_activaElModoOscuro_yLaPreferenciaSeRecuerda`, `chipDeRol_volverAlInicio_muestraLaSeleccionDeRol` |
| Solo el coleccionista ve el botón + en Álbumes | `coleccionista_veElBotonCrear`, `visitante_noVeElBotonCrear` |
| Guardar con campos vacíos o inválidos muestra el error debajo de cada campo | `guardarVacio_muestraElErrorDeCadaCampo`, `urlInvalida_muestraElErrorDeFormato` |
| Al guardar se muestra el progreso y luego se vuelve a Álbumes con el aviso Álbum creado | `crearAlbumValido_vuelveAAlbumesConElAviso` |
| Si el guardado falla, el formulario conserva los datos y muestra un aviso de error | `siFallaElGuardado_conservaLosDatos_yMuestraElAviso` |
| (Diseño, propuesta de la guía) Salir con datos pide confirmación; salir vacío no | `cancelarConDatos_pideConfirmacion_yDescartarVuelveAAlbumes`, `atrasConElFormularioVacio_vuelveSinPreguntar` |

## Orden en que se hicieron los cambios

1. Borrar `ExampleUnitTest.kt` y `ExampleInstrumentedTest.kt` de la plantilla.
2. Pruebas unitarias: la regla del hilo principal, los repositorios falsos y las pruebas de cada clase.
3. Pruebas E2E: las ayudas de espera, el repositorio que falla y `HU07CrearAlbumTest`.
4. `app/build.gradle.kts`: desactivar animaciones en las pruebas; `gradle.properties`: el arreglo para Android 5.
5. Correr las unitarias (28/28), Lint (0 errores) y las E2E en los emuladores de Android 5.0.2, 9 y 17.

## Cómo ejecutar el proyecto

### Requisitos

- Android Studio (probado con la versión 2026.2, que incluye el JDK).
- Android SDK con la plataforma 37 (Android Studio la ofrece al abrir el proyecto si falta).
- Un emulador o teléfono con **Android 5.0 (API 21) o superior** y conexión a Internet.

### La app

1. Clonar el repositorio y abrir la carpeta en Android Studio (**File › Open**).
2. Esperar a que termine **Gradle Sync**.
3. Elegir el dispositivo y pulsar **Run ▶**.

Desde una terminal: `./gradlew assembleDebug` (en Windows `gradlew.bat assembleDebug`). El APK queda en
`app/build/outputs/apk/debug/app-debug.apk`.

### Las pruebas unitarias

- En Android Studio: clic derecho sobre `app/src/test/java` › **Run 'Tests in…'**.
- Terminal: `./gradlew testDebugUnitTest`. El reporte queda en `app/build/reports/tests/testDebugUnitTest/index.html`.

### Las pruebas E2E

1. Encender un emulador o conectar un teléfono (con Internet).
2. Recomendado: desactivar las animaciones del dispositivo (Opciones de desarrollador › *Escala de animación de
   ventana*, *Escala de animación de transición* y *Escala de duración de animador* en «Desactivada»). Espresso es más
   estable sin animaciones; al terminar se pueden volver a activar.
3. En Android Studio: clic derecho sobre `app/src/androidTest/java` › **Run 'Tests in…'**. Terminal:
   `./gradlew connectedDebugAndroidTest`. El reporte queda en `app/build/reports/androidTests/connected/`.

La prueba `crearAlbumValido_vuelveAAlbumesConElAviso` crea un álbum real llamado «E2E HU07 …» en el backend
compartido. La primera vez puede tardar mientras el servidor de Render despierta; las pruebas esperan hasta 60 s.

---

## Explicación de cada archivo

### Archivos borrados

- `app/src/test/java/com/equipo2/vinilos/ExampleUnitTest.kt` y `app/src/androidTest/java/com/equipo2/vinilos/ExampleInstrumentedTest.kt`:
  las pruebas de ejemplo de la plantilla (`2 + 2 == 4` y el nombre del paquete). Las reemplazan las pruebas reales.

### `app/build.gradle.kts` · modificado por mí

```kotlin
    testOptions {
        animationsDisabled = true
    }
```

- Al correr las pruebas instrumentadas con Gradle, le pide al dispositivo que ejecute la app sin animaciones. Espresso
  espera a que la interfaz esté quieta, y las animaciones lo pueden hacer fallar o demorar.

### `gradle.properties` · modificado por mí

```properties
# En Android 5.0 (API 21) AGP no puede copiar la salida adicional de las pruebas y entra en un bucle.
android.enableAdditionalTestOutput=false
```

- Al terminar las pruebas en el dispositivo, AGP intenta copiar archivos extra que las pruebas pueden generar. En
  Android 5.0 ese paso falla y se queda repitiéndose sin terminar nunca (se encontró al correr las pruebas en el
  emulador de API 21). Estas pruebas no generan archivos extra, así que se desactiva.

---

### `app/src/test/java/com/equipo2/vinilos/MainDispatcherRule.kt` · creado por mí

```kotlin
/** Reemplaza Dispatchers.Main, que no existe fuera de Android, para probar ViewModels en el computador. */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
```

- `viewModelScope` usa el hilo principal de Android (`Dispatchers.Main`), que no existe en una prueba unitaria.
- Es una **regla de JUnit** (`TestWatcher`): `starting` se ejecuta antes de cada prueba y `finished` después. Antes
  reemplaza el hilo principal por uno de prueba y después lo restaura.
- `UnconfinedTestDispatcher`: ejecuta las corrutinas de inmediato, así cuando `save()` termina, el resultado ya está.
- `@OptIn(ExperimentalCoroutinesApi::class)`: estas herramientas están marcadas como experimentales; hay que aceptarlo
  explícitamente.

### `app/src/test/java/com/equipo2/vinilos/Fakes.kt` · creado por mí

**Dobles de prueba**: implementaciones falsas de las interfaces de repositorio. Por eso los repositorios son
interfaces (pasos 2 y 5): las pruebas no necesitan Internet ni DataStore.

```kotlin
/** Repositorio de álbumes en memoria: guarda lo que se le pide crear o falla a voluntad. */
class FakeAlbumRepository(var failCreate: Boolean = false) : AlbumRepository {

    val created = mutableListOf<NewAlbum>()

    override suspend fun createAlbum(album: NewAlbum): Result<Album> {
        if (failCreate) return Result.failure(IOException("Sin conexión"))
        created += album
        return Result.success(Album(id = created.size, name = album.name, …))
    }
}
```

- Con `failCreate = true` simula un error de red; si no, guarda el álbum en la lista `created` (para que la prueba
  revise qué se "envió") y devuelve un álbum con un id consecutivo.

```kotlin
/** Preferencias en memoria en lugar de DataStore. */
class FakePreferencesRepository : PreferencesRepository {

    override val role = MutableStateFlow<Role?>(null)
    override val themeMode = MutableStateFlow(ThemeMode.SYSTEM)

    override suspend fun setRole(role: Role) {
        this.role.value = role
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        themeMode.value = mode
    }
}
```

- Guarda el rol y el tema en `MutableStateFlow` (que también son `Flow`, como pide la interfaz). La prueba puede leer
  lo guardado o poner valores iniciales.

### `app/src/test/java/com/equipo2/vinilos/domain/AlbumFormValidatorTest.kt` · creado por mí

Prueba las reglas del formulario (paso 7). Usa una fecha de "hoy" fija para que la prueba dé lo mismo cualquier día.

```kotlin
    private val today = LocalDate.of(2026, 10, 18)
    private val validForm = AlbumForm(name = "Buscando América", cover = "https://ejemplo.com/portada.jpg", …)
```

- Un formulario válido de base; cada prueba cambia un campo con `copy(…)`.

| Prueba | Qué comprueba |
|---|---|
| `formularioValido_noTieneErrores` | Un formulario correcto no tiene errores |
| `formularioVacio_todosLosCamposSonObligatorios` | Vacío: los seis campos tienen error y todos son "obligatorio" |
| `soloEspacios_cuentaComoVacio` | Un nombre o descripción con solo espacios es obligatorio |
| `urlSinProtocolo_esInvalida` | `ejemplo.com/portada.jpg` da "URL no válida" |
| `formatosDeUrl` | Acepta `http://` y mayúsculas; rechaza sin punto, `ftp://` y con espacios |
| `fechaFutura_esInvalida_yHoyEsValida` | Mañana es error; hoy es válido |
| `generoOSelloQueLaApiNoAcepta_sonInvalidos` | `Pop` u `Otro` (que BackVynils rechazaría con 400) son error |
| `longitudesMaximas` | 101 caracteres en el nombre y 1001 en la descripción son error; 100 es válido |
| `losErroresVanEnElOrdenDelFormulario` | El primer error es el del campo más arriba |
| `alConvertirParaLaApi_seRecortanLosEspacios` | `toNewAlbum()` quita los espacios sobrantes |

- `@Test` marca cada prueba. `assertEquals(esperado, real)`, `assertTrue(…)` y `assertFalse(…)` hacen fallar la prueba
  si no se cumple. Los nombres describen el caso en español, separados con `_`.

### `app/src/test/java/com/equipo2/vinilos/ui/albums/create/AlbumCreateViewModelTest.kt` · creado por mí

```kotlin
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAlbumRepository()
    private val viewModel = AlbumCreateViewModel(repository) { LocalDate.of(2026, 10, 18) }
```

- `@get:Rule`: aplica la regla del hilo principal a cada prueba.
- El ViewModel recibe el repositorio falso y una fecha de hoy fija (la lambda del final es el parámetro `today`).

```kotlin
    private fun fillValidForm() { viewModel.onNameChange("Siembra"); … }
```

- Llena el formulario como lo haría la pantalla.

| Prueba | Qué comprueba |
|---|---|
| `antesDeGuardar_noSeMuestranErrores` | Escribir una URL inválida no muestra error hasta guardar |
| `guardarVacio_muestraLosErrores_yNoLlamaALaApi` | Seis errores, evento `ShowErrors(NAME)` y el repositorio no recibió nada |
| `despuesDeGuardar_losErroresSeActualizanAlEscribir` | Tras guardar, el error del nombre desaparece al escribirlo y aparece el de la URL al escribir una inválida |
| `guardarValido_creaElAlbum_yAvisa` | Evento `Created` con el álbum, el repositorio recibió uno y ya no está guardando |
| `siFallaElGuardado_conservaLosDatos_yAvisa` | Con el repositorio fallando: evento `SaveFailed` y el formulario sigue igual |
| `formularioVacio_permiteCerrarSinConfirmar` | `isEmpty` es verdadero al inicio y falso al escribir |

- `runTest { … }`: ejecuta una prueba con corrutinas. `viewModel.events.first()` espera el primer evento enviado.

### `app/src/test/java/com/equipo2/vinilos/ui/session/SessionViewModelTest.kt` · creado por mí

```kotlin
    private val preferences = FakePreferencesRepository()
    private val viewModel by lazy { SessionViewModel(preferences) }
```

- `by lazy`: el ViewModel se crea la primera vez que se usa, ya dentro de la prueba. Si se creara al construir la clase
  de prueba, su `init` correría antes de que la regla reemplace el hilo principal.

| Prueba | Qué comprueba |
|---|---|
| `alEntrarConUnRol_seGuardaElRol` | `enterAs` cambia el rol y lo guarda |
| `cambiarDeRol_avisaDelCambio` | `changeRole` envía `RoleChanged` y guarda el rol nuevo |
| `elegirElMismoRol_noHaceNada` | Elegir el rol actual no cambia nada; elegir otro sí |
| `elModoOscuro_seRecuerda` | `setDarkMode(true/false)` guarda `DARK` y `LIGHT` |
| `alCrearse_leeLoGuardado` | Un ViewModel nuevo arranca con el rol y el tema guardados |

### `app/src/test/java/com/equipo2/vinilos/data/network/NetworkServiceAdapterTest.kt` · creado por mí

| Prueba | Qué comprueba |
|---|---|
| `laFechaSeEnviaEnIsoCompletoAMedianocheUtc` | `1984-08-01` se envía como `"1984-08-01T00:00:00.000Z"`, el formato que BackVynils acepta |
| `laFechaDeLaApiSeLeeComoDia` | La fecha de la API se lee como el día escrito, también la de los álbumes que el servidor guardó a medianoche de Colombia (`05:00Z`) |

### `app/src/test/java/com/equipo2/vinilos/data/repository/ResultOfTest.kt` · creado por mí

`resultOf` (paso 2) lo comparten todos los repositorios, así que se prueba directamente:

| Prueba | Qué comprueba |
|---|---|
| `siElBloqueTermina_devuelveElValor` | Un bloque que termina bien da `Result.success` con su valor |
| `siElBloqueFalla_devuelveElError` | Un bloque que lanza una excepción (aquí, sin conexión) da `Result.failure` con esa excepción |
| `laCancelacion_noSeConvierteEnError` | Una `CancellationException` se vuelve a lanzar en lugar de convertirse en `Result.failure` |

- `runCatching { … }.exceptionOrNull()` en la última prueba: atrapa lo que lance `resultOf` para comprobar que es la
  misma cancelación.

### `app/src/test/java/com/equipo2/vinilos/ui/common/DateFormatsTest.kt` · creado por mí

| Prueba | Qué comprueba |
|---|---|
| `fechaLarga_enEspanolYConElMesEnMinusculas` | `1984-08-01` se muestra como «1 de agosto de 1984» |
| `milisegundosUtc_idaYVuelta` | Convertir una fecha a milisegundos y de vuelta da la misma fecha |

---

### `app/src/androidTest/java/com/equipo2/vinilos/EspressoHelpers.kt` · creado por mí

```kotlin
private const val POLL_INTERVAL_MS = 250L
```

- Cada cuánto se vuelve a revisar mientras se espera.

```kotlin
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
```

- Espresso espera solo a que la interfaz esté quieta, pero no sabe nada de las llamadas de red. Esta función revisa
  cada 250 ms si la vista ya se ve, hasta 60 s.
- `Matcher<View>`: la descripción de la vista buscada (por ejemplo, `withText(…)`).
- `onView(matcher).check(matches(isDisplayed()))`: falla si no existe (`NoMatchingViewException`) o si no se ve
  (`AssertionError`); ambos casos se reintentan.
- Si se acaba el tiempo, falla con el último error como causa, para saber qué pasó.
- `60_000`: los guiones bajos separan miles y solo ayudan a leer el número.

```kotlin
fun waitUntil(timeoutMs: Long = 10_000, condition: () -> Boolean) {
    val deadline = System.currentTimeMillis() + timeoutMs
    while (System.currentTimeMillis() < deadline) {
        if (runCatching(condition).getOrDefault(false)) return
        Thread.sleep(POLL_INTERVAL_MS)
    }
    throw AssertionError("La condición no se cumplió en ${timeoutMs / 1000} s")
}
```

- Igual, pero para cualquier condición (por ejemplo, "la actividad ya está en modo oscuro"). Si la condición lanza un
  error (la actividad se está recreando), cuenta como "todavía no".

### `app/src/androidTest/java/com/equipo2/vinilos/FailingAlbumRepository.kt` · creado por mí

```kotlin
/** Simula que el servidor falla al guardar: no se puede forzar un error en el backend compartido. */
class FailingAlbumRepository : AlbumRepository {
    override suspend fun createAlbum(album: NewAlbum): Result<Album> = Result.failure(IOException("Servidor no disponible"))
}
```

- Para probar el criterio de error se reemplaza el repositorio real por este con
  `ServiceLocator.albumRepositoryOverride` (paso 2). Es la única prueba E2E que no usa la API real.

### `app/src/androidTest/java/com/equipo2/vinilos/HU07CrearAlbumTest.kt` · creado por mí

```kotlin
@RunWith(AndroidJUnit4::class)
class HU07CrearAlbumTest {

    private lateinit var scenario: ActivityScenario<MainActivity>
```

- Se ejecuta en el dispositivo con el runner de AndroidX.
- `ActivityScenario`: abre y cierra la actividad real para cada prueba.

```kotlin
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
```

- `@Before` (antes de cada prueba): tema claro guardado y aplicado (`runOnMainSync`, porque AppCompat exige el hilo
  principal) y repositorio real. Así ninguna prueba depende de lo que dejó la anterior.
- `@After` (después de cada prueba): cierra la actividad si se abrió (`::scenario.isInitialized` revisa si la
  `lateinit` ya tiene valor) y quita el repositorio de reemplazo.

Cada prueba sigue el patrón de Espresso: **buscar** una vista (`onView(withId(…))` o `onView(withText(…))`),
**actuar** (`perform(click())`, `perform(replaceText(…))`) y **comprobar** (`check(matches(isDisplayed()))`). Los textos
se buscan por su id de `strings.xml` (`withText(R.string.…)`), así la prueba no se rompe si cambia la redacción.

```kotlin
    @Test
    fun alAbrir_muestraLaSeleccionDeRol_yAlElegirEntraAAlbumes() {
        launch()
        onView(withText(R.string.role_select_question)).check(matches(isDisplayed()))
        onView(withId(R.id.btnCollector)).check(matches(isDisplayed()))
        onView(withId(R.id.btnVisitor)).perform(click())
        onView(toolbarTitle(R.string.album_list_title)).check(matches(isDisplayed()))
        onView(withId(R.id.bottomNav)).check(matches(isDisplayed()))
    }
```

- Abre la app: se ve «¿Cómo quieres entrar?» y los dos botones. Toca Visitante: la barra superior dice «Álbumes» y se
  ve la barra inferior.

```kotlin
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
```

- Entra como coleccionista y abre el menú. `inRoot(isPlatformPopup())`: el menú es una ventana flotante aparte, y hay
  que decirle a Espresso que busque ahí.
- El encabezado dice el rol actual; al elegir Visitante aparece el aviso y el botón + queda oculto (`GONE`).

```kotlin
    @Test
    fun chipDeRol_activaElModoOscuro_yLaPreferenciaSeRecuerda() {
        launch()
        enterAsCollector()
        onView(withId(R.id.roleChip)).perform(click())
        onView(withId(R.id.switchDarkMode)).inRoot(isPlatformPopup()).perform(click())
        waitUntil { ThemeModes.isDark(currentActivity()) }
        waitForView(allOf(withId(R.id.switchDarkMode), isChecked()))
        pressBack()
```

- Activa el modo oscuro desde el menú, espera a que la actividad se recree en oscuro y a que el menú vuelva a
  aparecer con el interruptor activado (`allOf`: la vista debe cumplir las dos condiciones). Atrás lo cierra.

```kotlin
        scenario.close()
        launch()
        scenario.onActivity { assertTrue(ThemeModes.isDark(it)) }

        onView(withId(R.id.btnTheme)).perform(click())
        waitUntil { !ThemeModes.isDark(currentActivity()) }
        scenario.onActivity { assertFalse(ThemeModes.isDark(it)) }
    }
```

- Cierra y vuelve a abrir la app: sigue en oscuro (la preferencia se recordó). `onActivity { … }` ejecuta código con
  la actividad real.
- Con el botón de la selección de rol vuelve a claro.

```kotlin
    @Test
    fun chipDeRol_volverAlInicio_muestraLaSeleccionDeRol() { … }
```

- Desde el menú, «Volver a la pantalla de inicio» muestra otra vez la selección de rol.

```kotlin
    @Test
    fun coleccionista_veElBotonCrear() { … }

    @Test
    fun visitante_noVeElBotonCrear() {
        launch()
        onView(withId(R.id.btnVisitor)).perform(click())
        waitForView(toolbarTitle(R.string.album_list_title))
        onView(withId(R.id.fabCreate)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }
```

- El coleccionista ve el botón +; el visitante no (el botón existe en el layout pero está oculto).

```kotlin
    @Test
    fun guardarVacio_muestraElErrorDeCadaCampo() {
        launch()
        openCreateForm()
        onView(withId(R.id.btnSave)).perform(click())
        onView(withText(R.string.error_album_name_required)).check(matches(isDisplayed()))
        …
        onView(withText(R.string.error_description_required)).perform(scrollTo()).check(matches(isDisplayed()))
    }
```

- Guardar vacío muestra los seis mensajes. `scrollTo()` desplaza el formulario hasta los que quedan abajo.

```kotlin
    @Test
    fun urlInvalida_muestraElErrorDeFormato() { … }
```

- Escribe `portada.jpg` en la URL, guarda y aparece «URL no válida…».
- `replaceText` escribe el texto de una vez (más estable que teclear letra por letra); `closeSoftKeyboard` cierra el
  teclado para que no tape otras vistas.

```kotlin
    @Test
    fun crearAlbumValido_vuelveAAlbumesConElAviso() {
        launch()
        openCreateForm()
        fillForm("E2E HU07 ${System.currentTimeMillis()}")
        onView(withId(R.id.btnSave)).perform(click())
        waitForView(withText(R.string.snackbar_album_created))
        onView(toolbarTitle(R.string.album_list_title)).check(matches(isDisplayed()))
    }
```

- Llena el formulario y guarda **contra BackVynils**. El nombre lleva la hora en milisegundos para que cada álbum de
  prueba sea distinto. Espera hasta 60 s el aviso «Álbum creado» y comprueba que volvió a Álbumes.

```kotlin
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
```

- Con el repositorio que falla: aparece el aviso de error, la pantalla sigue siendo Crear álbum y el nombre sigue
  escrito.

```kotlin
    @Test
    fun cancelarConDatos_pideConfirmacion_yDescartarVuelveAAlbumes() { … }

    @Test
    fun atrasConElFormularioVacio_vuelveSinPreguntar() { … }
```

- Con algo escrito, Cancelar muestra «¿Descartar el álbum?» y Descartar vuelve a Álbumes. Con el formulario vacío,
  Atrás vuelve directo.

```kotlin
    private fun launch() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    private fun currentActivity(): MainActivity {
        lateinit var activity: MainActivity
        scenario.onActivity { activity = it }
        return activity
    }

    private fun toolbarTitle(title: Int) = allOf(withText(title), isDescendantOfA(withId(R.id.toolbar)))
```

- `launch`: abre la app. `currentActivity`: la actividad actual (cambia cuando se recrea por el tema).
- `toolbarTitle`: busca el texto **dentro de la barra superior**; «Álbumes» también aparece en la barra inferior y
  Espresso falla si encuentra dos vistas iguales.

```kotlin
    private fun enterAsCollector() {
        onView(withId(R.id.btnCollector)).perform(click())
        waitForView(withId(R.id.fabCreate))
    }

    private fun openCreateForm() {
        enterAsCollector()
        onView(withId(R.id.fabCreate)).perform(click())
        waitForView(toolbarTitle(R.string.album_create_title))
    }
```

- Pasos que repiten varias pruebas: entrar como coleccionista y abrir el formulario.

```kotlin
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
```

- Llena el formulario completo como un usuario: escribe nombre y URL, abre el calendario y acepta la fecha que
  propone (hoy), elige Rock y EMI en los desplegables (cuya lista es una ventana flotante) y escribe la descripción.

---

## Estructura final del proyecto

```
app/src/main/java/com/equipo2/vinilos/
├── VinilosApplication.kt            Inicia el ServiceLocator y aplica el tema guardado
├── ServiceLocator.kt                Inyección de dependencias manual
├── MainActivity.kt                  Actividad única: barras, chip y menú de rol, avisos
├── data/
│   ├── model/                       Album, NewAlbum, AlbumCatalog, Role, ThemeMode
│   ├── network/                     VinilosApiService (Retrofit), NetworkServiceAdapter, DTO
│   └── repository/                  AlbumRepository, PreferencesRepository (DataStore), ResultOf
├── domain/                          AlbumFormValidator
└── ui/
    ├── common/                      UiState, componentes de estado, AppSnackbar, Motion, ThemeModes, DateFormats, EmptySectionFragment
    ├── session/                     SessionViewModel
    ├── role/                        RoleSelectFragment, RoleMenu
    └── albums/                      AlbumListFragment (botón +); create/ AlbumCreateFragment, AlbumCreateViewModel
```

Cada historia nueva agrega lo suyo siguiendo el mismo esquema: su endpoint en `VinilosApiService` y
`NetworkServiceAdapter`, su repositorio en `data/repository`, su ViewModel y su fragment en `ui/<sección>`, y usa los
componentes de `ui/common`.
