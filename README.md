# Vinilos · Paso 5 de 9: T4 · Selección de rol y menú de rol

Tarea de la HU07: **«Construir la selección de rol y el menú de rol»**.

Cubre el criterio de aceptación **«Al abrir la app se muestra la selección de rol y al elegir un rol se entra a
Álbumes»** y el criterio **«Desde el chip de rol se puede cambiar de rol, activar el modo oscuro o volver al inicio,
y las preferencias se recuerdan»**.

- **Selección de rol** al abrir la app: logo, «Vinilos», «Entrar como visitante» y «Entrar como coleccionista», más un
  botón de luna o sol para el modo oscuro.
- **Chip de rol** en la barra superior: muestra el rol actual y abre el menú.
- **Menú de rol**: «MODO ACTUAL: …», cambiar a Visitante o Coleccionista (con aviso «Ahora estás en modo …»),
  interruptor de **modo oscuro** (el menú se queda abierto) y **volver a la pantalla de inicio**.
- El rol y el tema se **guardan** en el teléfono con DataStore; al volver a abrir la app se mantiene el tema.

La app no tiene login: el rol solo decide qué acciones se ven (en el paso 6 aparece el botón + del coleccionista).

| Paso | Commit |
|---|---|
| 1 | Proyecto base de Android Studio |
| 2 | T1 · Configuración, capa de red y estructura MVVM |
| 3 | T2 · Tema claro y oscuro y navegación |
| 4 | T3 · Componentes de carga, error, vacío y avisos |
| **5** | **T4 · Selección de rol y menú de rol** |
| 6 | T5 · Botón + en Álbumes |
| 7 | T6 · Formulario de crear álbum con validaciones |
| 8 | T7 · Creación del álbum en la API |
| 9 | T8 · Pruebas de la historia |

## Orden en que se hicieron los cambios

1. Gradle: agregar DataStore.
2. Datos: `Role.kt` y `ThemeMode.kt` (rol y tema) y `PreferencesRepository.kt` (guardarlos).
3. `ServiceLocator`: recibir el `Context` de la app y crear el repositorio de preferencias.
4. `VinilosApplication` y el manifiesto: iniciar el `ServiceLocator` y aplicar el tema guardado al abrir.
5. `ThemeModes.kt`: aplicar y consultar el tema.
6. `SessionViewModel.kt`: el estado de rol y tema compartido.
7. Recursos: íconos, logo, formas, colores, textos, medidas y estilos.
8. Selección de rol: `fragment_role_select.xml` y `RoleSelectFragment.kt`; transición `fade` en `Motion.kt`.
9. Chip y menú: `view_role_chip.xml`, `popup_role_menu.xml` y `RoleMenu.kt`.
10. Navegación: la selección de rol pasa a ser la primera pantalla.
11. `activity_main.xml` y `MainActivity.kt`: chip en la barra, menú, avisos de cambio de rol, ocultar las barras en la
    selección de rol y mantener el menú abierto al cambiar el tema.
12. Lint (0 errores) y prueba en el emulador.

## Cómo ejecutarlo

1. **File › Sync Project with Gradle Files** y **Run ▶**.
2. Aparece la selección de rol. Tocar **Entrar como coleccionista**: se llega a Álbumes con el chip «Coleccionista».
3. Tocar el chip: se abre el menú.
   - **Visitante**: el chip cambia y aparece el aviso «Ahora estás en modo visitante».
   - **Modo oscuro**: el interruptor se desliza, la app cambia a oscuro y el menú sigue abierto.
   - **Volver a la pantalla de inicio**: regresa a la selección de rol.
4. Cerrar la app y abrirla de nuevo: arranca en la selección de rol con el tema que se eligió.

---

## Explicación de cada archivo

### `gradle/libs.versions.toml` y `app/build.gradle.kts` · modificados por mí

```toml
datastore = "1.1.7"
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
```

```kotlin
    implementation(libs.androidx.datastore.preferences)
```

- **DataStore Preferences**: guarda pares clave-valor en el teléfono de forma asíncrona (reemplazo moderno de
  `SharedPreferences`). 1.1.7 es la última versión que soporta API 21 (la 1.2 exige API 23).

---

### `app/src/main/java/com/equipo2/vinilos/data/model/Role.kt` · creado por mí

```kotlin
package com.equipo2.vinilos.data.model

/** La app no tiene login: el usuario elige con qué rol entra. */
enum class Role { VISITOR, COLLECTOR }
```

- `enum class`: un tipo con un conjunto fijo de valores. `Role` tiene dos: visitante y coleccionista.

### `app/src/main/java/com/equipo2/vinilos/data/model/ThemeMode.kt` · creado por mí

```kotlin
package com.equipo2.vinilos.data.model

/** Preferencia de tema: por defecto sigue al sistema; el menú de rol la fuerza. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }
```

- Tres valores. `SYSTEM` es el inicial: la app sigue el modo del teléfono hasta que el usuario elige uno en la app.
- Va en su propio archivo: en Kotlin cada tipo principal vive en un archivo con su nombre, así se encuentra sin
  adivinar dónde está.

### `app/src/main/java/com/equipo2/vinilos/data/repository/PreferencesRepository.kt` · creado por mí

```kotlin
interface PreferencesRepository {
    val role: Flow<Role?>
    val themeMode: Flow<ThemeMode>
    suspend fun setRole(role: Role)
    suspend fun setThemeMode(mode: ThemeMode)
}
```

- La interfaz que usa el resto de la app. `Flow` es un flujo de valores en el tiempo: quien lo escucha recibe el valor
  guardado y cada cambio posterior.
- `role` puede ser `null` (el usuario todavía no eligió).
- Las funciones `set…` son `suspend` porque escribir en disco no debe bloquear la pantalla.

