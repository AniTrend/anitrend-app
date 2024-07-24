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
    /** Butter Knife Libraries */
    implementation(libs.butterknife)
    kapt(libs.butterknife.compiler)

    /** Material Dialogs */
    implementation(libs.material.dialogs.core)

    /** Tap Target Prompt */
    implementation(libs.material.tap.target.prompt)

    /** Pretty Time */
    implementation(libs.prettyTime)

    /** Highly Customizable Video Player */
    implementation(libs.jiaozivideoplayer)

    /** Photo View */
    implementation(libs.photoview)

    /** On-boarding Experience */
    implementation(libs.onboarder)

    /** Charts */
    implementation(libs.mpandroidchart)

    /** About Library */
    implementation(libs.android.about.page)

    /** Multi Dex */
    implementation(libs.androidx.multidex)

    /** Material Search View */
    implementation(libs.materialsearchview)

    /** State Layout Library */
    implementation(libs.progresslayout)

    /** Event Bus Library */
    implementation(libs.eventbus)

    /** Alerter */
    implementation(libs.alerter)

    /** Stream */
    implementation(libs.stream)

    /** Circular Progress View */
    implementation(libs.circularprogressview)

    /** Txtmark */
    implementation(libs.txtmark)
}
