package com.mxt.anitrend.base.custom.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.search.SearchBar
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.fragment.FragmentBase
import com.mxt.anitrend.base.custom.presenter.CommonPresenter
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase
import com.mxt.anitrend.base.custom.viewmodel.ViewModelBase
import com.mxt.anitrend.base.interfaces.event.ResponseCallback
import com.mxt.anitrend.extension.KoinExt
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.ConfigurationUtil
import com.mxt.anitrend.util.IntentBundleUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.activity.index.MainActivity
import com.mxt.anitrend.view.activity.index.SearchActivity
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.util.Locale
import kotlin.jvm.JvmName

/**
 * Created by max on 2017/06/09.
 * Activity base <M type of data model, P extends CommonPresenter>
 */
abstract class ActivityBase<M, P : CommonPresenter> :
    AppCompatActivity(),
    Observer<M?>,
    CommonPresenter.AbstractPresenter<P>,
    ResponseCallback {

    protected lateinit var TAG: String

    protected var mSearchBar: SearchBar? = null
    private var viewModelRef: ViewModelBase<M>? = null

    protected val viewModel: ViewModelBase<M>?
        get() = viewModelRef

    internal var mBottomSheet: BottomSheetBase<*>? = null
    protected var mFragment: FragmentBase<*, *, *>? = null
    protected var mActionBar: ActionBar? = null
    protected lateinit var intentBundleUtil: IntentBundleUtil
    protected var mediaActionUtil: MediaActionUtil? = null

    protected var id: Long = 0

    protected var offScreenLimit = 3
    protected var disableNavigationStyle = false

    private var isClosing = false

    private var presenterRef: P? = null
    protected var configurationUtil: ConfigurationUtil? = null

    /**
     * Some activities may have custom themes and if that's the case
     * override this method and set your own theme style.
     *
     * @see ConfigurationUtil
     */
    protected open fun configureActivity() {
        if (configurationUtil == null) {
            configurationUtil = KoinExt.get(ConfigurationUtil::class.java)
        }
        configurationUtil?.onCreateAttach(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        TAG = javaClass.simpleName
        configureActivity()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        intentBundleUtil = IntentBundleUtil(intent)
        intentBundleUtil.checkIntentData(this)
    }

    /**
     * Enables edge-to-edge display and handles system window insets properly
     * for content that should extend behind system bars.
     * This method should be called before super.onCreate() to ensure proper setup.
     */
    protected fun enableEdgeToEdge() {
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // For light themes, ensure status bar icons are dark
        if (configurationUtil != null) {
            val settings = KoinExt.get(Settings::class.java)
            if (CompatUtil.isLightTheme(settings)) {
                WindowCompat.getInsetsController(window, window.decorView)
                    ?.setAppearanceLightStatusBars(true)
                WindowCompat.getInsetsController(window, window.decorView)
                    ?.setAppearanceLightNavigationBars(true)
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mSearchBar?.apply {
            val editText = findViewById<android.widget.EditText>(
                resources.getIdentifier("search_bar_text_input", "id", "com.google.android.material"),
            )
            editText?.doOnTextChanged { text, _, _, _ ->
                presenterRef?.notifyAllListeners(
                    text?.toString()?.lowercase(Locale.getDefault()).orEmpty(),
                    false,
                )
            }
            editText?.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val query = v.text.toString()
                    if (query.isNotEmpty()) {
                        val intent = Intent(this@ActivityBase, SearchActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        intent.putExtra(KeyUtil.arg_search, query)
                        CompatUtil.startRevealAnim(this@ActivityBase, this, intent)
                    }
                    true
                } else {
                    false
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val text = mSearchBar?.text
        if (!text.isNullOrEmpty()) {
            outState.putCharSequence(KEY_SEARCHVIEW_QUERY, text)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        if (savedInstanceState.containsKey(KEY_SEARCHVIEW_QUERY)) {
            val savedQuery = savedInstanceState.getCharSequence(KEY_SEARCHVIEW_QUERY)
            mSearchBar?.setText(savedQuery)
            presenterRef?.notifyAllListeners(savedQuery.toString().lowercase(Locale.getDefault()), false)
        }
    }

    /**
     * Set a [Toolbar] to act as the
     * [ActionBar] for this Activity window.
     *
     * When set to a non-null value the [getActionBar] method will return
     * an [ActionBar] object that can be used to control the given
     * toolbar as if it were a traditional window decor action bar. The toolbar's menu will be
     * populated with the Activity's options menu and the main_navigation button will be wired through
     * the standard [android.R.id.home] menu select action.
     *
     * In order to use a Toolbar within the Activity's window content the application
     * must not request the window feature [Window.FEATURE_ACTION_BAR].
     *
     * @param toolbar Toolbar to set as the Activity's action bar, or null to clear it
     */
    override fun setSupportActionBar(toolbar: Toolbar?) {
        super.setSupportActionBar(toolbar)
        setHomeUp()
    }

    private fun setHomeUp() {
        mActionBar = supportActionBar
        mActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun disableToolbarTitle() {
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    protected fun setTransparentStatusBar() {
        val window = window
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        val color = ContextCompat.getColor(this, android.R.color.transparent)
        window.statusBarColor = color
    }

    protected fun setTransparentStatusBarWithColor() {
        val window = window
        val color = ContextCompat.getColor(this, R.color.colorTransparent)
        window.statusBarColor = color
        window.navigationBarColor = color
    }

    /**
     * Helper method to setup window insets for edge-to-edge content.
     * This should be called on the root view of your layout after setContentView().
     *
     * @param rootView The root view of the activity's layout
     */
    protected fun setupEdgeToEdgeContent(rootView: View) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, windowInsets ->
            val systemBarsType = WindowInsetsCompat.Type.systemBars()

            // Get system bar insets
            val statusBarHeight = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val navigationBarHeight = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val leftInset = windowInsets.getInsets(systemBarsType).left
            val rightInset = windowInsets.getInsets(systemBarsType).right

            // Apply padding to the root view to avoid content overlap
            v.setPadding(leftInset, statusBarHeight, rightInset, navigationBarHeight)

            // Return the insets unchanged for child views to handle if needed
            windowInsets
        }
    }

    /**
     * Helper method to setup window insets for toolbars when using edge-to-edge.
     * This ensures the toolbar has proper top padding for the status bar.
     *
     * @param toolbar The toolbar view to apply insets to
     */
    protected fun setupToolbarInsets(toolbar: Toolbar) {
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, windowInsets ->
            val statusBarHeight = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top

            val currentPaddingLeft = v.paddingLeft
            val currentPaddingRight = v.paddingRight
            val currentPaddingBottom = v.paddingBottom

            v.setPadding(currentPaddingLeft, statusBarHeight, currentPaddingRight, currentPaddingBottom)

            windowInsets
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean = super.onCreateOptionsMenu(menu)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    protected fun requestPermissionIfMissing(permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            return true
        } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_PERMISSION)
        }
        return false
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on [requestPermissions].
     *
     * @param requestCode The request code passed in [requestPermissions].
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     * which is either [PackageManager.PERMISSION_GRANTED]
     * or [PackageManager.PERMISSION_DENIED]. Never null.
     * @see requestPermissions
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    onPermissionGranted(permissions[i])
                } else {
                    NotifyUtil.makeText(
                        this,
                        R.string.text_permission_required,
                        R.drawable.ic_warning_white_18dp,
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    /**
     * Called for each of the requested permissions as they are granted
     *
     * @param permission the current permission granted
     */
    protected open fun onPermissionGranted(permission: String) {
        Timber.tag(TAG).d("Granted %s", permission)
    }

    /**
     * Dispatch onPause() to fragments.
     */
    override fun onPause() {
        super.onPause()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        mediaActionUtil?.onPause(null)
        presenterRef?.onPause(null)
    }

    /**
     * Dispatch onResume() to fragments. Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are not resumed.
     */
    override fun onResume() {
        super.onResume()
        configurationUtil?.onResumeAttach(this)
        mediaActionUtil?.onResume(null)
        presenterRef?.onResume(null)
    }

    override fun onDestroy() {
        mediaActionUtil?.onDestroy()
        presenterRef?.onDestroy()
        super.onDestroy()
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    override fun onBackPressed() {
        if (mFragment?.onBackPress() == true) {
            return
        }
        if (mSearchBar?.visibility == View.VISIBLE) {
            mSearchBar?.visibility = View.GONE
            return
        }
        if (this is MainActivity && !isClosing) {
            NotifyUtil.makeText(
                this,
                R.string.text_confirm_exit,
                R.drawable.ic_home_white_24dp,
                Toast.LENGTH_SHORT,
            ).show()
            isClosing = true
            return
        }
        super.onBackPressed()
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    protected abstract fun onActivityReady()

    protected abstract fun updateUI()

    protected abstract fun makeRequest()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    @get:JvmName("presenterInstance")
    internal val presenter: P
        get() = requireNotNull(presenterRef)

    @Suppress("UNCHECKED_CAST")
    override fun getPresenter(): P = presenter

    fun setPresenter(presenter: P) {
        presenterRef = presenter
    }

    @Suppress("UNCHECKED_CAST")
    protected fun setViewModel(stateSupported: Boolean) {
        if (viewModelRef == null) {
            val provider = ViewModelProvider(this)
            viewModelRef = provider.get(ViewModelBase::class.java) as ViewModelBase<M>
            viewModelRef?.setContext(this)
            if (viewModelRef?.model?.hasActiveObservers() != true) {
                viewModelRef?.model?.observe(this, this)
            }
            if (stateSupported) {
                viewModelRef?.state = this
            }
        }
    }

    protected fun getModel(): M? = viewModelRef?.snapshot()

    /**
     * Called when the model state is changed.
     *
     * @param model The new data
     */
    override fun onChanged(model: M?) {
        Timber.tag(TAG).v("onChanged() from view model has received data")
    }

    override fun showError(error: String) {
        if (error.isNotEmpty()) {
            Timber.tag(TAG).w(error)
        }
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            NotifyUtil.createAlerter(
                this,
                getString(R.string.text_error_request),
                error,
                R.drawable.ic_warning_white_18dp,
                R.color.colorStateOrange,
                KeyUtil.DURATION_MEDIUM,
            )
        }
    }

    override fun showEmpty(message: String) {
        if (message.isNotEmpty()) {
            Timber.tag(TAG).v(message)
        }
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            NotifyUtil.createAlerter(
                this,
                getString(R.string.text_error_request),
                message,
                R.drawable.ic_warning_white_18dp,
                R.color.colorStateBlue,
                KeyUtil.DURATION_MEDIUM,
            )
        }
    }

    internal fun showBottomSheet() {
        mBottomSheet?.let { sheet ->
            sheet.show(supportFragmentManager, sheet.tag)
        }
    }

    companion object {
        private const val KEY_SEARCHVIEW_QUERY = "SEARCHVIEW_QUERY"
        protected const val REQUEST_PERMISSION = 102
    }
}
