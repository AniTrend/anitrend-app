package com.mxt.anitrend.base.custom.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.annimon.stream.IntPair;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.StatefulRecyclerView;
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout;
import com.mxt.anitrend.base.interfaces.event.ItemClickListener;
import com.mxt.anitrend.base.interfaces.event.RecyclerLoadListener;
import com.mxt.anitrend.model.entity.anilist.ExternalLink;
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer;
import com.mxt.anitrend.model.entity.crunchy.Channel;
import com.mxt.anitrend.model.entity.crunchy.Episode;
import com.mxt.anitrend.model.entity.crunchy.Rss;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.DialogUtil;
import com.mxt.anitrend.util.EpisodeHelper;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.view.activity.index.SearchActivity;
import com.nguyenhoanglam.progresslayout.ProgressLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by max on 2017/11/04.
 */

public abstract class FragmentChannelBase extends FragmentBase<Channel, WidgetPresenter<ConnectionContainer<List<ExternalLink>>>, Rss> implements RecyclerLoadListener,
        CustomSwipeRefreshLayout.OnRefreshAndLoadListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public @BindView(R.id.refreshLayout) CustomSwipeRefreshLayout swipeRefreshLayout;
    public @BindView(R.id.recyclerView) StatefulRecyclerView recyclerView;
    public @BindView(R.id.stateLayout) ProgressLayout stateLayout;

    protected String query;
    protected boolean isLimit;
    protected boolean isPopular;
    protected String targetLink, copyright;

    protected List<ExternalLink> externalLinks;
    protected RecyclerViewAdapter<Episode> mAdapter;
    private GridLayoutManager mLayoutManager;

    private final View.OnClickListener stateLayoutOnClick = view -> {
        if(swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
        if(snackbar != null && snackbar.isShown())
            snackbar.dismiss();
        showLoading();
        onRefresh();
    };

    private final View.OnClickListener snackBarOnClick = view -> {
        if(swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
        if(snackbar != null && snackbar.isShown())
            snackbar.dismiss();
        swipeRefreshLayout.setLoading(true);
        makeRequest();
    };

    /**
     * Override and set presenter, mColumnSize, and fetch argument/s
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            isPopular = getArguments().getBoolean(KeyUtil.arg_popular);
            externalLinks = getArguments().getParcelableArrayList(KeyUtil.arg_list_model);
            if(externalLinks != null)
                targetLink = EpisodeHelper.episodeSupport(externalLinks);
        }
    }

    /**
     * Override this as normal the save instance for your model will be managed for you,
     * so there is no need to to restore the state of your model from save state.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_list, container, false);
        unbinder = ButterKnife.bind(this, root);
        recyclerView.setHasFixedSize(true); //originally set to fixed size true
        recyclerView.setNestedScrollingEnabled(true); //set to false if somethings fail to work properly
        mLayoutManager = new GridLayoutManager(getContext(), getResources().getInteger(mColumnSize));
        recyclerView.setLayoutManager(mLayoutManager);

        swipeRefreshLayout.setOnRefreshAndLoadListener(this);
        CompatUtil.configureSwipeRefreshLayout(swipeRefreshLayout, getActivity());

        return root;
    }

    /**
     * Called when the Fragment is visible to the user.  This is generally
     * tied to Activity.onStart of the containing Activity's lifecycle.
     * In this current context the Event bus is automatically registered for you
     * @see EventBus
     */
    @Override
    public void onStart() {
        super.onStart();
        showLoading();
        if(mAdapter.getItemCount() < 1)
            onRefresh();
        else
            updateUI();
    }

    /**
     * Called to ask the fragment to save its current dynamic state, so it
     * can later be reconstructed in a new instance of its process is
     * restarted.  If a new instance of the fragment later needs to be
     * created, the data you place in the Bundle here will be available
     * in the Bundle given to {@link #onCreate(Bundle)},
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}, and
     * {@link #onActivityCreated(Bundle)}.
     * <p>
     * <p>This corresponds to {@link Activity#onSaveInstanceState(Bundle)
     * Activity.onSaveInstanceState(Bundle)} and most of the discussion there
     * applies here as well.  Note however: <em>this method may be called
     * at any time before {@link #onDestroy()}</em>.  There are many situations
     * where a fragment may be mostly torn down (such as when placed on the
     * back stack with no UI showing), but its state will not be saved until
     * its owning activity actually needs to save its state.
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KeyUtil.key_pagination, isPager);
        outState.putInt(KeyUtil.key_columns, mColumnSize);
    }

    /**
     * Called when all saved state has been restored into the view hierarchy
     * of the fragment.  This can be used to do initialization based on saved
     * state that you are letting the view hierarchy track itself, such as
     * whether check box widgets are currently checked.  This is called
     * after {@link #onActivityCreated(Bundle)} and before
     * {@link #onStart()}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null) {
            isPager = savedInstanceState.getBoolean(KeyUtil.key_pagination);
            mColumnSize = savedInstanceState.getInt(KeyUtil.key_columns);
        }
    }

    protected void addScrollLoadTrigger() {
        if(isPager)
            if (!recyclerView.hasOnScrollListener()) {
                getPresenter().initListener(mLayoutManager, this);
                recyclerView.addOnScrollListener(getPresenter());
            }
    }

    protected void removeScrollLoadTrigger() {
        if (isPager)
            recyclerView.clearOnScrollListeners();
    }

    @Override
    public void onPause() {
        super.onPause();
        removeScrollLoadTrigger();
    }

    @Override
    public void onResume() {
        super.onResume();
        addScrollLoadTrigger();
    }

    @Override
    public void showError(String error) {
        super.showError(error);
        if(swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
        if(swipeRefreshLayout.isLoading())
            swipeRefreshLayout.setLoading(false);
        if(getPresenter() != null && getPresenter().getCurrentPage() > 1 && isPager) {
            if(stateLayout.isLoading())
                stateLayout.showContent();
            snackbar = NotifyUtil.make(stateLayout, R.string.text_unable_to_load_next_page, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.try_again, snackBarOnClick);
            snackbar.show();
        }
        else {
            showLoading();
            stateLayout.showError(CompatUtil.getDrawable(getContext(), R.drawable.ic_emoji_cry),
                    error, getString(R.string.try_again), stateLayoutOnClick);
        }
    }

    @Override
    public void showEmpty(String message) {
        super.showEmpty(message);
        if(swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
        if(swipeRefreshLayout.isLoading())
            swipeRefreshLayout.setLoading(false);
        if(getPresenter() != null && getPresenter().getCurrentPage() > 1 && isPager) {
            if(stateLayout.isLoading())
                stateLayout.showContent();
            snackbar = NotifyUtil.make(stateLayout, R.string.text_unable_to_load_next_page, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.try_again, snackBarOnClick);
            snackbar.show();
        }
        else {
            showLoading();
            stateLayout.showError(CompatUtil.getDrawable(getContext(), R.drawable.ic_emoji_sweat),
                    message, getString(R.string.try_again), stateLayoutOnClick);
        }
    }

    public void showContent() {
        stateLayout.showContent();
    }

    public void showLoading() {
        stateLayout.showLoading();
    }

    /**
     * While paginating if our request was a success and
     */
    public void setLimitReached() {
        if(getPresenter() != null && getPresenter().getCurrentPage() != 0) {
            swipeRefreshLayout.setLoading(false);
            isLimit = true;
        }
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        isLimit = false;
        if(getPresenter() != null)
            getPresenter().onRefreshPage();
        makeRequest();
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onLoadMore() {
        swipeRefreshLayout.setLoading(true);
        makeRequest();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSearch(String query) {
        if(isAlive() && mAdapter != null) {
            this.query = query;
            mAdapter.getFilter().filter(query);
        }
    }

    /**
     * Set your adapter and call this method when you are done to the current
     * parents data, then call this method after
     */
    protected void injectAdapter() {
        if(mAdapter.getItemCount() > 0) {
            if (recyclerView.getAdapter() == null) {
                recyclerView.setAdapter(mAdapter);
            } else {
                if (swipeRefreshLayout.isRefreshing())
                    swipeRefreshLayout.setRefreshing(false);
                else if(swipeRefreshLayout.isLoading())
                    swipeRefreshLayout.setLoading(false);
                if (!TextUtils.isEmpty(query))
                    mAdapter.getFilter().filter(query);
            }
            showContent();
        }
        else
            showEmpty(getString(R.string.layout_empty_response));
    }

    /**
     * Called when the model state is changed.
     *
     * @param content The new data
     */
    @Override
    public void onChanged(@Nullable Rss content) {
        if(content != null) {
            copyright = content.getChannel().getCopyright();
            mAdapter.onItemsInserted(content.getChannel().getEpisode());
            updateUI();
        } else
            showEmpty(getString(R.string.layout_empty_response));
    }

    protected ItemClickListener<Episode> clickListener = new ItemClickListener<Episode>() {
        /**
         * When the target view from {@link View.OnClickListener}
         * is clicked from a view holder this method will be called
         *
         * @param target view that has been clicked
         * @param data   the model that at the clicked index
         */
        @Override
        public void onItemClick(View target, IntPair<Episode> data) {
            switch (target.getId()) {
                case R.id.series_image:
                    DialogUtil.createMessage(getActivity(), data.getSecond().getTitle(), data.getSecond().getDescription()+"<br/><br/>"+copyright,
                            R.string.Watch, R.string.Dismiss, R.string.action_search, (dialog, which) -> {
                                Intent intent;
                                switch (which) {
                                    case POSITIVE:
                                        if(data.getSecond().getLink() != null) {
                                            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(data.getSecond().getLink()));
                                            startActivity(intent);
                                        } else
                                            NotifyUtil.makeText(getActivity(), R.string.text_premium_show, Toast.LENGTH_SHORT).show();
                                        break;
                                    case NEUTRAL:
                                        if(getActivity() != null) {
                                            intent = new Intent(getActivity(), SearchActivity.class);
                                            intent.putExtra(KeyUtil.arg_search, EpisodeHelper.getActualTile(data.getSecond().getTitle()));
                                            getActivity().startActivity(intent);
                                        }
                                        break;
                                }
                            });
                    break;
            }
        }

        /**
         * When the target view from {@link View.OnLongClickListener}
         * is clicked from a view holder this method will be called
         *
         * @param target view that has been long clicked
         * @param data   the model that at the long clicked index
         */
        @Override
        public void onItemLongClick(View target, IntPair<Episode> data) {

        }
    };
}