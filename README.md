# Vinilos · Paso 2 de 9: T1 · Configuración, capa de red y estructura MVVM

Tarea de la HU07: **«Crear el proyecto con la capa de red, la estructura MVVM y la configuración de pruebas»**.

Este paso prepara el proyecto para el curso y deja lista la comunicación con BackVynils:

- Baja el `minSdk` de 24 a **21** (Android 5.0), que es lo que exige la rúbrica.
- Fija las librerías en las últimas versiones que todavía funcionan en Android 5.0.
- Agrega las librerías de red (Retrofit, OkHttp, kotlinx.serialization), de corrutinas y de ViewModel.
- Crea la capa de datos con los patrones que pide el curso: **Service Adapter** (`NetworkServiceAdapter`) y
  **Repository** (`AlbumRepository`). La parte de vista y ViewModel de MVVM llega en los pasos siguientes.

La app todavía muestra «Hello World!»: la capa de red existe, pero ninguna pantalla la usa hasta el paso 8.

| Paso | Commit |
|---|---|
| 1 | Proyecto base de Android Studio |
| **2** | **T1 · Configuración, capa de red y estructura MVVM** |
| 3 | T2 · Tema claro y oscuro y navegación |
| 4 | T3 · Componentes de carga, error, vacío y avisos |
| 5 | T4 · Selección de rol y menú de rol |
| 6 | T5 · Botón + en Álbumes |
| 7 | T6 · Formulario de crear álbum con validaciones |
| 8 | T7 · Creación del álbum en la API |
| 9 | T8 · Pruebas de la historia |

## Lo que se aprendió de BackVynils y cómo se aplica

Antes de escribir la capa de red se revisó el código de BackVynils (`src/album/album.service.ts`) y se probó el
servidor desplegado:

| Comportamiento de la API | Cómo lo maneja este paso |
|---|---|
| `POST /albums` recibe `name`, `cover`, `releaseDate`, `description`, `genre` y `recordLabel`, todos obligatorios | `CreateAlbumRequest` tiene exactamente esos seis campos |
| `genre` solo acepta `Classical`, `Salsa`, `Rock`, `Folk`; `recordLabel` solo `Sony Music`, `EMI`, `Discos Fuentes`, `Elektra`, `Fania Records`. Otro valor responde **400** con `ValidationError: "genre" must be one of …` | `AlbumCatalog` guarda las dos listas para que el formulario (paso 7) solo ofrezca valores válidos |
| `releaseDate` debe ser una fecha válida; con texto inválido responde **400** | `toApiDate` envía siempre el formato ISO completo `1984-08-01T00:00:00.000Z` |
| Responde **200** (no 201) con el álbum creado, incluido su `id` | Retrofit trata cualquier 2xx como éxito; `AlbumDto` lee la respuesta |
| La respuesta trae más campos de los que usa la app | `ignoreUnknownKeys = true` evita errores por campos desconocidos |
| Al crear y al actualizar exige todos los campos y una fecha válida, así que siempre responde los álbumes completos (los 310 del servidor traen su fecha en formato ISO) | `AlbumDto` y `Album` declaran todos los campos obligatorios, sin valores por defecto ni nulos: la app confía en el contrato de la API |
| No valida que `cover` sea una URL ni que la fecha no sea futura ni la longitud de los textos | Esas reglas las hará la app en el formulario (paso 7) |
| Está en Render: la primera petición puede tardar varios segundos mientras el servidor despierta | Tiempos de espera de 30 s en OkHttp |
| Los mensajes de error están en inglés | La app nunca los muestra; el repositorio solo informa "falló" y la pantalla muestra su propio texto |

## Orden en que se hicieron los cambios

1. `gradle/libs.versions.toml`: bajar las versiones a las compatibles con API 21 y agregar las librerías nuevas.
2. `build.gradle.kts` (raíz): declarar el plugin de serialización de Kotlin.
3. `app/build.gradle.kts`: `minSdk = 21`, la URL de la API en `BuildConfig`, *desugaring* de `java.time` y las
   dependencias nuevas.
