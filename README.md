# Vinilos · Paso 4 de 9: T3 · Componentes de carga, error, vacío y avisos

Tarea de la HU07: **«Crear los componentes de carga, error, vacío y avisos»**.

Son las piezas que comparten todas las pantallas que cargan datos o guardan algo. **No se muestran en ninguna pantalla
todavía**: las pestañas siguen vacías a propósito y cada historia los usará cuando construya su contenido (por
ejemplo, HU01 en la lista de álbumes). El aviso (snackbar) sí se usa en los pasos 5 y 8.

| Componente | Cómo se ve | Archivos |
|---|---|---|
| **Carga** (LoadingState) | Barra de progreso, texto «Cargando…» y bloques grises que anticipan el contenido (*skeleton*) | `view_loading.xml` |
| **Error** (ErrorState) | Círculo con nube tachada, «No pudimos cargar {objeto}», «Revisa tu conexión…» y botón **Reintentar** | `view_state_message.xml` + `showError` |
| **Vacío** (EmptyState) | Círculo con el ícono que se elija, título y texto | `view_state_message.xml` + `showEmpty` |
| **Aviso** (AppSnackbar) | Barra oscura abajo con ícono de éxito o de error, texto, acción opcional y botón X. Dura 4 s y uno nuevo reemplaza al anterior | `AppSnackbar.kt` |
| **Estado de pantalla** | `Loading`, `Success`, `Empty` o `Error` | `UiState.kt` |

| Paso | Commit |
|---|---|
| 1 | Proyecto base de Android Studio |
| 2 | T1 · Configuración, capa de red y estructura MVVM |
| 3 | T2 · Tema claro y oscuro y navegación |
| **4** | **T3 · Componentes de carga, error, vacío y avisos** |
| 5 | T4 · Selección de rol y menú de rol |
| 6 | T5 · Botón + en Álbumes |
| 7 | T6 · Formulario de crear álbum con validaciones |
| 8 | T7 · Creación del álbum en la API |
| 9 | T8 · Pruebas de la historia |

## Orden en que se hicieron los cambios

1. `UiState.kt`: los cuatro estados de una pantalla que carga datos.
2. Recursos: los íconos (`ic_cloud_off`, `ic_refresh`, `ic_check`, `ic_error`, `ic_close`), las formas
   (`bg_circle_surface_container`, `bg_skeleton_block`, `bg_skeleton_line`), el color `md_divider`, las medidas de
   `dimens.xml`, los estilos de botón, aviso y *skeleton*, y los textos.
3. Layouts: `view_loading.xml` y `view_state_message.xml`.
4. `StateViews.kt`: funciones para pintar los layouts.
5. `AppSnackbar.kt` y la función `showSnackbar` de `MainActivity`.
6. Compilar y revisar con Lint (0 errores; ver «Revisión con Lint» al final).

## Cómo ejecutarlo

1. **Run ▶**. La app se ve igual que en el paso 3, porque ninguna pantalla usa todavía estos componentes.
2. Para **ver los componentes**: abrir `res/layout/view_loading.xml` o `res/layout/view_state_message.xml` en Android
   Studio y elegir la vista **Design** o **Split**. El editor los muestra con los textos de ejemplo (`tools:text`).

### Cómo los usa una historia

En el layout de su pantalla:

```xml
<include android:id="@+id/loading" layout="@layout/view_loading" android:visibility="gone" />
<include android:id="@+id/message" layout="@layout/view_state_message" android:visibility="gone" />
```

En su fragment, según el estado que exponga su ViewModel:

```kotlin
binding.loading.root.isVisible = state is UiState.Loading
binding.message.root.isVisible = state is UiState.Error || state is UiState.Empty
when (state) {
    UiState.Loading -> binding.loading.showLoading(getString(R.string.loading_albums))
    UiState.Error -> binding.message.showError(getString(R.string.error_obj_albums)) { viewModel.load() }
    UiState.Empty -> binding.message.showEmpty(R.drawable.ic_album, título, texto)
    is UiState.Success -> mostrarLaLista(state.data)
}
```

Y para un aviso, desde cualquier fragment:

```kotlin
(requireActivity() as MainActivity).showSnackbar(getString(R.string.mi_texto))
(requireActivity() as MainActivity).showSnackbar(texto, isError = true, actionText = "Reintentar") { reintentar() }
```

