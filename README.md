# Vinilos · Paso 7 de 9: T6 · Formulario de crear álbum con validaciones

Tarea de la HU07: **«Construir el formulario de creación con sus validaciones»**.

Cubre el criterio de aceptación **«Guardar con campos vacíos o inválidos muestra el error debajo de cada campo»**.

La pantalla Crear álbum ahora tiene el formulario completo (guía, sección 9.1 `album_create`):

- **Nombre**, **URL de la portada** (con vista previa de la imagen cuando la URL es válida), **fecha de lanzamiento**
  (selector de fecha sin fechas futuras), **género** y **sello discográfico** (desplegables con los únicos valores que
  acepta la API) y **descripción**.
- Barra fija abajo con **Cancelar** y **Guardar**.
- Al tocar Guardar se valida todo: cada error aparece debajo de su campo, la pantalla se desplaza al primero y, desde
  ese momento, los errores se actualizan mientras se escribe.
- Cerrar, Cancelar o Atrás con datos escritos pregunta **«¿Descartar el álbum?»**.

Con el formulario válido, Guardar todavía no envía nada: la llamada a la API se conecta en el paso 8.

| Paso | Commit |
|---|---|
| 1 | Proyecto base de Android Studio |
| 2 | T1 · Configuración, capa de red y estructura MVVM |
| 3 | T2 · Tema claro y oscuro y navegación |
| 4 | T3 · Componentes de carga, error, vacío y avisos |
| 5 | T4 · Selección de rol y menú de rol |
| 6 | T5 · Botón + en Álbumes |
| **7** | **T6 · Formulario de crear álbum con validaciones** |
| 8 | T7 · Creación del álbum en la API |
| 9 | T8 · Pruebas de la historia |

## Las reglas de validación

Las de la guía (sección 10.1). Se recortan los espacios del inicio y del final antes de validar.

| Campo | Regla | Mensaje |
|---|---|---|
| Nombre del álbum | Obligatorio; máximo 100 caracteres | «Escribe el nombre del álbum.» / «Puede tener hasta 100 caracteres.» |
| URL de la portada | Obligatoria; debe cumplir `^https?://[^\s/]+\.\S+$` | «Escribe la URL de la portada.» / «URL no válida. Debe empezar por http:// o https://» |
| Fecha de lanzamiento | Obligatoria; no puede ser futura | «Selecciona la fecha de lanzamiento.» / «La fecha no puede ser futura.» |
| Género | Uno de `Classical`, `Salsa`, `Rock`, `Folk` | «Selecciona un género.» |
| Sello discográfico | Uno de `Sony Music`, `EMI`, `Discos Fuentes`, `Elektra`, `Fania Records` | «Selecciona un sello discográfico.» |
| Descripción | Obligatoria; máximo 1000 caracteres | «Escribe una descripción.» / «Puede tener hasta 1000 caracteres.» |

**Relación con BackVynils:** la API solo rechaza campos faltantes, fechas inválidas y géneros o sellos fuera de su
lista. No valida que la portada sea una URL, que la fecha no sea futura ni las longitudes: sin estas reglas, un álbum
con portada `"hola"` se guardaría. Por eso la validación vive en la app.

## Orden en que se hicieron los cambios

1. `domain/AlbumFormValidator.kt`: el formulario (`AlbumForm`), sus campos, los tipos de error y las reglas.
2. `ui/common/DateFormats.kt`: mostrar fechas en español y convertirlas para el selector de fecha.
3. `AlbumCreateViewModel.kt`: el estado del formulario y la validación al guardar.
4. `FieldErrorMessages.kt`: el texto de cada error.
5. Recursos: textos, estilos de los campos y del botón con borde, íconos, fondo de la vista previa y el color de
   marcador; Coil en Gradle para cargar la imagen de la portada.
6. `fragment_album_create.xml`: el formulario.
7. `AlbumCreateFragment.kt`: conectar el formulario con el ViewModel, el selector de fecha, los desplegables, la vista
   previa y el diálogo de descartar.
8. Lint (0 errores) y prueba en el emulador.

## Cómo ejecutarlo

1. **File › Sync Project with Gradle Files** (se agregó Coil) y **Run ▶**.
2. Entrar como coleccionista y tocar **+**.
3. Tocar **Guardar** con todo vacío: aparecen los seis errores.
4. Escribir en un campo: su error desaparece en cuanto el valor es válido.
5. Escribir una URL como `https://ejemplo.com/a.jpg`: aparece la vista previa (con un marcador si la imagen no carga).
6. Tocar la fecha: se abre el calendario; los días futuros están deshabilitados.
7. Escribir algo y tocar la **X**: aparece «¿Descartar el álbum?».

---

## Explicación de cada archivo

### `app/src/main/java/com/equipo2/vinilos/domain/AlbumFormValidator.kt` · creado por mí

