# Vinilos · Paso 6 de 9: T5 · Botón + en Álbumes

Tarea de la HU07: **«Agregar el botón + en Álbumes (solo coleccionista)»**.

Cubre el criterio de aceptación **«Solo el coleccionista ve el botón + en Álbumes»**.

- La pestaña Álbumes deja de ser la pantalla vacía genérica y pasa a tener su propio fragment, `AlbumListFragment`.
  Sigue vacía (el catálogo es de HU01), pero ahora muestra el **botón +** cuando el rol es coleccionista y lo oculta
  en cuanto se cambia a visitante.
- El botón abre la pantalla **Crear álbum**: pantalla completa, con una **X** para cerrar, sin chip de rol ni barra
  inferior, que entra desde abajo y sale hacia abajo. Su formulario llega en el paso 7.

| Paso | Commit |
|---|---|
| 1 | Proyecto base de Android Studio |
| 2 | T1 · Configuración, capa de red y estructura MVVM |
| 3 | T2 · Tema claro y oscuro y navegación |
| 4 | T3 · Componentes de carga, error, vacío y avisos |
| 5 | T4 · Selección de rol y menú de rol |
| **6** | **T5 · Botón + en Álbumes** |
| 7 | T6 · Formulario de crear álbum con validaciones |
| 8 | T7 · Creación del álbum en la API |
| 9 | T8 · Pruebas de la historia |

## Orden en que se hicieron los cambios

1. Recursos: ícono `ic_add`, estilo del botón flotante (FAB) y textos.
2. `Motion.kt`: transiciones de pantalla completa.
3. `fragment_album_list.xml` y `AlbumListFragment.kt`: el botón + según el rol.
4. `fragment_album_create.xml` y `AlbumCreateFragment.kt`: la pantalla de Crear álbum, por ahora vacía.
5. `nav_graph.xml`: Álbumes usa su fragment nuevo y tiene la acción hacia Crear álbum.
6. `MainActivity.kt`: la X de cerrar, ocultar el chip y la barra inferior en Crear álbum y ajustar las transiciones.
7. Lint (0 errores) y prueba en el emulador.

## Cómo ejecutarlo

1. **Run ▶**.
2. **Entrar como coleccionista**: aparece el botón + abajo a la derecha.
3. Tocar **+**: Crear álbum entra desde abajo, con la X arriba a la izquierda y sin barra inferior.
4. Tocar la **X** (o Atrás): vuelve a Álbumes.
5. Desde el chip, cambiar a **Visitante**: el botón + desaparece.

---

## Explicación de cada archivo

### `res/drawable/ic_add.xml` · creado por mí

El ícono `add` (+) de Material Symbols, con la misma estructura de los demás íconos (paso 3).

### `res/values/styles.xml` · modificado por mí

```xml
    <style name="Widget.Vinilos.FloatingActionButton" parent="Widget.Material3.FloatingActionButton.Primary">
        <item name="backgroundTint">@color/md_primary</item>
        <item name="tint">@color/md_on_primary</item>
        <item name="shapeAppearanceOverlay">@style/ShapeAppearance.Vinilos.FloatingActionButton</item>
    </style>

    <style name="ShapeAppearance.Vinilos.FloatingActionButton" parent="">
        <item name="cornerFamily">rounded</item>
        <item name="cornerSize">16dp</item>
    </style>
```

- El botón flotante: fondo `primary`, ícono `on_primary` (guía, sección 5.1).
- `shapeAppearanceOverlay`: cambia la forma de un componente de Material. `ShapeAppearance.Vinilos.FloatingActionButton`
  define esquinas redondeadas (`cornerFamily`) de 16 dp, como pide la guía (sección 5.4).

### `res/values/themes.xml` · modificado por mí

```xml
        <item name="floatingActionButtonStyle">@style/Widget.Vinilos.FloatingActionButton</item>
```

- Única línea nueva: todos los botones flotantes de la app usan ese estilo.

### `res/values/strings.xml` · modificado por mí

```xml
    <string name="album_create_title">Crear álbum</string>
    …
    <string name="cd_create_album">Crear álbum</string>
    <string name="cd_close_without_saving">Cerrar sin guardar</string>
```

- `album_create_title`: título de la barra superior en Crear álbum.
- `cd_create_album`: lo que lee TalkBack en el botón +, que no tiene texto visible.
- `cd_close_without_saving`: lo que lee TalkBack en la X.

---

### `app/src/main/java/com/equipo2/vinilos/ui/common/Motion.kt` · modificado por mí

