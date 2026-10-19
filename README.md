# Vinilos · Paso 8 de 9: T7 · Creación del álbum en la API

Tarea de la HU07: **«Implementar la creación del álbum»**.

Cubre los criterios de aceptación **«Al guardar se muestra el progreso y luego se vuelve a Álbumes con el aviso Álbum
creado»** y **«Si el guardado falla, el formulario conserva los datos y muestra un aviso de error»**.

Con el formulario válido, **Guardar** ahora envía el álbum a BackVynils (`POST /albums`) usando la capa de red del
paso 2:

- **Guardando:** barra de progreso arriba, todos los campos y botones deshabilitados y el botón dice «Guardando…» con
  un indicador girando. Atrás y la X no hacen nada mientras tanto.
- **Éxito:** vuelve a Álbumes y aparece el aviso **«Álbum creado»**.
- **Error** (sin red, tiempo agotado o error del servidor): el formulario queda tal como estaba y aparece el aviso
  **«No pudimos crear el álbum. Revisa tu conexión e inténtalo de nuevo.»** con la acción **Reintentar**.

Con esto la HU07 queda completa; el paso 9 agrega sus pruebas.

| Paso | Commit |
|---|---|
| 1 | Proyecto base de Android Studio |
| 2 | T1 · Configuración, capa de red y estructura MVVM |
| 3 | T2 · Tema claro y oscuro y navegación |
| 4 | T3 · Componentes de carga, error, vacío y avisos |
| 5 | T4 · Selección de rol y menú de rol |
| 6 | T5 · Botón + en Álbumes |
| 7 | T6 · Formulario de crear álbum con validaciones |
| **8** | **T7 · Creación del álbum en la API** |
| 9 | T8 · Pruebas de la historia |

## Recorrido completo de un álbum

```
AlbumCreateFragment ──save()──▶ AlbumCreateViewModel ──createAlbum(NewAlbum)──▶ AlbumRepository
                                                                                     │
                                       NetworkServiceAdapter ◀────────────────────────┘
                                                │  CreateAlbumRequest (JSON, fecha "1984-08-01T00:00:00.000Z")
                                                ▼
                                       BackVynils: POST /albums  →  200 + álbum con id
                                                │
AlbumListFragment ◀──resultado (id)── AlbumCreateFragment ◀──Created(album)── ViewModel ◀──Result.success
      │
      └── aviso «Álbum creado»
```

Si la API falla, el repositorio devuelve `Result.failure`, el ViewModel envía `SaveFailed` y la pantalla muestra el
aviso de error sin tocar el formulario. El mensaje en inglés de la API nunca llega al usuario.

## Orden en que se hicieron los cambios

1. `AlbumCreateViewModel.kt`: recibir el repositorio, el estado `isSaving` y los eventos `Created` y `SaveFailed`.
2. `fragment_album_create.xml`: la barra de progreso.
3. `AlbumCreateFragment.kt`: pintar el estado guardando, devolver el resultado al crear y mostrar el aviso de error.
4. `AlbumListFragment.kt`: recibir el resultado y mostrar «Álbum creado».
5. Textos nuevos en `strings.xml`.
6. Lint (0 errores).

## Cómo ejecutarlo

1. **Run ▶** con el teléfono o emulador **con Internet**.
2. Entrar como coleccionista, tocar **+** y llenar todos los campos. Por ejemplo:
   nombre «Mi álbum de prueba», portada `https://i.imgur.com/0Ky0Fby.jpg`, fecha cualquiera pasada, género Salsa,
   sello Elektra y una descripción.
3. Tocar **Guardar**: se ve «Guardando…» y luego vuelve a Álbumes con el aviso «Álbum creado». La primera vez puede
   tardar varios segundos porque el servidor de Render se está despertando.
4. Para ver el error: activar el **modo avión**, llenar el formulario y guardar. Aparece el aviso de error con
   **Reintentar**; los datos siguen ahí. Quitar el modo avión y tocar Reintentar.

**Ojo:** el backend es compartido por todos los equipos, así que cada prueba crea un álbum real visible para todos.

---

## Explicación de cada archivo

### `app/src/main/java/com/equipo2/vinilos/ui/albums/create/AlbumCreateViewModel.kt` · modificado por mí

```kotlin
data class AlbumCreateUiState(
    val form: AlbumForm = AlbumForm(),
    val errors: Map<AlbumField, FieldError> = emptyMap(),
    val isSaving: Boolean = false,
)
```

- **Nuevo** `isSaving`: si se está enviando el álbum.

```kotlin
sealed interface AlbumCreateEvent {
    /** Guardar con errores: la pantalla lleva el foco al primer campo con error. */
    data class ShowErrors(val firstField: AlbumField) : AlbumCreateEvent
    data class Created(val album: Album) : AlbumCreateEvent
    data object SaveFailed : AlbumCreateEvent
}
```

- **Nuevos:** `Created` lleva el álbum que devolvió la API (con su `id`); `SaveFailed` no lleva datos porque la
  pantalla siempre muestra el mismo mensaje.