El paquete `domain` guarda reglas del negocio que no dependen de Android: se pueden probar en el computador sin
emulador (paso 9).

```kotlin
/** Lo que el usuario lleva escrito en el formulario de crear álbum. */
data class AlbumForm(
    val name: String = "",
    val cover: String = "",
    val releaseDate: LocalDate? = null,
    val genre: String? = null,
    val recordLabel: String? = null,
    val description: String = "",
) {
```

- El formulario tal cual está, válido o no. Todos los campos tienen valor por defecto (vacío o `null`), así
  `AlbumForm()` es un formulario vacío.
- Fecha, género y sello son nulos hasta que se eligen.

```kotlin
    val isEmpty: Boolean
        get() = name.isBlank() && cover.isBlank() && releaseDate == null &&
            genre == null && recordLabel == null && description.isBlank()
```

- Si no se ha escrito ni elegido nada. Decide si cerrar directo o preguntar antes de descartar.
- `isBlank()`: vacío o solo espacios.

```kotlin
    /** Solo se llama con un formulario válido: todos los campos tienen valor. */
    fun toNewAlbum() = NewAlbum(
        name = name.trim(),
        cover = cover.trim(),
        releaseDate = checkNotNull(releaseDate),
        description = description.trim(),
        genre = checkNotNull(genre),
        recordLabel = checkNotNull(recordLabel),
    )
}
```

- Convierte el formulario en lo que se envía a la API (paso 8), quitando espacios sobrantes con `trim()`.
- `checkNotNull(x)`: devuelve `x` sin el `?`; si fuera nulo, lanza un error con un mensaje claro. Documenta que solo
  se usa después de validar.

```kotlin
/** Campos del formulario, en el orden en que aparecen en pantalla. */
enum class AlbumField { NAME, COVER, RELEASE_DATE, GENRE, RECORD_LABEL, DESCRIPTION }
```

- Identifica cada campo. El orden importa: el primer error es el del campo más arriba.

```kotlin
sealed interface FieldError {
    data object Required : FieldError
    data object InvalidUrl : FieldError
    data object FutureDate : FieldError
    data class TooLong(val max: Int) : FieldError
}
```

- Los tipos de error posibles. `TooLong` lleva el máximo para poder decirlo en el mensaje.
- La regla devuelve el **tipo** de error y no el texto: el texto es cosa de la interfaz (está en `strings.xml`).

```kotlin
object AlbumFormValidator {

    const val NAME_MAX_LENGTH = 100
    const val DESCRIPTION_MAX_LENGTH = 1000
    private val urlPattern = Regex("""^https?://[^\s/]+\.\S+$""", RegexOption.IGNORE_CASE)
```

- Las longitudes máximas de la guía.
- La expresión regular de la URL (la de la guía). Las comillas triples `"""` evitan tener que escapar las `\`:
  - `^https?://`: empieza por `http://` o `https://` (la `s` es opcional por el `?`).
  - `[^\s/]+`: uno o más caracteres que no sean espacio ni `/` (el dominio).
  - `\.`: un punto (por ejemplo, el de `.com`).
  - `\S+$`: uno o más caracteres sin espacios hasta el final.
  - `IGNORE_CASE`: acepta `HTTPS://`.

```kotlin
    fun validate(form: AlbumForm, today: LocalDate): Map<AlbumField, FieldError> = buildMap {
        textError(form.name, NAME_MAX_LENGTH)?.let { put(AlbumField.NAME, it) }
        coverError(form.cover)?.let { put(AlbumField.COVER, it) }
        dateError(form.releaseDate, today)?.let { put(AlbumField.RELEASE_DATE, it) }
        if (form.genre !in AlbumCatalog.genres) put(AlbumField.GENRE, FieldError.Required)
        if (form.recordLabel !in AlbumCatalog.recordLabels) put(AlbumField.RECORD_LABEL, FieldError.Required)
        textError(form.description, DESCRIPTION_MAX_LENGTH)?.let { put(AlbumField.DESCRIPTION, it) }
    }
```

- Devuelve un mapa **campo → error** solo con los campos que tienen error; vacío si todo está bien.
- `buildMap { … }`: construye un mapa agregando entradas con `put`. Conserva el orden de inserción, que es el de la
  pantalla.
- `?.let { put(…) }`: si la función devolvió un error, se agrega; si devolvió `null`, no.
- `today` es un parámetro (y no `LocalDate.now()` adentro) para que las pruebas puedan fijar la fecha de "hoy".
- Género y sello: `!in` funciona también con `null` (nulo no está en la lista, así que es error).

```kotlin
    fun isValidUrl(value: String): Boolean = urlPattern.matches(value.trim())
```

- Pública porque la pantalla también la usa para decidir si muestra la vista previa.

