package com.mxt.anitrend.view.sheet

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase
import com.mxt.anitrend.base.custom.view.editor.ComposerWidget
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.databinding.BottomSheetComposerBinding
import com.mxt.anitrend.extension.hideKeyboard
import com.mxt.anitrend.extension.parcelable
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.repository.FeedRepository
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by max on 2017/12/13.
 */
class BottomSheetComposer :
    BottomSheetBase<FeedList>(),
    ItemClickListener<Any>,
    KoinComponent {
    private var binding: BottomSheetComposerBinding? = null
    private var composerWidget: ComposerWidget? = null

    @KeyUtil.RequestType
    private var requestType: Int = 0

    private var mBottomSheet: BottomSheetBase<*>? = null

    private var feedList: FeedList? = null
    private var user: UserBase? = null

    private val feedRepository: FeedRepository by inject()

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): BottomSheetComposer = BottomSheetComposer().apply {
            arguments = bundle
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            feedList = args.parcelable(KeyUtil.arg_model)
            requestType = args.getInt(KeyUtil.arg_request_type)
            user = args.parcelable(KeyUtil.arg_user_model)
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
                val currentFeed = feedList
                if (currentFeed != null) {
                    composerWidget?.setModel(currentFeed, KeyUtil.MUT_SAVE_TEXT_FEED)
                    composerWidget?.setText(currentFeed.text)
                } else {
                    composerWidget?.requestType = KeyUtil.MUT_SAVE_TEXT_FEED
                }
            }
            KeyUtil.MUT_SAVE_MESSAGE_FEED -> {
                toolbarTitle?.text = getString(mTitle, user?.name ?: "")
                val currentFeed = feedList
                if (currentFeed != null) {
                    composerWidget?.setText(currentFeed.text)
                    composerWidget?.setModel(currentFeed)
                }
                user?.let { composerWidget?.setModel(it, KeyUtil.MUT_SAVE_MESSAGE_FEED) }
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
                lifecycleScope.launch {
                    val success = when (requestType) {
                        KeyUtil.MUT_SAVE_TEXT_FEED -> {
                            val currentFeed = feedList
                            if (currentFeed != null) {
                                currentFeed.text = text
                                feedRepository.saveTextActivity(id = currentFeed.id, text = text, asHtml = false).isSuccess
                            } else {
                                feedRepository.saveTextActivity(id = null, text = text, asHtml = false).isSuccess
                            }
                        }
                        KeyUtil.MUT_SAVE_MESSAGE_FEED -> {
                            val currentFeed = feedList
                            val recipientId = user?.id ?: 0L
                            if (currentFeed != null) {
                                currentFeed.text = text
                                feedRepository.saveMessageActivity(id = currentFeed.id, message = text, recipientId = recipientId, asHtml = false).isSuccess
                            } else {
                                feedRepository.saveMessageActivity(id = null, message = text, recipientId = recipientId, asHtml = false).isSuccess
                            }
                        }
                        else -> false
                    }
                    onResult(success)
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
                    BottomSheetGiphy
                        .Builder()
                        .setTitle(R.string.title_bottom_sheet_giphy)
                        .build()
                        .also { (it as? BottomSheetGiphy)?.onGiphySelected = { giphy -> composerWidget?.insertGiphy(giphy) } }
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

        fun setUserActivity(feedList: FeedList): Builder {
            bundle.putParcelable(KeyUtil.arg_model, feedList)
            return this
        }

        fun setUserModel(userModel: UserBase): Builder {
            bundle.putParcelable(KeyUtil.arg_user_model, userModel)
            return this
        }
    }
}
