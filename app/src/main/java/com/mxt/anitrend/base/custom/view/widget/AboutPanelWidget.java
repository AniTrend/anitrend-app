package com.mxt.anitrend.base.custom.view.widget;

import android.arch.lifecycle.Lifecycle;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.model.entity.anilist.Favourite;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.model.entity.container.attribute.PageInfo;
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer;
import com.mxt.anitrend.model.entity.container.body.PageContainer;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.util.DateUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.view.activity.detail.FavouriteActivity;
import com.mxt.anitrend.databinding.WidgetProfileAboutPanelBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by max on 2017/11/27.
 * following, followers & favourites
 */

public class AboutPanelWidget extends FrameLayout implements CustomView, View.OnClickListener, BaseConsumer.onRequestModelChange<UserBase> {

    private WidgetProfileAboutPanelBinding binding;
    private Lifecycle lifecycle;
    private long userId;

    private long mLastSynced;

    private QueryContainerBuilder queryContainer;

    private PageInfo followers, following;
    private int favourites;

    private WidgetPresenter<PageContainer<UserBase>> usersPresenter;
    private WidgetPresenter<ConnectionContainer<Favourite>> favouritePresenter;

    private BottomSheetBase mBottomSheet;
    private FragmentManager fragmentManager;

    public AboutPanelWidget(@NonNull Context context) {
        super(context);
        onInit();
    }

