plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.androidx.room) apply false
}

tasks.register("projectCheck") {
    group = "verification"
    description = "Runs the minimum checks before opening a PR."

dependsOn(":core:testDebugUnitTest")
dependsOn(":app:testDebugUnitTest")
dependsOn(":app:assembleDebug")
    dependsOn(":wear:assembleDebug")
}