4. `AndroidManifest.xml`: permiso de Internet.
5. `data/model/Album.kt`: modelos de la app.
6. `data/network/AlbumDto.kt`: forma del JSON de la API.
7. `data/network/VinilosApiService.kt`: el endpoint `POST /albums` para Retrofit.
8. `data/network/NetworkServiceAdapter.kt`: el Service Adapter.
9. `data/repository/AlbumRepository.kt`: el repositorio.
10. `data/repository/ResultOf.kt`: `resultOf`, que comparten todos los repositorios.
11. `ServiceLocator.kt`: la inyección de dependencias manual.
12. Compilar y verificar que el proyecto sigue construyendo con `minSdk = 21`.

## Cómo ejecutarlo

1. En Android Studio, **File › Sync Project with Gradle Files** (cambiaron los archivos de Gradle).
2. Elegir un emulador o teléfono con **Android 5.0 (API 21) o superior**.
3. **Run ▶**. La app sigue mostrando «Hello World!»: lo nuevo de este paso no tiene interfaz.

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

---

## Explicación de cada archivo

### Estructura de lo nuevo

```
app/src/main/java/com/equipo2/vinilos/
├── ServiceLocator.kt                  Crea y comparte las dependencias
└── data/
    ├── model/Album.kt                 Modelos que usa la app
    ├── network/
    │   ├── AlbumDto.kt                Forma del JSON que envía y recibe la API
    │   ├── VinilosApiService.kt       Endpoints de la API (Retrofit)
    │   └── NetworkServiceAdapter.kt   Service Adapter: única puerta a la API
    └── repository/
        ├── AlbumRepository.kt         Repository: lo que las pantallas le piden a los datos
        └── ResultOf.kt                Convierte la respuesta o el error en un Result (lo usan todos los repositorios)
```

Cómo se relacionan (las flechas son "usa a"):

```
(Pantalla y ViewModel, pasos 7 y 8) → AlbumRepository → NetworkServiceAdapter → VinilosApiService (Retrofit) → BackVynils
```

---

### `gradle/libs.versions.toml` · modificado por mí

#### Por qué se bajaron versiones

Desde mediados de 2025, las versiones nuevas de AndroidX exigen Android 6.0 (API 23) o 7.0 (API 24). Con `minSdk = 21`
y las versiones que puso el asistente, la compilación falla en el paso *manifest merger* con un error como
`uses-sdk:minSdkVersion 21 cannot be smaller than version 23 declared in library …`. Por eso se usan las últimas
versiones que todavía declaran API 21:

| Librería | Versión del paso 1 (exige) | Versión ahora (exige) |
|---|---|---|
| `core-ktx` | 1.19.1 (API 23) | **1.17.0** (API 21) |
| `appcompat` | 1.8.0 (API 23) | **1.7.1** (API 21) |
| `material` | 1.14.0 (API 23) | **1.13.0** (API 21) |
| `activity-ktx` | 1.13.0 (API 23) | **1.11.0** (API 21) |
| `constraintlayout` | 2.2.2 (API 21) | 2.2.2 (sin cambio) |
| Las de pruebas (`junit`, `androidx.test.ext:junit`, `espresso`) | API 21 | sin cambio |

**Regla para el equipo:** no subir estas versiones (aunque Android Studio lo sugiera con una advertencia amarilla)
sin comprobar que la nueva versión todavía soporta API 21.

#### El archivo completo

```toml
[versions]
agp = "9.4.1"
kotlin = "2.2.10"
```

- `agp`: igual que en el paso 1.
- `kotlin` (**nuevo**): versión de los plugins de Kotlin que se agregan aparte. AGP 9 trae Kotlin incluido en la
  versión 2.2.10, así que el plugin de serialización debe usar exactamente la misma versión.

```toml
coreKtx = "1.17.0"
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"
appcompat = "1.7.1"
material = "1.13.0"
activityKtx = "1.11.0"
constraintlayout = "2.2.2"
```

- Las mismas librerías del paso 1, con las versiones de la tabla anterior.

```toml
lifecycle = "2.9.4"
coroutines = "1.10.2"
kotlinxSerialization = "1.9.0"
retrofit = "3.0.0"
okhttp = "4.12.0"
desugarJdkLibs = "2.1.5"
```