```kotlin
    private fun textError(value: String, maxLength: Int): FieldError? = when {
        value.isBlank() -> FieldError.Required
        value.trim().length > maxLength -> FieldError.TooLong(maxLength)
        else -> null
    }

    private fun coverError(value: String): FieldError? = when {
        value.isBlank() -> FieldError.Required
        !isValidUrl(value) -> FieldError.InvalidUrl
        else -> null
    }

    private fun dateError(value: LocalDate?, today: LocalDate): FieldError? = when {
        value == null -> FieldError.Required
        value.isAfter(today) -> FieldError.FutureDate
        else -> null
    }
}
```

- Cada función revisa un tipo de campo y devuelve su error o `null`.
- `when { … }` sin argumento evalúa las condiciones en orden y usa la primera verdadera. Un campo vacío da
  "obligatorio" aunque también sea "inválido".

### `app/src/main/java/com/equipo2/vinilos/ui/common/DateFormats.kt` · creado por mí

```kotlin
object DateFormats {

    private val spanish = Locale.forLanguageTag("es")
    private val longFormat = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", spanish)
```

- El formato largo de la guía: `d` es el día, `MMMM` el mes con letras, `yyyy` el año; lo que va entre comillas
  simples (`'de'`) es texto literal.

```kotlin
    /** «1 de junio de 1992». En Android 5 el mes llega con mayúscula inicial, por eso se pasa a minúsculas. */
    fun long(date: LocalDate): String = longFormat.format(date).lowercase(spanish)
```

- En Android 5 el nombre del mes sale como «Junio»; la guía lo pide en minúscula. Se detectó probando en el emulador
  de Android 5.

```kotlin
    /** Los selectores de fecha de Material trabajan en milisegundos UTC. */
    fun toUtcMillis(date: LocalDate): Long = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    fun fromUtcMillis(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
}
```

- `MaterialDatePicker` representa los días como milisegundos desde 1970 a medianoche UTC. Estas funciones convierten
  entre ese número y `LocalDate`. Usar siempre UTC evita que, según la zona horaria del teléfono, la fecha elegida se
  corra un día.

### `app/src/main/java/com/equipo2/vinilos/ui/albums/create/AlbumCreateViewModel.kt` · creado por mí

```kotlin
data class AlbumCreateUiState(
    val form: AlbumForm = AlbumForm(),
    val errors: Map<AlbumField, FieldError> = emptyMap(),
)
```

- El estado de la pantalla: lo escrito y los errores a mostrar. Al ser inmutable, cada cambio crea un estado nuevo
  con `copy`.

```kotlin
sealed interface AlbumCreateEvent {
    /** Guardar con errores: la pantalla lleva el foco al primer campo con error. */
    data class ShowErrors(val firstField: AlbumField) : AlbumCreateEvent
}
```

- Evento de una sola vez: desplazarse al primer error. En el paso 8 se agregan los eventos "creado" y "falló".

```kotlin
class AlbumCreateViewModel(
    private val today: () -> LocalDate = LocalDate::now,
) : ViewModel() {
```

- `today`: una función que devuelve la fecha de hoy. Por defecto `LocalDate::now` (referencia a la función); las
  pruebas pasan una fecha fija.

```kotlin
    private val _state = MutableStateFlow(AlbumCreateUiState())
    val state: StateFlow<AlbumCreateUiState> = _state.asStateFlow()

    private val _events = Channel<AlbumCreateEvent>(Channel.BUFFERED)
    val events: Flow<AlbumCreateEvent> = _events.receiveAsFlow()
```

- Estado y eventos con el mismo patrón del `SessionViewModel` (paso 5).

```kotlin
    /** Después del primer intento de guardar, cada cambio vuelve a validar el formulario. */
    private var validateOnChange = false
```

- Antes del primer Guardar no se muestran errores (no tiene sentido decir «Escribe el nombre» mientras se escribe).
  Después de intentarlo, cada cambio revalida para que el error desaparezca en cuanto se corrige (guía, sección 10).

```kotlin
    fun onNameChange(value: String) = edit { copy(name = value) }
    fun onCoverChange(value: String) = edit { copy(cover = value) }
    fun onReleaseDateChange(value: LocalDate) = edit { copy(releaseDate = value) }
    fun onGenreChange(value: String) = edit { copy(genre = value) }
    fun onRecordLabelChange(value: String) = edit { copy(recordLabel = value) }
    fun onDescriptionChange(value: String) = edit { copy(description = value) }
```

- Una función por campo, que la pantalla llama con cada cambio. Todas delegan en `edit`.
- `{ copy(name = value) }` es una lambda que se ejecuta "dentro" del formulario (ver `edit`): devuelve una copia con
  ese campo cambiado.

