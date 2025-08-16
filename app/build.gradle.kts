plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "made.by.human.tiktokantiburn"
    compileSdk = 35

    defaultConfig {
        applicationId = "made.by.human.tiktokantiburn"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.3.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }


    sourceSets {
        getByName("main") {
            assets.srcDirs("src/main/assets")
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
    implementation("com.google.android.material:material:1.14.0-alpha03")
    implementation ("com.google.code.gson:gson:2.11.0")
    implementation ("androidx.work:work-runtime:2.10.2") // или последнюю
    implementation(libs.ui.android)
    implementation(libs.rendering)
    compileOnly("de.robv.android.xposed:api:82")
    implementation (libs.material.v190)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
