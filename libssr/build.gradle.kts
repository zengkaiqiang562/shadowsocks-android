import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
    id("com.android.library")
    id("org.mozilla.rust-android-gradle.rust-android")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
}

android {
    namespace = "com.library.ssr"
    compileSdk = 33

    defaultConfig {
        minSdk = 23
        targetSdk = 33
        consumerProguardFiles("proguard-rules.pro")

        externalNativeBuild.ndkBuild {
            abiFilters("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            arguments("-j${Runtime.getRuntime().availableProcessors()}")
        }
    }

    val javaVersion = JavaVersion.VERSION_11
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    lint.apply {
        warning += "ExtraTranslation"
        warning += "ImpliedQuantity"
        informational += "MissingQuantity"
        informational += "MissingTranslation"

        disable += "BadConfigurationProvider"
        warning += "RestrictedApi"
        disable += "UseAppTint"
    }
    (this as ExtensionAware).extensions.getByName<org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions>("kotlinOptions").jvmTarget =
        javaVersion.toString()

    compileOptions.isCoreLibraryDesugaringEnabled = true
    ndkVersion = "25.1.8937393"

    externalNativeBuild.ndkBuild.path("src/main/jni/Android.mk")

}

cargo {
    module = "src/main/rust/shadowsocks-rust"
    libname = "sslocal"
    targets = listOf("arm", "arm64", "x86", "x86_64")
//    profile = findProperty("CARGO_PROFILE")?.toString() ?: currentFlavor
    profile = "release" // TODO 正式环境改成 "release"
    extraCargoBuildArguments = listOf("--bin", libname!!)
    featureSpec.noDefaultBut(arrayOf(
        "stream-cipher",
        "aead-cipher-extra",
        "logging",
        "local-flow-stat",
        "local-dns",
        "aead-cipher-2022",
    ))
    exec = { spec, toolchain ->
        run {
            try {
                Runtime.getRuntime().exec("python3 -V >/dev/null 2>&1")
                spec.environment("RUST_ANDROID_GRADLE_PYTHON_COMMAND", "python3")
                project.logger.lifecycle("Python 3 detected.")
            } catch (e: java.io.IOException) {
                project.logger.lifecycle("No python 3 detected.")
                try {
                    Runtime.getRuntime().exec("python -V >/dev/null 2>&1")
                    spec.environment("RUST_ANDROID_GRADLE_PYTHON_COMMAND", "python")
                    project.logger.lifecycle("Python detected.")
                } catch (e: java.io.IOException) {
                    throw GradleException("No any python version detected. You should install the python first to compile project.")
                }
            }
            spec.environment("RUST_ANDROID_GRADLE_LINKER_WRAPPER_PY", "$projectDir/$module/../linker-wrapper.py")
            spec.environment("RUST_ANDROID_GRADLE_TARGET", "target/${toolchain.target}/$profile/lib$libname.so")
        }
    }
}

tasks.whenTaskAdded {
    when (name) {
        "mergeDebugJniLibFolders", "mergeReleaseJniLibFolders" -> dependsOn("cargoBuild")
    }
}

tasks.register<Exec>("cargoClean") {
    executable("cargo")     // cargo.cargoCommand
    args("clean")
    workingDir("$projectDir/${cargo.module}")
}
tasks.clean.dependsOn("cargoClean")

dependencies {

    implementation(kotlin("stdlib-jdk8"))

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("com.google.android.material:material:1.8.0")

    implementation("androidx.lifecycle:lifecycle-livedata-core-ktx:2.5.1")
    implementation("androidx.work:work-multiprocess:2.8.1")
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("dnsjava:dnsjava:3.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    add("coreLibraryDesugaring", "com.android.tools:desugar_jdk_libs:2.0.2")
}