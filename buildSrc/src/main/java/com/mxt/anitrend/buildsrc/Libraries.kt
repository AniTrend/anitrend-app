package com.mxt.anitrend.buildSrc

import com.mxt.anitrend.buildsrc.common.Versions

object Libraries {

    const val timber = "com.jakewharton.timber:timber:${Versions.timber}"

    const val treessence = "com.github.bastienpaulfr:Treessence:${Versions.treesSence}"

    const val junit = "junit:junit:${Versions.junit}"
    const val mockk = "io.mockk:mockk:${Versions.mockk}"

    const val betterLinkMovement = "me.saket:better-link-movement-method:${Versions.betterLinkMovement}"

    const val scalingImageView = "com.davemorrissey.labs:subsampling-scale-image-view-androidx:${Versions.scalingImageView}"

    const val prettyTime = "org.ocpsoft.prettytime:prettytime:${Versions.prettyTime}"

    object Repositories {
        const val jitPack = "https://www.jitpack.io"
        const val sonyaType = "https://oss.sonatype.org/content/repositories/snapshots"
        const val dependencyUpdates = "https://dl.bintray.com/pdvrieze/maven"
    }

    object Android {

        object Tools {
            private const val version = "4.1.3"
            const val buildGradle = "com.android.tools.build:gradle:${version}"
        }
    }

    object AndroidX {

        object Activity {
            private const val version = "1.2.0"
            const val activity = "androidx.activity:activity:$version"
            const val activityKtx = "androidx.activity:activity-ktx:$version"
        }

        object AppCompat {
            private const val version = "1.3.0-rc01"
            const val appcompat = "androidx.appcompat:appcompat:$version"
            const val appcompatResources = "androidx.appcompat:appcompat-resources:$version"
        }

        object Browser {
            private const val version = "1.3.0"
            const val browser = "androidx.browser:browser:$version"
        }

        object Collection {
            private const val version = "1.1.0"
            const val collection = "androidx.collection:collection:$version"
            const val collectionKtx = "androidx.collection:collection-ktx:$version"
        }

        object Core {
            private const val version = "1.5.0-rc01"
            const val core = "androidx.core:core:$version"
            const val coreKtx = "androidx.core:core-ktx:$version"

            object Animation {
                private const val version = "1.0.0-alpha02"
                const val animation = "androidx.core:core-animation:${version}"
                const val animationTest = "androidx.core:core-animation-testing:${version}"
            }
        }

        object ConstraintLayout {
            private const val version = "2.1.0-beta01"
            const val constraintLayout = "androidx.constraintlayout:constraintlayout:$version"
            const val constraintLayoutSolver = "androidx.constraintlayout:constraintlayout-solver:$version"
        }

        object Emoji {
            private const val version = "1.1.0"
            const val appCompat = "androidx.emoji:emoji-appcompat:$version"
        }

        object Fragment {
            private const val version = "1.3.2"
            const val fragment = "androidx.fragment:fragment:$version"
            const val fragmentKtx = "androidx.fragment:fragment-ktx:$version"
            const val test = "androidx.fragment:fragment-ktx:fragment-testing$version"
        }

        object Lifecycle {
            private const val version = "2.3.1"
            const val extensions = "androidx.lifecycle:lifecycle-extensions:2.2.0"
            const val runTimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:$version"
            const val liveDataKtx = "androidx.lifecycle:lifecycle-livedata-ktx:$version"
            const val viewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
            const val liveDataCoreKtx = "androidx.lifecycle:lifecycle-livedata-core-ktx:$version"
        }

        object Palette {
            private const val version = "1.0.0"
            const val palette = "androidx.palette:palette:$version"
            const val paletteKtx = "androidx.palette:palette-ktx:$version"
        }

        object Preference {
            private const val version = "1.1.1"
            const val preference = "androidx.preference:preference:$version"
            const val preferenceKtx = "androidx.preference:preference-ktx:$version"
        }

        object Recycler {
            private const val version = "1.2.0-rc01"
            const val recyclerView = "androidx.recyclerview:recyclerview:$version"

