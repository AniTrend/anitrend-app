package com.mxt.anitrend.viewmodel.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.johnpersano.supertoasts.library.Style;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.SeriesMyAnimeAdapter;
import com.mxt.anitrend.adapter.recycler.index.SeriesMyMangaAdapter;
import com.mxt.anitrend.api.model.User;
import com.mxt.anitrend.api.structure.ListItem;
import com.mxt.anitrend.async.SeriesActionHelper;
import com.mxt.anitrend.async.SortHelper;
import com.mxt.anitrend.custom.RecyclerViewAdapter;
import com.mxt.anitrend.custom.StatefulRecyclerView;
import com.mxt.anitrend.event.FragmentCallback;
import com.mxt.anitrend.event.RemoteChangeListener;
import com.mxt.anitrend.event.SeriesInteractionListener;
import com.mxt.anitrend.presenter.base.MyListPresenter;
import com.mxt.anitrend.util.ComparatorProvider;
import com.mxt.anitrend.util.ErrorHandler;
import com.mxt.anitrend.util.FilterProvider;
import com.mxt.anitrend.view.detail.activity.AnimeActivity;
import com.mxt.anitrend.view.detail.activity.MangaActivity;
import com.nguyenhoanglam.progresslayout.ProgressLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static com.mxt.anitrend.api.structure.FilterTypes.SeriesType;
import static com.mxt.anitrend.async.AsyncTaskFetch.RequestType;

/**
 * Created by max on 2017/04/13.
 */
