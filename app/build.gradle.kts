import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.util.Properties

abstract class VerifySamsungHealthSdkTask : DefaultTask() {
    @get:Input
    abstract val sdkPath: Property<String>

    @TaskAction
    fun verify() {
        val file = File(sdkPath.get())
        if (!file.exists()) {
            throw GradleException(
                "Missing ${file.path}. Download Samsung Health Data SDK v1.1.0 " +
                    "from Samsung Developer, then copy its AAR here before building Samsung direct sync."
            )
        }
        println("Samsung Health Data SDK found: ${file.path}")
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-parcelize")
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

val releaseKeystoreProperties = Properties().apply {
    val propertiesFile = rootProject.file("keystore.properties")
    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use(::load)
    }
}
val samsungHealthDataSdk = file("libs/samsung-health-data-api-1.1.0.aar")

android {
    namespace = "com.coachapp"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.coachapp"
        minSdk = 29
        targetSdk = 37
        versionCode = 5
        versionName = "1.2.1"
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

    sourceSets {
        getByName("main") {
            if (samsungHealthDataSdk.exists()) {
                java.srcDir("src/samsungHealth/java")
            }
        }
    }
}

kotlin {
    jvmToolchain(17)
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(project(":core"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.compose.material3)
    implementation(libs.gson)
    implementation(libs.androidx.health.connect.client)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.play.services.wearable)
    if (samsungHealthDataSdk.exists()) {
        implementation(files(samsungHealthDataSdk))
    }
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
}

tasks.register<VerifySamsungHealthSdkTask>("verifySamsungHealthSdk") {
    group = "verification"
    description = "Checks that the official Samsung Health Data SDK AAR is available locally."
    sdkPath.set(samsungHealthDataSdk.absolutePath)
}