```kotlin
private val Context.dataStore by preferencesDataStore(name = "vinilos_prefs")
```

- Crea el archivo de DataStore `vinilos_prefs` como una propiedad de extensión de `Context`. `by preferencesDataStore`
  garantiza una sola instancia por proceso (DataStore falla si hay dos sobre el mismo archivo).

```kotlin
class DataStorePreferencesRepository(private val context: Context) : PreferencesRepository {

    override val role: Flow<Role?> = context.dataStore.data.map { prefs ->
        prefs[ROLE_KEY]?.let { name -> Role.entries.find { it.name == name } }
    }
```

- `context.dataStore.data`: el flujo con todas las preferencias.
- `.map { … }`: transforma cada valor del flujo. Lee el texto guardado y busca el `Role` con ese nombre.
- `Role.entries`: la lista de valores del enum. `find` devuelve `null` si no hay coincidencia, así un valor guardado
  inválido no hace fallar la app.

```kotlin
    override val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        ThemeMode.entries.find { it.name == prefs[THEME_KEY] } ?: ThemeMode.SYSTEM
    }
```

- Igual para el tema; si no hay nada guardado devuelve `SYSTEM`.

```kotlin
    override suspend fun setRole(role: Role) {
        context.dataStore.edit { it[ROLE_KEY] = role.name }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[THEME_KEY] = mode.name }
    }
```

- `edit { … }`: escribe de forma atómica. Se guarda el nombre del enum (`"COLLECTOR"`, `"DARK"`).

```kotlin
    private companion object {
        val ROLE_KEY = stringPreferencesKey("role")
        val THEME_KEY = stringPreferencesKey("theme_mode")
    }
}
```

- Las claves con su tipo (`String`). Son privadas: nadie fuera de esta clase conoce cómo se guardan los datos.

### `app/src/main/java/com/equipo2/vinilos/ServiceLocator.kt` · modificado por mí

```kotlin
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }
```

- **Nuevo.** DataStore necesita un `Context`. Se guarda el de la aplicación (`applicationContext`), que vive tanto
  como la app; guardar el de una actividad causaría una fuga de memoria.

```kotlin
    val preferencesRepository: PreferencesRepository by lazy { DataStorePreferencesRepository(appContext) }
```

- **Nuevo.** El repositorio de preferencias, creado la primera vez que se usa. El resto del archivo no cambió.

### `app/src/main/java/com/equipo2/vinilos/VinilosApplication.kt` · creado por mí

```kotlin
class VinilosApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
        // El tema guardado se aplica antes de crear la primera pantalla para que no aparezca con el tema equivocado.
        val themeMode = runBlocking { ServiceLocator.preferencesRepository.themeMode.first() }
        ThemeModes.apply(themeMode)
    }
}
```

- `Application` es la clase que Android crea antes que cualquier pantalla; `onCreate` se ejecuta una vez por proceso.
- Inicia el `ServiceLocator` con el contexto de la app.
- `runBlocking { … .first() }`: espera el primer valor del flujo (el tema guardado). Es la única espera bloqueante de
  la app y es corta (leer un archivo pequeño); vale la pena para que la primera pantalla ya salga con el tema correcto.

### `app/src/main/AndroidManifest.xml` · modificado por mí

```xml
    <application
        android:name=".VinilosApplication"
```

- **Única línea nueva**: le dice a Android que use `VinilosApplication` en lugar de la `Application` por defecto.

### `app/src/main/java/com/equipo2/vinilos/ui/common/ThemeModes.kt` · creado por mí

```kotlin
/** El tema se aplica con AppCompat, que recrea la actividad para que tome los colores de values-night. */
object ThemeModes {

    fun apply(mode: ThemeMode) {
        AppCompatDelegate.setDefaultNightMode(
            when (mode) {
                ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            }
        )
    }
```

- `AppCompatDelegate.setDefaultNightMode`: cambia el modo noche de toda la app. Si cambia, AppCompat **recrea** la
  actividad para que vuelva a cargar los recursos (`values-night`). Si es el mismo modo, no hace nada.
- `when (mode)`: convierte nuestro `ThemeMode` en la constante de AppCompat. Al cubrir los tres valores no necesita
  `else`.

```kotlin
    fun isDark(context: Context): Boolean =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
}
```

- Dice si la pantalla está en oscuro **ahora** (sea porque lo eligió el usuario o el teléfono).
- `uiMode` guarda varias opciones en bits; `and UI_MODE_NIGHT_MASK` se queda solo con los bits del modo noche. Los
  paréntesis son necesarios porque en Kotlin `and` tiene menos prioridad que `==`.

### `app/src/main/java/com/equipo2/vinilos/ui/session/SessionViewModel.kt` · creado por mí

El primer **ViewModel** de la app. Un ViewModel guarda el estado de la interfaz y **sobrevive** cuando la actividad se
recrea (al girar el teléfono o al cambiar el tema), por eso el rol no se pierde.

```kotlin
sealed interface SessionEvent {
    data class RoleChanged(val role: Role) : SessionEvent
}
```

- **Eventos**: cosas que pasan una vez (mostrar un aviso), a diferencia del **estado**, que siempre tiene un valor.

```kotlin
class SessionViewModel(private val preferences: PreferencesRepository) : ViewModel() {

    private val _role = MutableStateFlow<Role?>(null)
    val role: StateFlow<Role?> = _role.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()
```

- `StateFlow`: un flujo que siempre tiene un valor actual (`.value`) y avisa cada cambio.
- Patrón de *backing property*: `_role` es mutable y privado (solo el ViewModel lo cambia); `role` es la versión de
  solo lectura que ven las pantallas.