Los textos propios de cada pantalla (`loading_albums`, `error_obj_albums`…) los agrega cada historia en `strings.xml`.

---

## Explicación de cada archivo

### `app/src/main/java/com/equipo2/vinilos/ui/common/UiState.kt` · creado por mí

```kotlin
package com.equipo2.vinilos.ui.common

/** Estado de una pantalla que carga datos (sección 3.2 de la guía). */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data object Empty : UiState<Nothing>
    data object Error : UiState<Nothing>
}
```

- `sealed interface`: una interfaz cuyas implementaciones están todas en este archivo. Kotlin sabe que solo hay cuatro
  posibilidades, así que un `when` que las cubre todas no necesita `else`, y si alguien agrega un estado nuevo el
  compilador avisa en cada `when` que falte actualizar.
- `<out T>`: el tipo de dato que trae el éxito (por ejemplo, `UiState<List<Album>>`). `out` permite usar un
  `UiState<Nothing>` donde se espera cualquier `UiState<T>`.
- `data object Loading`: un único objeto (no tiene datos). `data` le da un `toString` legible.
- `data class Success<T>(val data: T)`: el único estado con datos.
- `Nothing`: el tipo de Kotlin que no tiene valores; aquí significa "estos estados no traen datos".

---

### `res/layout/view_loading.xml` · creado por mí

```xml
<LinearLayout … android:orientation="vertical">
```

- Columna vertical con las tres partes. Se declaran `xmlns:app` (atributos de Material) y `xmlns:tools` (solo para el
  editor).

```xml
    <com.google.android.material.progressindicator.LinearProgressIndicator
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:indeterminate="true"
        app:indicatorColor="@color/md_primary"
        app:trackColor="@color/md_primary_container"
        app:trackThickness="4dp" />
```

- Barra de progreso de Material a todo el ancho.
- `indeterminate`: no muestra un porcentaje; se anima mientras dura la carga.
- Indicador en `primary` sobre una pista `primary_container`, de 4 dp de alto (guía, sección 8).

```xml
    <TextView
        android:id="@+id/loadingText"
        …
        android:accessibilityLiveRegion="polite"
        android:paddingStart="16dp"
        android:paddingTop="12dp"
        android:paddingEnd="16dp"
        android:text="@string/loading_default"
        android:textAppearance="@style/TextAppearance.Vinilos.BodyMedium"
        android:textColor="@color/md_on_surface_variant"
        tools:text="Cargando álbumes…" />
```

- El texto de carga. Por defecto «Cargando…»; cada pantalla lo cambia con `showLoading`.
- `accessibilityLiveRegion="polite"`: TalkBack (el lector de pantalla) anuncia el texto cuando cambia, sin
  interrumpir lo que esté leyendo.
- `paddingStart`/`paddingEnd` en lugar de `paddingHorizontal`, porque este último solo existe desde Android 8.0.
- `tools:text`: el texto que muestra el editor de Android Studio; no llega a la app.

```xml
    <LinearLayout
        …
        android:importantForAccessibility="noHideDescendants"
        android:orientation="vertical"
        android:padding="16dp">

        <View style="@style/Widget.Vinilos.Skeleton.Block" />

        <View
            style="@style/Widget.Vinilos.Skeleton.Line"
            android:layout_width="200dp" />

        <View
            style="@style/Widget.Vinilos.Skeleton.Block"
            android:layout_marginTop="24dp" />

        <View
            style="@style/Widget.Vinilos.Skeleton.Line"
            android:layout_width="160dp" />
    </LinearLayout>
```

- El *skeleton*: bloques grises que imitan la forma del contenido que va a llegar. Dos bloques grandes (portadas)
  con una línea debajo de cada uno (título).
- `importantForAccessibility="noHideDescendants"`: TalkBack ignora estos bloques, que son solo decorativos.
- Cada historia puede copiar este layout y adaptar el *skeleton* a la forma de su pantalla.

### `res/layout/view_state_message.xml` · creado por mí

Un solo layout para los estados de vacío y de error, que solo se diferencian en el ícono, los textos y el botón.

```xml
<LinearLayout …
    android:gravity="center_horizontal"
    android:orientation="vertical"
    android:paddingStart="32dp"
    android:paddingTop="120dp"
    android:paddingEnd="32dp"
    android:paddingBottom="32dp">
```

- Columna centrada horizontalmente, a 120 dp del borde superior y con márgenes laterales de 32 dp (guía, sección 5.3).

