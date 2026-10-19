# Vinilos · Paso 1 de 9: proyecto base de Android Studio

Este commit es el proyecto tal como lo crea Android Studio con la plantilla **Empty Views Activity**, antes de cualquier
cambio. Sirve como punto de partida: cada commit siguiente agrega una tarea de la HU07 y reemplaza este README con la
explicación de lo que cambió.

| Paso | Commit |
|---|---|
| **1** | **Proyecto base de Android Studio** |
| 2 | T1 · Configuración, capa de red y estructura MVVM |
| 3 | T2 · Tema claro y oscuro y navegación |
| 4 | T3 · Componentes de carga, error, vacío y avisos |
| 5 | T4 · Selección de rol y menú de rol |
| 6 | T5 · Botón + en Álbumes |
| 7 | T6 · Formulario de crear álbum con validaciones |
| 8 | T7 · Creación del álbum en la API |
| 9 | T8 · Pruebas de la historia |

## Cómo se creó

En Android Studio: **File › New › New Project › Empty Views Activity**, con estas opciones:

| Campo del asistente | Valor |
|---|---|
| Name | `Vinilos` |
| Package name | `com.equipo2.vinilos` |
| Language | Kotlin |
| Minimum SDK | API 24 (el valor por defecto del asistente; en el paso 2 se baja a API 21) |
| Build configuration language | Kotlin DSL (`build.gradle.kts`) + catálogo de versiones |

El **package name** sigue la convención de Java de escribir un dominio al revés (`com.empresa.app`) para que el
nombre sea único en el mundo: `com` es la terminación del dominio, `equipo2` identifica al equipo y `vinilos` a
la app. En Android ese valor es además el `applicationId`, la identidad de la app en el teléfono y en Google
Play: dos apps con el mismo `applicationId` no pueden estar instaladas a la vez.

El proyecto se generó con **Android Studio 2026.2.1** y se subió tal cual, sin tocar ningún archivo. Las versiones de
las librerías son las últimas estables, que es lo que el asistente pone en un proyecto nuevo. Esta versión de la
plantilla ya usa la configuración nueva de AGP 9: reglas de R8 en `src/main/keepRules/`, optimización con el bloque
`optimization` y el Java del proceso de Gradle fijado en `gradle/gradle-daemon-jvm.properties`.

**Origen de cada archivo en este commit:** todos son **autogenerados** por Android Studio o por Gradle, salvo este
`README.md`, que lo escribí yo (Android Studio no crea README).

## Orden en que se hicieron los cambios

1. Crear el proyecto con el asistente (Empty Views Activity) con las opciones de la tabla anterior.
2. Android Studio sincroniza el proyecto con Gradle (descarga Gradle y las librerías la primera vez).
3. Agregar este `README.md`.

## Cómo ejecutarlo

1. Abrir Android Studio › **File › Open** › elegir la carpeta del proyecto.
2. Esperar a que termine **Gradle Sync** (barra inferior). La primera vez descarga Gradle y las librerías.
3. Elegir un emulador o teléfono con **Android 7.0 (API 24) o superior** en la barra superior.
4. Pulsar **Run ▶**. La app muestra el texto «Hello World!» en el centro.

Desde una terminal en la carpeta del proyecto:

```bash
./gradlew assembleDebug        # Windows: gradlew.bat assembleDebug
./gradlew testDebugUnitTest    # corre la prueba unitaria de ejemplo
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

---

## Explicación de cada archivo

### Estructura

```
Vinilos/
├── settings.gradle.kts        Qué módulos tiene el proyecto y de dónde se descargan plugins y librerías
├── build.gradle.kts           Plugins comunes a todo el proyecto
├── gradle.properties          Ajustes de Gradle
├── gradle/
│   ├── libs.versions.toml     Catálogo de versiones de librerías y plugins
│   ├── gradle-daemon-jvm.properties  Con qué Java se ejecuta Gradle
│   └── wrapper/               Wrapper de Gradle (descarga la versión correcta de Gradle)
├── gradlew, gradlew.bat       Scripts para ejecutar Gradle sin instalarlo
├── .gitignore                 Lo que Git no debe subir
└── app/                       El módulo de la aplicación
    ├── build.gradle.kts       Configuración de la app: paquete, SDK, dependencias
    ├── .gitignore
    └── src/
        ├── main/              Código, recursos y reglas de R8 (keepRules/) de la app
        ├── test/              Pruebas unitarias (se ejecutan en el computador)
        └── androidTest/       Pruebas instrumentadas (se ejecutan en el emulador o teléfono)