```kotlin
    private val _events = Channel<SessionEvent>(Channel.BUFFERED)
    val events: Flow<SessionEvent> = _events.receiveAsFlow()
```

- `Channel`: una cola donde cada evento se entrega **una sola vez**. Con un `StateFlow`, el aviso se repetiría al
  recrearse la actividad. `BUFFERED` guarda eventos si nadie está escuchando en ese instante.

```kotlin
    init {
        viewModelScope.launch { preferences.role.collect { _role.value = it } }
        viewModelScope.launch { preferences.themeMode.collect { _themeMode.value = it } }
    }
```

- Al crearse, escucha las preferencias guardadas y copia cada valor al estado.
- `viewModelScope.launch`: inicia una corrutina que se cancela sola cuando el ViewModel se destruye.

```kotlin
    /** Desde la selección de rol: guarda el rol con el que se entra a la app. */
    fun enterAs(role: Role) {
        _role.value = role
        viewModelScope.launch { preferences.setRole(role) }
    }
```

- Actualiza el estado **de inmediato** (para que la pantalla siguiente ya lo vea) y lo guarda en segundo plano.

```kotlin
    /** Desde el menú de rol: cambia el rol sin salir de la pantalla y avisa del cambio. */
    fun changeRole(role: Role) {
        if (_role.value == role) return
        _role.value = role
        viewModelScope.launch {
            preferences.setRole(role)
            _events.send(SessionEvent.RoleChanged(role))
        }
    }
```

- Si se elige el rol que ya estaba, no hace nada. Si no, lo cambia, lo guarda y envía el evento del aviso.

```kotlin
    fun setDarkMode(enabled: Boolean) {
        val mode = if (enabled) ThemeMode.DARK else ThemeMode.LIGHT
        _themeMode.value = mode
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }
```

- Cambia el tema elegido y lo guarda. Aplicarlo en pantalla le corresponde a la vista (`ThemeModes.apply`).

```kotlin
    companion object {
        val Factory = viewModelFactory {
            initializer { SessionViewModel(ServiceLocator.preferencesRepository) }
        }
    }
}
```

- Android crea los ViewModels con una **fábrica**. Como este recibe el repositorio en su constructor, se le indica
  cómo construirlo. `viewModelFactory { initializer { … } }` es la forma corta de Lifecycle.

---

### `app/src/main/java/com/equipo2/vinilos/ui/role/RoleSelectFragment.kt` · creado por mí

```kotlin
class RoleSelectFragment : Fragment(R.layout.fragment_role_select) {

    private val session: SessionViewModel by activityViewModels { SessionViewModel.Factory }
```

- `by activityViewModels { … }`: obtiene el ViewModel **de la actividad**, el mismo objeto que usa `MainActivity`.
  Así el rol que se elige aquí es el que ve el chip.

```kotlin
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = Motion.fade()
        exitTransition = Motion.fade()
    }
```

- La selección de rol entra y sale con un fundido de 300 ms (guía, sección 6.2).

```kotlin
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentRoleSelectBinding.bind(view)
        binding.btnVisitor.setOnClickListener { enter(Role.VISITOR) }
        binding.btnCollector.setOnClickListener { enter(Role.COLLECTOR) }
```

- `onViewCreated`: la vista ya existe. `bind(view)` crea el *binding* sobre la vista que el fragment infló con el
  layout del constructor.

```kotlin
        val isDark = ThemeModes.isDark(requireContext())
        binding.btnTheme.setImageResource(if (isDark) R.drawable.ic_light_mode else R.drawable.ic_dark_mode)
        binding.btnTheme.contentDescription = getString(if (isDark) R.string.cd_dark_mode_off else R.string.cd_dark_mode_on)
        binding.btnTheme.setOnClickListener {
            session.setDarkMode(!isDark)
            ThemeModes.apply(session.themeMode.value)
        }
    }
```

- El botón de la esquina muestra la luna en claro y el sol en oscuro, con la descripción para TalkBack.
- Al tocarlo guarda el tema contrario y lo aplica (la actividad se recrea con el tema nuevo).

```kotlin
    private fun enter(role: Role) {
        session.enterAs(role)
        findNavController().navigate(R.id.action_roleSelect_to_albums)
    }
}
```

- Guarda el rol y navega a Álbumes con la acción del grafo, que además limpia la pila (Atrás desde Álbumes sale de la
  app en vez de volver aquí).

### `app/src/main/java/com/equipo2/vinilos/ui/role/RoleMenu.kt` · creado por mí

```kotlin
/**
 * Menú del chip de rol: rol actual, cambio de rol, modo oscuro y volver al inicio.
 * Es un PopupWindow con un layout propio porque PopupMenu no admite el interruptor de modo oscuro.
 */
class RoleMenu(
    private val anchor: View,
    private val onRoleSelected: (Role) -> Unit,
    private val onDarkModeChanged: (Boolean) -> Unit,
    private val onGoHome: () -> Unit,
) {
```

- Una clase propia para no llenar `MainActivity` con los detalles del menú.
- Recibe la vista sobre la que se abre (`anchor`, el chip) y tres funciones que decide quien la usa: qué hacer al
  elegir un rol, al cambiar el modo oscuro y al volver al inicio.

```kotlin
    private var popup: PopupWindow? = null

    val isShowing: Boolean
        get() = popup?.isShowing == true
```

- La ventana flotante actual (o `null`) y si está visible. `== true` convierte el `Boolean?` en `false` cuando no hay
  ventana.

