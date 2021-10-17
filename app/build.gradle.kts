import com.mxt.anitrend.buildsrc.common.Versions

plugins {
    id("com.mxt.anitrend.plugin")
}

dependencies {
    /** Butter Knife Libraries */
    implementation("com.jakewharton:butterknife:${Versions.butterKnife}")
    kapt("com.jakewharton:butterknife-compiler:${Versions.butterKnife}")

    /** Material Dialogs */
    implementation("com.afollestad.material-dialogs:core:${Versions.materialDialogs}")

    /** Tap Target Prompt */
    implementation("uk.co.samuelwall:material-tap-target-prompt:${Versions.tapTarget}")

    /** Pretty Time */
    implementation("org.ocpsoft.prettytime:prettytime:${Versions.prettyTime}")

    /** Highly Customizable Video Player */
    implementation("cn.jzvd:jiaozivideoplayer:${Versions.jiaoziVideoPlayer}")

    /** Photo View */
    implementation("com.github.chrisbanes:PhotoView:${Versions.photoView}")

    /** On-boarding Experience */
    implementation("com.codemybrainsout.onboarding:onboarder:${Versions.onboarder}")

    /** Charts */
    implementation("com.github.PhilJay:MPAndroidChart:v${Versions.mpAndroidChart}")

    /** About Library */
    implementation("com.github.medyo:android-about-page:${Versions.aboutPage}")

    /** Multi Dex */
    implementation("androidx.multidex:multidex:${Versions.multidex}")

    /** Material Search View */
    implementation("com.github.ma-myair:MaterialSearchView:${Versions.materialSearchView}")

    /** State Layout Library */
    implementation("com.github.wax911:ProgressLayout:${Versions.progressLayout}")

    /** Event Bus Library */
    implementation("org.greenrobot:eventbus:${Versions.eventBus}")

    /** Alerter */
    implementation("com.tapadoo.android:alerter:${Versions.alerter}")

    /** Stream */
    implementation("com.annimon:stream:${Versions.stream}")

    /** Circular Progress View */
    implementation("com.github.rahatarmanahmed:circularprogressview:${Versions.circularProgressView}")

    /** Txtmark */
    implementation("es.nitaur.markdown:txtmark:${Versions.txtmark}")
}