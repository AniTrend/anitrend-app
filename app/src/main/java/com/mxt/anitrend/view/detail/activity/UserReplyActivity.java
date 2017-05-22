package com.mxt.anitrend.view.detail.activity;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder;
import com.github.rubensousa.bottomsheetbuilder.adapter.BottomSheetItemClickListener;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.details.CommentAdapter;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.api.model.UserActivity;
import com.mxt.anitrend.api.model.UserSmall;
import com.mxt.anitrend.api.structure.FilterTypes;
import com.mxt.anitrend.api.structure.UserActivityReply;
import com.mxt.anitrend.async.ActivityReplyTaskFetch;
import com.mxt.anitrend.async.AsyncTaskFetch;
import com.mxt.anitrend.async.RequestApiAction;
import com.mxt.anitrend.custom.Payload;
import com.mxt.anitrend.custom.RecyclerViewAdapter;
import com.mxt.anitrend.custom.StatefulRecyclerView;
import com.mxt.anitrend.custom.bottomsheet.BottomSheet;
import com.mxt.anitrend.custom.bottomsheet.BottomSheetEmoticon;
import com.mxt.anitrend.custom.bottomsheet.BottomSheetLikes;
import com.mxt.anitrend.custom.emoji4j.EmojiManager;
import com.mxt.anitrend.custom.emoji4j.EmojiUtils;
import com.mxt.anitrend.event.InteractionListener;
import com.mxt.anitrend.event.MultiInteractionListener;
import com.mxt.anitrend.presenter.index.MainPresenter;
import com.mxt.anitrend.utils.DialogManager;
import com.mxt.anitrend.utils.ErrorHandler;
import com.mxt.anitrend.utils.ImeAction;
import com.mxt.anitrend.view.index.activity.UserProfileActivity;
import com.mxt.anitrend.viewmodel.activity.DefaultActivity;
import com.nguyenhoanglam.progresslayout.ProgressLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

/**
 * User commenting hub accepts an instance of UserActivity then make a request ot get the replies for that request
 * */
