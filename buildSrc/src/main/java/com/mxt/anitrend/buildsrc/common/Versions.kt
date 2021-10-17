package com.mxt.anitrend.buildsrc.common

object Versions {

    private const val major = 1
    private const val minor = 8
    private const val patch = 9
    private const val candidate = 0

    const val compileSdk = 30
    const val targetSdk = 30
    const val minSdk = 17

     /**
      * **RR**_X.Y.Z_
      * > **RR** reserved for build flavours and **X.Y.Z** follow the [versionName] convention
      */
    const val versionCode = major.times(10_000) +
             minor.times(1000) +
             patch.times(100) +
             candidate.times(10)

    /**
     * Naming schema: X.Y.Z-variant##
     * > **X**(Major).**Y**(Minor).**Z**(Patch)
     */
    val versionName = when {
        candidate > 0 -> "$major.$minor.$patch-rc$candidate"
        else -> "$major.$minor.$patch"
    }

    const val mockk = "1.11.0"
    const val junit = "4.13.2"

    const val timber = "4.7.1"
    const val treesSence = "1.0.4"

    const val prettyTime = "4.0.4.Final"
    const val scalingImageView = "3.10.0"

    const val betterLinkMovement = "2.2.0"

    const val butterKnife = "10.2.3"
    const val multidex = "2.0.1"
    const val materialDialogs = "0.9.6.0"
    const val tapTarget = "3.1.0"
    const val jiaoziVideoPlayer = "7.6.0"
    const val photoView = "2.3.0"
    const val onboarder = "1.0.4"
    const val mpAndroidChart = "3.1.0"
    const val aboutPage = "1.2.5"
    const val materialSearchView = "1.4.15"
    const val progressLayout = "1.2.0"
    const val eventBus = "3.2.0"
    const val alerter = "7.0.1"
    const val stream = "1.2.2"
    const val circularProgressView = "2.5.0"
    const val hamcrest = "2.2"
    const val txtmark = "0.16"
}