Versiones **nuevas**:

| Nombre | Para qué |
|---|---|
| `lifecycle` 2.9.4 | `ViewModel` y `lifecycleScope` (la última que soporta API 21; la 2.10 exige API 23) |
| `coroutines` 1.10.2 | Corrutinas de Kotlin: tareas en segundo plano como las llamadas de red, sin bloquear la pantalla |
| `kotlinxSerialization` 1.9.0 | Convertir objetos Kotlin a JSON y de JSON a objetos (la serie 1.9 es la compatible con Kotlin 2.2) |
| `retrofit` 3.0.0 | Cliente HTTP declarativo: los endpoints se escriben como funciones de una interfaz |
| `okhttp` 4.12.0 | El cliente HTTP que usa Retrofit por debajo; aquí se configuran los tiempos de espera |
| `desugarJdkLibs` 2.1.5 | Permite usar `java.time` (`LocalDate`) en Android 5.0, que no lo trae (llegó en API 26) |

```toml
[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
androidx-appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }
material = { group = "com.google.android.material", name = "material", version.ref = "material" }
androidx-activity-ktx = { group = "androidx.activity", name = "activity-ktx", version.ref = "activityKtx" }
androidx-constraintlayout = { group = "androidx.constraintlayout", name = "constraintlayout", version.ref = "constraintlayout" }
```

- Sin cambios respecto al paso 1.

```toml
androidx-lifecycle-viewmodel-ktx = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-ktx", version.ref = "lifecycle" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
```

- **Nuevas.** `lifecycle-viewmodel-ktx`: la clase `ViewModel` y `viewModelScope` (corrutinas que se cancelan solas
  cuando la pantalla se destruye). `lifecycle-runtime-ktx`: `lifecycleScope` y `repeatOnLifecycle`, para escuchar
  datos solo mientras la pantalla está visible.

```toml
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
```

- **Nuevas.** `kotlinx-coroutines-android`: corrutinas con el hilo principal de Android (`Dispatchers.Main`).
  `kotlinx-coroutines-test`: herramientas para probar corrutinas (`runTest`), que usarán las pruebas del paso 9.

```toml
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinxSerialization" }
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-converter-kotlinx-serialization = { group = "com.squareup.retrofit2", name = "converter-kotlinx-serialization", version.ref = "retrofit" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
```

- **Nuevas.** El formato JSON de kotlinx.serialization, Retrofit, el conversor que conecta Retrofit con
  kotlinx.serialization y OkHttp.

```toml
desugar-jdk-libs = { group = "com.android.tools", name = "desugar_jdk_libs", version.ref = "desugarJdkLibs" }
```

- **Nueva.** La librería de *desugaring* (se explica en `app/build.gradle.kts`).

```toml
[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

- `kotlin-serialization` (**nuevo**): el plugin del compilador que genera, para cada clase marcada con
  `@Serializable`, el código que la convierte a JSON y de vuelta.

---

### `build.gradle.kts` (raíz) · modificado por mí

```kotlin
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
```

- La única línea nueva es `alias(libs.plugins.kotlin.serialization) apply false`: declara el plugin de serialización
  con su versión, sin aplicarlo a la raíz. Lo aplica el módulo `app`. El resto se explicó en el paso 1.

---

### `app/build.gradle.kts` · modificado por mí

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
}
```

- **Nuevo:** aplica el plugin de serialización al módulo de la app, para que las clases `@Serializable` funcionen.

```kotlin
android {
    namespace = "com.equipo2.vinilos"
    compileSdk {
        version = release(37)
    }
```

- Sin cambios (paso 1).

```kotlin
    defaultConfig {
        applicationId = "com.equipo2.vinilos"
        minSdk = 21
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "API_BASE_URL", "\"https://backvynils-q6yc.onrender.com/\"")
    }
```

