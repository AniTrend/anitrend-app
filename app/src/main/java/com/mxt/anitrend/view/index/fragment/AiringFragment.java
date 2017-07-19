package com.mxt.anitrend.view.index.fragment;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.SeriesAiringAdapter;
import com.mxt.anitrend.api.structure.Anime;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.api.structure.ListItem;
import com.mxt.anitrend.async.AiringTaskFetch;
import com.mxt.anitrend.async.SeriesActionHelper;
import com.mxt.anitrend.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.custom.view.StatefulRecyclerView;
import com.mxt.anitrend.custom.event.RemoteChangeListener;
import com.mxt.anitrend.custom.event.SeriesInteractionListener;
import com.mxt.anitrend.presenter.CommonPresenter;
import com.mxt.anitrend.presenter.index.FragmentPresenter;
import com.mxt.anitrend.view.detail.activity.AnimeActivity;
import com.nguyenhoanglam.progresslayout.ProgressLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class AiringFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks, SeriesInteractionListener, RemoteChangeListener {

    @BindView(R.id.generic_pull_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.generic_recycler) StatefulRecyclerView recyclerView;
    @BindView(R.id.generic_progress_state) ProgressLayout progressLayout;

    private Unbinder unbinder;

    private List<ListItem> model;

    private final String KEY_MODEL_STATE = "model_key";

    private CommonPresenter mPresenter;
    private RecyclerViewAdapter<ListItem> mAdapter;
    private GridLayoutManager mLayoutManager;
    private LoaderManager loaderManager;

    public AiringFragment() {
        // Required empty public constructor
    }

    public static AiringFragment newInstance() {
        return new AiringFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new FragmentPresenter(getContext());
        loaderManager = getActivity().getLoaderManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_base_view, container, false);
        unbinder = ButterKnife.bind(this, root);
        progressLayout.showLoading();
        swipeRefreshLayout.setOnRefreshListener(this);
        recyclerView.setHasFixedSize(true); //originally set to fixed size true
        recyclerView.setNestedScrollingEnabled(false); //set to false if somethings fail to work properly
        mLayoutManager = new GridLayoutManager(getContext(), getResources().getInteger(mPresenter.getAppPrefs().isNewStyle()?R.integer.list_col_size_rank:R.integer.card_col_size_home));
        recyclerView.setLayoutManager(mLayoutManager);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(mPresenter.getSavedParse() == null && model == null)
            loaderManager.initLoader(getResources().getInteger(R.integer.airing_anime), null, this);
        else
            updateUI();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mPresenter.destroySuperToast();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.setParcelable(recyclerView.onSaveInstanceState());
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mPresenter.getSavedParse() != null)
            recyclerView.onRestoreInstanceState(mPresenter.getSavedParse());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        try {
            outState.putParcelableArrayList(KEY_MODEL_STATE, (ArrayList<? extends Parcelable>) model);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null) {
            try {
                model = savedInstanceState.getParcelableArrayList(KEY_MODEL_STATE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateUI() {
        if(recyclerView != null) {
            if(swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);
            if(mAdapter == null) {
                mAdapter = new SeriesAiringAdapter(model, getActivity(), mPresenter.getAppPrefs(), mPresenter.getApiPrefs(), this);
                recyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.onDataSetModified(model);
            }
            if (mAdapter.getItemCount() > 0) {
                progressLayout.showContent();
            } else
                progressLayout.showEmpty(ContextCompat.getDrawable(getContext(), R.drawable.request_empty), "No results to display");
        }
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        loaderManager.restartLoader(getResources().getInteger(R.integer.airing_anime), null, this);
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new AiringTaskFetch(getContext());
    }

    /**
     * Called when a previously created loader has finished its load.  Note
     * that normally an application is <em>not</em> allowed to commit fragment
     * transactions while in this call, since it can happen after an
     * activity's state is saved.  See {@link FragmentManager#beginTransaction()
     * FragmentManager.openTransaction()} for further discussion on this.
     * <p>
     * <p>This function is guaranteed to be called prior to the release of
     * the last data that was supplied for this Loader.  At this point
     * you should remove all use of the old data (since it will be released
     * soon), but should not do your own release of the data since its Loader
     * owns it and will take care of that.  The Loader will take care of
     * management of its data so you don't have to.  In particular:
     * <p>
     * <ul>
     * <li> <p>The Loader will monitor for changes to the data, and report
     * them to you through new calls here.  You should not monitor the
     * data yourself.  For example, if the data is a {@link Cursor}
     * and you place it in a {@link CursorAdapter}, use
     * the constructor <em>without</em> passing
     * in either {@link CursorAdapter#FLAG_AUTO_REQUERY}
     * or {@link CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
     * (that is, use 0 for the flags argument).  This prevents the CursorAdapter
     * from doing its own observing of the Cursor, which is not needed since
     * when a change happens you will get a new Cursor throw another call
     * here.
     * <li> The Loader will release the data once it knows the application
     * is no longer using it.  For example, if the data is
     * a {@link Cursor} from a {@link CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link CursorAdapter}, you should use the
     * {@link CursorAdapter#swapCursor(Cursor)}
     * method so that the old Cursor is not closed.
     * </ul>
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader loader, Object data) {
        model = (List<ListItem>) data;
        updateUI();
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader loader) {

    }

    private void navigateToAnime(Anime mSeries) {
        final Intent starter = new Intent(getActivity(), AnimeActivity.class);
        starter.putExtra(AnimeActivity.MODEL_ID_KEY, mSeries.getId());
        starter.putExtra(AnimeActivity.MODEL_BANNER_KEY, mSeries.getImage_url_banner());
        startActivity(starter);
    }

    /**
     * Bind to onClickListener in an adapter, and implement in fragment or class
     * <br/>
     * @param item the adapter position of the clicked item.
     * @param vId the current view id.
     */
    @Override
    public void onClickSeries(ListItem item, int vId) {
        switch (vId){
            case R.id.img_lge:
                navigateToAnime(item.getAnime());
                break;
            case R.id.txt_anime_eps:
                if(mPresenter.getAppPrefs().isAuthenticated())
                    new SeriesActionHelper(getContext(), KeyUtils.ANIME, item, this, mPresenter.getDefaultPrefs().isAutoIncrement()).execute();
                else
                    Toast.makeText(getContext(), getString(R.string.info_login_req), Toast.LENGTH_SHORT).show();
                break;
            case R.id.card_view:
                navigateToAnime(item.getAnime());
                break;
        }
    }

    /**
     * Bind to onLongClickListener in an adapter, and implement in fragment or class
     * <br/>
     * @param item the adapter position of the clicked item.
     */
    @Override
    public void onLongClickSeries(ListItem item) {
        if(mPresenter.getAppPrefs().isAuthenticated())
            new SeriesActionHelper(getContext(), KeyUtils.ANIME, item, this, false).execute();
        else
            Toast.makeText(getContext(), getString(R.string.info_login_req), Toast.LENGTH_SHORT).show();
    }

    /**
     * Signals that any async tasks which were done in dialog manager are complete
     */
    @Override
    public void onResultSuccess() throws Exception {
        if(isVisible() && !isRemoving())
        {
            swipeRefreshLayout.setRefreshing(true);
            onRefresh();
        }
    }

    /**
     * Implementation may not be used since errors are handled within the dialog manager
     */
    @Override
    public void onResultFailure() throws Exception {

    }
}
