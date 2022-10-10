package com.mxt.anitrend.buildsrc

import com.mxt.anitrend.buildsrc.common.Versions

object Libraries {

    const val timber = "com.jakewharton.timber:timber:${Versions.timber}"

    const val treessence = "com.github.bastienpaulfr:Treessence:${Versions.treesSence}"

    const val junit = "junit:junit:${Versions.junit}"
    const val mockk = "io.mockk:mockk:${Versions.mockk}"
    const val hamcrest = "org.hamcrest:hamcrest-library:${Versions.hamcrest}"

    const val betterLinkMovement = "me.saket:better-link-movement-method:${Versions.betterLinkMovement}"

    const val prettyTime = "org.ocpsoft.prettytime:prettytime:${Versions.prettyTime}"

    object Repositories {
        const val jitPack = "https://www.jitpack.io"
        const val sonyaType = "https://oss.sonatype.org/content/repositories/snapshots"
        const val dependencyUpdates = "https://dl.bintray.com/pdvrieze/maven"
    }

    object Android {

        object Tools {
            private const val version = "7.3.0"
            const val buildGradle = "com.android.tools.build:gradle:$version"
        }
    }

    object AndroidX {

        object Activity {
            private const val version = "1.6.0-alpha05"
            const val activityKtx = "androidx.activity:activity-ktx:$version"
        }

        object AppCompat {
            private const val version = "1.6.0-alpha05"
            const val appcompat = "androidx.appcompat:appcompat:$version"
            const val appcompatResources = "androidx.appcompat:appcompat-resources:$version"
        }

        object Collection {
            private const val version = "1.2.0"
            const val collectionKtx = "androidx.collection:collection-ktx:$version"
        }

        object Core {
            private const val version = "1.8.0"
            const val core = "androidx.core:core:$version"
            const val coreKtx = "androidx.core:core-ktx:$version"
        }

        object ConstraintLayout {
            private const val version = "2.1.4"
            const val constraintLayout = "androidx.constraintlayout:constraintlayout:$version"
            const val constraintLayoutSolver = "androidx.constraintlayout:constraintlayout-solver:$version"
        }

        object Fragment {
            private const val version = "1.5.3"
            const val fragmentKtx = "androidx.fragment:fragment-ktx:$version"
            const val test = "androidx.fragment:fragment-ktx:fragment-testing$version"
        }

        object Lifecycle {
            private const val version = "2.5.0"
            const val extensions = "androidx.lifecycle:lifecycle-extensions:2.2.0"
            const val runTimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:$version"
            const val liveDataKtx = "androidx.lifecycle:lifecycle-livedata-ktx:$version"
            const val viewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
            const val liveDataCoreKtx = "androidx.lifecycle:lifecycle-livedata-core-ktx:$version"
        }

        object Preference {
            private const val version = "1.1.1"
            const val preference = "androidx.preference:preference:$version"
            const val preferenceKtx = "androidx.preference:preference-ktx:$version"
        }

        object StartUp {
            private const val version = "1.0.0"
            const val startUpRuntime = "androidx.startup:startup-runtime:$version"
        }

        object Test {
            private const val version = "1.3.0"
            const val core = "androidx.test:core:$version"
            const val coreKtx = "androidx.test:core-ktx:$version"
            const val runner = "androidx.test:runner:$version"
            const val rules = "androidx.test:rules:$version"

            object Espresso {
                private const val version = "3.4.0"
                const val core = "androidx.test.espresso:espresso-core:$version"
            }

            object Extension {
                private const val version = "1.1.3"
                const val junit = "androidx.test.ext:junit:$version"
                const val junitKtx = "androidx.test.ext:junit-ktx:$version"
            }
        }

        object Work {
            private const val version = "2.7.1"
            const val runtimeKtx = "androidx.work:work-runtime-ktx:$version"
            const val multiProcess = "androidx.work:work-multiprocess:$version"
            const val runtime = "androidx.work:work-runtime:$version"
            const val test = "androidx.work:work-test:$version"
        }
    }

    object AniTrend {

        object Emojify {
            private const val version = "1.6.0-beta01"
            const val emojify = "com.github.anitrend:android-emojify:$version"
        }

        object Markdown {
			private const val version = "0.12.1-alpha01"
            const val markdown = "com.github.AniTrend:support-markdown:$version"
        }

        object Retrofit {
            private const val version = "cf428d8430"
            const val graphQL = "com.github.anitrend:retrofit-graphql:$version"
        }
    }

    object CashApp {
        object Turbine {
            private const val version = "0.8.0"
            const val turbine = "app.cash.turbine:turbine:$version"
        }
    }

    object Google {