- `minSdk = 21` (**cambió** de 24): la app se puede instalar desde Android 5.0 Lollipop, como pide el curso.
- `buildConfigField(…)` (**nuevo**): crea la constante `BuildConfig.API_BASE_URL` con la dirección del backend. Sus
  tres argumentos son el tipo (`"String"`), el nombre y el valor escrito como código Java (por eso lleva comillas
  escapadas `\"…\"`). Así la URL vive en un solo lugar y el código no la tiene escrita a mano. La barra `/` final es
  obligatoria para Retrofit.
- El resto, sin cambios.

```kotlin
    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
```

- Sin cambios (paso 1).

```kotlin
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
```

- `isCoreLibraryDesugaringEnabled = true` (**nuevo**): activa el *desugaring* de librerías. El compilador reemplaza las
  clases de Java que no existen en Android viejo (como `java.time.LocalDate`, que llegó en Android 8.0) por una copia
  que se incluye en el APK. Así la app maneja fechas igual en Android 5.0 que en Android 17.

```kotlin
    buildFeatures {
        buildConfig = true
    }
}
```

- **Nuevo.** AGP 9 ya no genera la clase `BuildConfig` por defecto; hay que activarla para que exista
  `BuildConfig.API_BASE_URL`.

```kotlin
dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
```

- **Nuevo.** `coreLibraryDesugaring(…)` es la configuración especial que indica qué librería usar para el
  *desugaring*.

```kotlin
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
```

- Las mismas del paso 1 (ahora con las versiones fijadas en el catálogo).

```kotlin
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
```

- **Nuevas:** ViewModel, lifecycle, corrutinas, serialización JSON, Retrofit con su conversor y OkHttp.

```kotlin
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
```

- `testImplementation(libs.kotlinx.coroutines.test)` (**nueva**): queda lista para las pruebas de los ViewModels.
- El resto, sin cambios.

---

### `app/src/main/AndroidManifest.xml` · modificado por mí

```xml
    <uses-permission android:name="android.permission.INTERNET" />
```

- Es la **única línea nueva**: sin este permiso Android bloquea cualquier conexión a Internet y Retrofit fallaría.
  Es un permiso "normal": se concede al instalar, sin preguntarle al usuario. Va dentro de `<manifest>` y antes de
  `<application>`. El resto del archivo no cambió (se explicó en el paso 1).

---

### `app/src/main/java/com/equipo2/vinilos/data/model/Album.kt` · creado por mí

Los **modelos de dominio**: cómo representa la app un álbum, independiente de cómo lo envía la API.

```kotlin
package com.equipo2.vinilos.data.model

import java.time.LocalDate
```

- El paquete `data.model` agrupa los modelos. `LocalDate` representa una fecha sin hora (funciona en API 21 gracias al
  *desugaring*).

```kotlin
data class Album(
    val id: Int,
    val name: String,
    val cover: String,
    val releaseDate: LocalDate,
    val description: String,
    val genre: String,
    val recordLabel: String,
)
```

- `data class`: clase para guardar datos. Kotlin le genera `equals`, `hashCode`, `toString` y `copy`.
- `val`: propiedades de solo lectura; un álbum no cambia después de creado.
- `releaseDate: LocalDate`: sin `?`, porque BackVynils exige la fecha de todo álbum. Solo el formulario (paso 7) la
  maneja como opcional, mientras el usuario no la ha elegido.
- La coma después del último parámetro (*trailing comma*) es válida en Kotlin y facilita agregar campos.

```kotlin
/** Datos para crear un álbum, antes de que la API le asigne un id. */
data class NewAlbum(
    val name: String,
    val cover: String,
    val releaseDate: LocalDate,
    val description: String,
    val genre: String,
    val recordLabel: String,
)
```

- Un álbum que todavía no existe: no tiene `id` (lo asigna la API) y la fecha es obligatoria (sin `?`), porque la API
  la exige.

```kotlin
/** Únicos valores que acepta BackVynils; cualquier otro responde 400. */
object AlbumCatalog {
    val genres = listOf("Classical", "Salsa", "Rock", "Folk")
    val recordLabels = listOf("Sony Music", "EMI", "Discos Fuentes", "Elektra", "Fania Records")
}
```

