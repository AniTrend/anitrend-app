package com.mxt.anitrend.view.index.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.johnpersano.supertoasts.library.Style;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.StatusAdapter;
import com.mxt.anitrend.api.model.UserActivity;
import com.mxt.anitrend.api.model.UserSmall;
import com.mxt.anitrend.api.structure.FilterTypes;
import com.mxt.anitrend.async.RequestApiAction;
import com.mxt.anitrend.custom.Payload;
import com.mxt.anitrend.custom.RecyclerViewAdapter;
import com.mxt.anitrend.custom.StatefulRecyclerView;
import com.mxt.anitrend.custom.bottomsheet.BottomSheet;
import com.mxt.anitrend.custom.bottomsheet.BottomSheetLikes;
import com.mxt.anitrend.event.MultiInteractionListener;
import com.mxt.anitrend.event.RecyclerLoadListener;
import com.mxt.anitrend.presenter.index.UserActivityPresenter;
import com.mxt.anitrend.utils.DialogManager;
import com.mxt.anitrend.utils.ErrorHandler;
import com.mxt.anitrend.utils.PatternMatcher;
import com.mxt.anitrend.view.base.activity.ImagePreviewActivity;
import com.mxt.anitrend.view.detail.activity.UserReplyActivity;
import com.mxt.anitrend.viewmodel.activity.DefaultActivity;
import com.nguyenhoanglam.progresslayout.ProgressLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import butterknife.BindView;
import butterknife.ButterKnife;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by max on 2017/04/02.
 */