```kotlin
    fun save() {
        validateOnChange = true
        val errors = AlbumFormValidator.validate(_state.value.form, today())
        _state.update { it.copy(errors = errors) }
        errors.keys.firstOrNull()?.let { _events.trySend(AlbumCreateEvent.ShowErrors(it)) }
    }
```

- Activa la revalidación, valida y guarda los errores en el estado (la pantalla los pinta).
- Si hay errores, envía el evento con el primero (`firstOrNull()`: la primera clave, o `null` si no hay ninguna).
- `trySend`: envía sin esperar (no hace falta una corrutina).
- `update { … }`: cambia el estado de forma segura aunque se llame desde varios hilos.
- Con el formulario válido aquí termina; en el paso 8 sigue con la llamada a la API.

```kotlin
    private fun edit(change: AlbumForm.() -> AlbumForm) = _state.update { current ->
        val form = current.form.change()
        val errors = if (validateOnChange) AlbumFormValidator.validate(form, today()) else current.errors
        current.copy(form = form, errors = errors)
    }
```

- `change: AlbumForm.() -> AlbumForm`: una **lambda con receptor**; dentro de ella `this` es el formulario, por eso se
  puede escribir `copy(name = value)` sin prefijo.
- Aplica el cambio y, si ya se intentó guardar, recalcula los errores.

```kotlin
    companion object {
        val Factory = viewModelFactory {
            initializer { AlbumCreateViewModel() }
        }
    }
}
```

- La fábrica. En el paso 8 le pasará el repositorio.

### `app/src/main/java/com/equipo2/vinilos/ui/albums/create/FieldErrorMessages.kt` · creado por mí

```kotlin
/** El mensaje que se muestra debajo de cada campo (sección 10.1 de la guía). */
fun FieldError.message(context: Context, field: AlbumField): String = when (this) {
    FieldError.Required -> context.getString(
        when (field) {
            AlbumField.NAME -> R.string.error_album_name_required
            AlbumField.COVER -> R.string.error_cover_required
            AlbumField.RELEASE_DATE -> R.string.error_date_required
            AlbumField.GENRE -> R.string.error_genre_required
            AlbumField.RECORD_LABEL -> R.string.error_label_required
            AlbumField.DESCRIPTION -> R.string.error_description_required
        }
    )
    FieldError.InvalidUrl -> context.getString(R.string.error_cover_invalid)
    FieldError.FutureDate -> context.getString(R.string.error_date_future)
    is FieldError.TooLong -> context.getString(R.string.error_max_length, max)
}
```

- Traduce un error a su texto. "Obligatorio" depende del campo («Escribe el nombre…», «Selecciona un género…»).
- `is FieldError.TooLong`: comprueba el tipo; adentro Kotlin sabe que `this` es `TooLong` y deja leer `max` (*smart
  cast*).
- Separa la regla (dominio) del texto (interfaz): si cambia un mensaje, la regla no se toca.

---

### `res/layout/fragment_album_create.xml` · modificado por mí

Reemplaza la pantalla vacía del paso 6.

```xml
<LinearLayout … android:background="@color/md_background" android:orientation="vertical">

    <androidx.core.widget.NestedScrollView
        android:id="@+id/formScroll"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1">
```

- Columna con dos partes: el formulario con desplazamiento (ocupa todo el alto libre) y la barra de botones fija abajo.
- `NestedScrollView`: contenedor con desplazamiento vertical que funciona bien dentro de otras vistas con scroll.

```xml
        <LinearLayout … android:orientation="vertical"
            android:paddingStart="16dp" android:paddingTop="16dp" android:paddingEnd="16dp" android:paddingBottom="32dp">

            <TextView … android:text="@string/form_required_note" … />
```

- Márgenes de la guía y la nota «Todos los campos son obligatorios (*).».

```xml
            <com.google.android.material.textfield.TextInputLayout
                android:id="@+id/tilName"
                style="@style/Widget.Vinilos.FormField"
                android:hint="@string/field_album_name"
                app:placeholderText="@string/hint_album_name">

                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/etName"
                    …
                    android:imeOptions="actionNext"
                    android:inputType="textCapSentences"
                    android:maxLines="1" />
            </com.google.android.material.textfield.TextInputLayout>
```

- Un campo de Material son dos vistas: `TextInputLayout` (el borde, la etiqueta, el texto de ejemplo, el error y la
  ayuda) y adentro `TextInputEditText` (donde se escribe).
- `android:hint`: la etiqueta («Nombre del álbum *»). `app:placeholderText`: el texto de ejemplo («Ej. Buscando
  América»).
- `inputType="textCapSentences"`: el teclado pone mayúscula al inicio. `imeOptions="actionNext"`: la tecla de acción
  pasa al siguiente campo. `maxLines="1"`: una sola línea.

