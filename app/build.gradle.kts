import java.util.UUID
import java.io.File

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.apiarymanager.apiaryassistant"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.apiarymanager.apiaryassistant"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    sourceSets {
        getByName("main") {
            assets.srcDirs(
                "src/main/assets",              // .wav и другое, не модель!
            )
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.alphacephei:vosk-android:0.3.47")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    //xlsx
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")

    // WorkManager для фоновых задач
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    // Библиотека OkHttp для сетевых запросов
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

}
