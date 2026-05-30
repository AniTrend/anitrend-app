plugins {
    id("com.mxt.anitrend.plugin")
    id("com.apollographql.apollo") version "4.4.3"
    id("com.google.devtools.ksp") version "2.3.7"
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.21"
}

android {
    namespace = "com.mxt.anitrend.compose"

    buildFeatures {
        compose = true
    }
}

apollo {
    service("anilist") {
        packageName.set("com.mxt.anitrend.data.graphql")
        generateAsInternal.set(true)
        schemaFile.set(file("src/main/graphql/com/mxt/anitrend/schema.graphqls"))
    }
}

dependencies {
    implementation(platform(libs.compose.bom))

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)

    implementation(libs.activity.compose)
    implementation(libs.navigation3.runtime)
    implementation(libs.navigation3.ui)
    implementation(libs.lifecycle.viewmodel.navigation3)

    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.koin.androidx.compose)

    implementation(libs.jetbrains.kotlinx.serialization.json)
    implementation(libs.markwon.core)

    ksp(libs.room.compiler)
}