        object Firebase {
            private const val version = "17.4.4"
            const val firebaseCore = "com.google.firebase:firebase-core:$version"

            object Analytics {
                private const val version = "18.0.2"
                const val analyticsKtx = "com.google.firebase:firebase-analytics-ktx:$version"
            }

            object Crashlytics {
                private const val version = "18.2.11"
                const val crashlytics = "com.google.firebase:firebase-crashlytics:$version"

                object Gradle {
                    private const val version = "2.9.1"
                    const val plugin = "com.google.firebase:firebase-crashlytics-gradle:$version"
                }
            }
        }

        object FlexBox {
            private const val version = "2.0.1"
            const val flexBox = "com.google.android:flexbox:$version"
        }

        object Material {
            private const val version = "1.6.1"
            const val material = "com.google.android.material:material:$version"
        }

        object Services {
            private const val version = "4.3.13"
            const val googleServices = "com.google.gms:google-services:$version"
        }
    }

    object Glide {
        private const val version = "4.12.0"
        const val glide = "com.github.bumptech.glide:glide:$version"
        const val compiler = "com.github.bumptech.glide:compiler:$version"
    }

    object ObjectBox {
        private const val version = "2.9.1"
        const val android = "io.objectbox:objectbox-android:$version"
        const val processor = "io.objectbox:objectbox-processor:$version"

        object Gradle {
            const val plugin = "io.objectbox:objectbox-gradle-plugin:$version"
        }
    }

    object JetBrains {

        object Kotlin {
            internal const val version = "1.6.21"
            const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"
            const val reflect = "org.jetbrains.kotlin:kotlin-reflect:$version"

            object Gradle {
                const val plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
            }

            object Serialization {
                const val serialization = "org.jetbrains.kotlin:kotlin-serialization:$version"
            }
        }

        object KotlinX {
            object Coroutines {
                private const val version = "1.6.4"
                const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
                const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
                const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$version"
            }

            object Serialization {
                private const val version = "1.1.0"
                const val json = "org.jetbrains.kotlinx:kotlinx-serialization-json:$version"
            }
        }
    }

    object Koin {
        private const val version = "3.2.0"
        const val android = "io.insert-koin:koin-android:$version"
        const val core = "io.insert-koin:koin-core:$version"

        object AndroidX {
            const val workManager = "io.insert-koin:koin-androidx-workmanager:$version"
        }

        object Gradle {
            const val plugin = "io.insert-koin:koin-gradle-plugin:$version"
        }

        object Test {
            const val test = "io.insert-koin:koin-test:$version"
            const val testJUnit4 = "io.insert-koin:koin-test-junit4:$version"
        }
    }

    object Markwon {
        private const val version = "4.6.2"
        const val core = "io.noties.markwon:core:$version"
        const val editor = "io.noties.markwon:editor:$version"
        const val html = "io.noties.markwon:html:$version"
        const val image = "io.noties.markwon:image:$version"
        const val glide = "io.noties.markwon:image-glide:$version"
        const val parser = "io.noties.markwon:inline-parser:$version"
        const val linkify = "io.noties.markwon:linkify:$version"
        const val simpleExt = "io.noties.markwon:simple-ext:$version"
        const val syntaxHighlight = "io.noties.markwon:syntax-highlight:$version"

        object Extension {
            const val taskList = "io.noties.markwon:ext-tasklist:$version"
            const val strikeThrough = "io.noties.markwon:ext-strikethrough:$version"
            const val tables = "io.noties.markwon:ext-tables:$version"
            const val latex = "io.noties.markwon:ext-latex:$version"
        }
    }

    object Mockito {
        private const val version = "3.9.0"
        const val android = "org.mockito:mockito-android:$version"
        const val core = "org.mockito:mockito-core:$version"
    }

    object SmartTab {
        private const val version = "2.0.0@aar"
        const val layout = "com.ogaclejapan.smarttablayout:library:$version"
        const val utilities = "com.ogaclejapan.smarttablayout:utils-v4:$version"
    }

    object Square {

        object Retrofit {
            private const val version = "2.9.0"
            const val retrofit = "com.squareup.retrofit2:retrofit:$version"
            const val gsonConverter =  "com.squareup.retrofit2:converter-gson:$version"
            const val xmlConverter =  "com.squareup.retrofit2:converter-simplexml:$version"
        }

        object OkHttp {
            private const val version = "3.12.1"
            const val okhttp = "com.squareup.okhttp3:okhttp:$version"
            /** Won't upgrade beyond this to support pre-lollipop */
            const val logging = "com.squareup.okhttp3:logging-interceptor:$version"
            const val mockServer = "com.squareup.okhttp3:mockwebserver:$version"
        }
    }
}
