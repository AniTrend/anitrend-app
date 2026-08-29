package com.mxt.anitrend.view.fragment.detail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.markdown.MarkDownUtil
import com.mxt.anitrend.view.sheet.BottomSheetGiphy
import com.mxt.anitrend.view.sheet.buildComposerSaveRequest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SharedContentFragment :
    Fragment(),
    BottomSheetListener,
    ItemClickListener<Any> {

    private var _binding: ActivityShareContentBinding? = null
    private val binding get() = requireNotNull(_binding)

    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    private var bottomSheet: BottomSheetBase<*>? = null

    private val saveFeedInteractor: SaveFeedInteractor by inject()

    private val bottomSheetCallback =
        object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(
                bottomSheet: View,
                newState: Int,
            ) {
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    when (newState) {
                        BottomSheetBehavior.STATE_HIDDEN -> dismissShareSheet()
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = ActivityShareContentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val behavior = BottomSheetBehavior.from(binding.designBottomSheet)
        bottomSheetBehavior = behavior
        behavior.peekHeight = CompatUtil.dipToPx(KeyUtil.PEEK_HEIGHT)
        behavior.addBottomSheetCallback(bottomSheetCallback)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        binding.sheetSharePostTypeApprove.setOnClickListener { getItemSelected() }
        val iconArrayAdapter =
            IconArrayAdapter(
                requireContext(),
                R.layout.adapter_spinner_item,
                R.id.spinner_text,
                CompatUtil.getStringList(requireContext(), R.array.post_share_types),
            )
        iconArrayAdapter.setIndexIconMap(indexIconMap)
        binding.sheetSharePostType.adapter = iconArrayAdapter

        val toolbarBinding = binding.customSheetToolbar
        toolbarBinding.toolbarSearch.visibility = View.GONE
        toolbarBinding.toolbarTitle.setText(R.string.menu_title_new_activity_post)
        onStateExpanded()
        toolbarBinding.toolbarState.setOnClickListener {
            when (behavior.state) {
                BottomSheetBehavior.STATE_EXPANDED ->
                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                else -> behavior.state = BottomSheetBehavior.STATE_HIDDEN
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
                    viewLifecycleOwner.lifecycleScope.launch {
                        onResult(saveFeedInteractor(request) is MutationResult.Success)
                    }
                }
            },
        )

        val reader = ShareCompat.IntentReader(requireActivity())
        val sharedText = arguments?.getString(ARG_SHARED_TEXT) ?: reader.text
        val subject = arguments?.getString(ARG_SHARED_SUBJECT) ?: reader.subject
        binding.sheetSharedResource.setText(sharedText)
        if (sharedText != subject) {
            subject?.let { binding.composerWidget.setText(it) }
        }
    }

    /**
     * Dismisses the share composer by popping its NavController destination.
     *
     * The sheet is hosted as a destination in MainActivity's root nav graph, so
     * hide dismissal must go through the controller instead of the legacy
     * fragment-manager pop: the controller's back stack stays consistent and
     * the previous destination is restored underneath. The [runCatching] guard
     * is teardown safety: the behavior callback can fire while the entry is
     * already being removed, when the fragment is no longer attached to a
     * NavController host and [findNavController] would throw. The enclosing
     * STARTED lifecycle check already blocks dismissal after the fragment stops.
     */
    private fun dismissShareSheet() {
        runCatching { findNavController().popBackStack() }
    }

    override fun onStateCollapsed() {
        _binding?.customSheetToolbar?.toolbarState?.setImageDrawable(
            requireContext().getCompatTintedDrawable(R.drawable.ic_close_grey_600_24dp),
        )
    }

    override fun onStateExpanded() {
        _binding?.customSheetToolbar?.toolbarState?.setImageDrawable(
            requireContext().getCompatTintedDrawable(R.drawable.ic_keyboard_arrow_down_grey_600_24dp),
        )
    }

    private fun getItemSelected() {
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
                bottomSheet = BottomSheetGiphy().also { sheet ->
                    sheet.onGiphySelected = { giphy -> binding.composerWidget.insertGiphy(giphy) }
                }
                bottomSheet?.show(childFragmentManager, BottomSheetGiphy::class.java.simpleName)
            }
            R.id.widget_flipper -> requireActivity().hideKeyboard()
            else ->
                DialogUtil.createDialogAttachMedia(
                    target.id,
                    binding.composerWidget.editor,
                    requireContext(),
                )
        }
    }

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<Any>,
    ) = Unit

    override fun onDestroyView() {
        bottomSheetBehavior?.removeBottomSheetCallback(bottomSheetCallback)
        bottomSheetBehavior = null
        bottomSheet = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val ARG_SHARED_TEXT = "shared_text"
        const val ARG_SHARED_SUBJECT = "shared_subject"

        fun arguments(intent: Intent): Bundle = arguments(
            sharedText = intent.getStringExtra(Intent.EXTRA_TEXT),
            subject = intent.getStringExtra(Intent.EXTRA_SUBJECT),
        )

        fun arguments(
            sharedText: String?,
            subject: String?,
        ): Bundle = Bundle().apply {
            sharedText?.let { putString(ARG_SHARED_TEXT, it) }
            subject?.let { putString(ARG_SHARED_SUBJECT, it) }
        }
    }
}