- `object`: un *singleton*, una única instancia accesible como `AlbumCatalog.genres`.
- Las dos listas son copia exacta de la validación de BackVynils (`Joi.string().valid(…)`); con otro valor la API
  responde 400. El formulario del paso 7 las usa para los desplegables.

---

### `app/src/main/java/com/equipo2/vinilos/data/network/AlbumDto.kt` · creado por mí

Los **DTO** (*Data Transfer Objects*): la forma exacta del JSON que viaja por la red.

```kotlin
package com.equipo2.vinilos.data.network

import kotlinx.serialization.Serializable
```

- `@Serializable` le pide al plugin que genere el código de conversión a JSON.

```kotlin
@Serializable
data class AlbumDto(
    val id: Int,
    val name: String,
    val cover: String,
    val releaseDate: String,
    val description: String,
    val genre: String,
    val recordLabel: String,
)
```

- El álbum tal como lo responde la API.
- Todos los campos son **obligatorios**, igual que en BackVynils, que los exige al crear y al actualizar. Si algún día
  faltara uno, kotlinx.serialization lanzaría un error al leer la respuesta, el repositorio lo convertiría en un
  `Result` de error y la pantalla lo mostraría, en lugar de enseñar un álbum incompleto como si estuviera bien.
- `releaseDate` llega como texto (`"1984-08-01T00:00:00.000Z"`); se convierte a `LocalDate` en el Service Adapter.

```kotlin
@Serializable
data class CreateAlbumRequest(
    val name: String,
    val cover: String,
    val releaseDate: String,
    val description: String,
    val genre: String,
    val recordLabel: String,
)
```

- El cuerpo de `POST /albums`: los seis campos obligatorios de la API, con la fecha ya convertida a texto.

---

### `app/src/main/java/com/equipo2/vinilos/data/network/VinilosApiService.kt` · creado por mí

```kotlin
package com.equipo2.vinilos.data.network

import retrofit2.http.Body
import retrofit2.http.POST

interface VinilosApiService {

    @POST("albums")
    suspend fun createAlbum(@Body request: CreateAlbumRequest): AlbumDto
}
```

- Retrofit genera la implementación de esta interfaz: cada función es un endpoint.
- `@POST("albums")`: petición HTTP POST a `URL_BASE + "albums"`, es decir,
  `https://backvynils-q6yc.onrender.com/albums`.
- `suspend`: es una función de corrutina; espera la respuesta sin bloquear el hilo principal.
- `@Body request`: el objeto se envía como cuerpo de la petición, convertido a JSON.
- `: AlbumDto`: la respuesta JSON se convierte a `AlbumDto`. Si la API responde un error (4xx o 5xx), Retrofit lanza
  una excepción `HttpException`.
- Cada historia que necesite otro endpoint lo agrega aquí (por ejemplo, `GET /albums` para HU01).

---

### `app/src/main/java/com/equipo2/vinilos/data/network/NetworkServiceAdapter.kt` · creado por mí

El **Service Adapter**: la única clase que conoce Retrofit, OkHttp y el formato del JSON. El resto de la app solo ve
modelos (`Album`, `NewAlbum`).

```kotlin
package com.equipo2.vinilos.data.network

import com.equipo2.vinilos.data.model.Album
import com.equipo2.vinilos.data.model.NewAlbum
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit
```

- Los modelos de la app, la configuración JSON, OkHttp, Retrofit, el conversor, las clases de fecha y `TimeUnit` para
  los tiempos de espera.

```kotlin
/** Única puerta hacia BackVynils: oculta Retrofit y el JSON, y entrega modelos de la app. */
class NetworkServiceAdapter(baseUrl: String) {
```

- Recibe la URL base por parámetro (en lugar de leer `BuildConfig` adentro): así una prueba podría crear un adaptador
  apuntando a otro servidor.

```kotlin
    private val json = Json { ignoreUnknownKeys = true }
```

- Configuración de JSON. `ignoreUnknownKeys = true`: si la respuesta trae campos que `AlbumDto` no declara (BackVynils
  devuelve más), los ignora en vez de fallar.

```kotlin
    // El backend vive en Render y la primera petición puede tardar mientras el servidor despierta.
    private val client = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()
```

