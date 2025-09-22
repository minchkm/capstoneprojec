import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    kotlin("android")
}

val properties = Properties()
properties.load(FileInputStream(rootProject.file("local.properties")))
val apiKey = properties.getProperty("API_KEY")

android {
    namespace = "com.project.gudasi"
    compileSdk = 34 // 최신 안정 버전으로 변경

    defaultConfig {
        applicationId = "com.project.gudasi"
        minSdk = 26
        targetSdk = 34 // 최신 안정 버전으로 변경
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "API_KEY", "\"$apiKey\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8" // JVM Target 오류 해결을 위해 1.8로 설정
    }
}

dependencies {
    // AndroidX
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Lottie
    implementation("com.airbnb.android:lottie:6.0.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-analytics")

    // Google Services
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // Material Calendar View
    implementation("com.prolificinteractive:material-calendarview:1.4.3")

    // Gemini
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("com.prolificinteractive:material-calendarview:1.4.3")

}