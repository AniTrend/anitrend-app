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

    /** Tap Target Prompt */
    implementation(libs.material.tap.target.prompt)

    /** Pretty Time */
    implementation(libs.prettyTime)

    /** Media3 ExoPlayer */
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)

    /** Photo View */
    implementation(libs.photoview)

    /** On-boarding Experience */
    implementation(libs.onboarder)

    /** Charts */
    implementation(libs.mpandroidchart)

    /** Material Search View */
    implementation(libs.materialsearchview)

    /** Event Bus Library */
    implementation(libs.eventbus)
}