```

También existe `local.properties` (ruta del SDK en tu computador). Android Studio lo crea al abrir el proyecto y **no
se sube al repositorio** porque cada persona tiene una ruta distinta.

---

### `settings.gradle.kts` · autogenerado

Es lo primero que lee Gradle: define el nombre del proyecto, sus módulos y los repositorios.

```kotlin
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
```

- `pluginManagement { repositories { … } }`: dónde buscar los **plugins de Gradle** (por ejemplo, el plugin de Android).
- `google { content { includeGroupByRegex(…) } }`: el repositorio Maven de Google, pero solo para los grupos que
  coinciden con esas expresiones (`com.android…`, `com.google…`, `androidx…`). Así Gradle no le pregunta a Google por
  librerías que no están ahí y la sincronización es más rápida.
- `mavenCentral()`: el repositorio público principal de librerías Java/Kotlin.
- `gradlePluginPortal()`: el portal oficial de plugins de Gradle.

```kotlin
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
```

- Plugin de configuración que permite a Gradle **descargar automáticamente un JDK** si el proyecto pide una versión de
  Java que no está instalada. Android Studio lo agrega a todos los proyectos nuevos.

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
```

- `dependencyResolutionManagement`: dónde buscar las **librerías** que usa la app.
- `repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)`: obliga a declarar los repositorios solo aquí. Si un
  módulo intenta declarar los suyos, la compilación falla. Mantiene la configuración en un solo lugar.
- `google()` y `mavenCentral()`: los dos repositorios de librerías.

```kotlin
rootProject.name = "Vinilos"
include(":app")
```

- `rootProject.name`: nombre del proyecto (el que se ve en Android Studio).
- `include(":app")`: el proyecto tiene un módulo llamado `app`, que es la carpeta `app/`.

---

### `build.gradle.kts` (raíz) · autogenerado

```kotlin
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
}
```

- El comentario lo pone la plantilla: aquí va la configuración común a todos los módulos.
- `alias(libs.plugins.android.application)`: el plugin de Android para aplicaciones (AGP), tomado del catálogo
  `libs.versions.toml` (sección `[plugins]`).
- `apply false`: lo **declara** con su versión, pero no lo aplica a la raíz (la raíz no es una app). Lo aplica el módulo
  `app` en su propio `build.gradle.kts`. Así la versión se define una sola vez.

---

### `gradle/libs.versions.toml` · autogenerado

El **catálogo de versiones**: un solo archivo con las versiones de todas las librerías y plugins. Los `build.gradle.kts`
las usan con nombres como `libs.androidx.core.ktx`.

```toml
[versions]
agp = "9.4.1"
coreKtx = "1.19.1"
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"
appcompat = "1.8.0"
material = "1.14.0"
activityKtx = "1.13.0"
constraintlayout = "2.2.2"
```

`[versions]` le da un nombre a cada versión:

| Nombre | Versión | De qué |
|---|---|---|
| `agp` | 9.4.1 | Android Gradle Plugin: el plugin que sabe compilar apps Android |
| `coreKtx` | 1.19.1 | AndroidX Core KTX: utilidades básicas de Android con extensiones de Kotlin |
| `junit` | 4.13.2 | JUnit 4: framework de pruebas unitarias |
| `junitVersion` | 1.3.0 | Extensiones de JUnit para pruebas en Android (`androidx.test.ext:junit`) |
| `espressoCore` | 3.7.0 | Espresso: pruebas de interfaz en el dispositivo |
| `appcompat` | 1.8.0 | AppCompat: `AppCompatActivity` y compatibilidad con versiones viejas de Android |
| `material` | 1.14.0 | Material Components: botones, campos de texto y temas de Material Design |
| `activityKtx` | 1.13.0 | AndroidX Activity con extensiones de Kotlin: `enableEdgeToEdge()`, el botón Atrás y `by viewModels()` |
| `constraintlayout` | 2.2.2 | ConstraintLayout: el layout que usa `activity_main.xml` |

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

