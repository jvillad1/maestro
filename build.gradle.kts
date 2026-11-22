import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

// El R8 del release, más nuevo que el que trae AGP (ver `r8` en libs.versions.toml). Va en la raíz porque
// AGP se carga aquí (`apply false`), y R8 tiene que estar en el mismo classpath para reemplazarlo. Igual que Movi.
buildscript {
    repositories {
        google { mavenContent { includeGroup("com.android.tools") } }
    }
    dependencies {
        classpath(libs.r8)
    }
}

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinSerialization) apply false
}

// Cada link de Kotlin/Native corre dentro del único daemon de Kotlin. Un link optimizado (release) pide varios
// GB de heap, y la caché de configuración agenda los de :core y :shared al mismo tiempo: juntos agotan el heap
// en una Mac de 16 GB. Un servicio compartido deja correr uno a la vez; los de debug siguen en paralelo.
// Copiado de Movi.
abstract class KotlinNativeOptimizedLinkGate : BuildService<BuildServiceParameters.None>

val optimizedLinkGate = gradle.sharedServices.registerIfAbsent(
    "kotlinNativeOptimizedLinkGate",
    KotlinNativeOptimizedLinkGate::class,
) {
    maxParallelUsages.set(1)
}

subprojects {
    tasks.withType<KotlinNativeLink>().configureEach {
        if (binary.optimized) {
            usesService(optimizedLinkGate)
        }
    }
}

// ── Las pruebas siempre corren; lo demás sale de la caché (la «vuelta 122» de Movi) ──────────────
//
// Una `Test` **nunca** está al día ni sale de la caché: así no hay pruebas en verde que en realidad no se
// ejecutaron. La compilación sí sale de la caché. Y si una prueba falla, el log muestra el mensaje y la
// causa completos, sin tener que abrir el reporte.
//
// `maestro.forksDePruebas` (por defecto 1): cuántas JVM de prueba corren a la vez dentro de un módulo.
val forksDePruebas: Int = providers.gradleProperty("maestro.forksDePruebas").orNull?.let { valor ->
    val forks = valor.trim().toIntOrNull()
    require(forks != null && forks >= 1) {
        "maestro.forksDePruebas tiene que ser un entero ≥ 1, no «$valor»"
    }
    forks
} ?: 1
subprojects {
    tasks.withType<Test>().configureEach {
        outputs.upToDateWhen { false }
        outputs.doNotCacheIf("Las pruebas siempre corren") { true }
        testLogging {
            events(org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED)
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showCauses = true
            showStackTraces = true
        }
        maxParallelForks = forksDePruebas
    }
}