```xml
    <FrameLayout
        android:layout_width="88dp"
        android:layout_height="88dp"
        android:background="@drawable/bg_circle_surface_container">

        <ImageView
            android:id="@+id/stateIcon"
            android:layout_width="40dp"
            android:layout_height="40dp"
            android:layout_gravity="center"
            android:importantForAccessibility="no"
            app:tint="@color/md_on_surface_variant"
            tools:src="@drawable/ic_cloud_off" />
    </FrameLayout>
```

- Un círculo de 88 dp (`bg_circle_surface_container`) con un ícono de 40 dp en el centro.
- `importantForAccessibility="no"`: el ícono es decorativo; el título ya dice lo que pasa.
- `app:tint`: pinta el ícono (que es negro) del color del texto secundario.

```xml
    <TextView
        android:id="@+id/stateTitle"
        …
        android:layout_marginTop="24dp"
        android:accessibilityLiveRegion="polite"
        android:gravity="center"
        android:textAppearance="@style/TextAppearance.Vinilos.HeadlineSmall"
        tools:text="No pudimos cargar los álbumes" />

    <TextView
        android:id="@+id/stateBody"
        …
        android:layout_marginTop="8dp"
        android:gravity="center"
        android:textAppearance="@style/TextAppearance.Vinilos.BodyLarge"
        android:textColor="@color/md_on_surface_variant"
        tools:text="Revisa tu conexión a internet e inténtalo de nuevo." />
```

- Título (anunciado por TalkBack al aparecer) y texto explicativo, centrados.

```xml
    <com.google.android.material.button.MaterialButton
        android:id="@+id/stateRetry"
        …
        android:layout_marginTop="24dp"
        android:text="@string/error_network_retry"
        android:visibility="gone"
        app:icon="@drawable/ic_refresh"
        app:iconSize="20dp"
        tools:visibility="visible" />
</LinearLayout>
```

- Botón «Reintentar» con el ícono de recargar. Oculto por defecto (`gone`): solo aparece en el estado de error.
- `tools:visibility="visible"`: en el editor se ve, para poder revisar el diseño.

### Formas · creadas por mí

| Archivo | Qué dibuja |
|---|---|
| `res/drawable/bg_circle_surface_container.xml` | `<shape android:shape="oval">` con `<solid>` en `surface_container`: el círculo de fondo del ícono |
| `res/drawable/bg_skeleton_block.xml` | Rectángulo (la forma por defecto) con esquinas de 12 dp (`<corners>`) en `surface_variant`: bloque grande del *skeleton* |
| `res/drawable/bg_skeleton_line.xml` | Rectángulo con esquinas de 7 dp en `md_divider`: línea del *skeleton* |

### Íconos · creados por mí

`ic_cloud_off` (nube tachada, error de conexión), `ic_refresh` (Reintentar), `ic_check` (aviso de éxito), `ic_error`
(aviso de error) e `ic_close` (cerrar el aviso). Son de Material Symbols, con la misma estructura explicada en el paso 3.

### `res/values/colors.xml` y `res/values-night/colors.xml` · modificados por mí

```xml
    <color name="md_divider">#E9E4F0</color>   <!-- en values-night: #2A2433 -->
```

- Único cambio: el color de las líneas del *skeleton* (y de los divisores de las listas que vendrán). No es un rol de
  Material 3; la guía lo define aparte.

### `res/values/dimens.xml` · creado por mí

```xml
<resources>
    <dimen name="touch_target">48dp</dimen>
    <dimen name="snackbar_icon">20dp</dimen>
    <dimen name="snackbar_icon_padding">12dp</dimen>
</resources>
```

- Medidas que usa el código Kotlin (en los layouts se escriben directo). Guardarlas aquí permite leerlas en píxeles
  según la densidad de la pantalla con `getDimensionPixelSize`.
- `touch_target`: el área táctil mínima de accesibilidad (48 × 48 dp), usada por el botón X del aviso.
- `snackbar_icon` y `snackbar_icon_padding`: tamaño del ícono del aviso y su separación con el texto.

### `res/values/styles.xml` · modificado por mí

Estilos nuevos (el resto quedó igual):

```xml
    <style name="Widget.Vinilos.Button" parent="Widget.Material3.Button">
        <item name="android:minHeight">48dp</item>
        <item name="cornerRadius">24dp</item>
    </style>
```