```kotlin
    fun show(role: Role?, isDark: Boolean, animate: Boolean = true) {
        val context = anchor.context
        val resources = context.resources
        val binding = PopupRoleMenuBinding.inflate(LayoutInflater.from(context))
        val width = resources.getDimensionPixelSize(R.dimen.role_menu_width)
        val popup = PopupWindow(binding.root, width, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popup.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_popup_menu))
        popup.elevation = resources.getDimension(R.dimen.popup_elevation)
        if (!animate) popup.animationStyle = 0
```

- Infla el layout del menú y crea la ventana: **280 dp de ancho** (guía) y el alto de su contenido. El ancho se pasa
  aquí porque `PopupWindow` ignora el `layout_width` del layout; con `WRAP_CONTENT` el menú salía más ancho que el
  diseño. El último `true` la hace *focusable*: tocar fuera o Atrás la cierran.
- Fondo con esquinas redondeadas y sombra de 3 dp (guía, sección 5.5).
- `animationStyle = 0`: sin animación de apertura, para cuando se reabre después de cambiar el tema.

```kotlin
        val roleName = context.getString(roleNameRes(role))
        binding.menuHeader.text = context.getString(R.string.role_menu_current, roleName.uppercase(Locale.forLanguageTag("es")))
        binding.checkVisitor.isInvisible = role != Role.VISITOR
        binding.checkCollector.isInvisible = role != Role.COLLECTOR
        binding.switchDarkMode.isChecked = isDark
```

- Encabezado «MODO ACTUAL: COLECCIONISTA». `uppercase` con el idioma español convierte bien las letras con tilde.
- `isInvisible`: el check del rol actual se ve y el otro queda invisible pero ocupando su espacio, para que las filas
  no cambien de ancho.
- El interruptor empieza en el estado actual del tema.

```kotlin
        binding.optionVisitor.setOnClickListener { selectRole(Role.VISITOR) }
        binding.optionCollector.setOnClickListener { selectRole(Role.COLLECTOR) }
        binding.optionDarkMode.setOnClickListener { binding.switchDarkMode.toggle() }
        binding.switchDarkMode.setOnCheckedChangeListener { switch, checked ->
            switch.isEnabled = false
            onDarkModeChanged(checked)
        }
        binding.optionGoHome.setOnClickListener {
            dismiss()
            onGoHome()
        }
```

- Tocar toda la fila del modo oscuro también cambia el interruptor (área táctil más grande).
- Al cambiar el interruptor se deshabilita (para no tocarlo dos veces mientras se aplica el tema) y se avisa a quien
  usa el menú.
- «Volver al inicio» cierra el menú y ejecuta su acción.

```kotlin
        this.popup = popup
        popup.showAsDropDown(anchor, 0, resources.getDimensionPixelOffset(R.dimen.role_menu_offset), Gravity.END)
        dimBehind(popup)
    }
```

- Muestra el menú debajo del área táctil del chip, alineado a su borde derecho (`Gravity.END`) y 4 dp más abajo
  (`role_menu_offset`): queda a unos 12 dp del borde visible del chip, como en el prototipo.

```kotlin
    /** Oscurece la pantalla detrás del menú, como en un diálogo. */
    private fun dimBehind(popup: PopupWindow) {
        val window = popup.contentView.rootView
        val params = window.layoutParams as WindowManager.LayoutParams
        params.flags = params.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        params.dimAmount = DIM_AMOUNT
        (anchor.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).updateViewLayout(window, params)
    }
```

- Un `PopupWindow` es una ventana propia. `rootView` es su vista raíz y sus `layoutParams` son los de la ventana.
- `FLAG_DIM_BEHIND` pide a Android oscurecer lo que queda detrás, como hace con los diálogos; `dimAmount` dice cuánto.
- `updateViewLayout` aplica los parámetros a la ventana que ya está visible.

```kotlin
    fun dismiss() {
        popup?.dismiss()
        popup = null
    }

    private fun selectRole(role: Role) {
        dismiss()
        onRoleSelected(role)
    }

    companion object {
        /** Opacidad del velo de Material 3 para elementos modales. */
        private const val DIM_AMOUNT = 0.32f

        fun roleNameRes(role: Role?): Int = if (role == Role.COLLECTOR) R.string.role_collector else R.string.role_visitor
    }
}
```

- `DIM_AMOUNT`: 32 %, el velo (*scrim*) que usa Material 3 detrás de los elementos modales.
- `roleNameRes`: el texto de cada rol. Es pública porque `MainActivity` la usa también para el chip.

### `app/src/main/java/com/equipo2/vinilos/ui/common/Motion.kt` · modificado por mí

```kotlin
import androidx.transition.Fade
…
    private const val FADE_DURATION_MS = 300L
…
    /** Entre la selección de rol y la app. */
    fun fade() = Fade().apply { duration = FADE_DURATION_MS }
```

- **Nuevo**: la transición de fundido simple que usa la selección de rol. El resto no cambió.

---

### `app/src/main/java/com/equipo2/vinilos/MainActivity.kt` · modificado por mí

Se explica completa porque cambió mucho.

```kotlin
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var roleMenu: RoleMenu
    private val session: SessionViewModel by viewModels { SessionViewModel.Factory }
```

- **Nuevos:** el menú de rol y el ViewModel de sesión. `by viewModels` crea (o recupera, si la actividad se recreó)
  el ViewModel de esta actividad; es el mismo que los fragments obtienen con `activityViewModels`.

```kotlin
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInsets()

        navController = binding.navHost.getFragment<NavHostFragment>().navController
        setUpRoleMenu()
        setUpBottomNavigation()
        navController.addOnDestinationChangedListener { _, destination, _ -> onDestinationChanged(destination) }
        observeSession()
```

- El menú se crea **antes** de escuchar los cambios de destino: el listener se ejecuta en cuanto se registra y usa el
  menú.