            object Selection {
                private const val version = "1.1.0-rc03"
                const val selection = "androidx.recyclerview:recyclerview-selection:$version"
            }
        }

        object Room {
            private const val version = "2.2.6"
            const val compiler = "androidx.room:room-compiler:$version"
            const val runtime = "androidx.room:room-runtime:$version"
            const val test = "androidx.room:room-testing:$version"
            const val ktx = "androidx.room:room-ktx:$version"
        }

        object StartUp {
            private const val version = "1.0.0"
            const val startUpRuntime = "androidx.startup:startup-runtime:$version"
        }

        object SwipeRefresh {
            private const val version = "1.1.0"
            const val swipeRefreshLayout = "androidx.swiperefreshlayout:swiperefreshlayout:$version"
        }

        object Test {
            private const val version = "1.3.0"
            const val core = "androidx.test:core:$version"
            const val coreKtx = "androidx.test:core-ktx:$version"
            const val runner = "androidx.test:runner:$version"
            const val rules = "androidx.test:rules:$version"

            object Espresso {
                private const val version = "3.3.0"
                const val core = "androidx.test.espresso:espresso-core:$version"
            }

            object Extension {
                private const val version = "1.1.2"
                const val junit = "androidx.test.ext:junit:$version"
                const val junitKtx = "androidx.test.ext:junit-ktx:$version"
            }
        }

        object Work {
            private const val version = "2.5.0"
            const val runtimeKtx = "androidx.work:work-runtime-ktx:$version"
            const val multiProcess = "androidx.work:work-multiprocess:$version"
            const val runtime = "androidx.work:work-runtime:$version"
            const val test = "androidx.work:work-test:$version"
        }
    }

    object AniTrend {

        object Emojify {
            private const val version = "develop-SNAPSHOT"//"1.6.0-alpha01"
            const val emojify = "com.github.anitrend:android-emojify:$version"
        }

        object Markdown {
			private const val version = "4b669387c7"
            const val markdown = "com.github.anitrend:support-markdown:${version}"
        }

        object Retrofit {
            private const val version = "0.11.0-beta01"
            const val graphQL = "com.github.anitrend:retrofit-graphql:${version}"
        }
    }

    object AirBnB {

        object Lottie {
            private const val version = "3.6.1"
            const val lottie = "com.airbnb.android:lottie:$version"
        }

        object Epoxy {
            private const val version = "4.0.0"
            const val epoxy = "com.airbnb.android:epoxy:$version"
            const val paging = "com.airbnb.android:epoxy-paging:$version"
            const val dataBinding = "com.airbnb.android:epoxy-databinding:$version"
            const val processor = "com.airbnb.android:epoxy-processor:$version"
        }

        object Paris {
            private const val version = "1.7.2"
            const val paris = "com.airbnb.android:paris:$version"
            /** if using annotations */
            const val processor = "com.airbnb.android:paris-processor:$version"
        }
    }

    object Blitz {
        private const val version = "1.0.9"
        const val blitz = "com.github.perfomer:blitz:$version"
    }

    object CashApp {
        object Copper {
            private const val version = "1.0.0"
            const val copper = "app.cash.copper:copper-flow:$version"
        }

        object Contour {
            private const val version = "1.1.0"
            const val contour = "app.cash.contour:contour:$version"
        }

        object Turbine {
            private const val version = "0.4.1"
            const val turbine = "app.cash.turbine:turbine:$version"
        }
    }

    object Chuncker {
        private const val version = "3.4.0"

        const val debug = "com.github.ChuckerTeam.Chucker:library:$version"
        const val release = "com.github.ChuckerTeam.Chucker:library-no-op:$version"
    }

    object Coil {
        private const val version = "1.1.1"
        const val coil = "io.coil-kt:coil:$version"
        const val base = "io.coil-kt:coil-base:$version"
        const val gif = "io.coil-kt:coil-gif:$version"
        const val svg = "io.coil-kt:coil-svg:$version"
        const val video = "io.coil-kt:coil-video:$version"
    }

