// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath(com.mxt.anitrend.buildsrc.Libraries.Android.Tools.buildGradle)
        classpath(com.mxt.anitrend.buildsrc.Libraries.JetBrains.Kotlin.Gradle.plugin)
        classpath(com.mxt.anitrend.buildsrc.Libraries.JetBrains.Kotlin.Serialization.serialization)
        classpath(com.mxt.anitrend.buildsrc.Libraries.Koin.Gradle.plugin)
        classpath(com.mxt.anitrend.buildsrc.Libraries.Google.Services.googleServices)
        classpath(com.mxt.anitrend.buildsrc.Libraries.Google.Firebase.Crashlytics.Gradle.plugin)
        classpath(com.mxt.anitrend.buildsrc.Libraries.ObjectBox.Gradle.plugin)
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven {
            url = java.net.URI(com.mxt.anitrend.buildsrc.Libraries.Repositories.jitPack)
        }
    }
}

tasks {
    val clean by registering(Delete::class) {
        delete(rootProject.buildDir)
    }
}

tasks.register("generateVersions") {
    doLast {
        val app = subprojects.firstOrNull { it.name == "app" }
        if (app != null) {
            println("Generate versions for ${app.name} -> ${app.projectDir}")
            val versionMeta = File(app.projectDir, ".meta/version.json")
            if (!versionMeta.exists()) {
                println("Creating versions meta file in ${versionMeta.absolutePath}")
                versionMeta.mkdirs()
            }
            println("Writing version information to ${versionMeta.absolutePath}")
            java.io.FileWriter(versionMeta).use { writer ->
                writer.write(
                    """
                        {
                            "code": ${com.mxt.anitrend.buildsrc.common.Versions.versionCode},
                            "migration": false,
                            "minSdk": ${com.mxt.anitrend.buildsrc.common.Versions.minSdk},
                            "releaseNotes": "",
                            "version": "${com.mxt.anitrend.buildsrc.common.Versions.versionName}",
                            "appId": "com.mxt.anitrend"
                        }
                    """.trimIndent()
                )
            }
        }
    }
}