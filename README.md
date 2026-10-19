# Vinilos · Paso 3 de 9: T2 · Tema claro y oscuro y navegación

Tarea de la HU07: **«Construir la base visual (tema claro y oscuro) y la navegación de la app»**.

Al terminar este paso la app ya se ve como el diseño:

- Colores del diseño en claro y en oscuro (el oscuro sigue la configuración del teléfono; el interruptor manual llega
  en el paso 5).
- Tipografía del diseño.
- Ícono de la app propio (disco de vinilo).
- Barra superior con el título de la pantalla y barra inferior con las pestañas **Álbumes**, **Artistas** y
  **Coleccionistas**.
- Transición *fade through* al cambiar de pestaña.

Las tres pestañas quedan **vacías a propósito**: su contenido lo construye la historia de cada una (HU01, HU03 y HU05).

| Paso | Commit |
|---|---|
| 1 | Proyecto base de Android Studio |
| 2 | T1 · Configuración, capa de red y estructura MVVM |
| **3** | **T2 · Tema claro y oscuro y navegación** |
| 4 | T3 · Componentes de carga, error, vacío y avisos |
| 5 | T4 · Selección de rol y menú de rol |
| 6 | T5 · Botón + en Álbumes |
| 7 | T6 · Formulario de crear álbum con validaciones |
| 8 | T7 · Creación del álbum en la API |
| 9 | T8 · Pruebas de la historia |

## Orden en que se hicieron los cambios

1. Gradle: agregar Fragment y Navigation, activar View Binding y quitar ConstraintLayout (ya no se usa).
2. Colores: `values/colors.xml` (claro) y `values-night/colors.xml` (oscuro) con los tokens de la guía.
3. Tema: `values/themes.xml` con los roles de color de Material 3; borrar `values-night/themes.xml` (ya no hace falta).
4. Estilos: `values/styles.xml` con la barra superior, la barra inferior, el divisor y la tipografía; selectores de
   color de la barra inferior en `res/color/`.
5. Íconos: los de las pestañas (`ic_album`, `ic_mic`, `ic_groups`) y el ícono de la app (reemplaza el robot de
   Android y borra las imágenes `.webp`).
6. Textos en `strings.xml`.
7. Navegación: `menu/bottom_nav.xml` y `navigation/nav_graph.xml`.
8. Layouts: `activity_main.xml` (estructura de la app) y `fragment_empty_section.xml`.
9. Código: `Motion.kt`, `EmptySectionFragment.kt` y `MainActivity.kt`.
10. Probar en el emulador (Android 17).

## Cómo ejecutarlo

1. **File › Sync Project with Gradle Files**.
2. Elegir un emulador o teléfono con Android 5.0 o superior y pulsar **Run ▶**.
3. Se ve la pestaña Álbumes vacía. Al tocar Artistas o Coleccionistas cambia el título y la pantalla hace la
   transición; Atrás desde Artistas o Coleccionistas vuelve a Álbumes.
4. Para ver el tema oscuro: activar el modo oscuro del teléfono (Ajustes › Pantalla) y volver a la app.

---

## Explicación de cada archivo

### `gradle/libs.versions.toml` · modificado por mí

Cambios respecto al paso 2 (el resto del archivo quedó igual):

```toml
fragment = "1.8.9"
lifecycle = "2.9.4"
navigation = "2.9.8"
```

- `constraintlayout` se **quitó**: la nueva estructura de pantallas no usa `ConstraintLayout`.
- `fragment` (**nueva**): 1.8.9 es la última que soporta API 21.
- `navigation` (**nueva**): 2.9.8 es la última que soporta API 21 (la 2.10 exige API 24).

```toml
androidx-fragment-ktx = { group = "androidx.fragment", name = "fragment-ktx", version.ref = "fragment" }
androidx-navigation-fragment-ktx = { group = "androidx.navigation", name = "navigation-fragment-ktx", version.ref = "navigation" }
```

- `fragment-ktx`: los **Fragments** (pedazos de pantalla que se cambian dentro de la actividad) y las funciones
  `by viewModels()` y `by activityViewModels()` que se usarán en los pasos siguientes.
- `navigation-fragment-ktx`: el **Navigation Component**: `NavHostFragment`, el grafo de navegación y
  `NavController`, que cambia de fragment según el grafo.
