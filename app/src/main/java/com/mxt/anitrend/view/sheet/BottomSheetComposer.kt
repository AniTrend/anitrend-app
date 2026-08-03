package com.mxt.anitrend.view.sheet

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase
import com.mxt.anitrend.base.custom.view.editor.ComposerWidget
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.databinding.BottomSheetComposerBinding
import com.mxt.anitrend.domain.feed.interactor.SaveFeedInteractor
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.extension.hideKeyboard
import com.mxt.anitrend.extension.parcelable
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.navigation.model.FeedComposerScreenParam
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by max on 2017/12/13.
 *
 * Feed composer bottom sheet for text and message activities. The sheet receives only
 * the immutable [FeedComposerScreenParam] (feed id, draft text, recipient identity) and
 * routes every save through [SaveFeedInteractor], which commits to the canonical feed
 * store only after a server success. The sheet never holds or mutates a parceled
 * legacy [FeedList].
 */
class BottomSheetComposer :
    BottomSheetBase<Unit>(),
    ItemClickListener<Any>,
    KoinComponent {
    private var binding: BottomSheetComposerBinding? = null
    private var composerWidget: ComposerWidget? = null

    @KeyUtil.RequestType
    private var requestType: Int = 0

    private var mBottomSheet: BottomSheetBase<*>? = null

    private var composerParam: FeedComposerScreenParam? = null

    private val saveFeedInteractor: SaveFeedInteractor by inject()

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): BottomSheetComposer = BottomSheetComposer().apply {
            arguments = bundle
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            composerParam = args.parcelable(KeyUtil.arg_model)
            requestType = args.getInt(KeyUtil.arg_request_type)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        binding = BottomSheetComposerBinding.inflate(layoutInflater)
        dialog.setContentView(requireNotNull(binding).root)
        bindToolbarViews(requireNotNull(binding).root)
        createBottomSheetBehavior(requireNotNull(binding).root)
        composerWidget = requireNotNull(binding).composerWidget
        return dialog
    }

    override fun onStart() {
        super.onStart()
        when (requestType) {
            KeyUtil.MUT_SAVE_TEXT_FEED -> {
                composerParam?.draftText?.let { composerWidget?.setText(it) }
                composerWidget?.requestType = KeyUtil.MUT_SAVE_TEXT_FEED
            }
            KeyUtil.MUT_SAVE_MESSAGE_FEED -> {
                toolbarTitle?.text = getString(mTitle, composerParam?.recipientName ?: "")
                composerParam?.draftText?.let { composerWidget?.setText(it) }
                composerWidget?.requestType = KeyUtil.MUT_SAVE_MESSAGE_FEED
            }
        }
        composerWidget?.itemClickListener = this
        composerWidget?.lifecycle = lifecycle
        composerWidget?.setListener(object : ComposerWidget.Listener {
            override fun onSubmit(
                text: String,
                @KeyUtil.RequestType requestType: Int,
                onResult: (Boolean) -> Unit,
            ) {
                val request = buildComposerSaveRequest(requestType, composerParam, text)
                if (request == null) {
                    onResult(false)
                    return
                }
                lifecycleScope.launch {
                    onResult(saveFeedInteractor(request) is MutationResult.Success)
                }
            }
        })
    }

    override fun onDestroyView() {
        composerWidget?.setListener(null)
        composerWidget?.onViewRecycled()
        mBottomSheet?.closeDialog()
        super.onDestroyView()
        binding = null
    }

    override fun onItemClick(
        target: View,
        data: IndexedValue<Any>,
    ) {
        when (target.id) {
            R.id.insert_emoticon -> Unit
            R.id.insert_gif -> {
                mBottomSheet =
                    BottomSheetGiphy().apply {
                        arguments =
                            Bundle().apply {
                                putInt(KeyUtil.arg_title, R.string.title_bottom_sheet_giphy)
                            }
                    }.also { it.onGiphySelected = { giphy -> composerWidget?.insertGiphy(giphy) } }
                activity?.let { host ->
                    mBottomSheet?.show(host.supportFragmentManager, mBottomSheet?.tag)
                }
            }
            R.id.widget_flipper -> activity?.hideKeyboard()
            else -> {
                context?.let {
                    val editor = composerWidget?.editor ?: return
                    DialogUtil.createDialogAttachMedia(target.id, editor, it)
                }
            }
        }
    }

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<Any>,
    ) = Unit

    class Builder : BottomSheetBuilder() {
        override fun build(): BottomSheetBase<*> = newInstance(bundle)

        fun setRequestMode(
            @KeyUtil.RequestType requestType: Int,
        ): Builder {
            bundle.putInt(KeyUtil.arg_request_type, requestType)
            return this
        }

        /**
         * Legacy compatibility bridge: extracts only the stable feed id and initial draft
         * text from a [FeedList] and stores them in the typed [FeedComposerScreenParam].
         * The entity itself is never parceled into the fragment bundle.
         */
        fun setUserActivity(feedList: FeedList): Builder {
            val current = bundle.parcelable<FeedComposerScreenParam>(KeyUtil.arg_model) ?: FeedComposerScreenParam()
            bundle.putParcelable(
                KeyUtil.arg_model,
                current.copy(
                    feedId = feedList.id,
                    draftText = feedList.text,
                ),
            )
            return this
        }

        /**
         * Typed record/UI-model bridge for the feed list edit path. Extracts only the
         * stable feed id and draft text from the immutable [FeedItemUiModel] rendered by
         * the store-backed feed adapters. Any recipient identity already present in the
         * bundle (set via [setUserModel]) is preserved. The model itself is never
         * parceled into the fragment bundle.
         */
        fun setUserActivity(feedItem: FeedItemUiModel): Builder {
            val current = bundle.parcelable<FeedComposerScreenParam>(KeyUtil.arg_model) ?: FeedComposerScreenParam()
            bundle.putParcelable(KeyUtil.arg_model, feedItem.toComposerParam(current))
            return this
        }

        /**
         * Legacy compatibility bridge: extracts only the recipient identity (id and name)
         * from a [UserBase] and stores them in the typed [FeedComposerScreenParam].
         * The entity itself is never parceled into the fragment bundle.
         */
        fun setUserModel(userModel: UserBase): Builder {
            val current = bundle.parcelable<FeedComposerScreenParam>(KeyUtil.arg_model) ?: FeedComposerScreenParam()
            bundle.putParcelable(
                KeyUtil.arg_model,
                current.copy(
                    recipientId = userModel.id,
                    recipientName = userModel.name,
                ),
            )
            return this
        }

        /**
         * Typed identity/draft contract for the composer. Prefer this over the legacy
         * entity bridges once callers can supply identity-only values.
         */
        fun setComposerParam(composerParam: FeedComposerScreenParam): Builder {
            bundle.putParcelable(KeyUtil.arg_model, composerParam)
            return this
        }
    }
}
