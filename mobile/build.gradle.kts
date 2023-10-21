plugins {
    id("com.android.application")
//    id("com.google.android.gms.oss-licenses-plugin")
//    id("com.google.gms.google-services")
//    id("com.google.firebase.crashlytics")
    kotlin("android")
    id("kotlin-parcelize")
}

setupApp()

android {
    namespace = "com.github.shadowsocks"
    defaultConfig.applicationId = "com.github.shadowsocks"
}

dependencies {
//    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.10")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.fragment:fragment-ktx:1.6.1")
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation("com.google.android.material:material:1.8.0")
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("androidx.work:work-multiprocess:2.8.1")
    implementation("androidx.work:work-runtime-ktx:2.8.1")


    implementation("androidx.browser:browser:1.5.0-rc01")
    implementation("androidx.camera:camera-camera2:1.2.1")
    implementation("androidx.camera:camera-lifecycle:1.2.1")
    implementation("androidx.camera:camera-view:1.2.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("com.google.mlkit:barcode-scanning:17.0.3")
    implementation("com.google.zxing:core:3.5.1")
    implementation("com.takisoft.preferencex:preferencex-simplemenu:1.1.0")
    implementation("com.twofortyfouram:android-plugin-api-for-locale:1.0.4")
    implementation("me.zhanghai.android.fastscroll:library:1.2.0")
}
