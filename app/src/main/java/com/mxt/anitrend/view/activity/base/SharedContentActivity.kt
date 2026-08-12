package com.mxt.anitrend.view.activity.base

import android.os.Bundle
import android.view.View
import androidx.core.app.ShareCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.spinner.IconArrayAdapter
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase
import com.mxt.anitrend.base.custom.view.editor.ComposerWidget
import com.mxt.anitrend.base.interfaces.event.BottomSheetListener
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.databinding.ActivityShareContentBinding
import com.mxt.anitrend.domain.feed.interactor.SaveFeedInteractor
import com.mxt.anitrend.extension.getCompatTintedDrawable
import com.mxt.anitrend.extension.hideKeyboard
import com.mxt.anitrend.ui.fragmentByTagOrNew
import com.mxt.anitrend.ui.model.FragmentItem
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.IntentBundleUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.markdown.MarkDownUtil
import com.mxt.anitrend.view.activity.CommonActivity
import com.mxt.anitrend.view.sheet.BottomSheetGiphy
import com.mxt.anitrend.view.sheet.buildComposerSaveRequest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SharedContentActivity :
    CommonActivity(),
    BottomSheetListener,
    ItemClickListener<Any> {

    private lateinit var binding: ActivityShareContentBinding
    private lateinit var toolbarBinding: com.mxt.anitrend.databinding.CustomSheetToolbarBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private var mBottomSheet: BottomSheetBase<*>? = null

    private val saveFeedInteractor: SaveFeedInteractor by inject()

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
            ) = Unit
        }

    private val indexIconMap =
        mutableMapOf(
            0 to R.drawable.ic_textsms_white_24dp,
            1 to R.drawable.ic_link_white_24dp,
            2 to R.drawable.ic_crop_original_white_24dp,
            3 to R.drawable.ic_youtube,
            4 to R.drawable.ic_slow_motion_video_white_24dp,
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Process share-intent deep links (previously ActivityBase.onCreate).
        val intentUtil = IntentBundleUtil(intent)
        intentUtil.checkIntentData(this)

        binding = ActivityShareContentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bottomSheetBehavior = BottomSheetBehavior.from(binding.designBottomSheet)
        toolbarBinding = binding.customSheetToolbar
        binding.sheetSharePostTypeApprove.setOnClickListener { getItemSelected() }

        // Bottom-sheet setup (previously onPostCreate).
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

        // Toolbar + composer setup (previously onActivityReady).
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
        binding.composerWidget.setListener(
            object : ComposerWidget.Listener {
                override fun onSubmit(
                    text: String,
                    @KeyUtil.RequestType requestType: Int,
                    onResult: (Boolean) -> Unit,
                ) {
                    val request = buildComposerSaveRequest(requestType, null, text)
                    if (request == null) {
                        onResult(false)
                        return
                    }
                    lifecycleScope.launch {
                        onResult(saveFeedInteractor(request) is MutationResult.Success)
                    }
                }
            },
        )

        // Share-intent content (previously updateUI).
        val reader: ShareCompat.IntentReader? = intentUtil.sharedIntent
        if (reader != null) {
            binding.sheetSharedResource.setText(reader.text)
            if (reader.text != reader.subject) {
                reader.subject?.let { subject ->
                    binding.composerWidget.setText(subject)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
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
        data: IndexedValue<Any>,
    ) {
        when (target.id) {
            R.id.insert_emoticon -> Unit
            R.id.insert_gif -> {
                mBottomSheet =
                    FragmentItem(fragment = BottomSheetGiphy::class.java)
                        .fragmentByTagOrNew(this@SharedContentActivity)
                        .also { it.onGiphySelected = { giphy -> binding.composerWidget.insertGiphy(giphy) } }
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
        data: IndexedValue<Any>,
    ) = Unit
}
