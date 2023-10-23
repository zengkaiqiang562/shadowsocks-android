plugins {
    id("com.android.application")
}

android {
    namespace = "com.ssr.app"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.ssr.app"
        minSdk = 23
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    lint.apply {
        warning += "ExtraTranslation"
        warning += "ImpliedQuantity"
        informational += "MissingQuantity"
        informational += "MissingTranslation"

        disable += "BadConfigurationProvider"
        warning += "RestrictedApi"
        disable += "UseAppTint"

        disable += "RemoveWorkManagerInitializer"
    }

    compileOptions.isCoreLibraryDesugaringEnabled = true

    ndkVersion = "25.1.8937393"

    buildTypes {
        getByName("debug") {
            isPseudoLocalesEnabled = true
        }
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFile(getDefaultProguardFile("proguard-android.txt"))
        }
    }

    packagingOptions.jniLibs.useLegacyPackaging = true
    splits.abi {
        isEnable = true
        isUniversalApk = true
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    add("implementation", project(":libssr"))
    add("coreLibraryDesugaring", "com.android.tools:desugar_jdk_libs:2.0.2")

    implementation("androidx.work:work-multiprocess:2.8.1")
    implementation("androidx.work:work-runtime-ktx:2.8.1")
}