- Se quitó la línea de `androidx-constraintlayout`.

### `app/build.gradle.kts` · modificado por mí

```kotlin
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
```

- `viewBinding = true` (**nuevo**): por cada layout XML, Gradle genera una clase (`activity_main.xml` →
  `ActivityMainBinding`) con una propiedad por cada vista que tenga `android:id`. Reemplaza a `findViewById`: es
  seguro (si el id no existe, no compila) y no hace falta convertir tipos.

```kotlin
    implementation(libs.androidx.fragment.ktx)
    …
    implementation(libs.androidx.navigation.fragment.ktx)
```

- Se agregan Fragment y Navigation, y se quitó `implementation(libs.androidx.constraintlayout)`. El resto quedó igual.

---

### `res/values/colors.xml` · modificado por mí

Reemplaza los dos colores de ejemplo (`black`, `white`) por los **tokens de color** de la guía de diseño (sección 5.1).
El comentario del inicio aclara que es el tema claro y anota el único ajuste del equipo: un `primary_container` más
oscuro que el de la guía.

Cada línea `<color name="…">#RRGGBB</color>` define un color. El prefijo `md_` viene de *Material Design* y el resto
del nombre es el rol de Material 3:

| Color | Claro | Dónde se usa |
|---|---|---|
| `md_primary` | `#6A2FA0` | Color principal: botones rellenos, botón +, enlaces, campo enfocado |
| `md_on_primary` | `#FFFFFF` | Texto e íconos sobre `primary` |
| `md_primary_container` | `#D5B9F5` | Indicador de la pestaña activa, círculos de íconos, chips (ajuste del equipo: la guía usa `#EADCFA`, que contra la barra inferior tenía un contraste de 1,07:1 y casi no se veía; con `#D5B9F5` sube a 1,43:1 y el ícono encima conserva 9,4:1) |
| `md_on_primary_container` | `#2E0A52` | Texto e íconos sobre `primary_container` |
| `md_inverse_primary` | `#CDA8FF` | Acciones dentro del aviso (snackbar), que tiene fondo oscuro |
| `md_secondary` … `md_on_secondary_container` | `#625B71`, `#FFFFFF`, `#E8DEF8`, `#1D192B` | Valores base de Material 3; el diseño no los usa, pero algunos componentes los piden |
| `md_tertiary`, `md_on_tertiary` | `#C2410C`, `#FFFFFF` | Estrellas de calificación (HU02) |
| `md_background`, `md_surface` | `#F6F4F9` | Fondo de todas las pantallas y de la barra superior |
| `md_on_background`, `md_on_surface` | `#1E1A26` | Texto principal |
| `md_surface_variant` | `#E4DDEE` | Marcadores de portadas y bloques de carga (*skeleton*) |
| `md_on_surface_variant` | `#4F4760` | Texto secundario e íconos |
| `md_surface_container_lowest` | `#FFFFFF` | Tarjetas |
| `md_surface_container` | `#ECE7F3` | Barra inferior y círculos de los estados vacío y error |
| `md_surface_container_high` | `#F7F3FC` | Menús desplegables |
| `md_outline` | `#7C7290` | Bordes de campos de texto y del chip de rol |
| `md_outline_variant` | `#DED7E8` | Líneas divisoras |
| `md_error`, `md_on_error`, `md_error_container`, `md_on_error_container` | `#B3261E`, `#FFFFFF`, `#F9DEDC`, `#410E0B` | Mensajes de error |
| `md_inverse_surface`, `md_inverse_on_surface` | `#2A2335`, `#F3EDFA` | Fondo y texto del aviso (snackbar) |
| `launcher_background` | `#6A2FA0` | Fondo del ícono de la app |

Todos los textos cumplen el contraste mínimo de accesibilidad (WCAG AA, 4.5:1) sobre el fondo: texto principal 15.6:1,
secundario 8.0:1, `primary` 7.6:1 y error 6.0:1.

### `res/values-night/colors.xml` · creado por mí

Los mismos nombres con los valores del tema oscuro (por ejemplo, `md_primary` = `#CDA8FF` y `md_background` =
`#14111A`). Cuando el teléfono está en modo oscuro, Android toma los colores de `values-night` en lugar de los de
`values`, así que **el tema no se repite**: solo cambian los colores. `launcher_background` no se redefine porque el
ícono es igual en los dos modos.

