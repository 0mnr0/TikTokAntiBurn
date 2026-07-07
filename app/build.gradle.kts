plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
    alias(libs.plugins.google.firebase.firebase.perf)
}

android {
    namespace = "made.by.human.tiktokantiburn"
    compileSdk = 37
    ndkVersion = "28.2.13676358"

    defaultConfig {
        applicationId = "made.by.human.tiktokantiburn"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.5.3"

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

            manifestPlaceholders["firebaseAnalyticsDeactivated"] = "false"
        }

        debug {
            manifestPlaceholders["firebaseAnalyticsDeactivated"] = "true"
        }
    }

    buildFeatures {
        buildConfig = true
    }


    sourceSets {
        getByName("main") {
            assets.directories.add("src/main/assets")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}


dependencies {

    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.ui.android)
    implementation (libs.material.v190)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    //noinspection UseTomlInstead
    compileOnly("de.robv.android.xposed:api:82")
    //noinspection UseTomlInstead
    implementation("com.google.android.material:material:1.14.0-rc01")
    //noinspection UseTomlInstead
    implementation ("com.google.code.gson:gson:2.14.0")
    //noinspection UseTomlInstead
    implementation ("androidx.work:work-runtime:2.11.2")
}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
