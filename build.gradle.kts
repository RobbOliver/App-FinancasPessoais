buildscript {
    dependencies {
        // AGP 9's built-in Kotlin support defaults to an older KGP baseline;
        // pin explicitly so it matches the Compose compiler / KSP versions below.
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