`[libraries]` define cada librería con sus coordenadas Maven:

- `group`: la organización (por ejemplo, `androidx.core`).
- `name`: el artefacto (por ejemplo, `core-ktx`).
- `version.ref`: qué versión de `[versions]` usar.
- El nombre de la izquierda se usa en Gradle cambiando los guiones por puntos: `androidx-core-ktx` →
  `libs.androidx.core.ktx`.

```toml
[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
```

- `[plugins]`: los plugins de Gradle. `com.android.application` es el plugin de apps Android, con la versión `agp`.
  Se usa como `libs.plugins.android.application`.

---

### `gradle.properties` · autogenerado

Ajustes globales de Gradle. Las líneas que empiezan con `#` son comentarios de la plantilla.

```properties
# Project-wide Gradle settings.
# IDE (e.g. Android Studio) users:
# Gradle settings configured through the IDE *will override*
# any settings specified in this file.
# For more details on how to configure your build environment visit
# http://www.gradle.org/docs/current/userguide/build_environment.html
```

Comentario: estos ajustes aplican a todo el proyecto, y lo que se configure desde Android Studio tiene prioridad.

```properties
# Specifies the JVM arguments used for the daemon process.
# The setting is particularly useful for tweaking memory settings.
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
```

- `org.gradle.jvmargs`: opciones de la máquina virtual de Java que ejecuta Gradle. `-Xmx2048m` le da hasta 2 GB de
  memoria; `-Dfile.encoding=UTF-8` hace que lea y escriba archivos en UTF-8 (importante para las tildes).

```properties
# When configured, Gradle will run in incubating parallel mode.
# This option should only be used with decoupled projects. For more details, visit
# https://developer.android.com/r/tools/gradle-multi-project-decoupled-projects
# org.gradle.parallel=true
```

- Opción comentada (desactivada): compilar módulos en paralelo. No aplica porque hay un solo módulo.

```properties
# When enabled, the Configuration Cache allows Gradle to skip the configuration
# phase entirely if nothing that affects the build configuration (such as build scripts)
# has changed. Additionally, Gradle applies performance optimizations to task execution.
org.gradle.configuration-cache=true
```

- `org.gradle.configuration-cache=true`: guarda la configuración del proyecto ya calculada; si los archivos de Gradle
  no cambiaron, la siguiente compilación se salta esa fase y arranca más rápido.

```properties
# Kotlin code style for this project: "official" or "obsolete":
kotlin.code.style=official
```

- `kotlin.code.style=official`: Android Studio formatea el código Kotlin con la guía de estilo oficial.

Plantillas anteriores también traían `android.useAndroidX=true` (usar AndroidX en lugar de la antigua *support library*)
y `android.nonTransitiveRClass=true` (que la clase `R` de la app tenga solo sus propios recursos). Con AGP 9 los dos
vienen activados por defecto, por eso la plantilla ya no los escribe.

---

### `gradle/wrapper/gradle-wrapper.properties` · autogenerado

El **wrapper** permite compilar sin tener Gradle instalado: descarga la versión exacta que pide el proyecto.

```properties
#Mon Oct 19 00:22:36 GMT-05:00 2026
```