- `observeSession()` (**nuevo**): escucha el rol y los eventos.

```kotlin
        // La actividad se recrea al cambiar el tema; si el menú estaba abierto, se vuelve a abrir sin animación.
        if (savedInstanceState?.getBoolean(KEY_ROLE_MENU_OPEN) == true) {
            binding.roleChip.root.post { showRoleMenu(animate = false) }
        }
    }
```

- Al recrearse, `savedInstanceState` trae lo que se guardó en `onSaveInstanceState`. Si el menú estaba abierto, se
  vuelve a mostrar.
- `post { … }`: espera a que el chip esté dibujado; una ventana flotante no se puede anclar a una vista que todavía no
  tiene posición.

```kotlin
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_ROLE_MENU_OPEN, roleMenu.isShowing)
    }

    override fun onDestroy() {
        roleMenu.dismiss()
        super.onDestroy()
    }
```

- Antes de destruirse (por ejemplo, al recrearse por el tema) guarda si el menú estaba abierto.
- Al destruirse cierra el menú: una ventana flotante abierta sobre una actividad destruida provoca un error.

`showSnackbar`, `applyWindowInsets` y `setUpBottomNavigation` no cambiaron (pasos 3 y 4).

```kotlin
    private fun setUpRoleMenu() {
        roleMenu = RoleMenu(
            anchor = binding.roleChip.root,
            onRoleSelected = session::changeRole,
            onDarkModeChanged = ::changeDarkMode,
            onGoHome = { navController.navigate(R.id.action_global_roleSelect) },
        )
        binding.roleChip.root.setOnClickListener { showRoleMenu() }
    }
```

- Crea el menú con sus acciones. `session::changeRole` y `::changeDarkMode` son **referencias a funciones**: se pasan
  sin ejecutarlas.
- `binding.roleChip` es el *binding* del layout incluido (`<include>`); `.root` es su vista raíz, la que se toca.
- «Volver al inicio» navega con la acción global, que limpia toda la pila.

```kotlin
    private fun showRoleMenu(animate: Boolean = true) {
        roleMenu.show(session.role.value, ThemeModes.isDark(this), animate)
    }
```

- Abre el menú con el rol y el tema actuales.

```kotlin
    /** Deja terminar la animación del interruptor antes de aplicar el tema, que recrea la actividad. */
    private fun changeDarkMode(enabled: Boolean) {
        session.setDarkMode(enabled)
        binding.root.postDelayed({ ThemeModes.apply(session.themeMode.value) }, SWITCH_ANIMATION_MS)
    }
```

- Guarda el tema y lo aplica 250 ms después, cuando el interruptor terminó de deslizarse. Si se aplicara de
  inmediato, la actividad se recrearía antes y no se vería el cambio del interruptor.

```kotlin
    private fun observeSession() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { session.role.collect(::bindRoleChip) }
                launch {
                    session.events.collect { event ->
                        if (event is SessionEvent.RoleChanged) {
                            val message = if (event.role == Role.COLLECTOR) R.string.snackbar_role_collector else R.string.snackbar_role_visitor
                            showSnackbar(getString(message))
                        }
                    }
                }
            }
        }
    }
```

- `lifecycleScope.launch`: corrutina atada a la vida de la actividad.
- `repeatOnLifecycle(STARTED)`: escucha solo mientras la actividad está visible; se detiene en segundo plano y
  reanuda al volver. Evita actualizar vistas que no se ven.
- Dentro hay dos corrutinas en paralelo: una pinta el chip con cada cambio de rol y otra muestra el aviso de cada
  evento `RoleChanged`.

```kotlin
    private fun bindRoleChip(role: Role?) {
        val roleName = getString(RoleMenu.roleNameRes(role))
        binding.roleChip.roleChipIcon.setImageResource(if (role == Role.COLLECTOR) R.drawable.ic_album else R.drawable.ic_person)
        binding.roleChip.roleChipText.text = roleName
        binding.roleChip.root.contentDescription = getString(R.string.cd_change_role, roleName)
    }
```

- Pinta el chip: ícono de disco para el coleccionista o de persona para el visitante, el nombre del rol, y la
  descripción para TalkBack («Cambiar de rol. Modo actual: Coleccionista»).

```kotlin
    private fun onDestinationChanged(destination: NavDestination) {
        roleMenu.dismiss()
        val isRoleSelect = destination.id == R.id.roleSelectFragment
        binding.toolbar.isVisible = !isRoleSelect
        binding.toolbarDivider.isVisible = !isRoleSelect
        binding.bottomNav.isVisible = !isRoleSelect
        binding.bottomNavDivider.isVisible = !isRoleSelect
        binding.toolbar.title = destination.label
        binding.bottomNav.menu.findItem(destination.id)?.isChecked = true
    }
```

- Al cambiar de pantalla se cierra el menú.
- **Nuevo:** en la selección de rol se ocultan las dos barras y sus divisores (la pantalla ocupa todo).

```kotlin
    private companion object {
        const val KEY_ROLE_MENU_OPEN = "role_menu_open"
        const val SWITCH_ANIMATION_MS = 250L
    }
}
```

- La clave del estado guardado y la espera del interruptor.

---

### `res/navigation/nav_graph.xml` · modificado por mí

```xml
    app:startDestination="@id/roleSelectFragment">

    <fragment
        android:id="@+id/roleSelectFragment"
        android:name="com.equipo2.vinilos.ui.role.RoleSelectFragment"
        android:label="">
        <action
            android:id="@+id/action_roleSelect_to_albums"
            app:destination="@id/albumListFragment"
            app:popUpTo="@id/nav_graph"
            app:popUpToInclusive="true" />
    </fragment>
```

