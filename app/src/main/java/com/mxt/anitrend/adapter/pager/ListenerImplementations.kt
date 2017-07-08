package com.mxt.anitrend.adapter.pager

import android.support.v4.app.Fragment
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.api.model.*
import com.mxt.anitrend.api.structure.ExternalLink
import com.mxt.anitrend.api.structure.FilterTypes
import com.mxt.anitrend.view.detail.fragment.*
import com.mxt.anitrend.view.index.fragment.*
import java.util.*

//region Fragment Page Listeners
class MangaPageListener(val model: Series) : GenericFragmentPagerAdapter.Listener {
    override fun getFragmentForItem(position: Int): Fragment? {
        when (position) {
            0 -> return MangaOverviewFragment.newInstance(model)
            1 -> return MangaLinksFragment.newInstance(model)
            2 -> return MangaExtrasFragment.newInstance(model)
            3 -> return MangaReviewFragment.newInstance(model.id)
            else -> return null
        }
    }
}

//endregion

//region Fragment State Page Listeners
class MyAnimeStatePageListener(val user_id: Int) : GenericFragmentStatePagerAdapter.Listener {
    override fun getFragmentForItem(position: Int): Fragment? {
        when (position) {
            0 -> return WatchingFragment.newInstance(user_id)
            1 -> return PlanToWatchFragment.newInstance(user_id)
            2 -> return OnHoldAnimeFragment.newInstance(user_id)
            3 -> return CompletedAnimeFragment.newInstance(user_id)
            4 -> return DroppedAnimeFragment.newInstance(user_id)
            else -> return null
        }
    }
}

class MyMangaStatePageListener(val user_id: Int) : GenericFragmentStatePagerAdapter.Listener {
    override fun getFragmentForItem(position: Int): Fragment? {
        when (position) {
            0 -> return ReadingFragment.newInstance(user_id)
            1 -> return PlanToReadFragment.newInstance(user_id)
            2 -> return OnHoldMangaFragment.newInstance(user_id)
            3 -> return CompletedMangaFragment.newInstance(user_id)
            4 -> return DroppedMangaFragment.newInstance(user_id)
            else -> return null
        }
    }
}


class MangaStatePageListener : GenericFragmentStatePagerAdapter.Listener {
    override fun getFragmentForItem(position: Int): Fragment? {
        when (position) {
            0 -> return MangaFragment.newInstance()
            1 -> return NewMangaFragment.newInstance()
            else -> return null
        }
    }
}

class HubStatePageListener : GenericFragmentStatePagerAdapter.Listener {
    override fun getFragmentForItem(position: Int): Fragment? {
        when (position) {
            0 -> return SuggestionFragment.newInstance()
            else -> return null
        }
    }
}

class HomeStatePageListener : GenericFragmentStatePagerAdapter.Listener {
    override fun getFragmentForItem(position: Int): Fragment? {
        when (position) {
            0 -> return ProgressFragment.newInstance(FilterTypes.ActivtyTypes[FilterTypes.ActivityType.PROGRESS.ordinal])
            1 -> return StatusFragment.newInstance(FilterTypes.ActivtyTypes[FilterTypes.ActivityType.STATUS.ordinal])
            2 -> return PublicStatusFragment.newInstance(FilterTypes.ActivtyTypes[FilterTypes.ActivityType.PUBIC_STATUS.ordinal])
            else -> return null
        }
    }
}


class AiringStatePageListener : GenericFragmentStatePagerAdapter.Listener {
    override fun getFragmentForItem(position: Int): Fragment? {
        when (position) {
            0 -> return AiringFragment.newInstance()
            1 -> {
                val externalLinks = ArrayList<ExternalLink>(1)
                externalLinks.add(ExternalLink(BuildConfig.FEEDS_LINK, null))
                return AnimeWatchFragment.newInstance(externalLinks)
            }
            else -> return null
        }
    }
}

class AnimeStatePageListener(val model: Series) : GenericFragmentStatePagerAdapter.Listener {
    override fun getFragmentForItem(position: Int): Fragment? {
        when (position) {
            0 -> return AnimeOverviewFragment.newInstance(model)
            1 -> return AnimeLinksFragment.newInstance(model)
            2 -> return AnimeWatchFragment.newInstance(model.external_links)
            3 -> return AnimeExtrasFragment.newInstance(model)
            4 -> return AnimeReviewsFragment.newInstance(model.id)
            else -> return null
        }
    }
}