### `res/values-night/themes.xml` · borrado

La plantilla lo creó para personalizar el tema oscuro. Ya no hace falta, porque el cambio de colores lo hace
`values-night/colors.xml`.

### `res/values/themes.xml` · modificado por mí

```xml
<resources>
    <!-- Material 3 sin color dinámico: los colores cambian en oscuro por values-night/colors.xml. -->
    <style name="Base.Theme.Vinilos" parent="Theme.Material3.DayNight.NoActionBar">
```

- Mantiene la estructura de la plantilla (`Base.Theme.Vinilos` y `Theme.Vinilos`) y el mismo padre de Material 3.
- Se quitó `xmlns:tools` porque ya no se usa.
- "Sin color dinámico": Android 12+ puede tomar los colores del fondo de pantalla; el diseño usa siempre la paleta
  violeta, así que no se activa (`DynamicColors` nunca se llama).

```xml
        <item name="colorPrimary">@color/md_primary</item>
        <item name="colorOnPrimary">@color/md_on_primary</item>
        …
        <item name="colorOnSurfaceInverse">@color/md_inverse_on_surface</item>
```

- Cada `<item name="colorX">` asigna un **rol de color** de Material 3 a uno de los tokens de `colors.xml`. Los
  componentes de Material (botones, campos, barras) leen estos roles, así que con esto toda la app toma la paleta.
- `android:colorBackground` lleva el prefijo `android:` porque es un atributo del sistema, no de Material.
- `colorSurfaceContainerLow` y `colorSurfaceContainerHighest` no están en la guía; se igualan al más cercano para que
  ningún componente use un color fuera de la paleta.

```xml
        <item name="android:windowBackground">@color/md_background</item>
        <item name="toolbarStyle">@style/Widget.Vinilos.Toolbar</item>
        <item name="bottomNavigationStyle">@style/Widget.Vinilos.BottomNavigation</item>
    </style>

    <style name="Theme.Vinilos" parent="Base.Theme.Vinilos" />
</resources>
```

- `android:windowBackground`: el color de la ventana mientras la app abre, para que no se vea un destello blanco.
- `toolbarStyle` y `bottomNavigationStyle`: el estilo por defecto de todas las `MaterialToolbar` y
  `BottomNavigationView` de la app (definidos en `styles.xml`).

### `res/values/styles.xml` · creado por mí

Estilos de componentes y tipografía. Un estilo es un conjunto de atributos con nombre; `parent` es el estilo del que
hereda.

```xml
    <style name="Widget.Vinilos.Toolbar" parent="Widget.Material3.Toolbar">
        <item name="android:background">@color/md_surface</item>
        <item name="titleTextAppearance">@style/TextAppearance.Vinilos.TitleLarge</item>
        <item name="navigationIconTint">@color/md_on_surface</item>
    </style>
```

- Barra superior: fondo `surface`, título con el estilo `TitleLarge` y el ícono de la izquierda (por ejemplo, la X de
  cerrar del paso 6) en el color del texto.

```xml
    <style name="Widget.Vinilos.BottomNavigation" parent="Widget.Material3.BottomNavigationView">
        <item name="backgroundTint">@color/md_surface_container</item>
        <item name="elevation">0dp</item>
        <item name="labelVisibilityMode">labeled</item>
        <item name="itemActiveIndicatorStyle">@style/Widget.Vinilos.BottomNavigation.ActiveIndicator</item>
        <item name="itemIconTint">@color/nav_item_icon</item>
        <item name="itemTextColor">@color/nav_item_text</item>
        <item name="itemTextAppearanceActive">@style/TextAppearance.Vinilos.LabelMedium.Bold</item>
        <item name="itemTextAppearanceInactive">@style/TextAppearance.Vinilos.LabelMedium</item>
    </style>
```

- Barra inferior: fondo `surface_container`, sin sombra (`elevation` 0; el diseño es plano y usa una línea divisora).
- `labelVisibilityMode` `labeled`: las etiquetas de las tres pestañas siempre se ven.
- `itemActiveIndicatorStyle`: la "píldora" detrás del ícono activo.
- `itemIconTint` / `itemTextColor`: colores que cambian según la pestaña esté activa o no (selectores de `res/color/`).
- Etiqueta activa en negrita e inactiva normal, como pide la guía.