- La app ahora **empieza** en la selección de rol (`startDestination`). La guía pide mostrarla siempre al abrir.
- `<action>`: un camino con nombre de este destino a Álbumes.
- `popUpTo="@id/nav_graph"` + `popUpToInclusive="true"`: al ir a Álbumes se saca todo de la pila, incluida la
  selección de rol. Así Atrás en Álbumes sale de la app.

```xml
    <action
        android:id="@+id/action_global_roleSelect"
        app:destination="@id/roleSelectFragment"
        app:popUpTo="@id/nav_graph"
        app:popUpToInclusive="true" />
```

- **Acción global** (fuera de cualquier `<fragment>`): se puede usar desde cualquier pantalla. Es la de «Volver a la
  pantalla de inicio»: vacía la pila y deja solo la selección de rol.
- Los destinos de las pestañas no cambiaron.

---

### `res/layout/fragment_role_select.xml` · creado por mí

Estructura (guía, sección 9.1 `role_select`):

```xml
<FrameLayout … android:background="@color/md_background">
    <ScrollView … android:fillViewport="true">
        <LinearLayout … android:gravity="center" android:orientation="vertical"
            android:paddingStart="24dp" android:paddingTop="32dp" android:paddingEnd="24dp" android:paddingBottom="32dp">
```

- `FrameLayout` como raíz para poder poner el botón de tema encima, en la esquina.
- `ScrollView` con `fillViewport`: el contenido se centra en pantallas grandes y se puede desplazar en pequeñas o con
  letra grande.
- Columna centrada con márgenes de 24 dp a los lados y 32 dp arriba y abajo.

```xml
            <ImageView … android:contentDescription="@string/cd_logo" android:src="@drawable/ic_vinilos_logo" />
            <TextView … android:text="@string/app_name" android:textAppearance="@style/TextAppearance.Vinilos.DisplaySmall" />
            <TextView … android:maxWidth="300dp" android:text="@string/role_select_tagline" … />
            <TextView … android:layout_marginTop="40dp" android:text="@string/role_select_question" … />
```

- Logo de 120 dp (con descripción para TalkBack), «Vinilos» en 40 sp, la frase de bienvenida (máximo 300 dp de ancho)
  y «¿Cómo quieres entrar?» separada 40 dp.

```xml
            <com.google.android.material.card.MaterialCardView
                android:id="@+id/btnVisitor"
                style="@style/Widget.Vinilos.RoleButton"
                android:layout_marginTop="16dp"
                app:cardBackgroundColor="@color/md_primary"
                app:strokeWidth="0dp">

                <LinearLayout style="@style/Widget.Vinilos.RoleButton.Content">
                    <FrameLayout style="@style/Widget.Vinilos.RoleButton.IconCircle" android:background="@drawable/bg_circle_white_18">
                        <ImageView style="@style/Widget.Vinilos.RoleButton.Icon" android:importantForAccessibility="no"
                            android:src="@drawable/ic_person" app:tint="@color/md_on_primary" />
                    </FrameLayout>
                    <LinearLayout style="@style/Widget.Vinilos.RoleButton.Texts">
                        <TextView style="@style/Widget.Vinilos.RoleButton.Title" android:text="@string/role_visitor_enter" … />
                        <TextView style="@style/Widget.Vinilos.RoleButton.Description" android:text="@string/role_visitor_enter_desc" … />
                    </LinearLayout>
                    <ImageView style="@style/Widget.Vinilos.RoleButton.Chevron" android:importantForAccessibility="no" … />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
```

- Cada botón de rol es una `MaterialCardView` tocable: en Android 5 es la forma sencilla de tener una superficie con
  esquinas redondeadas, borde y efecto de toque.
- Visitante: fondo `primary`, círculo blanco translúcido con el ícono de persona, título y descripción en
  `on_primary`, y la flecha `›` a la derecha. Los íconos son decorativos (`importantForAccessibility="no"`): TalkBack
  lee el título y la descripción.
- Coleccionista (`btnCollector`): igual, con fondo blanco (`surface_container_lowest`), borde de 1 dp `outline` y
  círculo `primary_container` con el ícono de disco.
- Los estilos `RoleButton.*` (en `styles.xml`) guardan las medidas repetidas.

```xml
            <TextView … android:layout_marginTop="40dp" android:text="@string/role_select_footer" … />
        </LinearLayout>
    </ScrollView>

    <ImageButton
        android:id="@+id/btnTheme"
        android:layout_width="48dp"
        android:layout_height="48dp"
        android:layout_gravity="top|end"
        android:layout_margin="8dp"
        android:background="?attr/selectableItemBackgroundBorderless"
        android:contentDescription="@string/cd_dark_mode_on"
        android:src="@drawable/ic_dark_mode"
        app:tint="@color/md_on_surface_variant" />
</FrameLayout>
```

- El pie de página y, fuera del `ScrollView`, el botón de tema en la esquina superior derecha. El ícono y la
  descripción son los del tema claro; el fragment los cambia según el tema actual.

### `res/layout/view_role_chip.xml` · creado por mí

```xml
<!-- Chip de rol: 32 dp visibles (el fondo) dentro de un área táctil de 48 dp (la vista). -->
<LinearLayout …
    android:layout_width="wrap_content"
    android:layout_height="@dimen/touch_target"
    android:background="@drawable/bg_role_chip"
    android:clickable="true"
    android:focusable="true"
    android:gravity="center_vertical"
    android:orientation="horizontal"
    android:paddingStart="4dp"
    android:paddingEnd="6dp">
```

- **Una sola vista** hace de área táctil y de chip: mide 48 dp de alto (accesibilidad) y su fondo dibuja la píldora
  de 32 dp centrada, con el efecto de toque dentro (`bg_role_chip`, ver «Recursos nuevos»).