class FavouriteStatePageListener(val model: Favourite) : GenericFragmentStatePagerAdapter.Listener {
    override fun getFragmentForItem(position: Int): Fragment? {
        when (position) {
            0 -> return FavouritesFragment.newInstance<Series>(model.anime, 0)
            1 -> return FavouritesFragment.newInstance<CharacterSmall>(model.character, 1)
            2 -> return FavouritesFragment.newInstance<Series>(model.manga, 2)
            3 -> return FavouritesFragment.newInstance<StaffSmall>(model.staff, 3)
            4 -> return FavouritesFragment.newInstance<StudioSmall>(model.studio, 4)
            else -> return null
        }
    }
}

class StaffStatePageListener(val model: Staff) : GenericFragmentStatePagerAdapter.Listener {
    override fun getFragmentForItem(position: Int): Fragment? {
        when (position) {
            0 -> return StaffOverviewFragment.newInstance(model)
            1 -> return StaffAnimeFragment.newInstance(model)
            2 -> return StaffMangaFragment.newInstance(model)
            3 -> return StaffOverviewFragment.newInstance(model)
            else -> return null
        }
    }
}

class ReviewStatePageListener : GenericFragmentStatePagerAdapter.Listener {
    override fun getFragmentForItem(position: Int): Fragment? {
        when (position) {
            0 -> return ReviewFragment.newInstance(FilterTypes.ReviewType.LATEST.ordinal)
            1 -> return ReviewFragment.newInstance(FilterTypes.ReviewType.POPULAR.ordinal)
            2 -> return ReviewFragment.newInstance(FilterTypes.ReviewType.NEED_LOVE.ordinal)
            3 -> return ReviewFragment.newInstance(FilterTypes.ReviewType.CONTROVERSIAL.ordinal)
            else -> return null
        }
    }
}

class SeasonStatePageListener : GenericFragmentStatePagerAdapter.Listener {
    override fun getFragmentForItem(position: Int): Fragment? {
        when (position) {
            0 -> return SeasonFragment.newInstance(FilterTypes.SeasonTitle.WINTER.ordinal)
            1 -> return SeasonFragment.newInstance(FilterTypes.SeasonTitle.SPRING.ordinal)
            2 -> return SeasonFragment.newInstance(FilterTypes.SeasonTitle.SUMMER.ordinal)
            3 -> return SeasonFragment.newInstance(FilterTypes.SeasonTitle.FALL.ordinal)
            else ->
                return null
        }
    }
}

class TrendingStatePageListener : GenericFragmentStatePagerAdapter.Listener {
    override fun getFragmentForItem(position: Int): Fragment? {
        when (position) {
            0 -> return TrendingFragment.newInstance()
            1 -> return NewAnimeFragment.newInstance()
            else -> return null
        }
    }
}

class UserBaseStatePageListener(val user: User) : GenericFragmentStatePagerAdapter.Listener {
    override fun getFragmentForItem(position: Int): Fragment? {
        when (position) {
            0 -> return UserAboutFragment.newInstance(user)
            1 -> return UserProgressFragment.newInstance(FilterTypes.ActivtyTypes[FilterTypes.ActivityType.PROGRESS.ordinal], user.display_name)
            2 -> return UserStatusFragment.newInstance(FilterTypes.ActivtyTypes[FilterTypes.ActivityType.STATUS.ordinal], user.display_name)
            else -> return null
        }
    }
}

class UserStatePageListener(val user: User) : GenericFragmentStatePagerAdapter.Listener {
    override fun getFragmentForItem(position: Int): Fragment? {
        when (position) {
            0 -> return UserAboutFragment.newInstance(user)
            1 -> return UserProgressFragment.newInstance(FilterTypes.ActivtyTypes[FilterTypes.ActivityType.PROGRESS.ordinal], user.display_name)
            2 -> return UserStatusFragment.newInstance(FilterTypes.ActivtyTypes[FilterTypes.ActivityType.STATUS.ordinal], user.display_name)
            //3 -> return UserStatusFragment.newInstance(FilterTypes.ActivtyTypes[FilterTypes.ActivityType.MESSAGE.ordinal], user.display_name)
            else -> return null
        }
    }
}


//endregion