- Comentario con la fecha en que Gradle escribió el archivo. No tiene efecto.

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
```

- Dónde se guarda Gradle descargado: en la carpeta de usuario de Gradle (`~/.gradle`), subcarpeta `wrapper/dists`.

```properties
distributionSha256Sum=bbaeb2fef8710818cf0e261201dab964c572f92b942812df0c3620d62a529a01
distributionUrl=https\://services.gradle.org/distributions/gradle-9.6.0-bin.zip
```

- `distributionUrl`: qué versión de Gradle descargar, **9.6.0**. La `\` antes de `:` es el escape que exige el formato
  `.properties`.
- `distributionSha256Sum`: la huella SHA-256 del `.zip` de Gradle. Después de descargarlo, el wrapper calcula la huella
  y, si no coincide (archivo dañado o alterado), se niega a usarlo.

```properties
networkTimeout=10000
validateDistributionUrl=true
```

- `networkTimeout`: tiempo máximo de la descarga sin respuesta, en milisegundos (10 s).
- `validateDistributionUrl`: comprueba que la URL sea válida antes de descargar.

```properties
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

- Dónde se guarda el `.zip` descargado antes de descomprimirlo.

### `gradle/wrapper/gradle-wrapper.jar` · autogenerado (binario)

Programa Java pequeño que lee `gradle-wrapper.properties`, descarga Gradle si hace falta y lo ejecuta. Es un archivo
binario: no se lee ni se edita.

### `gradle/gradle-daemon-jvm.properties` · autogenerado

Gradle se ejecuta como un proceso de fondo, el **daemon**, que necesita Java. Este archivo fija con qué versión:

```properties
#This file is generated by updateDaemonJvm
toolchainUrl.FREE_BSD.AARCH64=https\://api.foojay.io/disco/v3.0/ids/cf726b4a1c84b50457225f9bba6d7650/redirect
…
toolchainUrl.WINDOWS.X86_64=https\://api.foojay.io/disco/v3.0/ids/d76df094a9cbbabd3b08251f9e61444a/redirect
toolchainVersion=25
```

- El comentario dice que lo genera la tarea de Gradle `updateDaemonJvm`; no se edita a mano.
- `toolchainVersion=25`: el daemon corre con **Java 25**, sin importar qué Java tenga instalado cada persona. Así todo
  el equipo compila igual.
- `toolchainUrl.<sistema>.<procesador>`: de dónde descargar ese Java si no está instalado, una línea por cada
  combinación de sistema operativo (FreeBSD, Linux, macOS, Unix, Windows) y procesador (ARM de 64 bits `AARCH64` o
  Intel/AMD `X86_64`). Las direcciones son de **foojay**, el mismo servicio que usa el plugin de `settings.gradle.kts`.
- Es distinto de `compileOptions` en `app/build.gradle.kts`: ese define la versión de Java del *código de la app*
  (Java 11); este, la del *programa Gradle*.

### `gradlew` y `gradlew.bat` · autogenerados

Scripts que lanzan `gradle-wrapper.jar`: `gradlew` para Linux y macOS (shell POSIX), `gradlew.bat` para Windows. Los
genera Gradle con una plantilla estándar; no se editan. Por bloques:

| Bloque | Qué hace |
|---|---|
| Licencia Apache 2.0 (comentarios del inicio) | Licencia del script de Gradle |
| Comentarios «Important for running/patching» | Requisitos de la shell y notas para quien quiera modificar el script |
| Detección de `APP_HOME` | Calcula la carpeta del proyecto aunque el script se llame desde otra carpeta o mediante enlaces |
| `DEFAULT_JVM_OPTS` | Memoria por defecto de la JVM que ejecuta el wrapper (`-Xmx64m -Xms64m`) |
| Detección del sistema (`cygwin`, `darwin`, `mingw`, `nonstop`) | Ajustes para cada sistema operativo |
| `CLASSPATH` | Queda vacío: el wrapper se lanza con `-jar` (ver la última fila) |
| Búsqueda de Java | Usa `JAVA_HOME` si existe; si no, busca `java` en el `PATH`. Si no encuentra Java, muestra un error |
| Límite de archivos abiertos (`ulimit`) | Sube el límite de archivos que puede abrir el proceso |
| Armado de argumentos y `exec` | Junta las opciones de `JAVA_OPTS`, `GRADLE_OPTS` y los argumentos del usuario, y ejecuta `java … -jar gradle/wrapper/gradle-wrapper.jar` |

`gradlew.bat` hace lo mismo con la sintaxis de los archivos por lotes de Windows (`set`, `if`, `goto`).

---