- Botón relleno de la app: al menos 48 dp de alto y esquinas de 24 dp (forma de píldora), como pide la guía.

```xml
    <style name="Widget.Vinilos.Snackbar" parent="Widget.Material3.Snackbar">
        <item name="backgroundTint">@color/md_inverse_surface</item>
    </style>
```

- Fondo del aviso en `inverse_surface` (oscuro en el tema claro y claro en el oscuro).

```xml
    <style name="Widget.Vinilos.Skeleton.Block" parent="">
        <item name="android:layout_width">match_parent</item>
        <item name="android:layout_height">120dp</item>
        <item name="android:background">@drawable/bg_skeleton_block</item>
    </style>

    <style name="Widget.Vinilos.Skeleton.Line" parent="">
        <item name="android:layout_height">14dp</item>
        <item name="android:layout_marginTop">12dp</item>
        <item name="android:background">@drawable/bg_skeleton_line</item>
    </style>
```

- Estilos para las piezas del *skeleton*. Un estilo puede llevar atributos de layout (ancho, alto, márgenes); así cada
  pieza en el XML queda en una línea. La línea no define su ancho: lo pone cada uso.

### `res/values/themes.xml` · modificado por mí

```xml
        <item name="materialButtonStyle">@style/Widget.Vinilos.Button</item>
        <item name="snackbarStyle">@style/Widget.Vinilos.Snackbar</item>
```

- Las dos líneas nuevas hacen que **todos** los `MaterialButton` y los `Snackbar` de la app usen esos estilos sin
  tener que indicarlo en cada uno.

### `res/values/strings.xml` · modificado por mí

```xml
    <string name="loading_default">Cargando…</string>
    <string name="error_load_title">No pudimos cargar %1$s</string>
    <string name="error_network_body">Revisa tu conexión a internet e inténtalo de nuevo.</string>
    <string name="error_network_retry">Reintentar</string>
    <string name="cd_close_snackbar">Cerrar aviso</string>
```

- `loading_default`: texto de carga por defecto. El `…` es un solo carácter (puntos suspensivos), no tres puntos.
- `error_load_title`: `%1$s` es un **marcador**: se reemplaza por el primer argumento de `getString`, por ejemplo
  `getString(R.string.error_load_title, "los álbumes")` → «No pudimos cargar los álbumes».
- `error_network_body` y `error_network_retry`: el mensaje y el botón del estado de error (guía, sección 11.3).
- `cd_close_snackbar`: descripción del botón X para TalkBack (`cd` = *content description*).

---

### `app/src/main/java/com/equipo2/vinilos/ui/common/StateViews.kt` · creado por mí

```kotlin
package com.equipo2.vinilos.ui.common

import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import com.equipo2.vinilos.R
import com.equipo2.vinilos.databinding.ViewLoadingBinding
import com.equipo2.vinilos.databinding.ViewStateMessageBinding
```

- `ViewLoadingBinding` y `ViewStateMessageBinding`: las clases que View Binding genera para los dos layouts.
- `@DrawableRes`: anotación que indica que un `Int` debe ser un id de drawable (Lint avisa si se pasa otra cosa).
- `isVisible`: propiedad de AndroidX que muestra (`VISIBLE`) u oculta (`GONE`) una vista con un `Boolean`.

El comentario largo del inicio es la **guía de uso** para el equipo: cómo incluir los layouts y cómo pintarlos según el
`UiState` (el mismo ejemplo de «Cómo los usa una historia»).

```kotlin
/** LoadingState: barra lineal, texto y bloques de carga. */
fun ViewLoadingBinding.showLoading(text: CharSequence) {
    loadingText.text = text
}
```

- Función de extensión sobre el *binding* del layout de carga: solo cambia el texto.

```kotlin
/** EmptyState: círculo con ícono, título y texto. */
fun ViewStateMessageBinding.showEmpty(@DrawableRes icon: Int, title: CharSequence, body: CharSequence) {
    stateIcon.setImageResource(icon)
    stateTitle.text = title
    stateBody.text = body
    stateRetry.isVisible = false
}
```

- Pinta el estado vacío: el ícono y los textos que diga la pantalla, sin botón.
- `CharSequence` (en lugar de `String`) acepta también textos con formato.

