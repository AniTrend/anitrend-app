package com.mxt.anitrend.view.fragment.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.fragment.FragmentBase
import com.mxt.anitrend.binding.richMarkDown
import com.mxt.anitrend.binding.setImage
import com.mxt.anitrend.databinding.FragmentUserAboutBinding
import com.mxt.anitrend.extension.empty
import com.mxt.anitrend.extension.extras
import com.mxt.anitrend.extension.getCompatColorAttr
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.base.StatsRing
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import java.util.*

/**
 * Created by max on 2017/11/27.
 * about user fragment for the profile
 */

class UserOverviewFragment : FragmentBase<User, BasePresenter, User>() {

    private var _binding: FragmentUserAboutBinding? = null
    private val binding: FragmentUserAboutBinding
        get() = requireNotNull(_binding)
    private var model: User? = null

    private val userId by extras(KeyUtil.arg_id, 0L)
    private val userName by extras(KeyUtil.arg_userName, String.empty())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isMenuDisabled = true
        setPresenter(BasePresenter(requireContext()))
        setViewModel(true)
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null (which
     * is the default implementation).  This will be called between
     * [.onCreate] and [.onActivityCreated].
     *
     *
     *
     * If you return a View from here, you will later be called in
     * [.onDestroyView] when the view is being released.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentUserAboutBinding.inflate(inflater, container, false)
        binding.userAvatar.setOnClickListener(this)
        binding.userStatsContainer.setOnClickListener(this)
        binding.stateLayout.showLoading()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        makeRequest()
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    override fun updateUI() {
        val currentModel = model ?: return
        val viewBinding = _binding ?: return
        viewBinding.stateLayout.showContent()
        viewBinding.userAvatar.setImage(currentModel.avatar)
        viewBinding.userNameText.text = currentModel.name
        viewBinding.widgetStatusText.richMarkDown(currentModel.about)
        if (!presenter.settings.experimentalMarkdown) {
            viewBinding.widgetStatus.visibility = View.VISIBLE
            viewBinding.widgetStatus.setTextData(currentModel.about)
        } else {
            viewBinding.widgetStatus.visibility = View.GONE
        }

        viewBinding.userFollowStateWidget.setUserModel(currentModel)
        viewBinding.userAboutPanelWidget.setFragmentActivity(activity)
        viewBinding.userAboutPanelWidget.setUserId(currentModel.id, lifecycle)
        showRingStats()
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    override fun makeRequest() {
        val model = viewModel ?: return
        val ctx = context ?: return
        model.params.apply {
            if (userName.isNotBlank()) {
                putString(KeyUtil.arg_userName, userName)
            } else {
                remove(KeyUtil.arg_userName)
            }
            if (userId > 0) {
                putLong(KeyUtil.arg_id, userId)
            } else {
                remove(KeyUtil.arg_id)
            }
            putBoolean(KeyUtil.arg_asHtml, false)
        }
        model.requestData(KeyUtil.USER_OVERVIEW_REQ, ctx)
    }

    /**
     * Called when the model state is changed.
     *
     * @param model The new data
     */
    override fun onChanged(value: User?) {
        val viewBinding = _binding ?: return
        if (value != null) {
            this.model = value
            updateUI()
        } else {
            viewBinding.stateLayout.showError(
                context?.getCompatDrawable(R.drawable.ic_emoji_sweat),
                getString(R.string.layout_empty_response),
                getString(R.string.try_again),
            ) {
                viewBinding.stateLayout.showLoading()
                makeRequest()
            }
        }
    }

    /**
     * Called when the view previously created by [.onCreateView] has
     * been detached from the fragment.  The next time the fragment needs
     * to be displayed, a new view will be created.  This is called
     * after [.onStop] and before [.onDestroy].  It is called
     * *regardless* of whether [.onCreateView] returned a
     * non-null view.  Internally it is called after the view's state has
     * been saved but before it has been removed from its parent.
     */
    override fun onDestroyView() {
        binding.userAboutPanelWidget.onViewRecycled()
        super.onDestroyView()
        _binding = null
    }

    private fun generateStatsData(): List<StatsRing> {
        var userGenreStats: List<StatsRing> = ArrayList()
        val statistics = model?.statistics
        val genres = statistics?.anime?.genres
        if (statistics != null && genres != null && genres.isNotEmpty()) {
            val highestValue = genres.maxByOrNull { it.count }?.count ?: 0

            userGenreStats = genres
                .sortedByDescending { it.count }.map { genreStats ->
                    val percentage = genreStats.count.toFloat() / highestValue.toFloat() * 100f
                    StatsRing(percentage.toInt(), genreStats.genre, genreStats.count.toString())
                }.take(5)
        }

        return userGenreStats
    }

    private fun showRingStats() {
        val viewBinding = _binding ?: return
        context?.apply {
            val ringList = generateStatsData()
            if (ringList.size > 1) {
                viewBinding.userStats.setDrawBg(
                    CompatUtil.isLightTheme(presenter.settings),
                    getCompatColorAttr(R.attr.subtitleColor),
                )
                viewBinding.userStats.setData(ringList, 500)
            }
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param view The view that was clicked.
     */
    override fun onClick(v: View) {
        val viewBinding = _binding ?: return
        when (v.id) {
            R.id.user_avatar -> CompatUtil.imagePreview(v, model?.avatar?.large, R.string.image_preview_error_user_avatar)
            R.id.user_stats_container -> {
                val ringList = generateStatsData()
                if (ringList.size > 1) {
                    context?.apply {
                        viewBinding.userStats.setDrawBg(
                            CompatUtil.isLightTheme(presenter.settings),
                            getCompatColorAttr(R.attr.subtitleColor),
                        )
                        viewBinding.userStats.setData(ringList, 500)
                    }
                } else {
                    activity?.apply {
                        NotifyUtil.makeText(this, R.string.text_error_request, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    companion object {

        fun newInstance(args: Bundle): UserOverviewFragment {
            val fragment = UserOverviewFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
