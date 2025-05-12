plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "made.by.human.tiktokantiburn"
    compileSdk = 34

    defaultConfig {
        applicationId = "made.by.human.tiktokantiburn"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.2.1"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.google.android.material:material:1.12.0")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation(libs.ui.android)
    implementation (libs.material.v190)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}