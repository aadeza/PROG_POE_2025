plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.google.services) apply false  // Firebase Google Services plugin
    alias(libs.plugins.ksp) apply false              // Kotlin Symbol Processing plugin
}