```kotlin
/** ErrorState: «No pudimos cargar {objeto}» con el botón «Reintentar». */
fun ViewStateMessageBinding.showError(objectName: CharSequence, onRetry: () -> Unit) {
    val context = root.context
    stateIcon.setImageResource(R.drawable.ic_cloud_off)
    stateTitle.text = context.getString(R.string.error_load_title, objectName)
    stateBody.text = context.getString(R.string.error_network_body)
    stateRetry.isVisible = true
    stateRetry.setOnClickListener { onRetry() }
}
```

- Pinta el estado de error con el ícono y los textos fijos de la guía; la pantalla solo dice qué no se pudo cargar
  (`objectName`) y qué hacer al reintentar.
- `onRetry: () -> Unit`: un parámetro que es una función sin argumentos ni resultado. Se ejecuta al tocar el botón.

### `app/src/main/java/com/equipo2/vinilos/ui/common/AppSnackbar.kt` · creado por mí

El aviso de la app sobre el `Snackbar` de Material, con lo que la guía pide y Material no trae: ícono, botón de cerrar
y reemplazo inmediato del aviso anterior.

```kotlin
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.equipo2.vinilos.R
import com.google.android.material.snackbar.Snackbar
import java.lang.ref.WeakReference
```

- `AppCompatResources` y `DrawableCompat`: cargar y teñir íconos vectoriales de forma compatible con Android 5.0.
- `WeakReference`: una referencia que no impide que el sistema libere el objeto (ver `current`).

```kotlin
/** Aviso de la app: ícono de éxito o error, texto, acción opcional y botón de cerrar. */
object AppSnackbar {

    private const val DURATION_MS = 4000

    private var current: WeakReference<Snackbar>? = null
```

- `DURATION_MS`: 4 s, la duración que pide la guía.
- `current`: el aviso visible, para poder quitarlo cuando llega otro. Es una referencia débil para no retener la
  pantalla en memoria si el aviso ya no existe.

```kotlin
    fun show(
        root: View,
        message: CharSequence,
        isError: Boolean = false,
        anchor: View? = null,
        actionText: CharSequence? = null,
        action: (() -> Unit)? = null,
    ) {
```

- `root`: la vista donde aparece (la raíz de `MainActivity`).
- `isError`: elige el ícono. `anchor`: vista sobre la que se coloca (por ejemplo, para quedar encima del botón +).
- `actionText` y `action`: botón de acción opcional (por ejemplo, «Reintentar»).
- Los valores por defecto (`= false`, `= null`) permiten llamarla solo con el texto.

```kotlin
        dismissCurrent()
        val snackbar = Snackbar.make(root, message, DURATION_MS)
        if (anchor != null && anchor.isShown) snackbar.anchorView = anchor
```

- Quita el aviso anterior y crea el nuevo. `Snackbar.make` acepta una duración en milisegundos.
- `anchorView`: el aviso aparece justo encima de esa vista. Solo si está visible (`isShown`); anclarlo a una vista
  oculta lo dejaría mal ubicado.

```kotlin
        if (actionText != null && action != null) {
            snackbar.setAction(actionText) { action() }
            snackbar.setActionTextColor(ContextCompat.getColor(root.context, R.color.md_inverse_primary))
        }
        snackbar.decorate(isError)
        current = WeakReference(snackbar)
        snackbar.show()
    }
```

- Si hay acción, agrega el botón con el color `inverse_primary` (se lee sobre el fondo oscuro).
- `decorate` agrega el ícono y la X; luego se guarda como aviso actual y se muestra.

```kotlin
    /** Un aviso nuevo reemplaza al anterior de inmediato, sin esperar su animación de salida. */
    private fun dismissCurrent() {
        current?.get()?.let { previous ->
            previous.view.visibility = View.INVISIBLE
            previous.dismiss()
        }
    }
```

- Material encola los avisos: el nuevo espera a que el anterior termine de animarse. Para que el nuevo aparezca de
  inmediato, se oculta el anterior (`INVISIBLE`) antes de cerrarlo; Material solo anima la salida de avisos visibles.
- `current?.get()?.let { … }`: si hay referencia y el aviso todavía existe, ejecuta el bloque.

```kotlin
    private fun Snackbar.decorate(isError: Boolean) {
        val context = view.context
        val contentColor = ContextCompat.getColor(context, R.color.md_inverse_on_surface)
        val text = view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        text.maxLines = 4
```

- Extensión privada del `Snackbar`. Busca el `TextView` interno del aviso con el id que define Material
  (`snackbar_text`) y permite hasta 4 líneas (por defecto son 2, y el mensaje de error del paso 8 es largo).