### `.gitignore` (raíz) · autogenerado

Archivos y carpetas que Git no debe subir porque se generan solos o son de cada computador:

| Línea | Qué excluye |
|---|---|
| `*.iml` | Archivos de módulo de IntelliJ / Android Studio |
| `.gradle` | Caché de Gradle del proyecto |
| `/local.properties` | Ruta del SDK en tu computador |
| `/.idea/caches`, `/.idea/libraries`, `/.idea/modules.xml`, `/.idea/workspace.xml`, `/.idea/navEditor.xml`, `/.idea/assetWizardSettings.xml` | Partes personales de la configuración de Android Studio |
| `.DS_Store` | Archivo que crea macOS en cada carpeta |
| `/build` | Salidas de compilación del proyecto raíz |
| `/captures` | Capturas de pantalla y perfiles que guarda Android Studio |
| `.externalNativeBuild`, `.cxx` | Temporales de código nativo C/C++ (no se usa aquí) |
| `local.properties` | Otra vez la ruta del SDK, en cualquier carpeta |

### `app/.gitignore` · autogenerado

```
/build
```

- Excluye `app/build/`: APKs, clases generadas y reportes. Se regeneran en cada compilación.

---

### `app/build.gradle.kts` · autogenerado

La configuración del módulo de la app.

```kotlin
plugins {
    alias(libs.plugins.android.application)
}
```

- Aplica el plugin de Android para aplicaciones (el que en la raíz se declaró con `apply false`). Con AGP 9 el soporte de
  Kotlin viene incluido en este plugin, por eso no hay un plugin de Kotlin aparte.

```kotlin
android {
    namespace = "com.equipo2.vinilos"
```

- `namespace`: el paquete de la clase `R` y de `BuildConfig`, y el que se usa para resolver nombres relativos en el
  manifiesto (`.MainActivity` → `com.equipo2.vinilos.MainActivity`).

```kotlin
    compileSdk {
        version = release(37)
    }
```

- `compileSdk`: con qué versión del SDK de Android se compila (API 37). Define qué clases y métodos de Android están
  disponibles al escribir código. `release(37)` es la forma nueva de AGP 9 de indicar la versión.

```kotlin
    defaultConfig {
        applicationId = "com.equipo2.vinilos"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
```

- `applicationId`: el identificador único de la app en el teléfono y en Google Play. Dos apps con el mismo
  `applicationId` se consideran la misma.
- `minSdk = 24`: la versión mínima de Android en la que se puede instalar (Android 7.0). El curso pide Android 5.0
  (API 21); se cambia en el paso 2.
- `targetSdk = 37`: la versión para la que se probó la app; Android activa los comportamientos de esa versión.
- `versionCode`: número entero de la versión, que debe subir en cada publicación.
- `versionName`: la versión que ve el usuario.
- `testInstrumentationRunner`: la clase que ejecuta las pruebas en el dispositivo (`androidTest`).

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

- `buildTypes`: tipos de compilación. `debug` existe por defecto; aquí se configura `release`, la versión para
  publicar.
- `optimization { … }`: el bloque nuevo de AGP 9 para **R8**, la herramienta que, al compilar en release, borra el
  código que no se usa, acorta los nombres de clases y métodos y optimiza el resto. El APK queda más pequeño y rápido.
- `enable = true`: activa R8 en release. En `debug` (la que se usa al programar y en las pruebas) no se aplica nunca.
- `packageScope = setOf(…)`: limita la optimización a las librerías de AndroidX (`androidx.**`), Kotlin (`kotlin.**`)
  y las de Kotlin de JetBrains (`kotlinx.**`); `**` significa "este paquete y todos sus subpaquetes". El código de la
  app no se toca, así R8 no puede romperlo por accidente, y se gana casi todo el ahorro porque esas librerías son la
  mayor parte del APK.
- Plantillas anteriores dejaban R8 apagado (`isMinifyEnabled = false`) y usaban un archivo `proguard-rules.pro`; AGP 9
  lo reemplazó por este bloque y por la carpeta `src/main/keepRules/`.

```kotlin
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
```

