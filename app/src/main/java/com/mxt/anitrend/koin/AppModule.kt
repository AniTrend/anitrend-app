package com.mxt.anitrend.koin

import android.app.NotificationManager
import android.content.Context
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.Target
import com.mxt.anitrend.R
import com.mxt.anitrend.analytics.AnalyticsLogging
import com.mxt.anitrend.analytics.contract.ISupportAnalytics
import com.mxt.anitrend.model.entity.MyObjectBox
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.presenter.fragment.MediaPresenter
import com.mxt.anitrend.presenter.widget.WidgetPresenter
import com.mxt.anitrend.util.NotificationUtil
import com.mxt.anitrend.util.Settings
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.glide.GlideImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinComponent
import org.koin.dsl.module

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

        factory {
            Settings(androidContext())
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
                    .usePlugin(HtmlPlugin.create())
                    .usePlugin(LinkifyPlugin.create())
                    .usePlugin(GlideImagesPlugin.create(Glide.with(androidContext())))
                    .usePlugin(GlideImagesPlugin.create(object : GlideImagesPlugin.GlideStore {
                        override fun cancel(target: Target<*>) {
                            Glide.with(androidContext()).clear(target)
                        }

                        override fun load(drawable: AsyncDrawable): RequestBuilder<Drawable> {
                            return Glide.with(androidContext()).load(drawable.destination)
                                    .transition(DrawableTransitionOptions.withCrossFade(250))
                                    .transform(
                                            CenterCrop(),
                                            RoundedCorners(androidContext().resources.getDimensionPixelSize(R.dimen.md_margin))
                                    )
                        }
                    })).build()
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

    @JvmStatic
    fun <T : Any> get(`class`: Class<T>): T = getKoin().get(
            `class`::class,
            null,
            null
    )
}