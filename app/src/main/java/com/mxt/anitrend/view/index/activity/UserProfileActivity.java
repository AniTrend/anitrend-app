package com.mxt.anitrend.view.index.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.johnpersano.supertoasts.library.Style;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.pager.user.UserPageAdapter;
import com.mxt.anitrend.api.model.User;
import com.mxt.anitrend.api.model.UserSmall;
import com.mxt.anitrend.base.custom.view.widget.emoji4j.EmojiUtils;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.api.structure.UserStats;
import com.mxt.anitrend.base.custom.async.AsyncTaskFetch;
import com.mxt.anitrend.base.custom.async.RequestApiAction;
import com.mxt.anitrend.base.custom.Payload;
import com.mxt.anitrend.presenter.index.UserActivityPresenter;
import com.mxt.anitrend.util.DialogManager;
import com.mxt.anitrend.util.TransitionHelper;
import com.mxt.anitrend.view.base.activity.ImagePreviewActivity;
import com.mxt.anitrend.view.base.activity.ListBrowseActivity;
import com.mxt.anitrend.viewmodel.activity.DefaultActivity;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

/**
 * Created by max on 2017/04/02.
 * External user profile
 */
public class UserProfileActivity extends DefaultActivity implements Callback<User>, View.OnClickListener {

    public static final String PROFILE_INTENT_KEY = "profile_intent_parcel";

