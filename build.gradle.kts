buildscript {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath(libs.android.gradle.plugin)
        classpath(libs.jetbrains.kotlin.gradle)
        classpath(libs.jetbrains.kotlin.serialization)
        classpath(libs.koin.gradle.plugin)
        classpath(libs.google.gsm.google.services)
        classpath(libs.google.firebase.crashlytics.gradle)
        classpath(libs.objectbox.gradle.plugin)
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven {
            setUrl("https://www.jitpack.io")
        }
        maven {
            setUrl("https://oss.sonatype.org/content/repositories/snapshots")
        }
    }

    configurations.all {
        handleConflicts(this@allprojects)
    }
}

tasks {
    val clean by registering(Delete::class) {
        delete(rootProject.layout.buildDirectory)
    }
}

fun Configuration.handleConflicts(project: Project): Unit = with(project) {
    resolutionStrategy.eachDependency {
        when (requested.group) {
            "org.jetbrains.kotlin" -> {
                when (requested.name) {
                    "kotlin-reflect",
                    "kotlin-stdlib",
                    "kotlin-stdlib-common",
                    "kotlin-stdlib-jdk8",
                    "kotlin-stdlib-jdk7" -> useVersion(libs.versions.jetbrains.kotlin.get())
                }
            }
        }
        if (requested.name == "kotlinx-serialization-json") {
            useTarget(libs.jetbrains.kotlinx.serialization.json)
        }
        if (requested.group == "com.google.android.material") {
            useTarget(libs.google.android.material)
        }
        if (requested.group == "com.jakewharton.timber") {
            useTarget(libs.timber)
        }
        if (requested.group == "androidx.startup") {
            useTarget(libs.androidx.startupRuntime)
        }
    }
}