```xml
            <com.google.android.material.textfield.TextInputLayout android:id="@+id/tilCover" … android:hint="@string/field_cover_url"
                app:placeholderText="@string/hint_cover_url">
                <com.google.android.material.textfield.TextInputEditText android:id="@+id/etCover" …
                    android:imeOptions="actionDone" android:inputType="textUri" android:maxLines="1" />
            </com.google.android.material.textfield.TextInputLayout>
```

- La URL: `inputType="textUri"` muestra el teclado de direcciones (con `/` y `.com`); `actionDone` cierra el teclado.

```xml
            <LinearLayout
                android:id="@+id/coverPreview"
                … android:layout_marginTop="12dp"
                android:background="@drawable/bg_cover_preview"
                android:baselineAligned="false"
                android:gravity="center_vertical" android:orientation="horizontal" android:padding="12dp"
                android:visibility="gone"
                tools:visibility="visible">

                <FrameLayout
                    android:layout_width="72dp"
                    android:layout_height="72dp"
                    android:background="@drawable/bg_cover_thumb">

                    <ImageView
                        android:layout_width="32dp"
                        android:layout_height="32dp"
                        android:layout_gravity="center"
                        android:importantForAccessibility="no"
                        android:src="@drawable/ic_cover_placeholder_image" />

                    <com.google.android.material.imageview.ShapeableImageView
                        android:id="@+id/ivCover"
                        android:layout_width="match_parent"
                        android:layout_height="match_parent"
                        android:contentDescription="@string/cd_cover_preview"
                        android:scaleType="centerCrop"
                        app:shapeAppearanceOverlay="@style/ShapeAppearance.Vinilos.CoverPreview" />
                </FrameLayout>

                <LinearLayout … android:layout_marginStart="12dp" android:layout_weight="1" android:orientation="vertical">
                    <TextView … android:text="@string/cover_preview_title" … />
                    <TextView android:id="@+id/tvCoverUrl" … tools:text="https://ejemplo.com/portada.jpg" />
                </LinearLayout>
            </LinearLayout>
```

- La **vista previa**: una tarjeta (fondo `surface_container`, esquinas de 12 dp) oculta hasta que la URL es válida.
- `baselineAligned="false"`: en una fila horizontal, Android intenta alinear los textos de los hijos por su línea
  base; aquí no hace falta y Lint recomienda desactivarlo cuando hay un hijo con `layout_weight`.
- El cuadro de la portada es un `FrameLayout` de 72 dp: sus hijos se dibujan uno encima del otro.
  - Abajo, el fondo redondeado (`bg_cover_thumb`) y el ícono de imagen de 32 dp centrado: es el **marcador** que se
    ve mientras no hay portada. `importantForAccessibility="no"`: es decorativo y TalkBack lo salta.
  - Encima, `ShapeableImageView`: un `ImageView` de Material que puede tener esquinas redondeadas (8 dp, por su
    `shapeAppearanceOverlay`). `centerCrop` llena el cuadro recortando lo que sobre. Cuando la imagen carga, tapa el
    marcador; si no carga, queda vacía y el marcador se sigue viendo.
- ¿Por qué el marcador no es la imagen de "cargando" o de "error" de Coil? Porque en Android 5.0 un ícono vectorial
  cierra la app si le toca dibujarse con tamaño 0, y eso pasa cuando la tarjeta acaba de aparecer y aún no tiene
  tamaño. Como vista aparte, con tamaño fijo, nunca se dibuja en 0 (se encontró con las pruebas E2E en Android 5).
- Al lado, «Vista previa de la portada» y la URL.

```xml
            <com.google.android.material.textfield.TextInputLayout
                android:id="@+id/tilDate"
                style="@style/Widget.Vinilos.FormField"
                android:hint="@string/field_release_date"
                app:endIconContentDescription="@string/cd_open_date_picker"
                app:endIconDrawable="@drawable/ic_calendar_today"
                app:endIconMode="custom"
                app:helperText="@string/help_release_date">

                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/etDate"
                    …
                    android:cursorVisible="false"
                    android:focusable="false"
                    android:inputType="none" />
            </com.google.android.material.textfield.TextInputLayout>
```

- La fecha no se escribe: se elige en el calendario.
- `endIconMode="custom"` + `endIconDrawable`: el ícono de calendario a la derecha; `endIconContentDescription` es lo
  que lee TalkBack.
- `helperText`: la ayuda «Toca el campo para abrir el selector de fecha.».
- `focusable="false"`, `inputType="none"` y `cursorVisible="false"`: no abre el teclado ni muestra cursor; al tocarlo
  se abre el calendario (lo hace el fragment).

```xml
            <com.google.android.material.textfield.TextInputLayout
                android:id="@+id/tilGenre"
                style="@style/Widget.Vinilos.FormField.Dropdown"
                android:hint="@string/field_genre"
                app:placeholderText="@string/hint_genre">

                <com.google.android.material.textfield.MaterialAutoCompleteTextView
                    android:id="@+id/acGenre"
                    …
                    android:inputType="none" />
            </com.google.android.material.textfield.TextInputLayout>
```

