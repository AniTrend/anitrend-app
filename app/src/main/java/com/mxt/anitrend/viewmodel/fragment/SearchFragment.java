package com.mxt.anitrend.viewmodel.fragment;

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

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.details.StaffAdapter;
import com.mxt.anitrend.adapter.recycler.index.CharacterSearchAdapter;
import com.mxt.anitrend.adapter.recycler.index.SeriesAnimeAdapter;
import com.mxt.anitrend.adapter.recycler.index.SeriesMangaAdapter;
import com.mxt.anitrend.adapter.recycler.index.StudioAdapter;
import com.mxt.anitrend.adapter.recycler.user.UserListAdapter;
import com.mxt.anitrend.api.model.Character;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.api.model.Staff;
import com.mxt.anitrend.api.model.StaffSmall;
import com.mxt.anitrend.api.model.StudioSmall;
import com.mxt.anitrend.api.model.UserSmall;
import com.mxt.anitrend.async.SortHelper;
import com.mxt.anitrend.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.custom.view.StatefulRecyclerView;
import com.mxt.anitrend.custom.event.FragmentCallback;
import com.mxt.anitrend.presenter.base.SearchPresenter;
import com.nguyenhoanglam.progresslayout.ProgressLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mxt.anitrend.async.AsyncTaskFetch.RequestType;

/**
 * Created by max on 2017/04/13.
 */
@SuppressWarnings("unchecked")
public abstract class SearchFragment<T extends Parcelable> extends Fragment implements Callback<List<T>>,
        SortHelper.SortCallback<T>, SwipeRefreshLayout.OnRefreshListener, FragmentCallback<String> {

    @BindView(R.id.generic_pull_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.generic_progress_state) ProgressLayout progressLayout;
    @BindView(R.id.generic_recycler) StatefulRecyclerView recyclerView;

    protected final static String ARG_KEY = "arg_data";

    private final String KEY_MODEL_STATE = "model_key";
    private final String KEY_QUERY_STATE = "query_key";

    protected SearchPresenter mPresenter;
    protected List<T> model;
    private String query;

    protected SortHelper<T> mSorter;
    protected Unbinder unbinder;

    private RecyclerViewAdapter mAdapter;
    private GridLayoutManager mLayoutManager;

    // set the type of request in each fragment
    public RequestType mRequestType;

    /**
     * Override if you need to include extra functionality into the method,
     * the method will get the arguments from the from your bundle and into
     * the model followed by initialization of your presenter
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments().containsKey(ARG_KEY))
            query = getArguments().getString(ARG_KEY);
        mPresenter = new SearchPresenter(getContext(), mRequestType);
        mPresenter.setSearch(query);
    }

    /**
     * Override this as normal the save instance for your model will be managed for you,
     * so there is no need to to restore the state of your model from save state.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
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
        switch (mRequestType) {
            case ANIME_SEARCH_REQ:
                mLayoutManager = new GridLayoutManager(getContext(), getResources().getInteger(R.integer.card_col_size_home));
                break;
            case MANGA_SEARCH_REQ:
                mLayoutManager = new GridLayoutManager(getContext(), getResources().getInteger(R.integer.card_col_size_home));
                break;
            case CHARACTER_SEARCH_REQ:
                mLayoutManager = new GridLayoutManager(getContext(), getResources().getInteger(R.integer.list_col_size_rank));
                break;
            case STAFF_SEARCH_REQ:
                mLayoutManager = new GridLayoutManager(getContext(), getResources().getInteger(R.integer.list_col_size_rank));
                break;
            default:
                mLayoutManager = new GridLayoutManager(getContext(), getResources().getInteger(R.integer.list_col_size));
                break;
        }
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
        if(query != null && query.length() < 1)
            progressLayout.showError(ContextCompat.getDrawable(getContext(), R.drawable.request_error), getString(R.string.text_error_request), getString(R.string.Close), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(v != null)
                        getActivity().finish();
                }
            });
         else {
            if (model == null) {
                mPresenter.beginAsync(SearchFragment.this);
            } else {
                updateUI();
            }
        }
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
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_QUERY_STATE, query);
        if(model != null && model.size() < 15)
            outState.putParcelableArrayList(KEY_MODEL_STATE, (ArrayList<? extends Parcelable>) model);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null) {
            query = savedInstanceState.getString(KEY_QUERY_STATE);
            model = savedInstanceState.getParcelableArrayList(KEY_MODEL_STATE);
        }
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

    /**
     * No need to call ButterKnife.unbind()
     * method is already called for you
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
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
                    case ANIME_SEARCH_REQ:
                        mAdapter = new SeriesAnimeAdapter((List<Series>) model, getActivity(), mPresenter.getAppPrefs());
                        break;
                    case MANGA_SEARCH_REQ:
                        mAdapter = new SeriesMangaAdapter((List<Series>) model, getActivity(), mPresenter.getAppPrefs());
                        break;
                    case USER_SEARCH_REQ:
                        mAdapter = new UserListAdapter((List<UserSmall>) model, getActivity());
                        break;
                    case STUDIO_SEARCH_REQ:
                        mAdapter = new StudioAdapter((List<StudioSmall>) model, getActivity());
                        break;
                    case CHARACTER_SEARCH_REQ:
                        mAdapter = new CharacterSearchAdapter((List<Character>) model, getActivity());
                        break;
                    case STAFF_SEARCH_REQ:
                        mAdapter = new StaffAdapter((List<StaffSmall>) model, getActivity());
                        break;
                }
                recyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.onDataSetModified(model);
            }

            if (mAdapter.getItemCount() > 0) {
                progressLayout.showContent();
            } else
                progressLayout.showEmpty(ContextCompat.getDrawable(getContext(), R.drawable.request_empty), "No results were found for '"+query+"'");
        }
    }

    public void showError() {
        progressLayout.showError(ContextCompat.getDrawable(getContext(), R.drawable.request_error), "No listing found under the specified criteria", getString(R.string.Go_Back), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v != null)
                    getActivity().finish();
            }
        });
    }

    @Override
    public abstract void onResponse(Call<List<T>> call, Response<List<T>> response);

    @Override
    public void onFailure(Call<List<T>> call, Throwable t) {
        if(isAlive()) {
            t.printStackTrace();
            progressLayout.showError(ContextCompat.getDrawable(getContext(), R.drawable.request_error), t.getLocalizedMessage(), getString(R.string.button_try_again), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    progressLayout.showLoading();
                    mPresenter.beginAsync(SearchFragment.this);
                }
            });
        }
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        mPresenter.setSearch(query);
        mPresenter.beginAsync(this);
    }

    @Override
    public void onSortComplete(List<T> result) {
        if(isAlive()) {
            model = result;
            updateUI();
        }
    }

    protected boolean isAlive() {
        return isVisible() && (!isDetached() || !isRemoving());
    }

    /**
     * Normal fragments
     */
    @Override
    public void update() {
        progressLayout.showLoading();
        sortItems();
    }

    /**
     * The sorting runner must be invoked in here
     */
    public abstract void sortItems();

    /**
     * Search page fragments
     *
     * @param query
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    @Override
    public void update(String query) {
        progressLayout.showLoading();
        this.query = query;
        onRefresh();
    }
}
