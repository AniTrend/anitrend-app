plugins {
    `kotlin-dsl`
    `version-catalog`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    maven {
        setUrl("https://plugins.gradle.org/m2/")
    }
    maven {
        setUrl("https://www.jitpack.io")
    }
}

dependencies {
    /* Depend on the android gradle plugin, since we want to access it in our plugin */
    implementation(libs.android.gradle.plugin)

    /* Depend on the kotlin plugin, since we want to access it in our plugin */
    implementation(libs.jetbrains.kotlin.gradle)

    /* Depend on the default Gradle API's since we want to build a custom plugin */
    implementation(gradleApi())
    implementation(localGroovy())
}