package com.mxt.anitrend.view.activity.base

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.lifecycle.Lifecycle
import com.annimon.stream.IntPair
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.spinner.IconArrayAdapter
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.base.custom.consumer.BaseConsumer
import com.mxt.anitrend.base.interfaces.event.BottomSheetListener
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.databinding.ActivityShareContentBinding
import com.mxt.anitrend.extension.KoinExt
import com.mxt.anitrend.extension.getCompatTintedDrawable
import com.mxt.anitrend.extension.hideKeyboard
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.markdown.MarkDownUtil
import com.mxt.anitrend.view.sheet.BottomSheetGiphy
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by max on 2017/12/14.
 * share content intent activity
 */
class SharedContentActivity :
    ActivityBase<FeedList, BasePresenter>(),
    BottomSheetListener,
    BaseConsumer.onRequestModelChange<FeedList>,
    ItemClickListener<Any> {
    private lateinit var binding: ActivityShareContentBinding
    private lateinit var toolbarBinding: com.mxt.anitrend.databinding.CustomSheetToolbarBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private val bottomSheetCallback =
        object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(
                bottomSheet: View,
                newState: Int,
            ) {
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    when (newState) {
                        BottomSheetBehavior.STATE_HIDDEN -> finish()
                        BottomSheetBehavior.STATE_COLLAPSED -> onStateCollapsed()
                        BottomSheetBehavior.STATE_EXPANDED -> onStateExpanded()
                    }
                }
            }

            override fun onSlide(
                bottomSheet: View,
                slideOffset: Float,
            ) {
            }
        }

    private val indexIconMap =
        mutableMapOf(
            0 to R.drawable.ic_textsms_white_24dp,
            1 to R.drawable.ic_link_white_24dp,
            2 to R.drawable.ic_crop_original_white_24dp,
            3 to R.drawable.ic_youtube,
            4 to R.drawable.ic_slow_motion_video_white_24dp,
        )

    override fun configureActivity() {
        val settings = KoinExt.get(Settings::class.java)
        setTheme(
            if (CompatUtil.isLightTheme(settings)) {
                R.style.AppThemeLight_Translucent
            } else {
                R.style.AppThemeDark_Translucent
            },
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShareContentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bottomSheetBehavior = BottomSheetBehavior.from(binding.designBottomSheet)
        setPresenter(BasePresenter(applicationContext))
        toolbarBinding = binding.customSheetToolbar
        mSearchView = toolbarBinding.searchView
        binding.sheetSharePostTypeApprove.setOnClickListener { getItemSelected() }
        setViewModel(true)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        bottomSheetBehavior.peekHeight = CompatUtil.dipToPx(KeyUtil.PEEK_HEIGHT)
        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        val iconArrayAdapter =
            IconArrayAdapter(
                this,
                R.layout.adapter_spinner_item,
                R.id.spinner_text,
                CompatUtil.getStringList(this, R.array.post_share_types),
            )
        iconArrayAdapter.setIndexIconMap(indexIconMap)
        binding.sheetSharePostType.adapter = iconArrayAdapter
        onActivityReady()
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    override fun onActivityReady() {
        toolbarBinding.toolbarSearch.visibility = View.GONE
        toolbarBinding.toolbarTitle.setText(R.string.menu_title_new_activity_post)
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            toolbarBinding.toolbarState.setImageDrawable(
                getCompatTintedDrawable(R.drawable.ic_keyboard_arrow_down_grey_600_24dp),
            )
        } else {
            toolbarBinding.toolbarState.setImageDrawable(
                getCompatTintedDrawable(R.drawable.ic_close_grey_600_24dp),
            )
        }
        toolbarBinding.toolbarState.setOnClickListener {
            when (bottomSheetBehavior.state) {
                BottomSheetBehavior.STATE_EXPANDED ->
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                else ->
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
        binding.composerWidget.itemClickListener = this
        binding.composerWidget.lifecycle = lifecycle
        binding.composerWidget.requestType = KeyUtil.MUT_SAVE_TEXT_FEED
        updateUI()
    }

    override fun updateUI() {
        val reader: ShareCompat.IntentReader? = intentBundleUtil.sharedIntent
        if (reader != null) {
            binding.sheetSharedResource.setText(reader.text)
            if (reader.text != reader.subject) {
                reader.subject?.let { subject ->
                    binding.composerWidget.setText(subject)
                }
            }
        }
    }

    override fun makeRequest() {
    }

    override fun onStateCollapsed() {
        toolbarBinding.toolbarState.setImageDrawable(
            getCompatTintedDrawable(R.drawable.ic_close_grey_600_24dp),
        )
    }

    override fun onStateExpanded() {
        toolbarBinding.toolbarState.setImageDrawable(
            getCompatTintedDrawable(R.drawable.ic_keyboard_arrow_down_grey_600_24dp),
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    override fun onModelChanged(consumer: BaseConsumer<FeedList>) {
        if (consumer.requestMode == KeyUtil.MUT_SAVE_TEXT_FEED) {
            NotifyUtil
                .makeText(
                    this,
                    R.string.text_compose_success,
                    R.drawable.ic_insert_emoticon_white_24dp,
                    Toast.LENGTH_SHORT,
                ).show()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    fun getItemSelected() {
        val text = binding.sheetSharedResource.text.toString()

        @KeyUtil.ShareType val position = binding.sheetSharePostType.selectedItemPosition
        when (position) {
            KeyUtil.IMAGE_TYPE -> binding.composerWidget.setText(MarkDownUtil.convertImage(text))
            KeyUtil.LINK_TYPE -> binding.composerWidget.setText(MarkDownUtil.convertLink(text))
            KeyUtil.WEBM_TYPE -> binding.composerWidget.setText(MarkDownUtil.convertVideo(text))
            KeyUtil.YOUTUBE_TYPE -> binding.composerWidget.setText(MarkDownUtil.convertYoutube(text))
            KeyUtil.PLAIN_TYPE -> binding.composerWidget.setText(text)
        }
    }

    override fun onItemClick(
        target: View,
        data: IntPair<Any>,
    ) {
        when (target.id) {
            R.id.insert_emoticon -> Unit
            R.id.insert_gif -> {
                mBottomSheet =
                    BottomSheetGiphy
                        .Builder()
                        .setTitle(R.string.title_bottom_sheet_giphy)
                        .build()
                mBottomSheet?.let { sheet ->
                    sheet.show(supportFragmentManager, sheet.tag)
                }
            }
            R.id.widget_flipper -> hideKeyboard()
            else ->
                DialogUtil.createDialogAttachMedia(
                    target.id,
                    binding.composerWidget.editor,
                    this,
                )
        }
    }

    override fun onItemLongClick(
        target: View,
        data: IntPair<Any>,
    ) {
    }
}