    public AboutPanelWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public AboutPanelWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        binding = WidgetProfileAboutPanelBinding.inflate(LayoutInflater.from(getContext()), this, true);
        queryContainer = GraphUtil.getDefaultQuery(false)
                .putVariable(KeyUtil.arg_id, userId)
                .putVariable(KeyUtil.arg_page_limit, 1);
        binding.setOnClickListener(this);
    }

    public void setUserId(long userId, Lifecycle lifecycle) {
        this.userId = userId; this.lifecycle = lifecycle;

        if(DateUtil.timeDifferenceSatisfied(KeyUtil.TIME_UNIT_MINUTES, mLastSynced, 5)) {
            binding.userFavouritesCount.setText("..");
            binding.userFollowersCount.setText("..");
            binding.userFollowingCount.setText("..");

            mLastSynced = System.currentTimeMillis();
            requestFavourites(); requestFollowers(); requestFollowing();
        }
    }

    private void requestFollowers() {
        usersPresenter = new WidgetPresenter<>(getContext());
        usersPresenter.getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        usersPresenter.requestData(KeyUtil.USER_FOLLOWERS_REQ, getContext(), new RetroCallback<PageContainer<UserBase>>() {
            @Override
            public void onResponse(@NonNull Call<PageContainer<UserBase>> call, @NonNull Response<PageContainer<UserBase>> response) {
                if(isAlive()) {
                    PageContainer<UserBase> pageContainer;
                    if (response.isSuccessful() && (pageContainer = response.body()) != null)
                        if(pageContainer.hasPageInfo()) {
                            followers = pageContainer.getPageInfo();
                            binding.userFollowersCount.setText(WidgetPresenter.valueFormatter(followers.getTotal()));
                        }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PageContainer<UserBase>> call, @NonNull Throwable throwable) {
                if(isAlive()) {
                    throwable.printStackTrace();
                    Log.e(this.toString(), throwable.getMessage());
                }
            }
        });
    }

    private void requestFollowing() {
        usersPresenter = new WidgetPresenter<>(getContext());
        usersPresenter.getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        usersPresenter.requestData(KeyUtil.USER_FOLLOWING_REQ, getContext(), new RetroCallback<PageContainer<UserBase>>() {
            @Override
            public void onResponse(@NonNull Call<PageContainer<UserBase>> call, @NonNull Response<PageContainer<UserBase>> response) {
                if(isAlive()) {
                    PageContainer<UserBase> pageContainer;
                    if(response.isSuccessful() && (pageContainer = response.body()) != null) {
                        if(pageContainer.hasPageInfo()) {
                            following = pageContainer.getPageInfo();
                            binding.userFollowingCount.setText(WidgetPresenter.valueFormatter(following.getTotal()));
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PageContainer<UserBase>> call, @NonNull Throwable throwable) {
                if(isAlive()) {
                    throwable.printStackTrace();
                    Log.e(this.toString(), throwable.getMessage());
                }
            }
        });
    }

    private void requestFavourites() {
        favouritePresenter = new WidgetPresenter<>(getContext());
        favouritePresenter.getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        favouritePresenter.requestData(KeyUtil.USER_FAVOURITES_COUNT_REQ, getContext(), new RetroCallback<ConnectionContainer<Favourite>>() {
            @Override
            public void onResponse(@NonNull Call<ConnectionContainer<Favourite>> call, @NonNull Response<ConnectionContainer<Favourite>> response) {
                if(isAlive()) {
                    ConnectionContainer<Favourite> connectionContainer;
                    if(response.isSuccessful() && (connectionContainer = response.body()) != null) {
                        if(connectionContainer.getConnection() != null) {
                            favourites += connectionContainer.getConnection().getAnime().getPageInfo().getTotal();
                            favourites += connectionContainer.getConnection().getManga().getPageInfo().getTotal();
                            favourites += connectionContainer.getConnection().getCharacter().getPageInfo().getTotal();
                            favourites += connectionContainer.getConnection().getStaff().getPageInfo().getTotal();
                            favourites += connectionContainer.getConnection().getStudio().getPageInfo().getTotal();
                            binding.userFavouritesCount.setText(WidgetPresenter.valueFormatter(favourites));
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ConnectionContainer<Favourite>> call, @NonNull Throwable throwable) {
                if(isAlive()) {
                    throwable.printStackTrace();
                    Log.e(this.toString(), throwable.getMessage());
                }
            }
        });
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {
        if(favouritePresenter != null)
            favouritePresenter.onDestroy();
        if(usersPresenter != null)
            usersPresenter.onDestroy();
        fragmentManager = null;
        mBottomSheet = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.user_favourites_container:
                if(favourites < 1)
                    NotifyUtil.makeText(getContext(), R.string.text_activity_loading, Toast.LENGTH_SHORT).show();
                else {
                    Intent intent = new Intent(getContext(), FavouriteActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(KeyUtil.arg_id, userId);
                    getContext().startActivity(intent);
                }
                break;
            case R.id.user_followers_container:
                if(followers == null || followers.getTotal() < 1)
                    NotifyUtil.makeText(getContext(), R.string.text_activity_loading, Toast.LENGTH_SHORT).show();
                else if (fragmentManager != null){
                    /*mBottomSheet = new BottomSheetListUsers.Builder()
                            .setModel(GraphUtil.getDefaultQuery(true)
                                    .putVariable(KeyUtils.arg_id, userId),
                                    KeyUtils.USER_FOLLOWERS_REQ)
                            .setTitle(R.string.title_bottom_sheet_followers)
                            .build();
                    mBottomSheet.show(fragmentManager, mBottomSheet.getTag());*/
                    NotifyUtil.makeText(getContext(), R.string.TBA, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.user_following_container:
                if(following == null || following.getTotal() < 1)
                    NotifyUtil.makeText(getContext(), R.string.text_activity_loading, Toast.LENGTH_SHORT).show();
                else if (fragmentManager != null){
                    /*mBottomSheet = new BottomSheetListUsers.Builder()
                            .setModel(GraphUtil.getDefaultQuery(true)
                                    .putVariable(KeyUtils.arg_id, userId),
                                    KeyUtils.USER_FOLLOWING_REQ)
                            .setTitle(R.string.title_bottom_sheet_following)
                            .build();
                    mBottomSheet.show(fragmentManager, mBottomSheet.getTag());*/
                    NotifyUtil.makeText(getContext(), R.string.TBA, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void setFragmentActivity(FragmentActivity activity) {
        if(activity != null)
           fragmentManager = activity.getSupportFragmentManager();
    }

    @Override @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onModelChanged(BaseConsumer<UserBase> consumer) {
        if(consumer.getRequestMode() == KeyUtil.MUT_TOGGLE_FOLLOW) {
            if(followers != null) {
                int total = followers.getTotal();
                followers.setTotal(!consumer.getChangeModel().isFollowing()? --total : ++total);
                if(isAlive())
                    binding.userFollowersCount.setText(WidgetPresenter.valueFormatter(followers.getTotal()));
            } else if(isAlive())
                requestFollowers();
        }
    }

    private boolean isAlive() {
        return lifecycle != null && lifecycle.getCurrentState().isAtLeast(Lifecycle.State.STARTED);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onDetachedFromWindow();
    }
}