    @BindView(R.id.container) ViewPager mViewPager;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.nts_center) SmartTabLayout tabLayout;
    @BindView(R.id.coordinator) CoordinatorLayout coordinatorLayout;


    @BindView(R.id.user_anime_time_container) View user_anime_time_container;
    @BindView(R.id.user_manga_chaps_container) View user_manga_chap_container;
    @BindView(R.id.user_anime_total_container) View user_anime_total_container;
    @BindView(R.id.user_manga_total_container) View user_manga_total_container;


    @BindView(R.id.profile_banner) ImageView user_banner;
    @BindView(R.id.user_anime_time) TextView user_anime_time;
    @BindView(R.id.user_manga_chaps) TextView user_manga_chap;
    @BindView(R.id.user_anime_total) TextView user_anime_total;
    @BindView(R.id.user_manga_total) TextView user_manga_total;

    private final String KEY_FULL_USER = "full_user_model";
    private final String KEY_MINI_USER = "mini_user_model";

    private UserSmall mTempUser;
    private User mCurrentUser;
    private UserActivityPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        mPresenter = new UserActivityPresenter(UserProfileActivity.this);
        mTempUser = getIntent().getParcelableExtra(PROFILE_INTENT_KEY);
        user_banner.setOnClickListener(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mViewPager.setOffscreenPageLimit(2);
        if (savedInstanceState != null) {
            startInit();
        } else {
            user_anime_time.setText(R.string.Loading);
            user_manga_chap.setText(R.string.Loading);
            user_anime_total.setText(R.string.Loading);
            user_manga_total.setText(R.string.Loading);
            if(mIntentData != null && mTempUser == null)
                new AsyncTaskFetch<>(this, getApplicationContext(), mIntentData).execute(AsyncTaskFetch.RequestType.USER_ACCOUNT_REQ);
            else
                new AsyncTaskFetch<>(this, getApplicationContext(), mTempUser.getId()).execute(AsyncTaskFetch.RequestType.USER_ACCOUNT_REQ);
        }
    }

    @Override
    protected void startInit() {
        if(mCurrentUser != null) {
            user_anime_time.setText(mPresenter.getAnimeTime(mCurrentUser.getAnime_time()));
            user_manga_chap.setText(mCurrentUser.getManga_chap());
            user_anime_total.setText("0");
            user_manga_total.setText("0");

            UserStats userStats = mCurrentUser.getStats();
            if(userStats != null) {
                if(userStats.getStatus_distribution() != null)
                    if(userStats.getStatus_distribution().getAnime() != null)
                        user_anime_total.setText(userStats.getStatus_distribution().getAnime().getTotalAnime());
                if(userStats.getStatus_distribution().getManga() != null)
                    user_manga_total.setText(userStats.getStatus_distribution().getManga().getTotalManga());
            }

            Glide.with(this)
                    .load(mCurrentUser.getImage_url_banner())
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(user_banner);

            updateUI();
        } else{
            Toast.makeText(this, R.string.text_error_request, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void updateUI() {
        //decide on which display id to use
        user_anime_time_container.setOnClickListener(this);
        user_manga_chap_container.setOnClickListener(this);
        user_anime_total_container.setOnClickListener(this);
        user_manga_total_container.setOnClickListener(this);
        UserPageAdapter mOverViewAdapter = new UserPageAdapter(getSupportFragmentManager(), mCurrentUser, getApplicationContext());
        mViewPager.setAdapter(mOverViewAdapter);
        tabLayout.setViewPager(mViewPager);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_MINI_USER, mTempUser);
        outState.putParcelable(KEY_FULL_USER, mCurrentUser);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mTempUser = savedInstanceState.getParcelable(KEY_MINI_USER);
            mCurrentUser = savedInstanceState.getParcelable(KEY_FULL_USER);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_message, menu);
        if(mPresenter.getAppPrefs().getComposeTip()) {
            new MaterialTapTargetPrompt.Builder(this)
                    //or use ContextCompat.getColor(this, R.color.colorAccent)
                    .setFocalColourFromRes(R.color.colorAccent)
                    .setBackgroundColourFromRes(R.color.colorDarkKnight)
                    .setTarget(toolbar.getChildAt(toolbar.getChildCount() - 1))
                    .setPrimaryText(R.string.tip_compose_message_title)
                    .setSecondaryText(R.string.tip_compose_message_text)
                    .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener() {
                        @Override
                        public void onHidePrompt(MotionEvent event, boolean tappedTarget) {

                        }

                        @Override
                        public void onHidePromptComplete() {
                            mPresenter.getAppPrefs().setComposeTip();
                            mPresenter.showAlertTip();
                        }
                    }).show();
        } else {
            mPresenter.showAlertTip();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_compose:
                if(mCurrentUser == null) {
                    Toast.makeText(this, R.string.text_activity_loading, Toast.LENGTH_SHORT).show();
                    return true;
                }
                new DialogManager(this).createDialogInput(getString(R.string.tip_compose_message_title), getString(R.string.text_message_to, mCurrentUser.getDisplay_name()), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                    }
                }, new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                        switch (which) {
                            case POSITIVE:
                                EditText editText = dialog.getInputEditText();
                                if(editText != null) {
                                    if(!TextUtils.isEmpty(editText.getText())) {
                                        Payload.ActivityMessage message = new Payload.ActivityMessage(editText.getText().toString(), mCurrentUser.getId());
                                        RequestApiAction.MessageActions<ResponseBody> action = new RequestApiAction.MessageActions<>(getApplicationContext(), new Callback<ResponseBody>() {
                                            @Override
                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                if(response.isSuccessful() && response.body() != null) {
                                                    if(!isDestroyed() || !isFinishing()) {
                                                        mPresenter.makeAlerterSuccess(UserProfileActivity.this ,getString(R.string.completed_success));
                                                        dialog.dismiss();
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                if(!isDestroyed() || !isFinishing()) {
                                                    t.printStackTrace();
                                                    Toast.makeText(UserProfileActivity.this, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        }, KeyUtils.ActionType.DIRECT_MESSAGE_SEND, message);
                                        action.execute();
                                        Toast.makeText(UserProfileActivity.this, R.string.Sending, Toast.LENGTH_SHORT).show();
                                    } else {
                                        mPresenter.makeAlerterWarning(UserProfileActivity.this ,getString(R.string.input_empty_warning));
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
                });
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResponse(Call<User> call, Response<User> response) {
        if(response.isSuccessful() && response.body() != null) {
            if(!isDestroyed() || !isFinishing())
                try {
                    mCurrentUser = response.body();
                    startInit();
                } catch (Exception ex){
                    ex.printStackTrace();
                }
        }
    }

    @Override
    public void onFailure(Call<User> call, Throwable t) {
        if(!isDestroyed() || !isFinishing()) {
            Toast.makeText(this, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            t.printStackTrace();
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.profile_banner:
                intent = new Intent(UserProfileActivity.this, ImagePreviewActivity.class);
                intent.putExtra(ImagePreviewActivity.IMAGE_SOURCE, mCurrentUser.getImage_url_banner());
                TransitionHelper.startSharedImageTransition(UserProfileActivity.this, v, getString(R.string.transition_image_preview), intent);
                break;
            case R.id.user_anime_total_container:
                intent = new Intent(getApplicationContext(), ListBrowseActivity.class);
                intent.putExtra(ListBrowseActivity.USER_ID, mCurrentUser.getId());
                intent.putExtra(ListBrowseActivity.CONT_TYPE, KeyUtils.ANIME);
                startActivity(intent);
                break;
            case R.id.user_anime_time_container:
                mPresenter.createSuperToast(this, String.format(getString(R.string.text_user_anime_time), mPresenter.getAnimeTime(mCurrentUser.getAnime_time())), R.drawable.ic_play_circle_outline_white_24dp, Style.TYPE_STANDARD);
                break;
            case R.id.user_manga_total_container:
                intent = new Intent(getApplicationContext(), ListBrowseActivity.class);
                intent.putExtra(ListBrowseActivity.USER_ID, mCurrentUser.getId());
                intent.putExtra(ListBrowseActivity.CONT_TYPE, KeyUtils.MANGA);
                startActivity(intent);
                break;
            case R.id.user_manga_chaps_container:
                mPresenter.createSuperToast(this, String.format(getString(R.string.text_user_manga_chapters), mCurrentUser.getManga_chap()), R.drawable.ic_book_white_24dp, Style.TYPE_STANDARD);
                break;
        }
    }
}