- **Desplegable de Material** (*exposed dropdown menu*): un `TextInputLayout` con el estilo `ExposedDropdownMenu` (pone
  la flecha) y adentro un `MaterialAutoCompleteTextView`, que muestra la lista de opciones al tocarlo.
- `inputType="none"`: no se puede escribir, solo elegir.
- El de sello (`tilLabel`, `acLabel`) es igual.

```xml
            <com.google.android.material.textfield.TextInputLayout android:id="@+id/tilDescription" … >
                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/etDescription"
                    …
                    android:gravity="top|start"
                    android:inputType="textMultiLine|textCapSentences"
                    android:minHeight="120dp"
                    android:minLines="4" />
            </com.google.android.material.textfield.TextInputLayout>
```

- La descripción: varias líneas (`textMultiLine`), al menos 4 líneas y 120 dp de alto, con el texto empezando arriba a
  la izquierda.

```xml
    <View style="@style/Widget.Vinilos.Divider" />

    <LinearLayout
        android:id="@+id/actionBar"
        … android:orientation="horizontal"
        android:paddingStart="16dp" android:paddingTop="12dp" android:paddingEnd="16dp" android:paddingBottom="16dp">

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnCancel"
            style="?attr/materialButtonOutlinedStyle"
            android:layout_width="0dp"
            android:layout_marginEnd="12dp"
            android:layout_weight="1"
            android:text="@string/action_cancel" />

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnSave"
            android:layout_width="0dp"
            android:layout_weight="1"
            android:text="@string/action_save" />
    </LinearLayout>
</LinearLayout>
```

- La **barra de acciones fija**: línea divisora y dos botones del mismo ancho (`layout_weight="1"` cada uno),
  separados 12 dp.
- `style="?attr/materialButtonOutlinedStyle"`: el `?attr/` toma el estilo definido en el tema (el botón con borde).
  «Guardar» es un botón relleno con el estilo por defecto.
- `actionBar` tiene id porque en el paso 8 el aviso de error se ancla encima de ella.

### `app/src/main/java/com/equipo2/vinilos/ui/albums/create/AlbumCreateFragment.kt` · modificado por mí

```kotlin
class AlbumCreateFragment : Fragment(R.layout.fragment_album_create) {

    private val viewModel: AlbumCreateViewModel by viewModels { AlbumCreateViewModel.Factory }
    private var loadedCover: String? = null
```

- `by viewModels { … }`: el ViewModel **de este fragment** (no de la actividad): el formulario se descarta al cerrar
  la pantalla, pero sobrevive si la pantalla se recrea (por ejemplo, al girar).
- `loadedCover`: la última URL cargada en la vista previa, para no volver a descargar la imagen con cada letra que se
  escribe en otro campo.

```kotlin
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentAlbumCreateBinding.bind(view)
        loadedCover = null
        bindInputs(binding)
        binding.btnSave.setOnClickListener {
            hideKeyboard(view)
            viewModel.save()
        }
        binding.btnCancel.setOnClickListener { requestClose() }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { requestClose() }
```

- `loadedCover = null`: si la vista se vuelve a crear, la imagen nueva está vacía y hay que cargarla otra vez.
- Guardar cierra el teclado y valida; Cancelar pide cerrar.
- `onBackPressedDispatcher.addCallback(viewLifecycleOwner) { … }`: mientras esta pantalla está visible, el botón Atrás
  (y la X de la barra, que en el paso 6 se conectó con Atrás) ejecuta `requestClose` en lugar de cerrar de inmediato.
  Con `viewLifecycleOwner`, el interceptor se quita solo al salir.

```kotlin
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.state.collect { render(binding, it) } }
                launch { viewModel.events.collect { handle(binding, it) } }
            }
        }
    }
```

- Escucha el estado (para pintarlo) y los eventos (para reaccionar) mientras la pantalla está visible.

```kotlin
    private fun bindInputs(binding: FragmentAlbumCreateBinding) {
        binding.etName.doAfterTextChanged { viewModel.onNameChange(it.toString()) }
        binding.etCover.doAfterTextChanged { viewModel.onCoverChange(it.toString()) }
        binding.etDescription.doAfterTextChanged { viewModel.onDescriptionChange(it.toString()) }
        binding.etDate.setOnClickListener { openDatePicker() }
        binding.tilDate.setEndIconOnClickListener { openDatePicker() }
        binding.acGenre.setSimpleItems(AlbumCatalog.genres.toTypedArray())
        binding.acGenre.setOnItemClickListener { _, _, position, _ -> viewModel.onGenreChange(AlbumCatalog.genres[position]) }
        binding.acLabel.setSimpleItems(AlbumCatalog.recordLabels.toTypedArray())
        binding.acLabel.setOnItemClickListener { _, _, position, _ -> viewModel.onRecordLabelChange(AlbumCatalog.recordLabels[position]) }
    }
```