```kotlin
        val iconSize = context.resources.getDimensionPixelSize(R.dimen.snackbar_icon)
        val icon = AppCompatResources.getDrawable(context, if (isError) R.drawable.ic_error else R.drawable.ic_check)!!.mutate()
        DrawableCompat.setTint(icon, contentColor)
        icon.setBounds(0, 0, iconSize, iconSize)
        text.setCompoundDrawablesRelative(icon, null, null, null)
        text.compoundDrawablePadding = context.resources.getDimensionPixelSize(R.dimen.snackbar_icon_padding)
```

- Carga el ícono de error o de éxito. `!!` afirma que no es nulo (el recurso existe). `mutate()` crea una copia para
  que teñirlo no cambie el color del mismo ícono en otras partes de la app.
- Lo tiñe del color del texto, le da 20 × 20 dp y lo pone al inicio del texto (`Relative`: a la izquierda, o a la
  derecha en idiomas que se leen al revés), separado 12 dp.

```kotlin
        // Material no trae botón de cerrar: se agrega al final del contenido del aviso.
        val content = text.parent as ViewGroup
        val close = ImageButton(context).apply {
            setImageResource(R.drawable.ic_close)
            setColorFilter(contentColor)
            contentDescription = context.getString(R.string.cd_close_snackbar)
            val ripple = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, ripple, true)
            setBackgroundResource(ripple.resourceId)
            setOnClickListener { dismiss() }
        }
        val touchTarget = context.resources.getDimensionPixelSize(R.dimen.touch_target)
        content.addView(close, LinearLayout.LayoutParams(touchTarget, touchTarget))
    }
}
```

- `text.parent`: el contenedor interno del aviso (una fila con el texto y la acción). Se le agrega un botón con la X.
- `apply { … }`: configura el objeto recién creado y lo devuelve.
- `setColorFilter`: tiñe la X. `contentDescription`: lo que lee TalkBack.
- `resolveAttribute(selectableItemBackgroundBorderless)`: busca en el tema el efecto de toque circular (*ripple*) y lo
  usa de fondo.
- Al tocarlo, `dismiss()` cierra el aviso. Mide 48 × 48 dp (área táctil mínima).

### `app/src/main/java/com/equipo2/vinilos/MainActivity.kt` · modificado por mí

```kotlin
import android.view.View
…
import com.equipo2.vinilos.ui.common.AppSnackbar
```

```kotlin
    /** Muestra un aviso sobre el elemento inferior visible: el indicado en [anchor] o la barra inferior. */
    fun showSnackbar(
        message: CharSequence,
        isError: Boolean = false,
        anchor: View? = null,
        actionText: CharSequence? = null,
        action: (() -> Unit)? = null,
    ) {
        val target = anchor?.takeIf { it.isShown } ?: binding.bottomNav.takeIf { it.isShown }
        AppSnackbar.show(binding.root, message, isError, target, actionText, action)
    }
```

- Función pública para que cualquier fragment muestre un aviso sin conocer la estructura de la actividad.
- `takeIf { it.isShown }`: devuelve la vista si está visible, o `null` si no.
- El aviso se ancla a la vista indicada si está visible; si no, a la barra inferior si está visible; si no, va al
  borde inferior. Así nunca queda tapado por la barra inferior.
- `binding.root` es el `CoordinatorLayout`, donde el aviso se muestra correctamente.
- `[anchor]` en el comentario KDoc es un enlace al parámetro.

---

## Revisión con Lint

`./gradlew lintDebug` revisa el código y los recursos. Resultado: **0 errores**. Las advertencias son esperadas:

| Advertencia | Por qué se deja |
|---|---|
| `GradleDependency`, `NewerVersionAvailable`, `AndroidGradlePluginVersion` | Hay versiones más nuevas, pero exigen Android 6.0 o superior (ver el paso 2). **No actualizar.** |
| `UnusedResources` | Los componentes todavía no se usan en ninguna pantalla: es intencional |
| `Overdraw` en `fragment_empty_section.xml` | La pantalla vacía pinta su propio fondo para que las transiciones no se vean superpuestas |

---

## Qué sigue

En el paso 5 (T4) se construyen la selección de rol al abrir la app y el menú del chip de rol, con el cambio de rol,
el modo oscuro manual y la opción de volver al inicio.