- El cliente HTTP con tiempos de espera de 30 s para conectar, leer y escribir. Los de OkHttp por defecto son 10 s,
  muy poco cuando el servidor de Render está dormido.
- `Builder()` → configurar → `build()` es el patrón *builder*.

```kotlin
    private val api = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(VinilosApiService::class.java)
```

- Crea Retrofit con la URL base, el cliente configurado y el conversor JSON (`application/json` es el tipo de
  contenido que envía y espera).
- `.create(VinilosApiService::class.java)`: Retrofit genera el objeto que implementa la interfaz.

```kotlin
    suspend fun createAlbum(album: NewAlbum): Album =
        api.createAlbum(album.toRequest()).toAlbum()
```

- Convierte el modelo a DTO (`toRequest()`), llama a la API y convierte la respuesta en modelo (`toAlbum()`).
- `= …`: función de una sola expresión, que devuelve ese resultado.

```kotlin
    companion object {
        private const val TIMEOUT_SECONDS = 30L
```

- `companion object`: miembros de la clase, no de cada instancia (como `static` en Java).
- `const val`: constante conocida al compilar. La `L` indica que es `Long`, el tipo que pide OkHttp.

```kotlin
        fun toApiDate(date: LocalDate): String = "${date}T00:00:00.000Z"
```

- Convierte una fecha al formato que guarda BackVynils: `LocalDate.toString()` produce `1984-08-01` y se le agrega la
  medianoche en UTC (`T00:00:00.000Z`).
- Es pública para poder probarla en el paso 9.

```kotlin
        fun fromApiDate(value: String): LocalDate = OffsetDateTime.parse(value).toLocalDate()
    }
}
```

- La conversión inversa: de `"1984-08-01T00:00:00.000Z"` a `LocalDate`.
- `OffsetDateTime.parse(value)`: lee una fecha ISO con su zona horaria (la `Z` final es UTC).
- `.toLocalDate()`: se queda con el día tal como viene escrito, sin pasarlo a la zona del teléfono. Así
  `1984-08-01T00:00:00.000Z` es el 1 de agosto en cualquier país (pasado a la hora de Colombia serían las 7 p. m. del
  31 de julio).
- No se protege contra formatos inválidos: BackVynils siempre responde la fecha en ISO. Si no lo hiciera, la
  excepción llega al repositorio, que la convierte en un error.

```kotlin
private fun NewAlbum.toRequest() = CreateAlbumRequest(
    name = name,
    cover = cover,
    releaseDate = NetworkServiceAdapter.toApiDate(releaseDate),
    description = description,
    genre = genre,
    recordLabel = recordLabel,
)
```

- **Función de extensión**: agrega `toRequest()` a `NewAlbum` sin modificar esa clase. Es `private`, así que solo
  existe en este archivo.
- Copia cada campo usando argumentos con nombre (`name = name`), y convierte la fecha a texto.

```kotlin
private fun AlbumDto.toAlbum() = Album(
    id = id,
    name = name,
    cover = cover,
    releaseDate = NetworkServiceAdapter.fromApiDate(releaseDate),
    description = description,
    genre = genre,
    recordLabel = recordLabel,
)
```

- La conversión de la respuesta (`AlbumDto`) al modelo de la app (`Album`), con la fecha convertida a `LocalDate`.

---

### `app/src/main/java/com/equipo2/vinilos/data/repository/AlbumRepository.kt` · creado por mí

El **Repository**: lo que las pantallas (sus ViewModels) le piden a los datos. Hoy solo hay red, pero si mañana se
agrega una caché local, solo cambia el repositorio.

```kotlin
package com.equipo2.vinilos.data.repository

import com.equipo2.vinilos.data.model.Album
import com.equipo2.vinilos.data.model.NewAlbum
import com.equipo2.vinilos.data.network.NetworkServiceAdapter
```

```kotlin
interface AlbumRepository {
    suspend fun createAlbum(album: NewAlbum): Result<Album>
}
```

- Una **interfaz**: los ViewModels dependen de ella y no de la implementación. Así las pruebas pueden darles un
  repositorio falso (por ejemplo, uno que siempre falla).
