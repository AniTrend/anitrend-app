package com.mxt.anitrend.base.custom.sheet

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Lifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.search.MaterialSearchView
import com.mxt.anitrend.base.interfaces.event.BottomSheetChoice
import com.mxt.anitrend.base.interfaces.event.BottomSheetListener
import com.mxt.anitrend.base.interfaces.event.ISearchDelegate
import com.mxt.anitrend.base.interfaces.event.ResponseCallback
import com.mxt.anitrend.extension.getCompatTintedDrawable
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.KeyUtil
import org.greenrobot.eventbus.EventBus
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * Created by max on 2017/11/02.
 * Custom bottom sheet base implementation
 */
abstract class BottomSheetBase<T> :
    BottomSheetDialogFragment(),
    BottomSheetListener,
    ResponseCallback {

    protected var toolbarTitle: TextView? = null
    protected var toolbarState: ImageButton? = null
    protected var toolbarSearch: ImageButton? = null
    protected var searchView: MaterialSearchView? = null

    protected var mSearchDelegate: ISearchDelegate? = null

    protected var bottomSheetChoice: BottomSheetChoice? = null

    @StringRes
    protected var mTitle: Int = 0

    @StringRes
    protected var mText: Int = 0

    @StringRes
    protected var mPositive: Int = 0

    @StringRes
    protected var mNegative: Int = 0
    protected var searchQuery: String? = null

    protected val presenter by inject<BasePresenter>()
    protected var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    protected var bottomSheetCallback: BottomSheetBehavior.BottomSheetCallback? =
        object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    return
                }
                try {
                    when (newState) {
                        BottomSheetBehavior.STATE_HIDDEN -> dismiss()
                        BottomSheetBehavior.STATE_COLLAPSED -> onStateCollapsed()
                        BottomSheetBehavior.STATE_EXPANDED -> onStateExpanded()
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Toolbar presentation state (string-resource ids), not navigation identity:
        // intentionally kept on the legacy bundle channel like other presentation reads.
        arguments?.let { args ->
            mTitle = args.getInt(KeyUtil.arg_title)
            mText = args.getInt(KeyUtil.arg_text)
            mPositive = args.getInt(KeyUtil.arg_positive_text)
            mNegative = args.getInt(KeyUtil.arg_negative_text)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = super.onCreateDialog(savedInstanceState)

    override fun onStart() {
        super.onStart()
        toolbarTitle?.setText(mTitle)
        val ctx = context
        val closeIcon = ctx?.getCompatTintedDrawable(R.drawable.ic_close_grey_600_24dp)
        bottomSheetBehavior?.apply {
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
        }
        toolbarState?.setImageDrawable(closeIcon)
        toolbarState?.setOnClickListener {
            closeDialog()
        }
        toolbarSearch?.setImageDrawable(ctx?.getCompatTintedDrawable(R.drawable.ic_search_grey_600_24dp))
        toolbarSearch?.setOnClickListener {
            searchView?.showSearch()
        }
        searchView?.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                mSearchDelegate?.onSearchSubmitted(query)
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                mSearchDelegate?.onQueryChanged(newText)
                return false
            }
        })
        searchView?.setOnSearchViewListener(object : MaterialSearchView.SearchViewListener {
            override fun onSearchViewShown() {
                mSearchDelegate?.onSearchShown()
            }
            override fun onSearchViewClosed() {
                mSearchDelegate?.onSearchClosed()
            }
        })
    }

    override fun onStop() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        super.onStop()
    }

    protected fun createBottomSheetBehavior(contentView: View) {
        val parentView = contentView.parent as? View ?: return
        val layoutParams = parentView.layoutParams as? CoordinatorLayout.LayoutParams ?: return
        val coordinatorBehavior = layoutParams.behavior as? BottomSheetBehavior<*> ?: return

        bottomSheetBehavior = coordinatorBehavior
        bottomSheetBehavior?.skipCollapsed = true
        bottomSheetBehavior?.isHideable = true
        bottomSheetCallback?.let { callback ->
            bottomSheetBehavior?.addBottomSheetCallback(callback)
        }
    }

    protected fun bindToolbarViews(rootView: View) {
        toolbarTitle = rootView.findViewById(R.id.toolbar_title)
        toolbarState = rootView.findViewById(R.id.toolbar_state)
        toolbarSearch = rootView.findViewById(R.id.toolbar_search)
    }

    fun closeDialog(): Boolean {
        if (bottomSheetBehavior?.state != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            return true
        }
        return false
    }

    override fun onDestroyView() {
        bottomSheetCallback?.let { callback ->
            bottomSheetBehavior?.removeBottomSheetCallback(callback)
        }
        super.onDestroyView()
        bottomSheetCallback = null
    }

    override fun onStateCollapsed() {
        toolbarState?.setImageDrawable(
            context?.getCompatTintedDrawable(R.drawable.ic_close_grey_600_24dp),
        )
    }

    override fun onStateExpanded() {
        toolbarState?.setImageDrawable(context?.getCompatTintedDrawable(R.drawable.ic_close_grey_600_24dp))
    }

    abstract class BottomSheetBuilder {
        protected val bundle: Bundle = Bundle()

        abstract fun build(): BottomSheetBase<*>

        fun buildWithCallback(bottomSheetChoice: BottomSheetChoice): BottomSheetBase<*> {
            val bottomSheetBase = build()
            bottomSheetBase.bottomSheetChoice = bottomSheetChoice
            return bottomSheetBase
        }

        fun setTitle(@StringRes title: Int): BottomSheetBuilder {
            bundle.putInt(KeyUtil.arg_title, title)
            return this
        }

        fun setPositiveText(@StringRes positiveText: Int): BottomSheetBuilder {
            bundle.putInt(KeyUtil.arg_positive_text, positiveText)
            return this
        }

        fun setNegativeText(@StringRes negativeText: Int): BottomSheetBuilder {
            bundle.putInt(KeyUtil.arg_negative_text, negativeText)
            return this
        }
    }

    override fun showError(error: String) {
        Timber.e(error)
    }

    override fun showEmpty(message: String) {
        Timber.d(message)
    }
}