- `doAfterTextChanged`: cada vez que cambia el texto de un campo, se le avisa al ViewModel.
- Tocar el campo de fecha o su ícono abre el calendario.
- `setSimpleItems`: las opciones de cada desplegable, tomadas de `AlbumCatalog` (los valores que acepta la API).
  `setOnItemClickListener` recibe la posición elegida.

```kotlin
    private fun render(binding: FragmentAlbumCreateBinding, state: AlbumCreateUiState) {
        val form = state.form
        fun TextInputLayout.showError(field: AlbumField) {
            error = state.errors[field]?.message(requireContext(), field)
        }
        binding.tilName.showError(AlbumField.NAME)
        …
        binding.tilDescription.showError(AlbumField.DESCRIPTION)
        binding.tilDate.helperText = getString(R.string.help_release_date).takeUnless { AlbumField.RELEASE_DATE in state.errors }
```

- `showError`: función de extensión **local** (solo existe dentro de `render`). Pone en cada campo su mensaje de error,
  o `null` para quitarlo. `TextInputLayout.error` muestra el texto debajo, pinta el borde de rojo y lo anuncia a
  TalkBack.
- La ayuda de la fecha se oculta cuando hay un error en ese campo (`takeUnless`: el valor, salvo que se cumpla la
  condición).

```kotlin
        // Los campos de texto conservan lo escrito; solo se pintan los valores que se eligen fuera del teclado.
        val dateText = form.releaseDate?.let(DateFormats::long).orEmpty()
        if (binding.etDate.text.toString() != dateText) binding.etDate.setText(dateText)
        form.genre?.takeIf { it != binding.acGenre.text.toString() }?.let { binding.acGenre.setText(it, false) }
        form.recordLabel?.takeIf { it != binding.acLabel.text.toString() }?.let { binding.acLabel.setText(it, false) }
        renderCoverPreview(binding, form.cover.trim())
    }
```

- Los campos donde se escribe no se vuelven a pintar desde el estado (si se hiciera, el cursor saltaría al inicio con
  cada letra). Solo se pintan la fecha (en formato largo) y los desplegables, y únicamente si cambiaron.
- `setText(it, false)`: el `false` evita que el desplegable filtre y se abra otra vez.

```kotlin
    private fun renderCoverPreview(binding: FragmentAlbumCreateBinding, cover: String) {
        val isValid = AlbumFormValidator.isValidUrl(cover)
        binding.coverPreview.isVisible = isValid
        if (isValid && cover != loadedCover) {
            loadedCover = cover
            binding.tvCoverUrl.text = cover
            binding.ivCover.load(cover)
        }
    }
```

- La vista previa se muestra solo con una URL válida y se carga solo si la URL cambió.
- `ivCover.load(url)`: **Coil** descarga la imagen en segundo plano, la guarda en caché y la pone en el
  `ImageView`. Al empezar a cargar otra URL deja la imagen vacía, así mientras carga, o si falla, se ve el marcador
  que está debajo.

```kotlin
    private fun handle(binding: FragmentAlbumCreateBinding, event: AlbumCreateEvent) {
        when (event) {
            is AlbumCreateEvent.ShowErrors -> focusField(binding, event.firstField)
        }
    }

    /** Lleva el scroll y, si es un campo de texto, el foco al primer campo con error. */
    private fun focusField(binding: FragmentAlbumCreateBinding, field: AlbumField) {
        val target = when (field) { … }
        binding.formScroll.post { binding.formScroll.smoothScrollTo(0, target.top) }
        target.editText?.takeIf { it.isFocusable }?.requestFocus()
    }
```

- Con el evento de errores, se desplaza suavemente hasta el primer campo con error (`target.top` es su posición
  dentro del formulario). `post` espera a que los mensajes de error ya estén dibujados, porque cambian las alturas.
- Si es un campo de texto, recibe el foco; la fecha y los desplegables no son *focusables*, por eso el `takeIf`.

```kotlin
    private fun openDatePicker() {
        if (childFragmentManager.findFragmentByTag(DATE_PICKER_TAG) != null) return
        val today = LocalDate.now()
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.label_release_date)
            .setSelection(DateFormats.toUtcMillis(viewModel.state.value.form.releaseDate ?: today))
            .setCalendarConstraints(
                CalendarConstraints.Builder()
                    .setEnd(DateFormats.toUtcMillis(today))
                    .setValidator(DateValidatorPointBackward.before(DateFormats.toUtcMillis(today)))
                    .build()
            )
            .setPositiveButtonText(R.string.action_accept)
            .setNegativeButtonText(R.string.action_cancel)
            .build()
        picker.addOnPositiveButtonClickListener { viewModel.onReleaseDateChange(DateFormats.fromUtcMillis(it)) }
        picker.show(childFragmentManager, DATE_PICKER_TAG)
    }
```