- `compileOptions`: la versión del lenguaje Java del código fuente y del bytecode generado (Java 11). Kotlin usa la
  misma versión de destino.

```kotlin
dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
```

- `implementation(…)`: librerías que usa la app y se incluyen en el APK.
- `testImplementation(…)`: solo para las pruebas unitarias (`src/test`).
- `androidTestImplementation(…)`: solo para las pruebas en el dispositivo (`src/androidTest`).
- Las librerías son las del catálogo explicadas arriba.

---

### `app/src/main/keepRules/rules.keep` · autogenerado

Reglas propias para R8. AGP junta todos los archivos de la carpeta `src/main/keepRules/` y se los pasa a R8 junto con
las reglas que ya traen las librerías.

```
# Add project specific R8 rules here.
# AGP will combine all keep rule files in src/main/keepRules to pass to R8
#
# For more details, see
#   https://d.android.com/r/tools/r8/keep-rules
```

- Explica para qué es el archivo y dónde está la documentación.

```
# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
```

- Un ejemplo desactivado (todas las líneas empiezan con `#`): si una página web dentro de la app llamara métodos de
  Kotlin, habría que pedirle a R8 que no les cambie el nombre (`-keepclassmembers`).
- Hoy el archivo no tiene reglas activas, y tampoco las necesita: `packageScope` deja fuera de R8 el código de la app.
  Una regla `-keep` sirve para clases que se usan por reflexión (por su nombre en texto), que R8 no puede ver.

---

### `app/src/main/AndroidManifest.xml` · autogenerado

Describe la app al sistema: componentes, permisos e íconos.

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">
```

- `<?xml …?>`: encabezado estándar de XML.
- `xmlns:android`: el espacio de nombres de los atributos `android:…`.
- `xmlns:tools`: el espacio de nombres `tools:…`, atributos que solo usan las herramientas y no llegan a la app. La
  plantilla lo declara aunque este archivo todavía no use ninguno.

```xml
    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Vinilos">
```

- `android:allowBackup`: permite que Android respalde los datos de la app en la nube.
- `android:dataExtractionRules`: qué datos se respaldan en Android 12 (API 31) o superior (`res/xml/data_extraction_rules.xml`).
- `android:fullBackupContent`: lo mismo para Android 11 o inferior (`res/xml/backup_rules.xml`).
- `android:icon` y `android:roundIcon`: el ícono de la app y su versión redonda (carpetas `mipmap`).
- `android:label`: el nombre de la app, tomado de `strings.xml`.
- `android:supportsRtl`: permite invertir el diseño en idiomas de derecha a izquierda (árabe, hebreo).
- `android:theme`: el tema de toda la app, definido en `themes.xml`.
- En versiones de Android que no conocen un atributo (por ejemplo, `dataExtractionRules` antes de Android 12), el sistema
  simplemente lo ignora.

```xml
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

- `<activity android:name=".MainActivity">`: declara la actividad; el punto inicial se completa con el `namespace`.
- `android:exported="true"`: otras apps (el launcher) pueden abrirla. Es obligatorio declararlo desde Android 12.
- `android:windowSoftInputMode="adjustResize"`: cuando aparece el teclado, la ventana se encoge para dejarle espacio en
  lugar de quedar tapada. Va junto con `enableEdgeToEdge()` de `MainActivity`: así la app recibe la altura del teclado
  y puede acomodar su contenido, también en las versiones viejas de Android.
- `<intent-filter>` con `MAIN` + `LAUNCHER`: esta actividad es la que se abre al tocar el ícono de la app.

---

### `app/src/main/java/com/equipo2/vinilos/MainActivity.kt` · autogenerado

La única pantalla de la plantilla.

```kotlin
package com.equipo2.vinilos
```

- El paquete de la clase; coincide con la carpeta `com/equipo2/vinilos`.

```kotlin
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
```