- Así no hace falta un contenedor extra solo para el área táctil, que es una vista menos que dibujar.
- No tiene `android:id`: el `<include>` de `activity_main.xml` le pone el id `roleChip` (un id en la raíz sería
  reemplazado de todos modos).
- `gravity="center_vertical"`: el ícono, el texto y la flecha quedan centrados en el alto de 48 dp, es decir, dentro
  de la píldora.

```xml
    <FrameLayout android:layout_width="24dp" android:layout_height="24dp" android:background="@drawable/bg_circle_primary_container">
        <ImageView android:id="@+id/roleChipIcon" android:layout_width="16dp" android:layout_height="16dp" …
            android:importantForAccessibility="no" app:tint="@color/md_on_primary_container" tools:src="@drawable/ic_album" />
    </FrameLayout>
    <TextView android:id="@+id/roleChipText" … android:importantForAccessibility="no"
        android:textAppearance="@style/TextAppearance.Vinilos.LabelLarge" tools:text="Coleccionista" />
    <ImageView … android:importantForAccessibility="no" android:src="@drawable/ic_expand_more" app:tint="@color/md_on_surface_variant" />
</LinearLayout>
```

- Círculo de 24 dp con el ícono del rol (16 dp), el nombre del rol y la flecha hacia abajo (18 dp). El ícono y el
  texto los pone `bindRoleChip`.
- Las tres vistas llevan `importantForAccessibility="no"`: TalkBack lee solo la descripción del chip («Cambiar de rol.
  Modo actual: …»), que pone `bindRoleChip`.

### `res/layout/popup_role_menu.xml` · creado por mí

```xml
<LinearLayout … android:layout_width="@dimen/role_menu_width" android:orientation="vertical" android:paddingTop="8dp" android:paddingBottom="8dp">
    <TextView android:id="@+id/menuHeader" … android:textAppearance="@style/TextAppearance.Vinilos.LabelMedium" tools:text="MODO ACTUAL: VISITANTE" />
```

- El menú mide 280 dp de ancho (guía), tomado de `dimens.xml` para que `RoleMenu` use el mismo valor. Arriba, el
  encabezado del rol actual.

```xml
    <LinearLayout android:id="@+id/optionVisitor" style="@style/Widget.Vinilos.MenuRow" android:minHeight="56dp">
        <ImageView style="@style/Widget.Vinilos.MenuRow.Icon" android:importantForAccessibility="no" android:src="@drawable/ic_person" />
        <LinearLayout style="@style/Widget.Vinilos.MenuRow.Texts">
            <TextView style="@style/Widget.Vinilos.MenuRow.Title" android:text="@string/role_visitor" />
            <TextView style="@style/Widget.Vinilos.MenuRow.Description" android:text="@string/role_visitor_desc" />
        </LinearLayout>
        <ImageView android:id="@+id/checkVisitor" style="@style/Widget.Vinilos.MenuRow.Check" android:importantForAccessibility="no" />
    </LinearLayout>
```

- Fila de 56 dp: ícono, título y descripción del rol, y el check si es el actual. La fila del coleccionista
  (`optionCollector`, `checkCollector`) es igual con el ícono de disco.

```xml
    <View style="@style/Widget.Vinilos.Divider" android:layout_marginTop="4dp" android:layout_marginBottom="4dp" />

    <LinearLayout android:id="@+id/optionDarkMode" style="@style/Widget.Vinilos.MenuRow" android:minHeight="56dp">
        <ImageView … android:src="@drawable/ic_dark_mode" />
        <com.google.android.material.materialswitch.MaterialSwitch
            android:id="@+id/switchDarkMode"
            android:layout_width="0dp"
            android:layout_weight="1"
            android:text="@string/role_menu_dark_mode"
            android:textAppearance="@style/TextAppearance.Vinilos.BodyMedium" … />
    </LinearLayout>
```

- Divisor y la fila del modo oscuro con un `MaterialSwitch` (el interruptor de Material 3). Su texto ocupa el ancho
  libre (`layout_weight="1"`), va en el mismo tamaño que los demás títulos del menú y el interruptor queda a la
  derecha.

```xml
    <View style="@style/Widget.Vinilos.Divider" … />

    <LinearLayout android:id="@+id/optionGoHome" style="@style/Widget.Vinilos.MenuRow" android:minHeight="48dp">
        <ImageView … android:src="@drawable/ic_home" />
        <TextView style="@style/Widget.Vinilos.MenuRow.Title" android:layout_width="0dp" android:layout_weight="1"
            android:text="@string/role_menu_go_home" />
    </LinearLayout>
</LinearLayout>
```

- Divisor y la fila «Volver a la pantalla de inicio» (48 dp).

### `res/layout/activity_main.xml` · modificado por mí

```xml
        <com.google.android.material.appbar.MaterialToolbar … >
            <include
                android:id="@+id/roleChip"
                layout="@layout/view_role_chip"
                android:layout_width="wrap_content"
                android:layout_height="@dimen/touch_target"
                android:layout_gravity="end"
                android:layout_marginEnd="8dp" />
        </com.google.android.material.appbar.MaterialToolbar>
```

- **Único cambio**: el chip de rol dentro de la barra superior, alineado a la derecha (`layout_gravity="end"`).
  `<include>` inserta otro layout; View Binding genera `binding.roleChip` con sus vistas.
- Los atributos `layout_*` de un `<include>` **reemplazan** los de la raíz del layout incluido. Por eso el alto se
  repite aquí (`touch_target`, 48 dp): con `wrap_content`, el área táctil del chip quedaba en 32 dp y el menú salía
  pegado al chip.

