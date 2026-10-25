plugins {
    id("com.mxt.anitrend.plugin")
}

android {
    namespace = "com.mxt.anitrend"

    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }
}

dependencies {

    /** Pretty Time */
    implementation(libs.prettyTime)

    /** Media3 ExoPlayer */
    implementation(libs.androidx.media3.exoplayer)

    /** Event Bus Library */
    implementation(libs.eventbus)
}