- `Bundle`: el estado guardado que recibe `onCreate`.
- `enableEdgeToEdge`: función de AndroidX Activity para dibujar la app detrás de las barras del sistema.
- `AppCompatActivity`: actividad de AppCompat, compatible con versiones antiguas de Android.
- `ViewCompat` y `WindowInsetsCompat`: para leer el tamaño de las barras del sistema (insets) de forma compatible.

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
```

- `MainActivity` hereda de `AppCompatActivity`.
- `onCreate` se ejecuta cuando Android crea la pantalla; `savedInstanceState` trae el estado si la pantalla se está
  recreando (por ejemplo, al girar el teléfono). `super.onCreate` es obligatorio.

```kotlin
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
```

- `enableEdgeToEdge()`: la app ocupa toda la pantalla, también detrás de la barra de estado y la de navegación.
- `setContentView(R.layout.activity_main)`: muestra el layout `res/layout/activity_main.xml`.

```kotlin
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
```

- Como la app ahora se dibuja detrás de las barras, hay que dejar espacio para que el contenido no quede tapado.
- `findViewById(R.id.main)`: la vista raíz del layout (tiene `android:id="@+id/main"`).
- `setOnApplyWindowInsetsListener { v, insets -> … }`: se ejecuta cuando el sistema informa el tamaño de sus barras.
- `insets.getInsets(WindowInsetsCompat.Type.systemBars())`: el tamaño de la barra de estado y de navegación.
- `v.setPadding(…)`: agrega ese espacio como relleno de la vista raíz.
- `insets`: la última línea de la lambda es el valor de retorno; devuelve los insets para que sigan llegando a las
  vistas hijas.

---

### `app/src/main/res/layout/activity_main.xml` · autogenerado

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">
```

- `ConstraintLayout`: un contenedor que posiciona a sus hijos con restricciones (arriba de, centrado en, etc.).
- `xmlns:app`: espacio de nombres de los atributos de las librerías (como `app:layout_constraint…`).
- `android:id="@+id/main"`: crea el id `main` (el `+` significa "crear"); lo usa `MainActivity` para los insets.
- `match_parent`: ocupa todo el ancho y alto disponibles.
- `tools:context=".MainActivity"`: solo para el editor de Android Studio, que sabe qué actividad usa este layout.

```xml
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Hello World!"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

- `TextView`: muestra un texto.
- `wrap_content`: mide lo justo para su contenido.
- `android:text="Hello World!"`: el texto (escrito directo; en código real va en `strings.xml`).
- Las cuatro restricciones lo atan a los cuatro bordes del padre, y por eso queda centrado.

---

### `app/src/main/res/values/strings.xml` · autogenerado

```xml
<resources>
    <string name="app_name">Vinilos</string>
</resources>
```

- `<resources>`: raíz de todo archivo de valores.
- `<string name="app_name">`: el nombre de la app; se usa como `@string/app_name` en el manifiesto.

### `app/src/main/res/values/colors.xml` · autogenerado

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
</resources>
```

- Dos colores de ejemplo en formato `#AARRGGBB` (opacidad, rojo, verde, azul). La plantilla no los usa.

### `app/src/main/res/values/themes.xml` · autogenerado

```xml
<resources xmlns:tools="http://schemas.android.com/tools">
    <!-- Base application theme. -->
    <style name="Base.Theme.Vinilos" parent="Theme.Material3.DayNight.NoActionBar">
        <!-- Customize your light theme here. -->
        <!-- <item name="colorPrimary">@color/my_light_primary</item> -->
    </style>

    <style name="Theme.Vinilos" parent="Base.Theme.Vinilos" />
</resources>
```

- `Base.Theme.Vinilos`: el tema base. Hereda de `Theme.Material3.DayNight.NoActionBar`, el tema de Material 3 que
  cambia solo entre claro y oscuro (`DayNight`) y no tiene barra de acciones (`NoActionBar`).
- Los comentarios marcan dónde personalizar los colores del tema claro.
- `Theme.Vinilos`: el tema que usa el manifiesto. Hereda del base; separarlos permite agregar ajustes por versión de
  Android sin repetir todo.

### `app/src/main/res/values-night/themes.xml` · autogenerado

