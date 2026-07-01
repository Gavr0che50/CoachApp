import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val releaseKeystoreProperties = Properties().apply {
    val propertiesFile = rootProject.file("keystore.properties")
    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use(::load)
    }
}

android {
    namespace = "com.coachapp.wear"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.coachapp.wear"
        minSdk = 30
        targetSdk = 37
        versionCode = 4
        versionName = "1.2.0"
    }

    signingConfigs {
        create("release") {
            storeFile = releaseKeystoreProperties["storeFile"]?.toString()?.let { rootProject.file(it) }
            storePassword = releaseKeystoreProperties["storePassword"]?.toString()
            keyAlias = releaseKeystoreProperties["keyAlias"]?.toString()
            keyPassword = releaseKeystoreProperties["keyPassword"]?.toString()
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            isDebuggable = false
        }
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.compose.material)
    implementation(libs.play.services.wearable)
}