- `Result<Album>`: tipo de Kotlin que contiene el álbum creado **o** el error. Obliga a quien lo llama a manejar los
  dos casos y evita que una excepción de red cierre la app.

```kotlin
class AlbumRepositoryImpl(private val adapter: NetworkServiceAdapter) : AlbumRepository {

    override suspend fun createAlbum(album: NewAlbum): Result<Album> = resultOf { adapter.createAlbum(album) }
}
```

- La implementación real: recibe el Service Adapter en el constructor (inyección de dependencias) y envuelve la
  llamada en `resultOf` (archivo siguiente).

---

### `app/src/main/java/com/equipo2/vinilos/data/repository/ResultOf.kt` · creado por mí

```kotlin
package com.equipo2.vinilos.data.repository

import kotlin.coroutines.cancellation.CancellationException

/**
 * Lo usan todos los repositorios para devolver un Result. Es como runCatching, pero deja pasar la cancelación de la
 * corrutina en lugar de tratarla como un error.
 */
internal suspend fun <T> resultOf(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}
```

- Va en su **propio archivo** porque no es de los álbumes: cada repositorio nuevo de las otras historias
  (`ArtistRepository`, `CollectorRepository`, `PrizeRepository`…) lo usa igual que `AlbumRepositoryImpl`.
- `CancellationException`: la excepción con la que Kotlin cancela una corrutina.
- Función genérica (`<T>`): sirve para cualquier tipo de resultado (`Album`, una lista de artistas…).
- `internal`: visible en todo el módulo `app`, pero no fuera de él.
- Ejecuta el bloque: si termina bien devuelve `Result.success`; si lanza una excepción (sin red, timeout, 400, 500)
  devuelve `Result.failure`.
- La cancelación se vuelve a lanzar (`throw e`). Si el usuario sale de la pantalla mientras se guarda, la corrutina
  debe cancelarse de verdad, no convertirse en un "error al guardar". `runCatching` de Kotlin no hace esta distinción.

---

### `app/src/main/java/com/equipo2/vinilos/ServiceLocator.kt` · creado por mí

**Inyección de dependencias manual**: un lugar que crea cada dependencia una vez y se la entrega a quien la necesite.

```kotlin
package com.equipo2.vinilos

import androidx.annotation.VisibleForTesting
import com.equipo2.vinilos.data.network.NetworkServiceAdapter
import com.equipo2.vinilos.data.repository.AlbumRepository
import com.equipo2.vinilos.data.repository.AlbumRepositoryImpl
```

```kotlin
/** Inyección de dependencias manual: crea cada dependencia una sola vez y la comparte. */
object ServiceLocator {
```

- `object`: una sola instancia en toda la app.

```kotlin
    private val networkServiceAdapter by lazy { NetworkServiceAdapter(BuildConfig.API_BASE_URL) }
    private val defaultAlbumRepository by lazy { AlbumRepositoryImpl(networkServiceAdapter) }
```

- `by lazy { … }`: el objeto se crea la primera vez que se usa, no al abrir la app, y después se reutiliza.
- El adaptador recibe la URL de `BuildConfig`, y el repositorio recibe el adaptador.

```kotlin
    @VisibleForTesting
    var albumRepositoryOverride: AlbumRepository? = null
```

- Un repositorio de reemplazo, nulo por defecto. Las pruebas E2E del paso 9 lo usan para simular que el servidor falla.
- `@VisibleForTesting`: indica (y Lint lo vigila) que solo las pruebas deberían cambiarlo.

```kotlin
    val albumRepository: AlbumRepository
        get() = albumRepositoryOverride ?: defaultAlbumRepository
}
```

- Lo que usa la app: el de reemplazo si existe y, si no (`?:`, el operador Elvis), el real.
- `get() = …`: un *getter* que se evalúa cada vez que se lee la propiedad, para que un cambio del reemplazo se note
  enseguida.

---

## Qué sigue

En el paso 3 (T2) se crea la base visual: los colores del diseño en claro y oscuro, la tipografía, la barra superior,
la barra inferior con las pestañas Álbumes, Artistas y Coleccionistas, y las transiciones entre pantallas.