```kotlin
class AlbumCreateViewModel(
    private val repository: AlbumRepository,
    private val today: () -> LocalDate = LocalDate::now,
) : ViewModel() {
```

- **Nuevo:** recibe el repositorio (la interfaz, no la implementación). Las pruebas del paso 9 le pasan uno falso.

```kotlin
    fun save() {
        if (_state.value.isSaving) return
        validateOnChange = true
        val form = _state.value.form
        val errors = AlbumFormValidator.validate(form, today())
        _state.update { it.copy(errors = errors) }
        if (errors.isNotEmpty()) {
            _events.trySend(AlbumCreateEvent.ShowErrors(errors.keys.first()))
            return
        }
```

- Si ya se está guardando, no hace nada (evita envíos dobles por dos toques rápidos).
- La validación es la del paso 7; si hay errores, termina ahí.

```kotlin
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val result = repository.createAlbum(form.toNewAlbum())
            _state.update { it.copy(isSaving = false) }
            _events.send(result.fold(onSuccess = AlbumCreateEvent::Created, onFailure = { AlbumCreateEvent.SaveFailed }))
        }
    }
```

- **Nuevo.** Con el formulario válido: marca "guardando" y lanza una corrutina que llama al repositorio.
- `form.toNewAlbum()`: el formulario ya validado, sin espacios sobrantes (paso 7).
- `viewModelScope.launch`: si el ViewModel se destruye a mitad de la llamada, la corrutina se cancela. Gracias a
  `resultOf` (paso 2) la cancelación no se confunde con un error.
- Al terminar, quita "guardando" y envía un evento según el resultado:
  - `result.fold(onSuccess = …, onFailure = …)`: ejecuta una función u otra según el `Result`.
  - `AlbumCreateEvent::Created` es una referencia al constructor: recibe el álbum y crea el evento.
  - En caso de error se ignora la excepción y se envía `SaveFailed`.

```kotlin
    private fun edit(change: AlbumForm.() -> AlbumForm) {
        if (_state.value.isSaving) return
        _state.update { current -> … }
    }
```

- **Nuevo** `if (_state.value.isSaving) return`: mientras se guarda, los cambios se ignoran, para que lo enviado sea lo
  que se ve. El resto no cambió.

```kotlin
    companion object {
        val Factory = viewModelFactory {
            initializer { AlbumCreateViewModel(ServiceLocator.albumRepository) }
        }
    }
```

- La fábrica le pasa el repositorio del `ServiceLocator`, que normalmente es el real y, en las pruebas, el de
  reemplazo.

### `res/layout/fragment_album_create.xml` · modificado por mí

```xml
    <com.google.android.material.progressindicator.LinearProgressIndicator
        android:id="@+id/savingProgress"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:indeterminate="true"
        android:visibility="gone"
        app:indicatorColor="@color/md_primary"
        app:trackColor="@color/md_primary_container"
        app:trackThickness="4dp" />
```

- **Único cambio**: la barra de progreso de 4 dp justo debajo de la barra superior, oculta salvo mientras se guarda
  (guía, sección 9.1, estado «Guardando»).

### `app/src/main/java/com/equipo2/vinilos/ui/albums/create/AlbumCreateFragment.kt` · modificado por mí

```kotlin
import androidx.core.content.ContextCompat
…
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec
import com.google.android.material.progressindicator.IndeterminateDrawable
import com.equipo2.vinilos.MainActivity
…
import com.equipo2.vinilos.ui.albums.AlbumListFragment
```

- Nuevas importaciones para el indicador del botón, el aviso y la clave del resultado.

```kotlin
        renderCoverPreview(binding, form.cover.trim())
        renderSaving(binding, state.isSaving)
    }

    /** Guardando: barra de progreso arriba, todo deshabilitado y «Guardando…» con un indicador en el botón. */
    private fun renderSaving(binding: FragmentAlbumCreateBinding, isSaving: Boolean) {
        binding.savingProgress.isVisible = isSaving
        listOf(binding.tilName, binding.tilCover, binding.tilDate, binding.tilGenre, binding.tilLabel, binding.tilDescription)
            .forEach { it.isEnabled = !isSaving }
        binding.btnCancel.isEnabled = !isSaving
        binding.btnSave.isEnabled = !isSaving
        binding.btnSave.setText(if (isSaving) R.string.action_saving else R.string.action_save)
        binding.btnSave.icon = if (isSaving) savingIndicator() else null
    }
```

- **Nuevo.** `render` termina pintando el estado guardando: muestra la barra, deshabilita los seis campos y los dos
  botones, y cambia el texto e ícono de Guardar. Al terminar se revierte todo.
- `listOf(…).forEach { … }`: aplica lo mismo a cada campo sin repetir seis líneas.

```kotlin
            is AlbumCreateEvent.Created -> {
                findNavController().previousBackStackEntry?.savedStateHandle?.set(AlbumListFragment.KEY_CREATED_ALBUM_ID, event.album.id)
                findNavController().popBackStack()
            }
```