```xml
<resources xmlns:tools="http://schemas.android.com/tools">
    <!-- Base application theme. -->
    <style name="Base.Theme.Vinilos" parent="Theme.Material3.DayNight.NoActionBar">
        <!-- Customize your dark theme here. -->
        <!-- <item name="colorPrimary">@color/my_dark_primary</item> -->
    </style>
</resources>
```

- La carpeta `values-night` se usa cuando el teléfono está en modo oscuro. Redefine el tema base para personalizar los
  colores oscuros (por ahora vacío).

---

### `app/src/main/res/xml/backup_rules.xml` · autogenerado

Reglas de respaldo automático para Android 11 o inferior. Todo está comentado: un comentario explica el archivo y
enlaza la documentación, y dentro de `<full-backup-content>` hay ejemplos desactivados de `<include>` y `<exclude>`
para elegir qué preferencias respaldar. Al estar vacío, se respalda todo lo que Android respalda por defecto.

### `app/src/main/res/xml/data_extraction_rules.xml` · autogenerado

Lo mismo para Android 12 o superior. `<cloud-backup>` (respaldo en la nube) y `<device-transfer>` (paso de datos a un
teléfono nuevo) contienen solo ejemplos comentados.

---

### Íconos de la app · autogenerados

| Archivo | Qué es |
|---|---|
| `res/drawable/ic_launcher_background.xml` | Fondo del ícono adaptativo: un cuadrado verde `#3DDC84` con una cuadrícula de líneas finas |
| `res/drawable/ic_launcher_foreground.xml` | Primer plano: el robot de Android, con una sombra en degradado (`aapt:attr` con `gradient`) |
| `res/mipmap-anydpi-v26/ic_launcher.xml` | Ícono adaptativo para Android 8.0+ (API 26): une `<background>`, `<foreground>` y `<monochrome>` (versión de un solo color para los íconos temáticos de Android 13) |
| `res/mipmap-anydpi-v26/ic_launcher_round.xml` | Lo mismo para el ícono redondo |
| `res/mipmap-{m,h,xh,xxh,xxxh}dpi/ic_launcher.webp` y `ic_launcher_round.webp` | Imágenes binarias para Android 7.x, que no tiene íconos adaptativos. Hay una por densidad de pantalla (de 48 a 192 píxeles) |

Los íconos `.xml` son *vector drawables*: dibujos descritos con trazos (`android:pathData`) en un lienzo de 108 × 108,
que se escalan sin perder calidad.

---

### `app/src/test/java/com/equipo2/vinilos/ExampleUnitTest.kt` · autogenerado

```kotlin
package com.equipo2.vinilos

import org.junit.Test

import org.junit.Assert.*
```

- `@Test` marca un método como prueba; `org.junit.Assert.*` trae `assertEquals` y las demás verificaciones.

```kotlin
/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}
```

- El comentario KDoc dice que es una prueba local: corre en el computador, sin emulador.
- `assertEquals(4, 2 + 2)`: verifica que el valor esperado (4) sea igual al calculado. Es solo un ejemplo.

### `app/src/androidTest/java/com/equipo2/vinilos/ExampleInstrumentedTest.kt` · autogenerado

```kotlin
package com.equipo2.vinilos

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
```

- `InstrumentationRegistry`: acceso al entorno de la prueba en el dispositivo.
- `AndroidJUnit4`: el runner que ejecuta pruebas JUnit 4 en Android.
- `@Test`, `@RunWith` y `Assert.*`: las mismas piezas de JUnit de la prueba unitaria.

```kotlin
/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.equipo2.vinilos", appContext.packageName)
    }
}
```

- `@RunWith(AndroidJUnit4::class)`: la prueba se ejecuta en un dispositivo Android con el runner de AndroidX.
- `InstrumentationRegistry.getInstrumentation().targetContext`: el `Context` de la app que se está probando.
- `assertEquals("com.equipo2.vinilos", appContext.packageName)`: verifica que el paquete instalado sea el de la app.

---

## Qué sigue

En el paso 2 (T1) se baja el `minSdk` a 21 como pide el curso, se fijan versiones de librerías compatibles con esa
versión y se crea la capa de red con la estructura MVVM.
