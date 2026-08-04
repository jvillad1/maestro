import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget {
        compilations.all {
            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            compileTaskProvider.configure {
                compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
            }
        }
    }
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }

    // SQLDelight has no wasmJs artifact. Insert a "nonWasm" intermediate
    // source set between commonMain and the android/jvm targets so the
    // database code never reaches the wasm compilation (same as movi).
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("nonWasm") {
                withAndroidTarget()
                withJvm()
                group("apple") {
                    group("ios") {
                        withIosX64()
                        withIosArm64()
                        withIosSimulatorArm64()
                    }
                }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }
        jvmMain.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
    }
}

// SQLDelight 2.0.2 has no wasmJs artifact. Exclude its group from every
// wasmJs configuration so Gradle dependency resolution does not fail.
configurations.configureEach {
    if (name.startsWith("wasmJs")) {
        exclude(group = "app.cash.sqldelight")
    }
}

// SQLDelight generates its Kotlin under commonMain, so wasmJs (which inherits
// from commonMain) would try to compile it without the excluded runtime. Move
// the generated dir to nonWasmMain; the Provider keeps task wiring intact.
afterEvaluate {
    val generateTask = tasks.named("generateCommonMainMaestroDatabaseInterface")
    val generatedDirProvider = generateTask.map {
        layout.buildDirectory.dir("generated/sqldelight/code/MaestroDatabase/commonMain").get()
    }

    val commonMain = kotlin.sourceSets.getByName("commonMain")
    val nonWasmMain = kotlin.sourceSets.getByName("nonWasmMain")

    val toRemove = commonMain.kotlin.srcDirs.filter { it.path.contains("generated/sqldelight") }.toSet()
    if (toRemove.isNotEmpty()) {
        commonMain.kotlin.setSrcDirs(commonMain.kotlin.srcDirs - toRemove)
    }
    nonWasmMain.kotlin.srcDir(generatedDirProvider)
}

android {
    namespace = "com.maestro.core"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

sqldelight {
    databases {
        create("MaestroDatabase") {
            packageName.set("com.maestro.shared.db")
        }
    }
}
