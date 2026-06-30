plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.coachapp.core"
    compileSdk = 37

    defaultConfig {
        minSdk = 29
    }
}
