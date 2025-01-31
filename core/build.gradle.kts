import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
    id("com.android.library")
    id("org.mozilla.rust-android-gradle.rust-android")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
}

//setupCore()

android {
    namespace = "com.github.shadowsocks.core"


    buildToolsVersion("33.0.1")
    compileSdkVersion(33)
    defaultConfig {
        minSdk = 23
        targetSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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


    defaultConfig {
        consumerProguardFiles("proguard-rules.pro")

        externalNativeBuild.ndkBuild {
            abiFilters("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            arguments("-j${Runtime.getRuntime().availableProcessors()}")
        }

//        kapt.arguments {
//            arg("room.incremental", true)
//            arg("room.schemaLocation", "$projectDir/schemas")
//        }
    }

    externalNativeBuild.ndkBuild.path("src/main/jni/Android.mk")

//    sourceSets.getByName("androidTest") {
//        assets.setSrcDirs(assets.srcDirs + files("$projectDir/schemas"))
//    }
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

//    api(project(":plugin"))
//    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.10")
    implementation(kotlin("stdlib-jdk8"))
//    api("androidx.core:core-ktx:1.7.0")
//    api("androidx.fragment:fragment-ktx:1.6.1")
//    api("com.google.android.material:material:1.6.0")

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("com.google.android.material:material:1.8.0")

    implementation("androidx.lifecycle:lifecycle-livedata-core-ktx:2.5.1")
//    api("androidx.preference:preference:1.2.0")
//    api("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.work:work-multiprocess:2.8.1")
    implementation("androidx.work:work-runtime-ktx:2.8.1")
//    api("com.google.android.gms:play-services-oss-licenses:17.0.0")
    implementation("com.google.code.gson:gson:2.10.1")
//    api("com.google.firebase:firebase-analytics-ktx:21.2.0")
//    api("com.google.firebase:firebase-crashlytics:18.3.3")
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("dnsjava:dnsjava:3.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
//    api("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$coroutinesVersion")
//    kapt("androidx.room:room-compiler:$roomVersion")
//    androidTestImplementation("androidx.room:room-testing:$roomVersion")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.5")

    add("testImplementation", "junit:junit:4.13.2")
    add("androidTestImplementation", "androidx.test:runner:1.5.2")
    add("androidTestImplementation", "androidx.test.espresso:espresso-core:3.5.1")
    add("coreLibraryDesugaring", "com.android.tools:desugar_jdk_libs:2.0.2")
}