    object Dropbox {
        private const val version = "4.0.0"
        const val store = "com.dropbox.mobile.store:store4:$version"
    }

    object Google {

        object Firebase {
            private const val version = "17.4.4"
            const val firebaseCore = "com.google.firebase:firebase-core:$version"

            object Analytics {
                private const val version = "18.0.2"
                const val analytics = "com.google.firebase:firebase-analytics:$version"
                const val analyticsKtx = "com.google.firebase:firebase-analytics-ktx:$version"
            }

            object Crashlytics {
                private const val version = "17.4.1"
                const val crashlytics = "com.google.firebase:firebase-crashlytics:$version"

                object Gradle {
                    private const val version = "2.5.2"
                    const val plugin = "com.google.firebase:firebase-crashlytics-gradle:$version"
                }
            }
        }

        object FlexBox {
            private const val version = "2.0.1"
            const val flexBox = "com.google.android:flexbox:$version"
        }

        object Material {
            private const val version = "1.3.0"
            const val material = "com.google.android.material:material:$version"
        }

        object Services {
            private const val version = "4.3.5"
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
            internal const val version = "1.4.31"
            const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"
            const val reflect = "org.jetbrains.kotlin:kotlin-reflect:$version"

            object Gradle {
                const val plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
            }

            object Android {
                const val extensions = "org.jetbrains.kotlin:kotlin-android-extensions:$version"
            }

            object Serialization {
                const val serialization = "org.jetbrains.kotlin:kotlin-serialization:$version"
            }
        }

        object KotlinX {
            object Coroutines {
                private const val version = "1.4.3"
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
        private const val version = "2.2.2"
        const val core = "org.koin:koin-core:$version"
        const val extension = "org.koin:koin-core-ext:$version"
        const val test = "org.koin:koin-test:$version"

        object AndroidX {
            const val scope = "org.koin:koin-androidx-scope:$version"
            const val fragment = "org.koin:koin-androidx-fragment:$version"
            const val viewModel = "org.koin:koin-androidx-viewmodel:$version"
            const val workManager = "org.koin:koin-androidx-workmanager:$version"
            const val compose = "org.koin:koin-androidx-compose:$version"
        }

        object Gradle {
            const val plugin = "org.koin:koin-gradle-plugin:$version"
        }
    }

    object Markwon {
        private const val version = "4.6.2"
        const val core = "io.noties.markwon:core:$version"
        const val editor = "io.noties.markwon:editor:$version"
        const val html = "io.noties.markwon:html:$version"
        const val image = "io.noties.markwon:image:$version"
        const val glide = "io.noties.markwon:image-glide:$version"
        const val coil = "io.noties.markwon:image-coil:$version"
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
        private const val version = "3.8.0"
        const val android = "org.mockito:mockito-android:$version"
        const val core = "org.mockito:mockito-core:$version"
    }

    object SmartTab {
        private const val version = "2.0.0@aar"
        const val layout = "com.ogaclejapan.smarttablayout:library:$version"
        const val utilities = "com.ogaclejapan.smarttablayout:utils-v4:$version"
    }

    object Square {

        object LeakCanary {
            private const val version = "2.7"
            const val leakCanary = "com.squareup.leakcanary:leakcanary-android:$version"
        }

        object Retrofit {
            private const val version = "2.9.0"
            const val retrofit = "com.squareup.retrofit2:retrofit:$version"
            const val gsonConverter =  "com.squareup.retrofit2:converter-gson:$version"
            const val xmlConverter =  "com.squareup.retrofit2:converter-simplexml:$version"
        }

        object OkHttp {
            private const val version = "4.9.1"
            const val okhttp = "com.squareup.okhttp3:okhttp:$version"
            /** Won't upgrade beyond this to support pre-lollipop */
            const val logging = "com.squareup.okhttp3:logging-interceptor:3.12.1"
            const val mockServer = "com.squareup.okhttp3:mockwebserver:$version"
        }
    }
}