```xml
    <style name="Widget.Vinilos.BottomNavigation.ActiveIndicator" parent="Widget.Material3.BottomNavigationView.ActiveIndicator">
        <item name="android:color">@color/md_primary_container</item>
    </style>
```

- La píldora de la pestaña activa en `primary_container` (Material 3 usaría `secondary_container`; la guía pide este).

```xml
    <style name="Widget.Vinilos.Divider" parent="">
        <item name="android:layout_width">match_parent</item>
        <item name="android:layout_height">1dp</item>
        <item name="android:background">@color/md_outline_variant</item>
    </style>
```

- Línea divisora de 1 dp a todo el ancho. `parent=""` evita que herede de un estilo "Widget.Vinilos" que no existe
  (en Android, un nombre con puntos hereda implícitamente del prefijo).

```xml
    <!-- Tipografía (sección 5.2 de la guía). Roboto del sistema; sans-serif-medium hace de SemiBold en API 21. -->
```

- La guía usa peso SemiBold (600), pero `android:textFontWeight` solo existe desde Android 9. En Android 5 la forma de
  obtener un peso intermedio es la familia `sans-serif-medium`.

Cada `TextAppearance.Vinilos.X` es un estilo de texto. Heredan de los de Material 3 y cambian el tamaño (y a veces el
peso y el color):

| Estilo | Tamaño | Peso | Dónde se usa |
|---|---|---|---|
| `DisplaySmall` | 40 sp | Negrita | «Vinilos» en la selección de rol |
| `HeadlineSmall` | 22 sp | Medium | Títulos de los estados vacío y error |
| `TitleLarge` | 22 sp | Normal | Título de la barra superior |
| `TitleMedium` | 18 sp | Medium | Títulos de los botones de rol |
| `BodyLarge` | 16 sp | Normal | Textos y campos |
| `BodyMedium` | 14 sp | Normal | Textos secundarios |
| `LabelLarge` | 14 sp | Medium | Botones y chip de rol |
| `LabelMedium` | 12 sp | Normal | Etiquetas de la barra inferior |
| `LabelMedium.Bold` | 12 sp | Negrita | Etiqueta de la pestaña activa (hereda de `LabelMedium` por el nombre) |
| `LabelSmall` | 13 sp | Normal | Mensajes de error y ayuda de los campos |

- `sp` (píxeles escalables) crece con el tamaño de letra que el usuario elige en el teléfono.
- Se repite `android:fontFamily` y `fontFamily` porque Material lee uno u otro según el componente.

### `res/color/nav_item_icon.xml` y `res/color/nav_item_text.xml` · creados por mí

```xml
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:color="@color/md_on_primary_container" android:state_checked="true" />
    <item android:color="@color/md_on_surface_variant" />
</selector>
```

- Un `selector` es un color que depende del estado: Android recorre los `item` en orden y usa el primero que coincide.
- Pestaña activa (`state_checked="true"`): ícono `on_primary_container` (texto: `on_surface`). Si no: `on_surface_variant`.

---

### Íconos de las pestañas · creados por mí

`res/drawable/ic_album.xml`, `ic_mic.xml` e `ic_groups.xml` son los íconos de Material Symbols (estilo Outlined) que
pide la guía: disco para Álbumes, micrófono para Artistas y grupo de personas para Coleccionistas. Se descargaron como
SVG del repositorio oficial de Google y se convirtieron a *vector drawable*:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="960"
    android:viewportHeight="960">
    <group android:translateY="960">
        <path
            android:fillColor="#FF000000"
            android:pathData="M480-300q75 0 …" />
    </group>
