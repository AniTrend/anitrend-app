package com.mxt.anitrend.base.custom.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntegerRes
import androidx.annotation.MenuRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.mxt.anitrend.R
import com.mxt.anitrend.analytics.contract.ISupportAnalytics
import com.mxt.anitrend.base.custom.presenter.CommonPresenter
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase
import com.mxt.anitrend.base.custom.viewmodel.ViewModelBase
import com.mxt.anitrend.base.interfaces.event.ActionModeListener
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.base.interfaces.event.ResponseCallback
import com.mxt.anitrend.extension.KoinExt
import com.mxt.anitrend.util.ActionModeUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.media.MediaActionUtil
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlin.jvm.JvmName

abstract class FragmentBase<M, P : CommonPresenter, VM> :
    Fragment(),
    View.OnClickListener,
    ActionModeListener,
    SharedPreferences.OnSharedPreferenceChangeListener,
    CommonPresenter.AbstractPresenter<P>,
    Observer<VM?>,
    ResponseCallback,
    ItemClickListener<M> {

    protected var isFilterableEnabled: Boolean = false
    protected var isPager: Boolean = false
    protected var isMenuDisabled: Boolean = false
    protected var isFeed: Boolean = false

    private val settings by inject<Settings>()
    private val analytics by inject<ISupportAnalytics>()

    @MenuRes
    private var inflateMenu: Int = 0
    protected var actionMode: ActionModeUtil<M>? = null
    private var viewModelRef: ViewModelBase<VM>? = null
    private var presenterRef: P? = null
    protected lateinit var mediaActionUtil: MediaActionUtil

    protected var snackbar: Snackbar? = null
    protected var mBottomSheet: BottomSheetBase<*>? = null

    @IntegerRes
    protected var mColumnSize: Int = 0

    val TAG: String = javaClass.simpleName

    protected val viewModel: ViewModelBase<VM>?
        get() = viewModelRef

    @get:JvmName("presenterInstance")
    val presenter: P
        get() = requireNotNull(presenterRef)

    override fun getPresenter(): P = presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let { host ->
            analytics.logCurrentScreen(host, TAG)
        }
    }

    abstract override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View?

    override fun onDestroyView() {
        super.onDestroyView()
        presenterRef?.onDestroy()
        if (this::mediaActionUtil.isInitialized) {
            mediaActionUtil.onDestroy()
        }
        actionMode = null
    }

    override fun onStart() {
        super.onStart()
        @Suppress("DEPRECATION")
        if (!isMenuDisabled) {
            setHasOptionsMenu(true)
        }
    }

    override fun onPause() {
        super.onPause()
        if (this::mediaActionUtil.isInitialized) {
            mediaActionUtil.onPause(null)
        }
        settings.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        if (this::mediaActionUtil.isInitialized) {
            mediaActionUtil.onResume(null)
        }
        settings.registerOnSharedPreferenceChangeListener(this)
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (inflateMenu != 0) {
            inflater.inflate(inflateMenu, menu)
        } else {
            inflater.inflate(R.menu.shared_menu, menu)
            menu.findItem(R.id.action_filter).isVisible = isFilterableEnabled
            menu.findItem(R.id.action_post).isVisible = isFeed
        }
    }

    fun setInflateMenu(@MenuRes inflateMenu: Int) {
        this.inflateMenu = inflateMenu
    }

    protected abstract fun updateUI()

    abstract fun makeRequest()

    open fun onBackPress(): Boolean {
        val isBackAllowed = actionMode?.selectedItems?.isNotEmpty() == true
        if (isBackAllowed) {
            actionMode?.clearSelection()
        }
        return isBackAllowed
    }

    override fun onClick(v: View) = Unit

    @Deprecated(
        "Do not attach a presenter in new fragments. Inject collaborators instead. " +
            "See AGENTS.md (ViewModel-first architecture) for the migration direction.",
        level = DeprecationLevel.ERROR,
    )
    fun setPresenter(presenter: P) {
        presenterRef = presenter
    }

    @Deprecated(
        "Use direct androidx.lifecycle.ViewModel subclasses with ViewModelProvider " +
            "(or later Koin by viewModel() / activityViewModel()) instead of the " +
            "legacy ViewModelBase wrapper. " +
            "See StaffOverviewFragment and StudioMediaFragment for proven fragment-side patterns.",
        level = DeprecationLevel.ERROR,
    )
    @Suppress("UNCHECKED_CAST")
    protected fun setViewModel(stateSupported: Boolean) {
        if (viewModelRef == null) {
            val provider = ViewModelProvider(this)
            viewModelRef = provider.get(ViewModelBase::class.java) as ViewModelBase<VM>
            viewModelRef?.setContext(requireContext())
            if (viewModelRef?.model?.hasActiveObservers() == false) {
                viewModelRef?.model?.observe(this, this)
            }
            if (stateSupported) {
                viewModelRef?.state = this
            }
        }
    }

    fun setFilterable(filterable: Boolean) {
        isFilterableEnabled = filterable
    }

    protected fun setActionModeEnabled(isEnabled: Boolean) {
        actionMode = ActionModeUtil(this, isEnabled)
    }

    override fun onSelectionChanged(actionMode: ActionMode, count: Int) {
        actionMode.title = getString(R.string.action_mode_selected, count)
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean = if (activity != null) {
        activity?.menuInflater?.inflate(R.menu.action_mode, menu)
        true
    } else {
        false
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean = false

    override fun onDestroyActionMode(mode: ActionMode) {
        actionMode?.clearSelection()
    }

    override fun showError(error: String) {
        if (!TextUtils.isEmpty(error)) {
            Timber.d(error)
        }
    }

    override fun showEmpty(message: String) {
        if (!TextUtils.isEmpty(message)) {
            Timber.i(message)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        if (key != null) {
            Timber.i(key)
        }
    }

    protected fun showBottomSheet() {
        activity?.let { host ->
            mBottomSheet?.show(host.supportFragmentManager, mBottomSheet?.tag)
        }
    }

    override fun onItemClick(target: View, data: IndexedValue<M>) = Unit

    override fun onItemLongClick(target: View, data: IndexedValue<M>) = Unit
}
