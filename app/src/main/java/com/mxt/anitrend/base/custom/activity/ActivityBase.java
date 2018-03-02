package com.mxt.anitrend.base.custom.activity;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import android.widget.Toolbar;

import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.mxt.anitrend.App;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.fragment.FragmentBase;
import com.mxt.anitrend.base.custom.presenter.CommonPresenter;
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase;
import com.mxt.anitrend.base.custom.viewmodel.ViewModelBase;
import com.mxt.anitrend.base.interfaces.event.ResponseCallback;
import com.mxt.anitrend.util.AnalyticsUtil;
import com.mxt.anitrend.util.ApplicationPref;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.IntentBundleUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.SeriesActionUtil;
import com.mxt.anitrend.view.activity.index.MainActivity;
import com.mxt.anitrend.view.activity.index.SearchActivity;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;


/**
 * Created by max on 2017/06/09.
 * Activity base <T type of data model, S extends CommonPresenter>
 */

public abstract class ActivityBase<M, P extends CommonPresenter> extends AppCompatActivity implements Observer<M>, CommonPresenter.AbstractPresenter<P>,
        ResponseCallback, MaterialSearchView.SearchViewListener, MaterialSearchView.OnQueryTextListener {

    protected String TAG;

    protected @Nullable @BindView(R.id.search_view) MaterialSearchView mSearchView;

    protected BottomSheetBase mBottomSheet;
    protected ViewModelBase<M> viewModel;
    protected FragmentBase mFragment;
    protected ActionBar mActionBar;
    protected IntentBundleUtil intentBundleUtil;
    protected SeriesActionUtil seriesActionUtil;

    protected long id;

    protected int offScreenLimit = 3;
    protected boolean disableNavigationStyle;
    protected static final int REQUEST_PERMISSION = 102;

    private boolean isClosing;

    private App application;
    private CommonPresenter presenter;

    private @StyleRes int style;

    /**
     * Some activities may have custom themes and if that's the case
     * override this method and set your own theme style, also if you wish
     * to apply the default navigation bar style for light themes
     * @see ActivityBase#setNavigationStyle()
     */
    protected void configureActivity() {
        setTheme((style = new ApplicationPref(this).getTheme()));
        setNavigationStyle();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        TAG = this.toString(); configureActivity();
        super.onCreate(savedInstanceState);
        intentBundleUtil = new IntentBundleUtil(getIntent(), this);
        AnalyticsUtil.logCurrentScreen(this, TAG);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(mSearchView != null) {
            mSearchView.setVoiceSearch(true);
            mSearchView.setOnQueryTextListener(this);
            mSearchView.setOnSearchViewListener(this);
            mSearchView.setCursorDrawable(R.drawable.material_search_cursor);
        }
    }

    /**
     * Changes the navigation bar color depending on the selected theme
     */
    protected void setNavigationStyle() {
        if(!disableNavigationStyle && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (style == R.style.AppThemeLight)
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            else
                getWindow().clearFlags(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
    }

    public App getApplicationBase() {
        if(application == null)
            application = ((App)getApplication());
        return application;
    }

    /**
     * Set a {@link Toolbar Toolbar} to act as the
     * {@link ActionBar} for this Activity window.
     * <p>
     * <p>When set to a non-null value the {@link #getActionBar()} method will return
     * an {@link ActionBar} object that can be used to control the given
     * toolbar as if it were a traditional window decor action bar. The toolbar's menu will be
     * populated with the Activity's options menu and the main_navigation button will be wired through
     * the standard {@link android.R.id#home home} menu select action.</p>
     * <p>
     * <p>In order to use a Toolbar within the Activity's window content the application
     * must not request the window feature
     * {@link Window#FEATURE_ACTION_BAR FEATURE_SUPPORT_ACTION_BAR}.</p>
     *
     * @param toolbar Toolbar to set as the Activity's action bar, or {@code null} to clear it
     */
    @Override
    public void setSupportActionBar(@Nullable android.support.v7.widget.Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
        setHomeUp();
    }

    private void setHomeUp() {
        if((mActionBar = getSupportActionBar()) != null)
            mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    public void disableToolbarTitle() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    protected void setTransparentStatusBar() {
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            int color = ContextCompat.getColor(this, android.R.color.transparent);
            window.setStatusBarColor(color);
        }
    }

    protected void setTransparentStatusBarWithColor() {
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            int color = ContextCompat.getColor(this, R.color.colorTransparent);
            window.setStatusBarColor(color);
            window.setNavigationBarColor(color);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            super.onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    protected boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        else if (ContextCompat.checkSelfPermission(this,
                permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
            ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_PERMISSION);
        return false;
    }

    /**
     * Check to see if activity is still alive
     * <br/>
     *
     * @return true if the activity is still valid otherwise false
     */
    protected boolean isAlive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            return !isFinishing() || !isDestroyed();
        return !isDestroyed();
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    NotifyUtil.makeText(this, R.string.completed_success, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Granted " + permissions[i]);
                }
                else
                    NotifyUtil.makeText(this, R.string.text_permission_required, R.drawable.ic_warning_white_18dp, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if(seriesActionUtil != null)
            seriesActionUtil.onPause(null);
        if(presenter != null)
            presenter.onPause(null);
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if(seriesActionUtil != null)
            seriesActionUtil.onResume(null);
        if(presenter != null)
            presenter.onResume(null);
    }

    @Override
    protected void onDestroy() {
        if(seriesActionUtil != null)
            seriesActionUtil.onDestroy();
        if(presenter != null)
            presenter.onDestroy();
        super.onDestroy();
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        if(mFragment != null && mFragment.onBackPress())
            return;
        if(mSearchView != null && mSearchView.isSearchOpen()) {
            mSearchView.closeSearch();
            return;
        } if(this instanceof MainActivity && !isClosing) {
            NotifyUtil.makeText(this, R.string.text_confirm_exit, R.drawable.ic_home_white_24dp, Toast.LENGTH_SHORT).show();
            isClosing = true;
            return;
        }
        super.onBackPressed();
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    protected abstract void onActivityReady();

    protected abstract void updateUI();

    protected abstract void makeRequest();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd) && mSearchView != null)
                    mSearchView.setQuery(searchWrd, false);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressWarnings("unchecked")
    @Override
    public P getPresenter() {
        return (P) presenter;
    }

    public void setPresenter(CommonPresenter presenter) {
        this.presenter = presenter;
    }

    protected ViewModelBase<M> getViewModel() {
        return viewModel;
    }

    @SuppressWarnings("unchecked")
    protected void setViewModel(boolean stateSupported) {
        if(viewModel == null) {
            viewModel = ViewModelProviders.of(this).get(ViewModelBase.class);
            viewModel.setContext(this);
            if(!viewModel.getModel().hasActiveObservers())
                viewModel.getModel().observe(this, this);
            if(stateSupported)
                viewModel.setState(this);
        }
    }

    /**
     * Called when the model state is changed.
     *
     * @param model The new data
     */
    @Override
    public void onChanged(@Nullable M model) {
        Log.i(TAG, "onChanged() from view model has received data");
    }

    @Override
    public void showError(String error) {
        if(!TextUtils.isEmpty(error))
            Log.e(TAG, error);
        if(isAlive()) {
            AnalyticsUtil.reportException(TAG, error);
            NotifyUtil.createAlerter(this, getString(R.string.text_error_request), error,
                    R.drawable.ic_warning_white_18dp, R.color.colorStateOrange,
                    KeyUtils.DURATION_MEDIUM);
        }

    }

    @Override
    public void showEmpty(String message) {
        if(!TextUtils.isEmpty(message))
            Log.d(TAG, message);
        if (isAlive()) {
            NotifyUtil.createAlerter(this, getString(R.string.text_error_request), message,
                    R.drawable.ic_warning_white_18dp, R.color.colorStateBlue,
                    KeyUtils.DURATION_MEDIUM);
        }
    }

    protected void showBottomSheet() {
        mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());
    }

    /**
     * Called when the user submits the query. This could be due to a key press on the
     * keyboard or due to pressing a submit button.
     * The listener can override the standard behavior by returning true
     * to indicate that it has handled the submit request. Otherwise return false to
     * let the SearchView handle the submission by launching any associated intent.
     *
     * @param query the query text that is to be submitted
     * @return true if the query has been handled by the listener, false to let the
     * SearchView perform the default action.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        if(!TextUtils.isEmpty(query)) {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(KeyUtils.arg_search_query, query);
            CompatUtil.startRevealAnim(this, mSearchView, intent);
            Bundle bundle = new Bundle();
            bundle.putString(KeyUtils.arg_search_query, query);
            AnalyticsUtil.logEvent(getApplicationContext(), KeyUtils.arg_search_query, bundle);
            return true;
        }
        NotifyUtil.makeText(this, R.string.text_search_empty, Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * Called when the query text is changed by the user.
     *
     * @param newText the new content of the query text field.
     * @return false if the SearchView should perform the default action of showing any
     * suggestions if available, true if the action was handled by the listener.
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        if(presenter != null && !TextUtils.isEmpty(newText))
            presenter.notifyAllListeners(newText.toLowerCase(Locale.getDefault()), false);
        return false;
    }

    @Override
    public void onSearchViewShown() {

    }

    @Override
    public void onSearchViewClosed() {
        if(presenter != null)
            presenter.notifyAllListeners("", false);
    }
}