</vector>
```

- `width`/`height`: tamaño en pantalla (24 dp).
- `viewportWidth`/`viewportHeight`: el sistema de coordenadas del dibujo (960 × 960, el de Material Symbols).
- `<group android:translateY="960">`: los SVG de Material Symbols dibujan entre y = −960 y 0; el grupo los baja 960
  unidades para que queden dentro del lienzo.
- `fillColor` negro: es solo la forma; el color real lo pone quien usa el ícono (la barra inferior lo tiñe con el
  selector).
- `pathData`: el trazo del dibujo.

### Ícono de la app · modificados y creados por mí

| Archivo | Cambio |
|---|---|
| `res/drawable/ic_launcher_background.xml` | **Modificado**: un cuadrado de 108 × 108 en `launcher_background` (morado), en lugar de la cuadrícula verde |
| `res/drawable/ic_launcher_foreground.xml` | **Modificado**: un disco de vinilo (círculo blanco, etiqueta morada y lila, agujero central) en lugar del robot. Va dentro de la zona segura de 66 dp para que ningún launcher lo recorte |
| `res/mipmap-anydpi-v26/ic_launcher.xml` y `ic_launcher_round.xml` | Sin cambios: siguen uniendo fondo, primer plano y monocromo, ahora con los dibujos nuevos |
| `res/mipmap-anydpi/ic_launcher.xml` | **Creado**: el ícono para Android 5.0 a 7.1 (no tienen íconos adaptativos). Es un `layer-list`: dos capas una encima de otra, un óvalo morado y el disco |
| `res/mipmap-anydpi/ic_launcher_round.xml` | **Creado**: la versión redonda, que reutiliza la anterior |
| `res/mipmap-*dpi/*.webp` | **Borrados**: eran el robot de Android en imágenes; el `layer-list` vectorial las reemplaza en todas las densidades |

Los paths `M54,24a30,30 0,1 1,0 60a30,30 0,1 1,0 -60z` dibujan círculos: se mueven al punto superior (`M`) y trazan
dos arcos (`a`) de medio círculo cada uno.

---

### `res/values/strings.xml` · modificado por mí

```xml
<resources>
    <string name="app_name">Vinilos</string>

    <string name="nav_albums">Álbumes</string>
    <string name="nav_artists">Artistas</string>
    <string name="nav_collectors">Coleccionistas</string>
    <string name="album_list_title">Álbumes</string>
    <string name="artist_list_title">Artistas</string>
    <string name="collector_list_title">Coleccionistas</string>
</resources>
```

- `nav_*`: las etiquetas de la barra inferior.
- `*_title`: los títulos de la barra superior. Son textos separados aunque hoy digan lo mismo, porque cumplen funciones
  distintas y la guía los define por separado.

---

### `res/menu/bottom_nav.xml` · creado por mí

```xml
<!-- Los ids coinciden con los destinos de nav_graph.xml. -->
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:id="@+id/albumListFragment"
        android:icon="@drawable/ic_album"
        android:title="@string/nav_albums" />
    …
</menu>
```

- Las tres opciones de la barra inferior, cada una con id, ícono y texto.
- Los ids son los mismos que los de los destinos del grafo de navegación: así `MainActivity` sabe a qué destino ir al
  tocar cada pestaña y qué pestaña marcar al llegar a un destino.

### `res/navigation/nav_graph.xml` · creado por mí

El **grafo de navegación**: las pantallas (destinos) de la app y cómo se llega a ellas.

```xml
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/nav_graph"
    app:startDestination="@id/albumListFragment">
```

- `app:startDestination`: la pantalla con la que arranca la app (Álbumes). En el paso 5 cambia a la selección de rol.

```xml
    <!-- El contenido de cada pestaña lo construye su historia: Álbumes (HU01), Artistas (HU03), Coleccionistas (HU05). -->
    <fragment
        android:id="@+id/albumListFragment"
        android:name="com.equipo2.vinilos.ui.common.EmptySectionFragment"
        android:label="@string/album_list_title" />
```

- Cada `<fragment>` es un destino: `android:id` lo identifica, `android:name` es la clase del fragment y
  `android:label` el título (lo usa `MainActivity` para la barra superior).
- Los tres destinos usan `EmptySectionFragment` (pantalla vacía). Cada historia cambiará `android:name` por su propio
  fragment; Álbumes lo hace en el paso 6.

---

### `res/layout/activity_main.xml` · modificado por mí

Reemplaza el «Hello World!» por la estructura de la app.

```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/root"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">
```

- La raíz es un `CoordinatorLayout`: además de contener vistas, coordina componentes de Material; los avisos
  (snackbar) del paso 4 se muestran dentro de él.

```xml
    <LinearLayout
        android:id="@+id/content"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical">
```

- Columna vertical con todo el contenido. Tiene id porque `MainActivity` le aplica el espacio de las barras del
  sistema.

```xml
        <com.google.android.material.appbar.MaterialToolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="64dp" />

        <View
            android:id="@+id/toolbarDivider"
            style="@style/Widget.Vinilos.Divider" />
```

- La barra superior de Material (64 dp de alto, como la guía) y su línea divisora. El estilo de la barra viene del
  tema (`toolbarStyle`), por eso aquí no hace falta repetirlo.

```xml
        <androidx.fragment.app.FragmentContainerView
            android:id="@+id/navHost"
            android:name="androidx.navigation.fragment.NavHostFragment"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            app:defaultNavHost="true"
            app:navGraph="@navigation/nav_graph" />
```

- El contenedor donde se muestran las pantallas (fragments).
- `android:name="…NavHostFragment"`: el fragment de Navigation que cambia de pantalla según el grafo.
- `layout_height="0dp"` + `layout_weight="1"`: en un `LinearLayout`, ocupa todo el alto que dejan libre las barras.
- `app:defaultNavHost="true"`: el botón Atrás del sistema navega hacia atrás dentro del grafo.
- `app:navGraph`: el grafo que usa.

```xml
        <View
            android:id="@+id/bottomNavDivider"
            style="@style/Widget.Vinilos.Divider" />

        <com.google.android.material.bottomnavigation.BottomNavigationView
            android:id="@+id/bottomNav"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            app:menu="@menu/bottom_nav" />
    </LinearLayout>
</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

- La línea divisora y la barra inferior con las opciones de `bottom_nav.xml`.
- Los divisores y las barras tienen id porque en los pasos siguientes se ocultan en ciertas pantallas.

### `res/layout/fragment_empty_section.xml` · creado por mí

```xml
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/md_background" />
```

- Una pantalla vacía del color de fondo. El fondo es necesario para que, durante la transición, una pantalla no se vea
  a través de la otra.

---

### `app/src/main/java/com/equipo2/vinilos/ui/common/Motion.kt` · creado por mí

Las transiciones entre pantallas. El paquete `ui.common` agrupa lo que comparten todas las pantallas.

```kotlin
package com.equipo2.vinilos.ui.common

import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialFadeThrough
```

```kotlin
/** Transiciones entre pantallas de la sección 6.2 de la guía, con Material motion. */
object Motion {

