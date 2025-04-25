plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
<<<<<<< HEAD
    id("kotlin-kapt")

=======
    id("com.google.devtools.ksp")
>>>>>>> aba7f101684f8867721e362d5897d957da0731c6
}

android {
    namespace = "com.example.prog_poe_2025"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.prog_poe_2025"
        minSdk = 34
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


        buildTypes {
            debug {
                // Enable debugging mode
                isDebuggable = true
            }
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

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.mpandroidchart.vv310)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
<<<<<<< HEAD

    // Room
    implementation(libs.room.runtime)
    kapt(libs.room.compiler)
    implementation(libs.room.ktx)

// Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

// Lifecycle
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
=======
    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.material)


    ksp(libs.androidx.room.compiler.v250)

    // Room database dependencies
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx.v251)
>>>>>>> aba7f101684f8867721e362d5897d957da0731c6

}




