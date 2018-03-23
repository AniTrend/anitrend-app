package com.mxt.anitrend.base.custom.view.widget;

import android.arch.lifecycle.Lifecycle;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.databinding.WidgetProfileAboutPanelBinding;
import com.mxt.anitrend.model.entity.anilist.Favourite;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.util.DateUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.view.activity.detail.FavouriteActivity;
import com.mxt.anitrend.view.sheet.BottomSheetUsers;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

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

    private List<UserBase> followers, following;
    private Favourite favourites;

    private WidgetPresenter<List<UserBase>> followersPresenter, followingPresenter;
    private WidgetPresenter<Favourite> favouritePresenter;

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
        binding.setOnClickListener(this);
    }

    public void setUserId(long userId, Lifecycle lifecycle) {
        this.userId = userId; this.lifecycle = lifecycle;

        if(DateUtil.timeDifferenceSatisfied(KeyUtils.TIME_UNIT_MINUTES, mLastSynced, 5)) {
            binding.userFavouritesCount.setText("..");
            binding.userFollowersCount.setText("..");
            binding.userFollowingCount.setText("..");

            mLastSynced = System.currentTimeMillis();
            requestFavourites(); requestFollowers(); requestFollowing();
        }
    }

    private void requestFollowers() {
        followersPresenter = new WidgetPresenter<>(getContext());
        followersPresenter.getParams().putLong(KeyUtils.arg_userId, userId);
        followersPresenter.requestData(KeyUtils.USER_FOLLOWERS_REQ, getContext(), new RetroCallback<List<UserBase>>() {
            @Override
            public void onResponse(@NonNull Call<List<UserBase>> call, @NonNull Response<List<UserBase>> response) {
                if(isAlive()) {
                    if (response.isSuccessful() && (followers = response.body()) != null)
                        binding.userFollowersCount.setText(WidgetPresenter.valueFormatter(followers.size()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<UserBase>> call, @NonNull Throwable throwable) {
                if(isAlive())
                    throwable.printStackTrace();
            }
        });
    }

    private void requestFollowing() {
        followingPresenter = new WidgetPresenter<>(getContext());
        followingPresenter.getParams().putLong(KeyUtils.arg_userId, userId);
        followingPresenter.requestData(KeyUtils.USER_FOLLOWING_REQ, getContext(), new RetroCallback<List<UserBase>>() {
            @Override
            public void onResponse(@NonNull Call<List<UserBase>> call, @NonNull Response<List<UserBase>> response) {
                if(isAlive()) {
                    if(response.isSuccessful() && (following = response.body()) != null) {
                        binding.userFollowingCount.setText(WidgetPresenter.valueFormatter(following.size()));
                        if(followingPresenter.isCurrentUser(userId))
                            followingPresenter.getDatabase().saveUsers(following);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<UserBase>> call, @NonNull Throwable throwable) {
                if(isAlive())
                    throwable.printStackTrace();
            }
        });
    }

    private void requestFavourites() {
        favouritePresenter = new WidgetPresenter<>(getContext());
        favouritePresenter.getParams().putLong(KeyUtils.arg_userId, userId);
        favouritePresenter.requestData(KeyUtils.USER_FAVOURITES_REQ, getContext(), new RetroCallback<Favourite>() {
            @Override
            public void onResponse(@NonNull Call<Favourite> call, @NonNull Response<Favourite> response) {
                if(isAlive()) {
                    if(response.isSuccessful() && (favourites = response.body()) != null) {
                        if(favouritePresenter.isCurrentUser(userId)) {
                            favourites.setId(userId);
                            favouritePresenter.getDatabase().saveFavourite(favourites);
                        }
                        binding.userFavouritesCount.setText(WidgetPresenter.valueFormatter(favourites.getFavouritesCount()));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Favourite> call, @NonNull Throwable throwable) {
                if(isAlive())
                    throwable.printStackTrace();
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
        if(followersPresenter != null)
            followersPresenter.onDestroy();
        if(followingPresenter != null)
            followingPresenter.onDestroy();
        fragmentManager = null;
        mBottomSheet = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.user_favourites_container:
                if(favourites == null)
                    NotifyUtil.makeText(getContext(), R.string.text_activity_loading, Toast.LENGTH_SHORT).show();
                else {
                    Intent intent = new Intent(getContext(), FavouriteActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(KeyUtils.arg_userId, userId);
                    getContext().startActivity(intent);
                }
                break;
            case R.id.user_followers_container:
                if(followers == null)
                    NotifyUtil.makeText(getContext(), R.string.text_activity_loading, Toast.LENGTH_SHORT).show();
                else if (fragmentManager != null && followers.size() > 0){
                    mBottomSheet = new BottomSheetUsers.Builder()
                            .setModel(followers)
                            .setTitle(R.string.title_bottom_sheet_followers)
                            .build();
                    mBottomSheet.show(fragmentManager, mBottomSheet.getTag());
                }
                break;
            case R.id.user_following_container:
                if(following == null)
                    NotifyUtil.makeText(getContext(), R.string.text_activity_loading, Toast.LENGTH_SHORT).show();
                else if (fragmentManager != null && following.size() > 0){
                    mBottomSheet = new BottomSheetUsers.Builder()
                            .setModel(following)
                            .setTitle(R.string.title_bottom_sheet_following)
                            .build();
                    mBottomSheet.show(fragmentManager, mBottomSheet.getTag());
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
        if(consumer.getRequestMode() == KeyUtils.ACTION_FOLLOW_TOGGLE_REQ) {
            if(followers != null) {
                if(followers.contains(consumer.getChangeModel()))
                    followers.remove(consumer.getChangeModel());
                else
                    followers.add(consumer.getChangeModel());
                if(isAlive())
                    binding.userFollowersCount.setText(WidgetPresenter.valueFormatter(followers.size()));
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