    private const val TAB_DURATION_MS = 300L

    /** Cambio de pestaña: la pantalla que sale se desvanece y la que entra aparece con fade y escala 0,92 → 1. */
    fun fadeThrough() = MaterialFadeThrough().apply { duration = TAB_DURATION_MS }
}
```

- `MaterialFadeThrough` es la transición de Material para cambiar entre pantallas que no tienen relación entre sí
  (como las pestañas): la que sale se desvanece y la que entra aparece con un leve zoom (de 0,92 a 1).
- `.apply { duration = … }`: crea la transición y le cambia la duración a 300 ms, la que pide la guía.
- Es una función (y no una sola instancia guardada) porque cada fragment necesita su propio objeto de transición.

```kotlin
fun Fragment.useTabTransitions() {
    enterTransition = Motion.fadeThrough()
    exitTransition = Motion.fadeThrough()
    reenterTransition = Motion.fadeThrough()
    returnTransition = Motion.fadeThrough()
}
```

- Función de extensión: cualquier fragment puede llamar `useTabTransitions()`.
- Un fragment tiene cuatro transiciones: al **entrar**, al **salir** porque se abre otra encima, al **volver a entrar**
  cuando la de encima se cierra, y al **irse** cuando se cierra él mismo. Las pestañas usan *fade through* en las
  cuatro.
- Se usan transiciones de Material y no archivos de animación XML porque se ven más fluidas: interpolan cuadro a cuadro
  y animan juntas la pantalla que sale y la que entra.

### `app/src/main/java/com/equipo2/vinilos/ui/common/EmptySectionFragment.kt` · creado por mí

```kotlin
/** Pestaña que todavía no tiene contenido; cada historia la reemplaza por su propio fragment. */
class EmptySectionFragment : Fragment(R.layout.fragment_empty_section) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        useTabTransitions()
    }
}
```

- `Fragment(R.layout.fragment_empty_section)`: el constructor de `Fragment` acepta el layout a mostrar, así no hace
  falta sobrescribir `onCreateView`.
- En `onCreate` se asignan las transiciones; deben estar listas antes de que el fragment aparezca.

### `app/src/main/java/com/equipo2/vinilos/MainActivity.kt` · modificado por mí

```kotlin
package com.equipo2.vinilos

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.equipo2.vinilos.databinding.ActivityMainBinding
import kotlin.math.max
```

- Nuevos: las clases de Navigation, `ActivityMainBinding` (generada por View Binding a partir de `activity_main.xml`)
  y `max`.

```kotlin
/** Actividad única: barra superior, contenedor de navegación y barra inferior. */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
```

- "Actividad única": toda la app vive en esta actividad y las pantallas son fragments.
- `lateinit var`: la propiedad se asigna en `onCreate`, no en el constructor. Kotlin permite declararla sin valor
  inicial con la promesa de asignarla antes de usarla.

```kotlin
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInsets()
```

- `ActivityMainBinding.inflate(layoutInflater)`: crea las vistas del layout y devuelve el *binding* con todas ellas.
- `setContentView(binding.root)`: muestra la vista raíz. Reemplaza al `setContentView(R.layout.activity_main)` de la
  plantilla.

```kotlin
        navController = binding.navHost.getFragment<NavHostFragment>().navController
        navController.addOnDestinationChangedListener { _, destination, _ -> onDestinationChanged(destination) }
        setUpBottomNavigation()
    }