public class UserReplyActivity extends DefaultActivity implements SwipeRefreshLayout.OnRefreshListener, BottomSheetItemClickListener,
        LoaderManager.LoaderCallbacks, MultiInteractionListener, View.OnClickListener, Callback<ResponseBody>, InteractionListener {

    public final static String USER_ACTIVITY_EXTRA = "user_activity_post_instance";
    public final static String USER_ACTIVITY_NOTIFICATION_EXTRA = "user_notification_id_key";

    @BindView(R.id.coordinator) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.activity_avatar) ImageView mAvatar;
    @BindView(R.id.activity_heading) TextView mHeading;
    @BindView(R.id.activity_comments) StatefulRecyclerView recyclerView;

    @BindView(R.id.comment) TextInputEditText mInputEditText;
    @BindView(R.id.mFlipper) ViewFlipper mPostComment;
    @BindView(R.id.insert_emoticon) TextView mEmoticonInsert;
    @BindView(R.id.insert_media) TextView mInsertMedia;

    @BindView(R.id.comment_pull_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.content_comment) ProgressLayout progressLayout;

    @BindView(R.id.activity_subject_header) View mHeaderSubject;
    private ActionBar mActionBar;

    private boolean mPresentLike;
    private MenuItem favMenuItem;
    private MainPresenter mPresenter;

    private int mReplyKey = -1;
    private int mModelLookUpKey;
    private UserActivity mPassedData;
    private List<UserActivityReply> mData;

    private EmoticonLoader emoticonLoader;

    private final String MODEL_IS_FAVOURITE = "model_on_favourite_list";
    private final String MODEL_REPLY_TEXT = "model_reply_value";
    private final String MODEL_KEY_PARAM = "model_activity_key";
    private final String MODEL_OBJ_PARAM = "model_activity_param";
    private final String MODEL_DATA_COLLECTION = "model_activity_data";

    private RecyclerViewAdapter<UserActivityReply> mAdapter;
    private StaggeredGridLayoutManager mLayoutManager;
    private LoaderManager loaderManager;

    private UserSmall mCurrentUser;

    private BottomSheetBehavior mBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if ((mActionBar = getSupportActionBar()) != null)
            mActionBar.setDisplayHomeAsUpEnabled(true);
        if(savedInstanceState != null) {
            mPresentLike = savedInstanceState.getBoolean(MODEL_IS_FAVOURITE);
            mPassedData = savedInstanceState.getParcelable(MODEL_OBJ_PARAM);
            mData = savedInstanceState.getParcelableArrayList(MODEL_DATA_COLLECTION);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        setSupportActionBar(toolbar);
        mPresenter = new MainPresenter(getApplicationContext());
        emoticonLoader = new EmoticonLoader();
        mEmoticonInsert.setVisibility(View.GONE);
        onInit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_generic_detail, menu);
        favMenuItem = menu.findItem(R.id.action_favor_state);
        favMenuItem.setVisible(true);
        favMenuItem.setIcon(ContextCompat.getDrawable(getApplicationContext(), mPresentLike ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp));
        return true;
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        if(mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return;
        }
        else if(mInputEditText.hasFocus()) {
            mInputEditText.clearFocus();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_favor_state:
                Payload.ActionIdBased actionIdBased = new Payload.ActionIdBased(mPassedData.getId());
                RequestApiAction.IdActions userPostActions = new RequestApiAction.IdActions(getApplicationContext(), new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.errorBody() == null) {
                            if (!isFinishing() || !isDestroyed()) {
                                if (mPassedData.getLikes().contains(mCurrentUser)) {
                                    mPassedData.getLikes().remove(mCurrentUser);
                                    favMenuItem.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite_border_white_24dp));
                                }
                                else {
                                    mPassedData.getLikes().add(mCurrentUser);
                                    favMenuItem.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite_white_24dp));
                                }
                            }
                        } else
                            try {
                                if (!isFinishing() || !isDestroyed())
                                    Toast.makeText(getApplicationContext(), response.errorBody().string(), Toast.LENGTH_LONG).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        t.printStackTrace();
                        if(!isFinishing() || !isDestroyed())
                            try {
                                Toast.makeText(getApplicationContext(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                }, FilterTypes.ActionType.ACTIVITY_FAVOURITE, actionIdBased);
                userPostActions.execute();
                mPresenter.createSuperToast(this, getString(R.string.text_sending_request), R.drawable.ic_info_outline_white_18dp, Style.TYPE_PROGRESS_BAR);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onInit() {
        mCurrentUser = mPresenter.getCurrentUser();
        loaderManager = getLoaderManager();
        swipeRefreshLayout.setOnRefreshListener(this);
        recyclerView.setHasFixedSize(true); //originally set to fixed size true
        recyclerView.setNestedScrollingEnabled(false); //set to false if somethings fail to work properly
        mLayoutManager = new StaggeredGridLayoutManager(getResources().getInteger(R.integer.card_col_size_home), StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);

        View bottomSheet = new BottomSheetBuilder(this, coordinatorLayout)
                .setMode(BottomSheetBuilder.MODE_GRID)
                .setBackgroundColorResource(R.color.colorDarkKnight)
                .setItemTextColorResource(R.color.white)
                .setMenu(R.menu.menu_attachments)
                .setItemClickListener(this).createView();

        mBehavior = BottomSheetBehavior.from(bottomSheet);
        mBehavior.setBottomSheetCallback(null);
        updateUI();
    }

    @Override
    protected void updateUI() {
        progressLayout.showLoading();
        //restored from saved instance
        if(mPassedData != null) {
            Begin();
        }
        else {
            Intent action = getIntent();
            if (action.hasExtra(USER_ACTIVITY_EXTRA)) {
                mPassedData = action.getParcelableExtra(USER_ACTIVITY_EXTRA);
                Begin();
            } else if (action.hasExtra(USER_ACTIVITY_NOTIFICATION_EXTRA)) {
                mModelLookUpKey = action.getIntExtra(USER_ACTIVITY_NOTIFICATION_EXTRA, 0);
                new AsyncTaskFetch<>(new Callback<UserActivity>() {
                    @Override
                    public void onResponse(Call<UserActivity> call, Response<UserActivity> response) {
                        if (!isDestroyed() || isFinishing()) {
                            if (response.isSuccessful() && response.body() != null) {
                                mPassedData = response.body();
                                Begin();
                            } else
                                Toast.makeText(UserReplyActivity.this, ErrorHandler.getError(response).toString(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<UserActivity> call, Throwable t) {
                        if (!isDestroyed() || isFinishing()) {
                            t.printStackTrace();
                            Toast.makeText(UserReplyActivity.this, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, getApplicationContext(), mModelLookUpKey).execute(AsyncTaskFetch.RequestType.USER_ACTIVITY_REQ);
                mHeading.setText(R.string.Loading);
            }
        }
    }

    private void Begin() {
        mAvatar.setOnClickListener(this);
        mHeaderSubject.setOnClickListener(this);
        mPostComment.setOnClickListener(this);
        mEmoticonInsert.setOnClickListener(this);
        mInsertMedia.setOnClickListener(this);

        // Load necessary emoticons
        if(EmojiManager.isEmpty())
            emoticonLoader.execute();
        else
            enableEmoticonsPicker();

        Glide.with(getApplicationContext()).load(mPassedData.getUsers().get(0).getImage_url_med())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new BitmapImageViewTarget(mAvatar) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        mAvatar.setImageDrawable(circularBitmapDrawable);
                    }
                });

        if(mPassedData.getSeries() == null) {
            if (mPassedData.getStatus() != null)
                mHeading.setText(String.format(Locale.getDefault(), "%s: %s %s", mPassedData.getUsers().get(0).getDisplay_name(), mPassedData.getStatus(), mPassedData.getSpannedValue()));
            else
                mHeading.setText(String.format(Locale.getDefault(), "%s: %s", mPassedData.getUsers().get(0).getDisplay_name(), mPassedData.getSpannedValue()));
        } else {
            Series series = mPassedData.getSeries();
            String title = null;
            switch (mPresenter.getApiPrefs().getTitleLanguage()) {
                case "romaji":
                    title = series.getTitle_romaji();
                    break;
                case "english":
                    title = series.getTitle_english();
                    break;
                case "japanese":
                    title = series.getTitle_japanese();
                    break;
            }
            mHeading.setText(String.format(Locale.getDefault(), "%s: %s %s - %s", mPassedData.getUsers().get(0).getDisplay_name(), mPassedData.getStatus(), mPassedData.getSpannedValue(), title));
        }
        if(mPassedData.getReplies() == null) {
            if (mPresenter.getSavedParse() == null && mData == null)
                loaderManager.initLoader(getResources().getInteger(R.integer.activity_replies), null, this);
            else
                showData();
        } else {
            mData = mPassedData.getReplies();
            showData();
        }
    }

    private void enableEmoticonsPicker() {
        mEmoticonInsert.setVisibility(View.VISIBLE);
    }

    private void showData() {
        if(recyclerView != null) {
            if(swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);
            mAdapter = new CommentAdapter(mData, getApplicationContext(), mPresenter.getAppPrefs(), mPresenter.getApiPrefs(), this, getSupportFragmentManager());
            recyclerView.setAdapter(mAdapter);

            if (mAdapter.getItemCount() > 0) {
                progressLayout.showContent();
            } else
                progressLayout.showEmpty(ContextCompat.getDrawable(getApplicationContext(), R.drawable.request_empty), getString(R.string.layout_empty_response));
        }

        if(favMenuItem != null) {
            mPresentLike = mPassedData.getLikes().contains(mCurrentUser);
            favMenuItem.setVisible(true);
            favMenuItem.setIcon(ContextCompat.getDrawable(getApplicationContext(), mPresentLike ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp));
        }

        if(mPresenter.getAppPrefs().getHeaderTip()) {
            new MaterialTapTargetPrompt.Builder(this)
                    //or use ContextCompat.getColor(this, R.color.colorAccent)
                    .setFocalColourFromRes(R.color.colorAccent)
                    .setBackgroundColourFromRes(R.color.colorDarkKnight)
                    .setTarget(mHeaderSubject)
                    .setPrimaryText(R.string.tip_more_actions_title)
                    .setSecondaryText(R.string.tip_more_actions_text)
                    .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener() {
                        @Override
                        public void onHidePrompt(MotionEvent event, boolean tappedTarget) {

                        }

                        @Override
                        public void onHidePromptComplete() {
                            mPresenter.getAppPrefs().setHeaderTip();
                        }
                    }).show();
        }
    }

    @Override
    protected void onDestroy() {
        emoticonLoader.cancel(false);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.setParcelable(recyclerView.onSaveInstanceState());
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(mPresenter.getSavedParse() != null)
            recyclerView.onRestoreInstanceState(mPresenter.getSavedParse());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mInputEditText != null)
            outState.putString(MODEL_REPLY_TEXT, mInputEditText.getText().toString());
        outState.putBoolean(MODEL_IS_FAVOURITE, mPresentLike);
        outState.putInt(MODEL_KEY_PARAM, mModelLookUpKey);
        outState.putParcelable(MODEL_OBJ_PARAM, mPassedData);
        outState.putParcelableArrayList(MODEL_DATA_COLLECTION, (ArrayList<? extends Parcelable>) mData);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mModelLookUpKey = savedInstanceState.getInt(MODEL_KEY_PARAM);
        mInputEditText.setText(savedInstanceState.getString(MODEL_REPLY_TEXT));
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new ActivityReplyTaskFetch(getApplicationContext(), mPassedData.getId());
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        mData = (List<UserActivityReply>) data;
        showData();
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        loaderManager.restartLoader(getResources().getInteger(R.integer.activity_replies), null, this);
    }

    @Override
    public void onItemClick(final int index, int viewId) {
        final UserActivityReply reply = mData.get(index);
        switch (viewId) {
            case R.id.post_avatar:
                Intent intent = new Intent(this, UserProfileActivity.class);
                intent.putExtra(UserProfileActivity.PROFILE_INTENT_KEY, reply.getUser());
                startActivity(intent);
                break;
            case R.id.mFlipper:
                Payload.ActionIdBased actionIdBased = new Payload.ActionIdBased(reply.getId());
                RequestApiAction.IdActions userPostActions = new RequestApiAction.IdActions(getApplicationContext(), new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (!isFinishing() && !isDestroyed()) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<UserSmall> mItemLikes = reply.getLikes();

                                if(mItemLikes.contains(mPresenter.getCurrentUser()))
                                    mItemLikes.remove(mPresenter.getCurrentUser());
                                else
                                    mItemLikes.add(mPresenter.getCurrentUser());
                                mData.set(index, reply);
                                mAdapter.onItemChanged(mData, index);
                            } else {
                                Toast.makeText(UserReplyActivity.this, ErrorHandler.getError(response).toString(), Toast.LENGTH_LONG).show();
                                mAdapter.refreshItem(index);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        if(!isFinishing() && !isDestroyed())
                            try {
                                t.printStackTrace();
                                Toast.makeText(UserReplyActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                mAdapter.refreshItem(index);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                }, FilterTypes.ActionType.ACTIVITY_REPLY_FAVOURITE, actionIdBased);
                userPostActions.execute();
                break;
            case R.id.post_reply:
                mInputEditText.append(String.format(Locale.getDefault(), "@%s ", reply.getUser().getDisplay_name()));
                Toast.makeText(this, R.string.text_reply_action_feedback, Toast.LENGTH_SHORT).show();
                break;
            case R.id.post_edit:
                mReplyKey = reply.getId();
                mInputEditText.setText(reply.getReply_value());
                break;
            case R.id.post_delete:
                new DialogManager(this).createDialogMessage(getString(R.string.dialog_confirm_delete),
                        getString(R.string.dialog_delete_message),
                        getString(R.string.Yes),
                        getString(R.string.No),
                        new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                switch (which) {
                                    case POSITIVE:
                                        Payload.ActionIdBased action = new Payload.ActionIdBased(reply.getId());
                                        RequestApiAction.IdActions deleteAction = new RequestApiAction.IdActions(getApplicationContext(), new Callback<ResponseBody>() {
                                            @Override
                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                if(!isFinishing() || !isDestroyed()) {
                                                    if (response.isSuccessful() && response.body() != null) {
                                                        mData.remove(index);
                                                        mAdapter.onItemRemoved(mData, index);
                                                    }
                                                    else
                                                        Toast.makeText(UserReplyActivity.this, ErrorHandler.getError(response).toString(), Toast.LENGTH_LONG).show();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                if(!isFinishing() || !isDestroyed()) {
                                                    t.printStackTrace();
                                                    Toast.makeText(UserReplyActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }, FilterTypes.ActionType.ACTIVITY_REPLY_DELETE, action);
                                        deleteAction.execute();
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

    private void attemptPostComment() {
        String replyText = mInputEditText.getText().toString();
        if(!TextUtils.isEmpty(replyText)) {
            ImeAction.hideSoftKeyboard(this);

            if (mPostComment.getDisplayedChild() == 0) {
                mPostComment.showNext();
                replyText = EmojiUtils.hexHtmlify(replyText);
                Payload.ActivityStruct actionReply = new Payload.ActivityStruct(mReplyKey, replyText, mPassedData.getId());

                RequestApiAction.ActivityActions<ResponseBody> replyAction = new RequestApiAction.ActivityActions<>(getApplicationContext(), this,
                        mReplyKey==-1?FilterTypes.ActionType.ACTIVITY_REPLY:FilterTypes.ActionType.ACTIVITY_REPLY_EDIT,
                        actionReply);

                replyAction.execute();
            }
            else {
                Toast.makeText(this, R.string.busy_please_wait, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.warning_empty_input, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_avatar:
                Intent intent = new Intent(this, UserProfileActivity.class);
                intent.putExtra(UserProfileActivity.PROFILE_INTENT_KEY, mPassedData.getUsers().get(0));
                startActivity(intent);
                break;
            case R.id.mFlipper:
                attemptPostComment();
                break;
            case R.id.insert_emoticon:
                BottomSheet bottomSheet = BottomSheetEmoticon.newInstance(getString(R.string.text_emoticons_scroll), EmojiManager.data());
                bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
                break;
            case R.id.insert_media:
                mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                break;
            case R.id.activity_subject_header:
                new DialogManager(this).createDialogMessage(getString(R.string.text_post_information),
                        mPassedData.getSpannedValue(),
                        getString(R.string.title_view_likes),
                        getString(android.R.string.cancel),
                        new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                switch (which) {
                                    case POSITIVE:
                                        int size = mPassedData.getLikes().size();
                                        if(size > 0) {
                                            BottomSheet mSheet = BottomSheetLikes.newInstance(getString(R.string.title_bottom_sheet_likes, size), mPassedData.getLikes());
                                            mSheet.show(getSupportFragmentManager(), mSheet.getTag());
                                        } else {
                                            mPresenter.createSuperToast(UserReplyActivity.this, getString(R.string.text_no_likes), R.drawable.ic_info_outline_white_18dp, Style.TYPE_STANDARD);
                                        }
                                        break;
                                    case NEUTRAL:
                                        break;
                                    case NEGATIVE:
                                        dialog.dismiss();
                                        break;
                                }
                            }
                        }
                );
                break;
        }
    }

    /**
     * Invoked for a received HTTP response.
     * <p>
     * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
     * Call {@link Response#isSuccessful()} to determine if the response indicates success.
     *
     * @param call
     * @param response
     */
    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        if(!isFinishing() && !isDestroyed()) {
            if(response.isSuccessful() && response.body() != null) {
                swipeRefreshLayout.setRefreshing(true);
                mInputEditText.setText("");
                mReplyKey = -1;
                onRefresh();
            } else {
                Toast.makeText(UserReplyActivity.this, ErrorHandler.getError(response).toString(), Toast.LENGTH_LONG).show();
            }
            if(mPostComment.getDisplayedChild() != 0)
                mPostComment.showPrevious();
        }
    }

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     *
     * @param call
     * @param t
     */
    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        if(!isFinishing() && !isDestroyed()) {
            t.printStackTrace();
            if(mPostComment.getDisplayedChild() != 0)
                mPostComment.showPrevious();
            Toast.makeText(this, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemClick(int index) {
        mInputEditText.append(String.format(Locale.getDefault(), "%s", EmojiManager.data().get(index).getEmoji()));
    }

    @Override
    public void onBottomSheetItemClick(MenuItem item) {
        mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        new DialogManager(UserReplyActivity.this).createDialogAttachMedia(item.getItemId(), mInputEditText);
    }

    private class EmoticonLoader extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if(!isCancelled()) {
                try {
                    if(EmojiManager.isEmpty())
                        EmojiManager.initEmojiData(UserReplyActivity.this);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(!isDestroyed() || !isFinishing() || isCancelled()) {
                enableEmoticonsPicker();
            }
        }
    }
}