- **Nuevo.** Al crear el álbum:
  - `previousBackStackEntry`: la entrada de la pantalla anterior (Álbumes) en la pila de navegación.
  - `savedStateHandle.set(clave, valor)`: le deja el id del álbum creado como **resultado**. Es la forma que propone
    Navigation para devolver datos a la pantalla anterior.
  - `popBackStack()`: cierra Crear álbum y vuelve a Álbumes.

```kotlin
            AlbumCreateEvent.SaveFailed -> (requireActivity() as MainActivity).showSnackbar(
                message = getString(R.string.error_album_create_failed),
                isError = true,
                anchor = binding.actionBar,
                actionText = getString(R.string.error_network_retry),
                action = viewModel::save,
            )
```

- **Nuevo.** Si falla: el aviso de error del paso 4, encima de la barra de botones (`anchor`), con la acción
  «Reintentar» que vuelve a llamar a `save` con el mismo formulario.
- Se usan **argumentos con nombre** (`message = …`) para que se entienda qué es cada valor.

```kotlin
        if (viewModel.state.value.isSaving || childFragmentManager.findFragmentByTag(DATE_PICKER_TAG) != null) return
```

- **Cambió:** el calendario no se abre mientras se guarda.

```kotlin
    private fun requestClose() {
        val state = viewModel.state.value
        if (state.isSaving) return
        if (state.form.isEmpty) {
```

- **Cambió:** mientras se guarda, Cerrar, Cancelar y Atrás no hacen nada (el álbum podría crearse igual, y la pantalla
  ya no existiría para avisarlo).

```kotlin
    private fun savingIndicator(): IndeterminateDrawable<CircularProgressIndicatorSpec> {
        val context = requireContext()
        val spec = CircularProgressIndicatorSpec(context, null, 0, com.google.android.material.R.style.Widget_Material3_CircularProgressIndicator_ExtraSmall)
        spec.indicatorColors = intArrayOf(ContextCompat.getColor(context, R.color.md_on_surface_variant))
        return IndeterminateDrawable.createCircularDrawable(context, spec)
    }
```

- **Nuevo.** Crea el círculo de progreso que gira dentro del botón:
  - `CircularProgressIndicatorSpec`: la configuración del indicador de Material, con el estilo de tamaño extra pequeño.
  - `indicatorColors`: su color, el del texto del botón deshabilitado.
  - `IndeterminateDrawable.createCircularDrawable`: lo convierte en un *drawable* animado que se puede usar como ícono
    de un botón.

### `app/src/main/java/com/equipo2/vinilos/ui/albums/AlbumListFragment.kt` · modificado por mí

```kotlin
        binding.fabCreate.setOnClickListener { openCreateAlbum() }
        showCreatedAlbumNotice(binding)
```

- **Nuevo:** empieza a escuchar el resultado de Crear álbum.

```kotlin
    /** Al volver de Crear álbum con un álbum guardado, avisa «Álbum creado». */
    private fun showCreatedAlbumNotice(binding: FragmentAlbumListBinding) {
        val result = findNavController().currentBackStackEntry?.savedStateHandle ?: return
        result.getLiveData<Int>(KEY_CREATED_ALBUM_ID).observe(viewLifecycleOwner) {
            result.remove<Int>(KEY_CREATED_ALBUM_ID)
            (requireActivity() as MainActivity).showSnackbar(getString(R.string.snackbar_album_created), anchor = binding.fabCreate)
        }
    }
```

- `currentBackStackEntry?.savedStateHandle`: el "buzón" de esta pantalla, el mismo donde Crear álbum dejó el id.
  `?: return` sale si por algún motivo no existe.
- `getLiveData<Int>(clave)`: un `LiveData` (dato observable) que avisa cuando llega el resultado.
  `observe(viewLifecycleOwner)` lo escucha mientras exista la vista.
- Al llegar: se borra (`remove`) para que el aviso no se repita al volver a esta pantalla, y se muestra «Álbum creado»
  encima del botón + (o de la barra inferior si el botón no está visible).

```kotlin
    companion object {
        /** Resultado que deja Crear álbum: el id del álbum guardado. HU01 lo usará para marcarlo como «Nuevo». */
        const val KEY_CREATED_ALBUM_ID = "createdAlbumId"
    }
```

- La clave compartida entre las dos pantallas. HU01 podrá usar el id para poner primero el álbum nuevo con el chip
  «Nuevo» (guía, sección 9.2) y recargar la lista.

### `res/values/strings.xml` · modificado por mí

```xml
    <string name="action_saving">Guardando…</string>
    …
    <string name="snackbar_album_created">Álbum creado</string>
    <string name="error_album_create_failed">No pudimos crear el álbum. Revisa tu conexión e inténtalo de nuevo.</string>
```

- El texto del botón mientras guarda, el aviso de éxito y el de error (guía, secciones 11.1 y 11.2).

---

## Qué sigue

En el paso 9 (T8) se agregan las pruebas de la historia: unitarias de las reglas y del ViewModel, y E2E con Espresso
para cada criterio de aceptación, ejecutadas en Android 5, 9 y 17.