```kotlin
import com.google.android.material.transition.MaterialSharedAxis
…
    private const val OPEN_FULL_SCREEN_MS = 400L
    private const val CLOSE_FULL_SCREEN_MS = 250L
…
    /** Abrir una pantalla completa: la nueva entra desde abajo y la anterior se va hacia arriba. */
    fun openFullScreen() = MaterialSharedAxis(MaterialSharedAxis.Y, true).apply { duration = OPEN_FULL_SCREEN_MS }

    /** Cerrar una pantalla completa: el movimiento inverso, más rápido. */
    fun closeFullScreen() = MaterialSharedAxis(MaterialSharedAxis.Y, false).apply { duration = CLOSE_FULL_SCREEN_MS }
```

- `MaterialSharedAxis`: la transición de Material para pantallas que tienen una relación de orden (abrir y cerrar
  algo). Las dos pantallas se mueven por el mismo eje mientras se desvanecen.
- `MaterialSharedAxis.Y`: eje vertical. `true` es "hacia adelante" (la nueva entra desde abajo) y `false` "hacia
  atrás".
- 400 ms al abrir y 250 ms al cerrar, como la guía (sección 6.2: "Shared axis Y o deslizar desde abajo").

---

### `res/layout/fragment_album_list.xml` · creado por mí

```xml
<!-- El catálogo de álbumes lo construye HU01; HU07 solo agrega el botón + del coleccionista. -->
<FrameLayout …
    android:background="@color/md_background">

    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fabCreate"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|end"
        android:layout_margin="16dp"
        android:contentDescription="@string/cd_create_album"
        android:visibility="gone"
        app:srcCompat="@drawable/ic_add" />
</FrameLayout>
```

- La pantalla ocupa todo y solo tiene el botón flotante, abajo a la derecha (`bottom|end`) a 16 dp de los bordes.
- Empieza oculto (`gone`) y el fragment lo muestra si el rol es coleccionista.
- `app:srcCompat`: forma compatible con Android 5 de poner un ícono vectorial.
- HU01 agregará aquí la cuadrícula y los estados de carga, error y vacío (paso 4).

### `app/src/main/java/com/equipo2/vinilos/ui/albums/AlbumListFragment.kt` · creado por mí

```kotlin
/** Pestaña Álbumes. El catálogo llega con HU01; HU07 agrega el botón + que solo ve el coleccionista. */
class AlbumListFragment : Fragment(R.layout.fragment_album_list) {

    private val session: SessionViewModel by activityViewModels { SessionViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        useTabTransitions()
    }
```

- Comparte el ViewModel de sesión de la actividad para saber el rol. Usa las transiciones de pestaña.

```kotlin
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentAlbumListBinding.bind(view)
        binding.fabCreate.setOnClickListener { openCreateAlbum() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                session.role.collect { role -> binding.fabCreate.isVisible = role == Role.COLLECTOR }
            }
        }
    }
```

- Escucha el rol mientras la pantalla está visible: con cada cambio, el botón se muestra solo si es coleccionista. Por
  eso desaparece al instante al cambiar a visitante desde el menú.
- En un fragment se usa `viewLifecycleOwner` (la vida de **la vista**, no del fragment): la vista se destruye al ir a
  otra pestaña y se vuelve a crear al volver, y así la escucha no queda apuntando a vistas que ya no existen.

```kotlin
    private fun openCreateAlbum() {
        // Crear álbum es una pantalla completa: esta pantalla sale y vuelve por el eje Y en lugar de con fade through.
        exitTransition = Motion.openFullScreen()
        reenterTransition = Motion.closeFullScreen()
        findNavController().navigate(R.id.action_albums_to_create)
    }
}
```

- Antes de navegar se cambian las transiciones de esta pantalla: al **salir** se va hacia arriba y al **volver** baja,
  coordinadas con las de Crear álbum. Si no, Álbumes haría su *fade through* mientras Crear álbum sube.
- `findNavController().navigate(…)`: usa la acción del grafo hacia Crear álbum.

### `res/layout/fragment_album_create.xml` · creado por mí

```xml
<!-- El formulario llega en el paso 7 (T6). -->
<FrameLayout … android:background="@color/md_background" />
```

- Por ahora, una pantalla vacía con el color de fondo.

### `app/src/main/java/com/equipo2/vinilos/ui/albums/create/AlbumCreateFragment.kt` · creado por mí

```kotlin
/** Pantalla completa para crear un álbum (HU07). */
class AlbumCreateFragment : Fragment(R.layout.fragment_album_create) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = Motion.openFullScreen()
        returnTransition = Motion.closeFullScreen()
    }
}
```

- Entra desde abajo (`enterTransition`) y, al cerrarse, sale hacia abajo (`returnTransition`).
- Va en el paquete `ui.albums.create`, dentro de `ui.albums`, porque es parte de la sección de álbumes.

