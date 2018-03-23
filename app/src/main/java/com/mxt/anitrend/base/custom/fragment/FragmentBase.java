package com.mxt.anitrend.base.custom.fragment;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.presenter.CommonPresenter;
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase;
import com.mxt.anitrend.base.custom.viewmodel.ViewModelBase;
import com.mxt.anitrend.base.interfaces.event.ActionModeListener;
import com.mxt.anitrend.base.interfaces.event.ItemClickListener;
import com.mxt.anitrend.base.interfaces.event.ResponseCallback;
import com.mxt.anitrend.model.entity.anilist.Genre;
import com.mxt.anitrend.util.ActionModeHelper;
import com.mxt.anitrend.util.AnalyticsUtil;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.DateUtil;
import com.mxt.anitrend.util.DialogUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.SeriesActionUtil;
import com.mxt.anitrend.view.sheet.BottomSheetComposer;
import com.nguyenhoanglam.progresslayout.ProgressLayout;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.Unbinder;

public abstract class FragmentBase<M, P extends CommonPresenter, VM> extends Fragment implements View.OnClickListener, ActionModeListener,
        SharedPreferences.OnSharedPreferenceChangeListener, CommonPresenter.AbstractPresenter<P>, Observer<VM>, ResponseCallback, ItemClickListener<M> {

    protected boolean isFilterable, isPager, isMenuDisabled, isFeed;

    private @MenuRes int inflateMenu;
    private ActionModeHelper<M> actionMode;
    protected ViewModelBase<VM> viewModel;
    private CommonPresenter presenter;
    protected SeriesActionUtil seriesActionUtil;

    protected Snackbar snackbar;
    protected BottomSheetBase mBottomSheet;
    protected Unbinder unbinder;
    protected @IntegerRes int mColumnSize;

    public String TAG;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        TAG = this.toString();
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        AnalyticsUtil.logCurrentScreen(getActivity(), TAG);
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null (which
     * is the default implementation).  This will be called between
     * {@link #onCreate(Bundle)} and {@link #onActivityCreated(Bundle)}.
     * <p>
     * <p>If you return a View from here, you will later be called in
     * {@link #onDestroyView} when the view is being released.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable @Override
    public abstract View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    /**
     * Called when the view previously created by {@link #onCreateView} has
     * been detached from the fragment.  The next time the fragment needs
     * to be displayed, a new view will be created.  This is called
     * after {@link #onStop()} and before {@link #onDestroy()}.  It is called
     * <em>regardless</em> of whether {@link #onCreateView} returned a
     * non-null view.  Internally it is called after the view's state has
     * been saved but before it has been removed from its parent.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(unbinder != null)
            unbinder.unbind();
        if(presenter != null)
            presenter.onDestroy();
        if(seriesActionUtil != null)
            seriesActionUtil.onDestroy();
        actionMode = null;
    }

    /**
     * Called when the Fragment is visible to the user.  This is generally
     * tied to {@link Activity#onStart() Activity.onStart} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStart() {
        super.onStart();
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        if(!isMenuDisabled)
            setHasOptionsMenu(true);
    }

    /**
     * Called when the Fragment is no longer started.  This is generally
     * tied to {@link Activity#onStop() Activity.onStop} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStop() {
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onStop();
    }

    /**
     * Called when the Fragment is no longer resumed.  This is generally
     * tied to {@link Activity#onPause() Activity.onPause} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onPause() {
        super.onPause();
        if(seriesActionUtil != null)
            seriesActionUtil.onPause(null);
        if(presenter != null)
            presenter.onPause(this);
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to {@link Activity#onResume() Activity.onResume} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onResume() {
        super.onResume();
        if(seriesActionUtil != null)
            seriesActionUtil.onResume(null);
        if(presenter != null)
            presenter.onResume(this);
    }

    /**
     * Initialize the contents of the Fragment host's standard options menu.  You
     * should place your menu items in to <var>menu</var>.  For this method
     * to be called, you must have first called {@link #setHasOptionsMenu}.  See
     * {@link Activity#onCreateOptionsMenu(Menu) Activity.onCreateOptionsMenu}
     * for more information.
     *
     * @param menu The options menu in which you place your items.
     * @param inflater menu inflater
     * @see #setHasOptionsMenu
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(inflateMenu != 0)
            inflater.inflate(inflateMenu, menu);
        else {
            inflater.inflate(R.menu.shared_menu, menu);
            menu.findItem(R.id.action_filter).setVisible(isFilterable);
            menu.findItem(R.id.action_post).setVisible(isFeed);
        }
    }

    public void setInflateMenu(@MenuRes int inflateMenu) {
        this.inflateMenu = inflateMenu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort:
                DialogUtil.createSelection(getContext(), R.string.app_filter_sort, CompatUtil.getIndexOf(KeyUtils.SeriesSortTypes,
                        presenter.getApplicationPref().getSort()), CompatUtil.getStringList(getContext(), R.array.series_sort_types),
                        (dialog, which) -> {
                    if(which == DialogAction.POSITIVE)
                        presenter.getApplicationPref().saveSort(KeyUtils.SeriesSortTypes[dialog.getSelectedIndex()]);
                });
                return true;
            case R.id.action_order:
                DialogUtil.createSelection(getContext(), R.string.app_filter_order, CompatUtil.getIndexOf(KeyUtils.OrderTypes,
                        presenter.getApplicationPref().getOrder()), CompatUtil.getStringList(getContext(), R.array.order_by_types),
                        (dialog, which) -> {
                    if(which == DialogAction.POSITIVE)
                        presenter.getApplicationPref().saveOrder(KeyUtils.OrderTypes[dialog.getSelectedIndex()]);
                });
                return true;
            case R.id.action_genre:
                final List<Genre> genres = presenter.getDatabase().getGenreCollection();
                if(genres == null)
                    NotifyUtil.makeText(getContext(), R.string.app_splash_loading, R.drawable.ic_new_releases_white_24dp, Toast.LENGTH_SHORT).show();
                else
                    DialogUtil.createCheckList(getContext(), R.string.app_filter_genres, genres, presenter.getSelectedGenres(),
                            (dialog, which, text) -> true,  (dialog, which) -> {
                        switch (which) {
                            case POSITIVE:
                                presenter.saveSelectedGenres(dialog.getSelectedIndices(), genres);
                                break;
                            case NEGATIVE:
                                presenter.getApplicationPref().resetGenres();
                                break;
                        }
                    });
                return true;
            case R.id.action_type_anime:
                DialogUtil.createSelection(getContext(), R.string.app_filter_show_type, CompatUtil.getIndexOf(KeyUtils.AnimeMediaTypes,
                        presenter.getApplicationPref().getAnimeMediaType()), CompatUtil.getStringList(getContext(), R.array.media_formats),
                        (dialog, which) -> {
                    if(which == DialogAction.POSITIVE)
                        presenter.getApplicationPref().saveAnimeMediaType(KeyUtils.AnimeMediaTypes[dialog.getSelectedIndex()]);
                });
                return true;
            case R.id.action_type_manga:
                DialogUtil.createSelection(getContext(), R.string.app_filter_show_type, CompatUtil.getIndexOf(KeyUtils.MangaMediaTypes,
                        presenter.getApplicationPref().getMangaMediaType()), CompatUtil.getStringList(getContext(), R.array.media_sources),
                        (dialog, which) -> {
                    if(which == DialogAction.POSITIVE)
                        presenter.getApplicationPref().saveMangaMediaType(KeyUtils.MangaMediaTypes[dialog.getSelectedIndex()]);
                });
                return true;
            case R.id.action_year:
                final List<Integer> yearRanges = DateUtil.getYearRanges();
                DialogUtil.createSelection(getContext(), R.string.app_filter_year, CompatUtil.getIndexOf(yearRanges, presenter.getApplicationPref().getYear()),
                        yearRanges, (dialog, which) -> {
                    if(which == DialogAction.POSITIVE)
                        presenter.getApplicationPref().saveYear(yearRanges.get(dialog.getSelectedIndex()));
                });
                return true;
            case R.id.action_status_anime:
                DialogUtil.createSelection(getContext(), R.string.anime, CompatUtil.getIndexOf(KeyUtils.AnimeStatusTypes,
                        presenter.getApplicationPref().getAnimeStatus()), CompatUtil.getStringList(getContext(), R.array.series_status_types),
                        (dialog, which) -> {
                    if(which == DialogAction.POSITIVE)
                        presenter.getApplicationPref().saveAnimeStatus(dialog.getSelectedIndex());
                });
                return true;
            case R.id.action_status_manga:
                DialogUtil.createSelection(getContext(), R.string.manga, CompatUtil.getIndexOf(KeyUtils.MangaStatusTypes,
                        presenter.getApplicationPref().getMangaStatus()), CompatUtil.getStringList(getContext(), R.array.series_status_types),
                        (dialog, which) -> {
                    if(which == DialogAction.POSITIVE)
                        presenter.getApplicationPref().saveMangaStatus(dialog.getSelectedIndex());
                });
                return true;
            case R.id.action_post:
                mBottomSheet = new BottomSheetComposer.Builder()
                        .setRequestMode(KeyUtils.ACTIVITY_CREATE_REQ)
                        .setTitle(R.string.menu_title_new_activity_post)
                        .build();
                showBottomSheet();
                return true;
            case R.id.action_share:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Check to see if fragment is still alive
     * <br/>
     *
     * @return true if the fragment is still valid otherwise false
     */
    protected boolean isAlive() {
        //return getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED);
        return isVisible() || !isDetached() || !isRemoving();
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    protected abstract void updateUI();

    /**
     * All new or updated network requests should be handled in this method
     */
    public abstract void makeRequest();

    /**
     * Informs parent activity if on back can continue to super method or not
     */
    public boolean onBackPress() {
        boolean isBackAllowed;
        if(isBackAllowed = (actionMode != null && actionMode.getSelectedItems().size() > 0))
            actionMode.clearSelection();
        return isBackAllowed;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {

    }

    @SuppressWarnings("unchecked")
    @Override
    public P getPresenter() {
        return (P) presenter;
    }

    public void setPresenter(CommonPresenter presenter) {
        this.presenter = presenter;
    }

    public ViewModelBase<VM> getViewModel() {
        return viewModel;
    }

    @SuppressWarnings("unchecked")
    protected void setViewModel(boolean stateSupported) {
        if(viewModel == null) {
            viewModel = ViewModelProviders.of(this).get(ViewModelBase.class);
            viewModel.setContext(getContext());
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
    public abstract void onChanged(@Nullable VM model);

    protected void setActionModeEnabled(boolean isEnabled) {
        this.actionMode = new ActionModeHelper<>(this, isEnabled);
    }

    protected ActionModeHelper<M> getActionMode() {
        return actionMode;
    }

    @Override
    public void onSelectionChanged(ActionMode actionMode, int count) {
        actionMode.setTitle(getString(R.string.action_mode_selected, count));
    }

    /**
     * Called when action mode is first created. The menu supplied will be used to
     * generate action buttons for the action mode.
     *
     * @param mode ActionMode being created
     * @param menu Menu used to populate action buttons
     * @return true if the action mode should be created, false if entering this
     * mode should be aborted.
     */
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if(getActivity() != null) {
            getActivity().getMenuInflater().inflate(R.menu.action_mode, menu);
            return true;
        }
        return false;
    }

    /**
     * Called to refresh an action mode's action menu whenever it is invalidated.
     *
     * @param mode ActionMode being prepared
     * @param menu Menu used to populate action buttons
     * @return true if the menu or action mode was updated, false otherwise.
     */
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    /**
     * Called to report a user click on an action button.
     *
     * @param mode The current ActionMode
     * @param item The item that was clicked
     * @return true if this callback handled the event, false if the standard MenuItem
     * invocation should continue.
     */
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    /**
     * Called when an action mode is about to be exited and destroyed.
     *
     * @param mode The current ActionMode being destroyed
     */
    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode.clearSelection();
    }

    @Override
    public void showError(String error) {
        if(!TextUtils.isEmpty(error)) {
            Log.e(TAG, error);
            AnalyticsUtil.reportException(TAG, error);
        }
    }

    @Override
    public void showEmpty(String message) {
        if(!TextUtils.isEmpty(message))
            Log.d(TAG, message);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i(TAG, key);
    }

    protected void showBottomSheet() {
        if(getActivity() != null)
            mBottomSheet.show(getActivity().getSupportFragmentManager(), mBottomSheet.getTag());
    }

    /**
     * When the target view from {@link View.OnClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been clicked
     * @param data   the model that at the click index
     */
    @Override
    public void onItemClick(View target, M data) {

    }

    /**
     * When the target view from {@link View.OnLongClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been long clicked
     * @param data   the model that at the long click index
     */
    @Override
    public void onItemLongClick(View target, M data) {

    }
}
