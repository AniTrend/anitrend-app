package com.mxt.anitrend.koin

import android.app.NotificationManager
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.preference.PreferenceManager
import android.text.method.ScrollingMovementMethod
import android.text.util.Linkify
import co.anitrend.support.markdown.core.CoreDelimiterPlugin
import co.anitrend.support.markdown.html.AlignTagHandler
import co.anitrend.support.markdown.html.CenterTagHandler
import co.anitrend.support.markdown.image.ImageRenderPlugin
import co.anitrend.support.markdown.style.CenterPlugin
import co.anitrend.support.markdown.text.SpoilerPlugin
import co.anitrend.support.markdown.util.UtilityPlugin
import co.anitrend.support.markdown.video.WebMPlugin
import co.anitrend.support.markdown.video.YouTubePlugin
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.target.Target
import com.mxt.anitrend.R
import com.mxt.anitrend.analytics.AnalyticsLogging
import com.mxt.anitrend.analytics.contract.ISupportAnalytics
import com.mxt.anitrend.model.api.interceptor.AuthInterceptor
import com.mxt.anitrend.extension.getCompatColor
import com.mxt.anitrend.extension.getCompatColorAttr
import com.mxt.anitrend.model.entity.MyObjectBox
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.presenter.fragment.MediaPresenter
import com.mxt.anitrend.presenter.widget.WidgetPresenter
import com.mxt.anitrend.util.ConfigurationUtil
import com.mxt.anitrend.util.JobSchedulerUtil
import com.mxt.anitrend.util.NotificationUtil
import com.mxt.anitrend.util.Settings
import io.noties.markwon.Markwon
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.glide.GlideImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.movement.MovementMethodPlugin
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinComponent
import org.koin.dsl.module
import kotlin.math.roundToInt

object AppModule : KoinComponent {
    val appModule = module {
        single {
            MyObjectBox.builder()
                .androidContext(androidContext())
                .build()
        }

        single<ISupportAnalytics> {
            AnalyticsLogging(
                    context = androidContext(),
                    settings = get()
            )
        }

        single {
            JobSchedulerUtil(
                context = androidContext(),
                settings = get()
            )
        }

        single {
            PreferenceManager.getDefaultSharedPreferences(androidContext())
        }

        factory {
            Settings(
                context = androidContext(),
                sharedPreferences = get()
            )
        }

        factory {
            ConfigurationUtil()
        }

        factory {
            NotificationUtil(
                    androidContext(),
                    get(),
                    androidContext().getSystemService(
                            Context.NOTIFICATION_SERVICE
                    ) as NotificationManager?
            )
        }
    }

    val widgetModule = module {
        single {
            Markwon.builder(androidContext())
                .usePlugin(HtmlPlugin.create { plugin -> plugin
                    .addHandler(AlignTagHandler())
                    .addHandler(CenterTagHandler())
                })
                .usePlugin(CoreDelimiterPlugin.create(
                    androidContext()
                        .getCompatColorAttr(
                            R.attr.colorAccent
                        )
                ))
                .usePlugin(UtilityPlugin.create())
                //.usePlugin(CenterPlugin.create())
                .usePlugin(ImageRenderPlugin.create())
                .usePlugin(YouTubePlugin.create())
                .usePlugin(WebMPlugin.create())
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
                .usePlugin(TaskListPlugin.create(androidContext()))
                .usePlugin(SpoilerPlugin.create(
                    androidContext().getCompatColorAttr(R.attr.cardColor),
                    androidContext().getCompatColorAttr(R.attr.colorAccent)
                ))
                .usePlugin(GlideImagesPlugin.create(
                    object : GlideImagesPlugin.GlideStore {
                        override fun cancel(target: Target<*>) {
                            Glide.with(androidContext()).clear(target)
                        }

                        override fun load(drawable: AsyncDrawable): RequestBuilder<Drawable> {
                            return Glide.with(androidContext())
                                .load(drawable.destination)
                                .transform(
                                    RoundedCorners(
                                        androidContext()
                                            .resources
                                            .getDimensionPixelSize(R.dimen.md_margin)
                                    )
                                )
                        }
                    }
                )).build()
        }
        single {
            MarkwonEditor.create(
                get()
            )
        }
    }

    val presenterModule = module {
        factory {
            BasePresenter(androidContext())
        }
        factory {
            WidgetPresenter<Any>(androidContext())
        }
        factory {
            MediaPresenter(androidContext())
        }
    }

    val networkModule = module {
        factory {
            AuthInterceptor(
                settings = get()
            )
        }
    }

    @JvmStatic
    fun <T : Any> get(`class`: Class<T>): T = getKoin().get(
            `class`::class,
            null,
            null
    )
}