---

### Recursos nuevos · creados por mí

| Archivo | Qué es |
|---|---|
| `drawable/ic_vinilos_logo.xml` | El logo de la guía (sección 14): disco en `on_surface`, surcos finos en `logo_groove`, borde de la etiqueta del color de fondo, etiqueta en `primary` y agujero central. Usa colores del tema, así que cambia en oscuro. Los tres surcos están en un solo `path` con tres subtrazos (`M…z M…z M…z`), dibujados con `strokeColor` en lugar de relleno |
| `drawable/ic_person.xml`, `ic_chevron_right.xml`, `ic_dark_mode.xml`, `ic_light_mode.xml`, `ic_home.xml`, `ic_expand_more.xml` | Íconos de Material Symbols (persona, flecha derecha, luna, sol, casa, flecha abajo) |
| `drawable/bg_circle_primary_container.xml` | Óvalo en `primary_container` (círculo del ícono del coleccionista y del chip) |
| `drawable/bg_circle_white_18.xml` | Óvalo blanco al 18 % de opacidad (círculo del ícono del visitante) |
| `drawable/bg_role_chip.xml` | El fondo del chip. Un `<inset>` lo dibuja 8 dp más adentro arriba y abajo, así la píldora mide 32 dp dentro de la vista de 48 dp. Adentro, un `<ripple>`: borde de 1 dp `outline` (`<stroke>`) con esquinas de 16 dp y una máscara (`@android:id/mask`) con la misma forma, para que la onda del toque no se salga de la píldora. El color de la onda es `?attr/colorControlHighlight`, el del tema |
| `drawable/bg_popup_menu.xml` | Rectángulo con esquinas de 8 dp en `surface_container_high`: fondo del menú |

### `res/values/colors.xml` y `values-night/colors.xml` · modificados por mí

```xml
    <color name="white_18">#2EFFFFFF</color>      <!-- solo en values -->
    <color name="logo_groove">#14F6F4F9</color>   <!-- values-night: #1414111A -->
```

- `white_18`: blanco con opacidad `2E` en hexadecimal (≈ 18 %). Es igual en los dos temas porque va sobre el botón
  morado.
- `logo_groove`: el color de fondo con opacidad `14` (≈ 8 %) para los surcos del logo; cambia con el tema.

### `res/values/dimens.xml` · modificado por mí

```xml
    <dimen name="popup_elevation">3dp</dimen>
    <dimen name="role_menu_width">280dp</dimen>
    <dimen name="role_menu_offset">4dp</dimen>
```

- `popup_elevation`: la sombra del menú de rol (nivel 2 de elevación de Material).
- `role_menu_width`: el ancho del menú (guía). Lo usan el layout y `RoleMenu`.
- `role_menu_offset`: cuánto más abajo del área táctil del chip se abre el menú.

### `res/values/strings.xml` · modificado por mí

Textos de la selección de rol (`role_select_*`, `role_visitor_enter*`, `role_collector_enter*`), del menú
(`role_visitor`, `role_collector`, `role_menu_current` con el marcador `%1$s`, `role_*_desc`, `role_menu_dark_mode`,
`role_menu_go_home`), de los avisos (`snackbar_role_*`) y de accesibilidad (`cd_change_role`, `cd_dark_mode_on`,
`cd_dark_mode_off`, `cd_logo`). Todos son los de la guía (sección 12.1), salvo `role_collector_enter_desc`, que el
equipo cambió a «Además puedes crear y completar álbumes y artistas.» porque el texto original solo mencionaba álbumes
y tracks.

### `res/values/styles.xml` · modificado por mí

Dos grupos de estilos nuevos, con atributos de layout para no repetirlos en los XML (todos con `parent=""`):

| Estilo | Qué define |
|---|---|
| `RoleButton` | La tarjeta: ancho completo, tocable, esquinas de 16 dp, sin sombra |
| `RoleButton.Content` | La fila interior: alto mínimo de 88 dp, centrada, márgenes de 20 × 16 dp |
| `RoleButton.IconCircle` / `RoleButton.Icon` | Círculo de 48 dp y el ícono de 24 dp centrado |
| `RoleButton.Texts` | Columna de textos que ocupa el ancho libre, separada 16 dp del ícono |
| `RoleButton.Title` / `RoleButton.Description` | Estilos de texto `TitleMedium` y `BodyMedium` |
| `RoleButton.Chevron` | La flecha `›` de 24 dp |
| `MenuRow` | Fila del menú: horizontal, centrada, márgenes de 16 dp, tocable con efecto de toque |
| `MenuRow.Icon` | Ícono de 24 dp en `on_surface_variant`, separado 12 dp |
| `MenuRow.Texts` | Columna de textos que ocupa el ancho libre |
| `MenuRow.Title` / `MenuRow.Description` | Textos en `BodyMedium` (14 sp) y `BodySmall` (12 sp), los tamaños del prototipo: así «Crea álbumes y agrega tracks» cabe en una línea dentro de los 280 dp |
| `MenuRow.Check` | El check de 20 dp en `primary` |

También se agrega `TextAppearance.Vinilos.BodySmall` (12 sp). No está en la tabla tipográfica de la guía; es el
estilo estándar de Material 3 de ese tamaño y el que muestra el prototipo en las descripciones del menú.

El atributo de accesibilidad de los íconos decorativos va en cada layout y no en el estilo: Lint no lee los estilos y
marcaría las imágenes como sin descripción.

---

## Qué sigue

En el paso 6 (T5) se reemplaza la pestaña vacía de Álbumes por la pantalla de Álbumes con el botón + que solo ve el
coleccionista, y se crea la pantalla (todavía vacía) de «Crear álbum».