---

### `res/navigation/nav_graph.xml` · modificado por mí

```xml
    <fragment
        android:id="@+id/albumListFragment"
        android:name="com.equipo2.vinilos.ui.albums.AlbumListFragment"
        android:label="@string/album_list_title">
        <action
            android:id="@+id/action_albums_to_create"
            app:destination="@id/albumCreateFragment" />
    </fragment>
```

- Álbumes ahora usa `AlbumListFragment` (antes `EmptySectionFragment`) y tiene la acción hacia Crear álbum. La acción
  no limpia la pila: Atrás desde Crear álbum vuelve a Álbumes.

```xml
    <fragment
        android:id="@+id/albumCreateFragment"
        android:name="com.equipo2.vinilos.ui.albums.create.AlbumCreateFragment"
        android:label="@string/album_create_title" />
```

- El destino nuevo, con el título «Crear álbum».

---

### `app/src/main/java/com/equipo2/vinilos/MainActivity.kt` · modificado por mí

```kotlin
import androidx.fragment.app.Fragment
…
import com.equipo2.vinilos.ui.common.Motion
…
import com.equipo2.vinilos.ui.common.useTabTransitions
```

```kotlin
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
```

- **Nuevo.** Al tocar el ícono de la izquierda de la barra (la X de Crear álbum) se hace lo mismo que con el botón
  Atrás del sistema. En el paso 7, el formulario intercepta Atrás para preguntar antes de descartar lo escrito, y así
  la X pasa por la misma lógica.

```kotlin
            if (navController.currentDestination?.id != item.itemId) {
                // La pestaña actual sale con fade through aunque haya abierto antes una pantalla completa.
                currentFragment()?.useTabTransitions()
                navController.navigate(item.itemId, null, navOptions {
```

- **Nuevo.** Al abrir Crear álbum, Álbumes cambió sus transiciones a las de pantalla completa; antes de ir a otra
  pestaña se le devuelven las de pestaña.

```kotlin
            onGoHome = ::goHome,
…
    private fun goHome() {
        currentFragment()?.exitTransition = Motion.fade()
        navController.navigate(R.id.action_global_roleSelect)
    }

    private fun currentFragment(): Fragment? =
        binding.navHost.getFragment<NavHostFragment>().childFragmentManager.primaryNavigationFragment
```

- `goHome` (**nuevo**): al volver al inicio, la pantalla actual sale con el mismo fundido con el que entra la
  selección de rol.
- `currentFragment()`: el fragment que se está mostrando. El `NavHostFragment` guarda sus pantallas en su
  `childFragmentManager`, y `primaryNavigationFragment` es la visible.

```kotlin
    /** Barras según la pantalla: ninguna en la selección de rol; sin barra inferior ni chip en Crear álbum. */
    private fun onDestinationChanged(destination: NavDestination) {
        roleMenu.dismiss()
        val isRoleSelect = destination.id == R.id.roleSelectFragment
        val isTab = destination.id in TAB_DESTINATIONS
        binding.toolbar.isVisible = !isRoleSelect
        binding.toolbarDivider.isVisible = !isRoleSelect
        binding.bottomNav.isVisible = isTab
        binding.bottomNavDivider.isVisible = isTab
        binding.roleChip.root.isVisible = isTab
        binding.toolbar.title = destination.label
```

- Ahora hay tres tipos de pantalla: la selección de rol (sin barras), las pestañas (todo visible) y Crear álbum
  (barra superior sin chip y sin barra inferior). La barra inferior y el chip solo se ven en las pestañas (`isTab`).

```kotlin
        if (isTab) {
            binding.toolbar.navigationIcon = null
            binding.bottomNav.menu.findItem(destination.id)?.isChecked = true
        } else if (destination.id == R.id.albumCreateFragment) {
            binding.toolbar.setNavigationIcon(R.drawable.ic_close)
            binding.toolbar.navigationContentDescription = getString(R.string.cd_close_without_saving)
        }
    }
```

- En las pestañas la barra no tiene ícono a la izquierda; en Crear álbum tiene la X con su descripción para TalkBack.

```kotlin
    private companion object {
        val TAB_DESTINATIONS = setOf(R.id.albumListFragment, R.id.artistsFragment, R.id.collectorsFragment)
```

- **Nuevo.** El conjunto de destinos que son pestañas. `in` comprueba si un id está en el conjunto.

---

## Qué sigue

En el paso 7 (T6) se construye el formulario de Crear álbum con todas sus validaciones, la vista previa de la portada,
el selector de fecha, los desplegables y la confirmación al salir con datos escritos.