- Si el calendario ya está abierto (dos toques rápidos), no abre otro.
- `MaterialDatePicker`: el calendario de Material. Empieza en la fecha elegida o, si no hay, en hoy.
- `CalendarConstraints`: el calendario no pasa del mes actual (`setEnd`) y los días posteriores a hoy quedan
  deshabilitados (`DateValidatorPointBackward.before(…)`, que acepta ese día y los anteriores).
- «Hoy» es `LocalDate.now()`, el día según la zona del teléfono, el mismo que usa el validador. Con
  `DateValidatorPointBackward.now()` el calendario usaría la hora UTC: en Colombia, después de las 7 p. m. dejaría
  elegir el día siguiente, y el validador lo rechazaría al guardar.
- Botones «Aceptar» y «Cancelar»; al aceptar, la fecha va al ViewModel.
- Se muestra como un fragment hijo con una etiqueta, para poder encontrarlo.

```kotlin
    /** Cerrar, Cancelar o Atrás: si hay algo escrito, pide confirmación antes de descartarlo. */
    private fun requestClose() {
        if (viewModel.state.value.form.isEmpty) {
            findNavController().navigateUp()
            return
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.discard_title)
            .setMessage(R.string.discard_body)
            .setNegativeButton(R.string.discard_keep, null)
            .setPositiveButton(R.string.discard_confirm) { _, _ -> findNavController().navigateUp() }
            .show()
    }
```

- Formulario vacío: vuelve a Álbumes. Con datos: diálogo de Material «¿Descartar el álbum?» con «Seguir editando»
  (cierra el diálogo, `null` = sin acción) y «Descartar» (vuelve a Álbumes).
- `navigateUp()`: vuelve a la pantalla anterior del grafo.

```kotlin
    private fun hideKeyboard(view: View) {
        val keyboard = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        keyboard.hideSoftInputFromWindow(view.windowToken, 0)
    }
```

- Cierra el teclado al guardar, para que se vean los errores y, en el paso 8, el aviso.

---

### Recursos · creados o modificados por mí

| Archivo | Cambio |
|---|---|
| `gradle/libs.versions.toml`, `app/build.gradle.kts` | **Coil 3.4.0** (`coil` y `coil-network-okhttp`): carga imágenes desde Internet usando OkHttp. Es la última versión que soporta API 21 |
| `res/values/styles.xml` | `Widget.Vinilos.Button.Outlined`: botón con borde `outline`, 48 dp de alto y esquinas de 24 dp. `Widget.Vinilos.FormField`: campo con borde (`OutlinedBox`), margen superior de 24 dp, esquinas de 4 dp, ícono y estilo del error y de la ayuda, y `expandedHintEnabled=false` (la etiqueta queda siempre arriba y el texto de ejemplo siempre visible, como en el diseño). `FormField.Dropdown`: lo mismo para los desplegables, sin ícono de error para no tapar la flecha. `ShapeAppearance.Vinilos.CoverPreview`: esquinas de 8 dp |
| `res/values/themes.xml` | `materialButtonOutlinedStyle`: el estilo del botón con borde para toda la app |
| `res/values/strings.xml` | Los textos del formulario (sección 12.1 de la guía): nota, etiquetas `field_*` (con `*`), ejemplos `hint_*`, ayuda de la fecha, `label_release_date` (sin `*`, para el título del calendario), botones, los nueve mensajes de error (`error_max_length` usa `%1$d`, un número), el diálogo de descartar y dos descripciones de accesibilidad |
| `res/values/colors.xml` y `values-night` | `md_placeholder_icon` (`#7A6C90` / `#A398B4`): el color del ícono de marcador (guía, sección 5.1) |
| `res/drawable/ic_calendar_today.xml` | Ícono de calendario de Material Symbols |
| `res/drawable/ic_cover_placeholder_image.xml` | El ícono `image` de Material Symbols ya pintado del color de marcador |
| `res/drawable/bg_cover_thumb.xml` | Fondo del cuadro de la portada: esquinas de 8 dp en `surface_variant`; junto con el ícono anterior forma el marcador |
| `res/drawable/bg_cover_preview.xml` | Fondo de la tarjeta de vista previa: esquinas de 12 dp en `surface_container` |

---

## Qué sigue

En el paso 8 (T7) Guardar envía el álbum a BackVynils: estado «Guardando…», vuelta a Álbumes con el aviso «Álbum
creado» y, si falla, aviso de error con «Reintentar» sin perder lo escrito.
