plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    kotlin("android")

}


android {
    namespace = "com.project.gudasi"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.project.gudasi"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {

    implementation("androidx.core:core:1.13.0")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("androidx.activity:activity:1.9.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(libs.scenecore)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("com.airbnb.android:lottie:6.0.0")

    // Firebase Firestore 라이브러리 직접 명시
    implementation ("com.google.firebase:firebase-firestore:24.10.3")

    // Firebase SDK 설정을 위해 필요
    implementation ("com.google.firebase:firebase-analytics:21.6.1")

    implementation("com.google.firebase:firebase-auth:22.3.0") // Firebase 인증
    implementation("com.google.android.gms:play-services-auth:20.7.0") // Google 로그인
    implementation("com.prolificinteractive:material-calendarview:1.4.3")
    implementation(kotlin("stdlib"))




}