public abstract class UserListFragment extends Fragment implements Callback<User>,
        SortHelper.SortCallback<ListItem>, SwipeRefreshLayout.OnRefreshListener, SeriesInteractionListener,
        RemoteChangeListener, FragmentCallback<String>, SharedPreferences.OnSharedPreferenceChangeListener {

    @BindView(R.id.generic_pull_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.generic_recycler) StatefulRecyclerView recyclerView;
    @BindView(R.id.generic_progress_state) ProgressLayout progressLayout;

    protected final static String ARG_KEY = "arg_data";

    private final String KEY_MODEL_STATE = "model_key";
    private final String KEY_USER_ID_STATE = "user_id_key";

    protected MyListPresenter mPresenter;
    protected List<ListItem> model;
    protected int user_id;

    protected SortHelper<ListItem> mSorter;
    protected Unbinder unbinder;

    private RecyclerViewAdapter<ListItem> mAdapter;
    private GridLayoutManager mLayoutManager;

    // set the type of request for the presenter to use
    public RequestType mRequestType;

    /**
     * Override if you need to include extra functionality into the method,
     * the method will get the arguments from the from your bundle and into
     * the model followed by initialization of your presenter
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments().containsKey(ARG_KEY))
            user_id = getArguments().getInt(ARG_KEY);
        mPresenter = new MyListPresenter(getContext(), mRequestType);
    }

    /**
     * Override this as normal the save instance for your model will be managed for you,
     * so there is no need to to restore the state of your model from save state.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
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

    /**
     * Called when the Fragment is visible to the user.  This is generally
     * tied to Activity.onStart of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        if(mPresenter.getSavedParse() == null && model == null)
            mPresenter.beginAsync(this, user_id);
        else
            updateUI();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    /**
     * Override as normal, the saving of the model is also managed for
     * you so no need to save it.
     *
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_USER_ID_STATE, user_id);
        if(model != null && model.size() < 15)
            outState.putParcelableArrayList(KEY_MODEL_STATE, (ArrayList<? extends Parcelable>) model);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null) {
            user_id = savedInstanceState.getInt(KEY_USER_ID_STATE);
            model = savedInstanceState.getParcelableArrayList(KEY_MODEL_STATE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.getApiPrefs().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        mPresenter.setParcelable(recyclerView.onSaveInstanceState());
        mPresenter.destroySuperToast();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.getApiPrefs().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        if(mPresenter.getSavedParse() != null)
            recyclerView.onRestoreInstanceState(mPresenter.getSavedParse());
    }

    /**
     * No need to call ButterKnife.unbind()
     * method is already called for you
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mPresenter.destroySuperToast();
        if(mSorter != null)
            mSorter.cancel(false);
    }

    /**
     * Is automatically called in the @onStart Method
     */
    protected void updateUI() {
        if(recyclerView != null) {
            if(swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);
            if(mAdapter == null) {
                switch (mRequestType) {
                    case USER_ANIME_LIST_REQ:
                        if(user_id == mPresenter.getCurrentUser().getId())
                            mAdapter = new SeriesMyAnimeAdapter(model, getActivity(), mPresenter.getAppPrefs(), mPresenter.getApiPrefs(), this);
                        else
                            mAdapter = new SeriesMyAnimeAdapter(model, getActivity(), mPresenter.getAppPrefs(), mPresenter.getApiPrefs());
                        break;
                    case USER_MANGA_LIST_REQ:
                        if(user_id == mPresenter.getCurrentUser().getId())
                            mAdapter = new SeriesMyMangaAdapter(model, getActivity(), mPresenter.getAppPrefs(), mPresenter.getApiPrefs(), this);
                        else
                            mAdapter = new SeriesMyMangaAdapter(model, getActivity(), mPresenter.getAppPrefs(), mPresenter.getApiPrefs());
                        break;
                }
                recyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.onDataSetModified(model);
            }

            if (mAdapter.getItemCount() > 0) {
                progressLayout.showContent();
            } else
                showEmpty();
        }
    }

    public void showError(Response body) {
        progressLayout.showError(ContextCompat.getDrawable(getContext(), R.drawable.request_error),
                ErrorHandler.getError(body).toString(), getString(R.string.button_try_again), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressLayout.showLoading();
                onRefresh();
            }
        });
    }

    public void showEmpty() {
        progressLayout.showEmpty(ContextCompat.getDrawable(getContext(), R.drawable.request_empty), getString(R.string.layout_empty_response));
    }

    @Override
    public abstract void onResponse(Call<User> call, Response<User> response);

    @Override
    public void onFailure(Call<User> call, Throwable t) {
        if(isVisible() && (!isDetached() || !isRemoving())) {
            t.printStackTrace();
            progressLayout.showError(ContextCompat.getDrawable(getContext(), R.drawable.request_error), t.getLocalizedMessage(), getString(R.string.button_try_again), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    progressLayout.showLoading();
                    onRefresh();
                }
            });
        }
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        mPresenter.beginAsync(this, user_id);
    }

    @Override
    public void onSortComplete(List<ListItem> result) {
        if(isVisible() && (!isRemoving() || !isDetached())) {
            model = result;
            updateUI();
        }
    }

    /**
     * Signals that any async tasks which were done in dialog manager are complete
     */
    @Override
    public void onResultSuccess() throws Exception {
        if(isVisible() && (!isRemoving() || !isDetached()))
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
        if(isVisible() && (!isRemoving() || !isDetached()))
            mPresenter.createSuperToast(getActivity(), "Something went wrong.", R.drawable.ic_info_outline_white_18dp, Style.TYPE_STANDARD);
    }

    /**
     * TODO: Handle Manga Items clicks
     * Bind to onClickListener in an adapter, and implement in fragment or class
     * <br/>
     *
     * @param item the adapter position of the clicked item.
     * @param vId      the current view id.
     */
    @Override
    public void onClickSeries(ListItem item, int vId) {
        switch (vId) {
            case R.id.txt_anime_eps:
                if(mPresenter.getAppPrefs().isAuthenticated())
                    new SeriesActionHelper(getActivity(), mRequestType == RequestType.USER_ANIME_LIST_REQ? SeriesType.ANIME: SeriesType.MANGA, item, this, mPresenter.getDefaultPrefs().isAutoIncrement()).execute();
                else
                    mPresenter.createSuperToast(getActivity(), getString(R.string.info_login_req), R.drawable.ic_info_outline_white_18dp, Style.TYPE_STANDARD);
                break;
            default:
                if (mRequestType == RequestType.USER_ANIME_LIST_REQ) {
                    Intent mAnimeStarter = new Intent(getActivity(), AnimeActivity.class);
                    mAnimeStarter.putExtra(AnimeActivity.MODEL_ID_KEY, item.getAnime().getId());
                    mAnimeStarter.putExtra(AnimeActivity.MODEL_BANNER_KEY, item.getAnime().getImage_url_banner());
                    startActivity(mAnimeStarter);
                } else {
                    Intent mMangaStarter = new Intent(getActivity(), MangaActivity.class);
                    mMangaStarter.putExtra(MangaActivity.MODEL_ID_KEY, item.getManga().getId());
                    mMangaStarter.putExtra(MangaActivity.MODEL_BANNER_KEY, item.getManga().getImage_url_banner());
                    startActivity(mMangaStarter);
                }
                break;
        }
    }

    /**
     * Bind to onLongClickListener in an adapter, and implement in fragment or class
     * <br/>
     *
     * @param item the adapter position of the clicked item.
     */
    @Override
    public void onLongClickSeries(ListItem item) {
        if(mPresenter.getAppPrefs().isAuthenticated())
            new SeriesActionHelper(getActivity(), mRequestType == RequestType.USER_ANIME_LIST_REQ? SeriesType.ANIME: SeriesType.MANGA, item, this, false).execute();
        else
            mPresenter.createSuperToast(getActivity(), getString(R.string.info_login_req), R.drawable.ic_info_outline_white_18dp, Style.TYPE_STANDARD);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    /**
     * Called when a shared preference is changed, added, or removed. This
     * may be called even if a preference is set to its existing value.
     * <p>
     * <p>This callback will be run on your main thread.
     *
     * @param sharedPreferences The {@link SharedPreferences} that received
     *                          the change.
     * @param key               The key of the preference that was changed, added, or
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        update();
    }

    /**
     * Recall the sorter to sort the list of data
     */
    @Override
    public void update() {
        progressLayout.showLoading();
        sortItems(model);
    }

    /**
     * The sorting runner must be invoked in here
     */
    public void sortItems(List<ListItem> body) {
        Comparator<ListItem> comparator = mRequestType == RequestType.USER_ANIME_LIST_REQ?
                ComparatorProvider.getAnimeComparator(mPresenter.getApiPrefs())
                :
                ComparatorProvider.getMangaComparator(mPresenter.getApiPrefs());
        mSorter = new SortHelper<>(comparator, this, user_id!=mPresenter.getCurrentUser().getId()?
                FilterProvider.getListItemFilter(mPresenter.getCurrentUser().getId() == user_id, body) : body);
        mSorter.executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    /**
     * Search page fragments
     *
     * @param query
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    @Override
    public void update(String query) {
        if(mAdapter != null) {
            mAdapter.getFilter().filter(query);
        }
    }
}