public class MessageActivity extends DefaultActivity implements Callback<List<UserActivity>>, SwipeRefreshLayout.OnRefreshListener, MultiInteractionListener, RecyclerLoadListener {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.content_notification) ProgressLayout progressLayout;
    @BindView(R.id.notification_pull_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.notification_recycler) StatefulRecyclerView recyclerView;

    private ActionBar mActionBar;

    private int mPage = 1;
    private boolean isLimit;
    private List<UserActivity> mData;

    private final String MODEL_LIMIT = "loading_limit_reached";
    private final String MODEL_PAGE = "current_page_number";
    private final String MODEL_CACHE = "model_list";

    private GridLayoutManager mLayoutManager;
    private UserActivityPresenter mPresenter;
    private RecyclerViewAdapter<UserActivity> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if ((mActionBar = getSupportActionBar()) != null)
            mActionBar.setDisplayHomeAsUpEnabled(true);
        mPresenter = new UserActivityPresenter(getApplicationContext());
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        progressLayout.showLoading();
        swipeRefreshLayout.setOnRefreshListener(this);
        recyclerView.setHasFixedSize(true); //originally set to fixed size true
        recyclerView.setNestedScrollingEnabled(false);//set to false if somethings fail to work properly
        mLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.card_col_size_home));
        recyclerView.setLayoutManager(mLayoutManager);

        if(mData == null)
            mPresenter.beginAsync(this, mPage, FilterTypes.ActivtyTypes[FilterTypes.ActivityType.MESSAGE.ordinal()]);
        else
            updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addScrollLoadTrigger() {
        if(!recyclerView.hasOnScrollListener()) {
            mPresenter.initListener(mLayoutManager, mPage, this);
            recyclerView.addOnScrollListener(mPresenter);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //outState.putParcelable(VIEW_PARCABLE, recyclerView.onSaveInstanceState());
        outState.putInt(MODEL_PAGE, mPage);
        outState.putBoolean(MODEL_LIMIT, isLimit);
        outState.putParcelableArrayList(MODEL_CACHE, (ArrayList<? extends Parcelable>) mData);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null) {
            //recyclerView.onRestoreInstanceState(savedInstanceState.getParcelable(VIEW_PARCABLE));
            mPage = savedInstanceState.getInt(MODEL_PAGE);
            isLimit = savedInstanceState.getBoolean(MODEL_LIMIT);
            mData = savedInstanceState.getParcelableArrayList(MODEL_CACHE);
        }
    }

    @Override
    public void onResponse(Call<List<UserActivity>> call, Response<List<UserActivity>> response) {
        if(response.body() != null && response.body().size() > 0) {
            if (mData == null)
                mData = response.body();
            else
                mData.addAll(response.body());
        } else if(response.isSuccessful() && response.body() != null && mPage != 1) {
            isLimit = true;
        }
        updateUI();
    }

    @Override
    protected void updateUI() {
        if(recyclerView != null) {
            if(swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);
            if(mAdapter == null) {
                mAdapter = new StatusAdapter(mData, getApplicationContext(), mPresenter.getAppPrefs(), mPresenter.getApiPrefs(), this);
                recyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.onDataSetModified(mData);
            }
            if (mAdapter.getItemCount() > 0) {
                progressLayout.showContent();
                addScrollLoadTrigger();
            } else
                progressLayout.showEmpty(ContextCompat.getDrawable(getApplicationContext(), R.drawable.request_empty), getString(R.string.layout_empty_response));
        }
    }

    @Override
    public void onFailure(Call<List<UserActivity>> call, Throwable t) {
        progressLayout.showError(ContextCompat.getDrawable(getApplicationContext(), R.drawable.request_error), t.getLocalizedMessage(), getString(R.string.try_again), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressLayout.showLoading();
                mPresenter.beginAsync(MessageActivity.this, mPage, FilterTypes.ActivtyTypes[FilterTypes.ActivityType.MESSAGE.ordinal()]);
            }
        });
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        JCVideoPlayer.releaseAllVideos();
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        mPage = 1;
        mData = null;
        mPresenter.onRefreshPage();
        mPresenter.beginAsync(this, mPage, FilterTypes.ActivtyTypes[FilterTypes.ActivityType.MESSAGE.ordinal()]);
    }

    @Override
    public void onItemClick(final int index, int viewId) {
        Intent intent;
        final UserActivity mItem = mData.get(index);
        switch (viewId) {
            case R.id.status_avatar:
                intent = new Intent(this, UserProfileActivity.class);
                intent.putExtra(UserProfileActivity.PROFILE_INTENT_KEY, mItem.getUsers().get(0));
                startActivity(intent);
                break;
            case R.id.status_extra_img:
                // Open the clicked series
                intent = new Intent(this, ImagePreviewActivity.class);
                Matcher matcher = PatternMatcher.findImages(mItem.getValue());
                if(matcher.find())
                    intent.putExtra(ImagePreviewActivity.IMAGE_SOURCE, matcher.group(matcher.groupCount()).replace("(", "").replace(")", ""));
                startActivity(intent);
                break;
            case R.id.likes_viewer:
                int size = mItem.getLikes().size();
                if(size > 0) {
                    BottomSheet mSheet = BottomSheetLikes.newInstance(getString(R.string.title_bottom_sheet_likes, size), mItem.getLikes());
                    mSheet.show(getSupportFragmentManager(), mSheet.getTag());
                } else {
                    mPresenter.createSuperToast(MessageActivity.this, getString(R.string.text_no_likes), R.drawable.ic_info_outline_white_18dp, Style.TYPE_STANDARD);
                }
                break;
            case R.id.status_comment:
                // Open comment activity
                intent = new Intent(this, UserReplyActivity.class);
                intent.putExtra(UserReplyActivity.USER_ACTIVITY_EXTRA, mItem);
                startActivity(intent);
                break;
            case R.id.mFlipper:
                // Like the feed
                Payload.ActionIdBased actionIdBased = new Payload.ActionIdBased(mItem.getId());
                RequestApiAction.IdActions userPostActions = new RequestApiAction.IdActions(getApplicationContext(), new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (!isDestroyed() || !isFinishing()) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<UserSmall> mItemLikes = mItem.getLikes();

                                if(mItemLikes.contains(mPresenter.getCurrentUser()))
                                    mItemLikes.remove(mPresenter.getCurrentUser());
                                else
                                    mItemLikes.add(mPresenter.getCurrentUser());
                                mData.set(index, mItem);
                                mAdapter.onItemChanged(mData, index);
                            }
                            else {
                                Toast.makeText(MessageActivity.this, ErrorHandler.getError(response).toString(), Toast.LENGTH_LONG).show();
                                mAdapter.refreshItem(index);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        if(!isDestroyed() || !isFinishing())
                            try {
                                t.printStackTrace();
                                Toast.makeText(getApplicationContext(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                }, FilterTypes.ActionType.ACTIVITY_FAVOURITE, actionIdBased);
                userPostActions.execute();
                break;
            case R.id.status_edit:
                new DialogManager(this).createDialogActivityEdit(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                        switch (which) {
                            case POSITIVE:
                                EditText editText = dialog.getInputEditText();
                                if(editText != null) {
                                    if(!TextUtils.isEmpty(editText.getText())) {
                                        Payload.ActivityStruct status = new Payload.ActivityStruct(editText.getText().toString());
                                        RequestApiAction.ActivityActions<UserActivity> request = new RequestApiAction.ActivityActions<>(getApplicationContext(), new Callback<UserActivity>() {
                                            @Override
                                            public void onResponse(Call<UserActivity> call, Response<UserActivity> response) {
                                                if(!isDestroyed() || !isFinishing()) {
                                                    if (response.isSuccessful() && response.body() != null) {
                                                        dialog.dismiss();
                                                        mItem.setValue(response.body().getValue());
                                                        mData.set(index, mItem);
                                                        mAdapter.onItemChanged(mData, index);
                                                    }
                                                    else
                                                        Toast.makeText(MessageActivity.this, ErrorHandler.getError(response).toString(), Toast.LENGTH_LONG).show();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<UserActivity> call, Throwable t) {
                                                if(!isFinishing() || !isDestroyed()) {
                                                    try {
                                                        t.printStackTrace();
                                                        Toast.makeText(getApplicationContext(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        }, FilterTypes.ActionType.ACTIVITY_EDIT, status);
                                        request.execute();
                                        mPresenter.createSuperToast(MessageActivity.this, getString(R.string.text_sending_request), R.drawable.ic_info_outline_white_18dp, Style.TYPE_PROGRESS_BAR);
                                    } else {
                                        Toast.makeText(getApplicationContext(), R.string.input_empty_warning, Toast.LENGTH_SHORT).show();
                                    }
                                }
                                break;
                            case NEUTRAL:
                                // TODO: 2017/05/16 Open bottom bar for inserting media
                                break;
                            case NEGATIVE:
                                dialog.dismiss();
                                break;
                        }
                    }
                }, mItem.getSpannedValue());
                break;
            case R.id.status_delete:
                new DialogManager(this).createDialogMessage(getString(R.string.dialog_confirm_delete),
                        getString(R.string.dialog_delete_message),
                        getString(R.string.Yes),
                        getString(R.string.No),
                        new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                switch (which) {
                                    case POSITIVE:

                                        Payload.ActionIdBased action = new Payload.ActionIdBased(mItem.getId());
                                        RequestApiAction.IdActions deleteAction = new RequestApiAction.IdActions(getApplicationContext(), new Callback<ResponseBody>() {
                                            @Override
                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                if (!isDestroyed() || !isFinishing()) {
                                                    if (response.isSuccessful() && response.body() != null) {
                                                        mData.remove(index);
                                                        mAdapter.onItemRemoved(mData, index);
                                                    }
                                                    else
                                                        Toast.makeText(MessageActivity.this, ErrorHandler.getError(response).toString(), Toast.LENGTH_LONG).show();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                if(!isDestroyed() || !isFinishing())
                                                    try {
                                                        t.printStackTrace();
                                                        Toast.makeText(getApplicationContext(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                            }
                                        }, FilterTypes.ActionType.ACTIVITY_DELETE, action);
                                        deleteAction.execute();
                                        mPresenter.createSuperToast(MessageActivity.this, getString(R.string.text_sending_request), R.drawable.ic_info_outline_white_18dp, Style.TYPE_PROGRESS_BAR);
                                        break;
                                    case NEUTRAL:
                                        break;
                                    case NEGATIVE:
                                        dialog.dismiss();
                                        break;
                                }
                            }
                        });
                break;
        }
    }

    @Override
    public void onLoadMore(int currentPage) {
        if(!isLimit) {
            mPage = currentPage;
            swipeRefreshLayout.setRefreshing(true);
            mPresenter.beginAsync(this, mPage, FilterTypes.ActivtyTypes[FilterTypes.ActivityType.MESSAGE.ordinal()]);
        }
    }

}
