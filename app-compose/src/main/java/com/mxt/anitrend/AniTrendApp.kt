package com.mxt.anitrend

import android.app.Application
import com.mxt.anitrend.data.api.apiModule
import com.mxt.anitrend.data.auth.authModule
import com.mxt.anitrend.data.character.characterModule
import com.mxt.anitrend.data.favourite.favouriteModule
import com.mxt.anitrend.data.feed.feedModule
import com.mxt.anitrend.data.forum.threadModule
import com.mxt.anitrend.data.genre.genreModule
import com.mxt.anitrend.data.local.localModule
import com.mxt.anitrend.data.media.mediaModule
import com.mxt.anitrend.data.medialist.medialistModule
import com.mxt.anitrend.data.notification.notificationModule
import com.mxt.anitrend.data.onboarding.onboardingModule
import com.mxt.anitrend.data.profile.profileModule
import com.mxt.anitrend.data.review.reviewModule
import com.mxt.anitrend.data.schedule.scheduleModule
import com.mxt.anitrend.data.search.searchModule
import com.mxt.anitrend.data.social.socialModule
import com.mxt.anitrend.data.staff.staffModule
import com.mxt.anitrend.data.studio.studioModule
import com.mxt.anitrend.data.watchlist.watchListModule
import com.mxt.anitrend.util.NotificationUtil
import com.mxt.anitrend.util.WorkerScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AniTrendApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AniTrendApp)
            modules(listOf(apiModule, authModule, characterModule, favouriteModule, feedModule, threadModule, genreModule, mediaModule, medialistModule, notificationModule, onboardingModule, profileModule, reviewModule, scheduleModule, searchModule, socialModule, staffModule, studioModule, localModule, watchListModule))
        }
        NotificationUtil.createChannels(this)
        WorkerScheduler.scheduleAll(this)
    }
}