```

- `binding.navHost.getFragment<NavHostFragment>()`: el `NavHostFragment` que vive dentro del contenedor; su
  `navController` es quien cambia de pantalla.
- `addOnDestinationChangedListener`: se ejecuta cada vez que cambia la pantalla. Los `_` son parámetros que no se usan
  (el controlador y los argumentos).

```kotlin
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.content) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(bars.left, bars.top, bars.right, max(bars.bottom, keyboard.bottom))
            WindowInsetsCompat.CONSUMED
        }
    }
```

- Es la misma idea de la plantilla, ahora aplicada a la columna de contenido y teniendo en cuenta el **teclado**
  (`ime`): abajo se usa el mayor entre la barra de navegación y el teclado, para que el formulario del paso 7 no quede
  tapado.
- `WindowInsetsCompat.CONSUMED`: los insets ya se usaron aquí y no se pasan a las vistas hijas (si no, se aplicarían
  dos veces).

```kotlin
    /** Cada pestaña conserva su estado y, desde Artistas o Coleccionistas, Atrás vuelve a Álbumes. */
    private fun setUpBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            if (navController.currentDestination?.id != item.itemId) {
                navController.navigate(item.itemId, null, navOptions {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(R.id.albumListFragment) { saveState = true }
                })
            }
            true
        }
        binding.bottomNav.setOnItemReselectedListener { }
    }
```

- Al tocar una pestaña se navega a su destino (el id del menú es el id del destino).
- `launchSingleTop`: si ya está arriba, no crea otra copia.
- `popUpTo(R.id.albumListFragment) { saveState = true }`: antes de ir a la pestaña, cierra todo lo que haya encima de
  Álbumes, guardando su estado. Por eso Atrás desde Artistas vuelve a Álbumes.
- `restoreState`: si se vuelve a una pestaña que ya se visitó, recupera su estado guardado.
- Devolver `true` marca la pestaña como seleccionada.
- `setOnItemReselectedListener { }`: tocar la pestaña activa no hace nada.
- No se usa `setupWithNavController` (la conexión automática de Navigation) porque esa función vuelve siempre al
  destino inicial del grafo, que en el paso 5 será la selección de rol.

```kotlin
    private fun onDestinationChanged(destination: NavDestination) {
        binding.toolbar.title = destination.label
        binding.bottomNav.menu.findItem(destination.id)?.isChecked = true
    }
}
```

- En cada cambio de pantalla: el título de la barra superior es el `android:label` del destino, y se marca la pestaña
  correspondiente (por ejemplo, al volver con Atrás). `?.` evita un error si el destino no es una pestaña.

---

## Qué sigue

En el paso 4 (T3) se crean los componentes que comparten todas las pantallas: carga, error con «Reintentar», vacío y
avisos. Quedan listos para que las demás historias los usen.
