package com.mxt.anitrend.view.index.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
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
import com.mxt.anitrend.adapter.pager.user.UserBasePageAdapter;
import com.mxt.anitrend.api.model.User;
import com.mxt.anitrend.api.structure.FilterTypes;
import com.mxt.anitrend.api.structure.UserStats;
import com.mxt.anitrend.async.RequestApiAction;
import com.mxt.anitrend.custom.Payload;
import com.mxt.anitrend.custom.emoji4j.EmojiUtils;
import com.mxt.anitrend.presenter.detail.UserProfilePresenter;
import com.mxt.anitrend.utils.DialogManager;
import com.mxt.anitrend.utils.ErrorHandler;
import com.mxt.anitrend.utils.TransitionHelper;
import com.mxt.anitrend.view.base.activity.ImagePreviewActivity;
import com.mxt.anitrend.view.base.activity.ListBrowseActivity;
import com.mxt.anitrend.viewmodel.activity.DefaultActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import top.wefor.circularanim.CircularAnim;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class ProfileActivity extends DefaultActivity implements View.OnClickListener {

    public static final String PROFILE_INTENT_KEY = "profile_intent_parcel";

    @BindView(R.id.container) ViewPager mViewPager;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tabs) TabLayout tabLayout;


    @BindView(R.id.user_anime_time_container) View user_anime_time_container;
    @BindView(R.id.user_manga_chaps_container) View user_manga_chap_container;
    @BindView(R.id.user_anime_total_container) View user_anime_total_container;
    @BindView(R.id.user_manga_total_container) View user_manga_total_container;


    @BindView(R.id.profile_banner) ImageView user_banner;
    @BindView(R.id.user_anime_time) TextView user_anime_time;
    @BindView(R.id.user_manga_chaps) TextView user_manga_chap;
    @BindView(R.id.user_anime_total) TextView user_anime_total;
    @BindView(R.id.user_manga_total) TextView user_manga_total;

    private final String KEY_USER = "USER_SAVE_INST";
    private User mCurrentUser;
    private ActionBar mActionBar;
    private UserProfilePresenter mPresenter;

    private ImageView notification_item;
    private ImageView messages_item;
    private ImageView activity_post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if ((mActionBar = getSupportActionBar()) != null)
            mActionBar.setDisplayHomeAsUpEnabled(true);
        user_banner.setOnClickListener(this);

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        try {
            onBeginInit(savedInstanceState);
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void onBeginInit(Bundle savedInstanceState) {
        mViewPager.setOffscreenPageLimit(2);
        if(savedInstanceState == null) {
            mCurrentUser = getIntent().getParcelableExtra(PROFILE_INTENT_KEY);
        }
        else
            mCurrentUser = savedInstanceState.getParcelable(KEY_USER);

        if(mCurrentUser != null) {
            mPresenter = new UserProfilePresenter(ProfileActivity.this, mCurrentUser.getId());
            mActionBar.setTitle(mCurrentUser.getDisplay_name());
            user_anime_time.setText(mCurrentUser.getAnime_time());
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
            Toast.makeText(this, "User model was not initialized, received null", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void updateUI() {
        //decide on which display id to use
        user_anime_time_container.setOnClickListener(this);
        user_manga_chap_container.setOnClickListener(this);
        user_anime_total_container.setOnClickListener(this);
        user_manga_total_container.setOnClickListener(this);

        UserBasePageAdapter mOverViewAdapter = new UserBasePageAdapter(getSupportFragmentManager(), mCurrentUser);
        mViewPager.setAdapter(mOverViewAdapter);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_USER, mCurrentUser);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_profile, menu);
        notification_item = (ImageView) menu.findItem(R.id.action_notification).getActionView();
        messages_item = (ImageView) menu.findItem(R.id.action_message).getActionView();
        activity_post = (ImageView) menu.findItem(R.id.action_status_post).getActionView();

        if(mCurrentUser != null) {
            menu.findItem(R.id.action_notification).setVisible(true);
            menu.findItem(R.id.action_message).setVisible(true);
            menu.findItem(R.id.action_status_post).setVisible(true);


            notification_item.setImageDrawable(mCurrentUser.getNotifications() > 0?
                                 ContextCompat.getDrawable(this, R.drawable.ic_notifications_active_white_24dp):
                                 ContextCompat.getDrawable(this, R.drawable.ic_notifications_none_white_24dp)
                        );
            notification_item.setPadding(0,0,20,0);

            messages_item.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_mail_outline_white_24dp));
            messages_item.setPadding(25,0,30,0);

            activity_post.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_chat_bubble_outline_white_24dp));
            activity_post.setPadding(25,0,15,0);

            notification_item.setClickable(true);
            activity_post.setClickable(true);
            messages_item.setClickable(true);

            notification_item.setOnClickListener(this);
            messages_item.setOnClickListener(this);
            activity_post.setOnClickListener(this);

            showTutorial();
        }
        return true;
    }



    private void showTutorial() {

        final MaterialTapTargetPrompt.Builder notification = new MaterialTapTargetPrompt.Builder(ProfileActivity.this)
                //or use ContextCompat.getColor(this, R.color.colorAccent)
                .setFocalColourFromRes(R.color.colorAccent)
                .setBackgroundColourFromRes(R.color.colorDarkKnight)
                .setTarget(notification_item)
                .setPrimaryText(R.string.tip_notifications_title)
                .setSecondaryText(R.string.tip_notifications_text)
                .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener() {
                    @Override
                    public void onHidePrompt(MotionEvent event, boolean tappedTarget) {

                    }

                    @Override
                    public void onHidePromptComplete() {
                        mPresenter.getAppPrefs().setNotificationTip();
                        mPresenter.showAlertTip();
                    }
                });

        final MaterialTapTargetPrompt.Builder message = new MaterialTapTargetPrompt.Builder(ProfileActivity.this)
                //or use ContextCompat.getColor(this, R.color.colorAccent)
                .setFocalColourFromRes(R.color.colorAccent)
                .setBackgroundColourFromRes(R.color.colorDarkKnight)
                .setTarget(messages_item)
                .setPrimaryText(R.string.tip_messsages_title)
                .setSecondaryText(R.string.tip_messages_text)
                .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener() {
                    @Override
                    public void onHidePrompt(MotionEvent event, boolean tappedTarget) {

                    }

                    @Override
                    public void onHidePromptComplete() {
                        mPresenter.getAppPrefs().setMessageTip();
                        if(mPresenter.getAppPrefs().getNotificationTip())
                            notification.show();
                    }
                });

        final MaterialTapTargetPrompt.Builder status = new MaterialTapTargetPrompt.Builder(ProfileActivity.this)
                //or use ContextCompat.getColor(this, R.color.colorAccent)
                .setFocalColourFromRes(R.color.colorAccent)
                .setBackgroundColourFromRes(R.color.colorDarkKnight)
                .setTarget(activity_post)
                .setPrimaryText(R.string.tip_status_post_title)
                .setSecondaryText(R.string.tip_status_post_text)
                .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener() {
                    @Override
                    public void onHidePrompt(MotionEvent event, boolean tappedTarget) {

                    }

                    @Override
                    public void onHidePromptComplete() {
                        mPresenter.getAppPrefs().setStatusPost();
                        if(mPresenter.getAppPrefs().getMessageTip())
                            message.show();
                    }
                });

        if(mPresenter.getAppPrefs().getStatusPostTip())
            status.show();
        else
            mPresenter.showAlertTip();
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
                intent = new Intent(ProfileActivity.this, ImagePreviewActivity.class);
                intent.putExtra(ImagePreviewActivity.IMAGE_SOURCE, mCurrentUser.getImage_url_banner());
                TransitionHelper.startSharedImageTransition(ProfileActivity.this, v, getString(R.string.transition_image_preview), intent);
                break;
            case R.id.action_notification:
                mCurrentUser.setNotifications(0);
                CircularAnim.fullActivity(ProfileActivity.this, notification_item).colorOrImageRes(mPresenter.getAppPrefs().isLightTheme()?R.color.colorAccent_Ripple:R.color.colorDarkKnight).go(new CircularAnim.OnAnimationEndListener() {
                    @Override
                    public void onAnimationEnd() {
                        startActivity(new Intent(ProfileActivity.this, NotificationActivity.class));
                    }
                });
                break;
            case R.id.action_message:
                CircularAnim.fullActivity(ProfileActivity.this, messages_item).colorOrImageRes(mPresenter.getAppPrefs().isLightTheme()?R.color.colorAccent_Ripple:R.color.colorDarkKnight).go(new CircularAnim.OnAnimationEndListener() {
                    @Override
                    public void onAnimationEnd() {
                        startActivity(new Intent(ProfileActivity.this, MessageActivity.class));
                    }
                });
                break;
            case R.id.action_status_post:
                new DialogManager(this).createDialogActivityPost(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                        switch (which) {
                            case POSITIVE:
                                EditText editText = dialog.getInputEditText();
                                if(editText != null) {
                                    if(!TextUtils.isEmpty(editText.getText())) {
                                        Payload.ActivityStruct status = new Payload.ActivityStruct(EmojiUtils.hexHtmlify(editText.getText().toString()));
                                        RequestApiAction.ActivityActions<ResponseBody> request = new RequestApiAction.ActivityActions<>(getApplicationContext(), new Callback<ResponseBody>() {
                                            @Override
                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                if (!isFinishing() || !isDestroyed()) {
                                                    if (response.isSuccessful() && response.body() != null) {
                                                        dialog.dismiss();
                                                        mPresenter.makeAlerterSuccess(ProfileActivity.this, getString(R.string.completed_success));
                                                    }
                                                    else
                                                        Toast.makeText(ProfileActivity.this, ErrorHandler.getError(response).toString(), Toast.LENGTH_LONG).show();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                if(!isFinishing() || !isDestroyed()) {
                                                    t.printStackTrace();
                                                    Toast.makeText(ProfileActivity.this, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        }, FilterTypes.ActionType.ACTIVITY_CREATE, status);
                                        request.execute();
                                        Toast.makeText(ProfileActivity.this, R.string.Sending, Toast.LENGTH_SHORT).show();
                                    } else {
                                        mPresenter.makeAlerterWarning(ProfileActivity.this ,getString(R.string.input_empty_warning));
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
                break;
            case R.id.user_anime_total_container:
                intent = new Intent(getApplicationContext(), ListBrowseActivity.class);
                intent.putExtra(ListBrowseActivity.USER_ID, mPresenter.getCurrentUser().getId());
                intent.putExtra(ListBrowseActivity.CONT_TYPE, FilterTypes.SeriesType.ANIME.ordinal());
                startActivity(intent);
                break;
            case R.id.user_anime_time_container:
                mPresenter.createSuperToast(this, String.format(getString(R.string.text_user_anime_time), mCurrentUser.getAnime_time()), R.drawable.ic_play_circle_outline_white_24dp, Style.TYPE_STANDARD);
                break;
            case R.id.user_manga_total_container:
                intent = new Intent(getApplicationContext(), ListBrowseActivity.class);
                intent.putExtra(ListBrowseActivity.USER_ID, mPresenter.getCurrentUser().getId());
                intent.putExtra(ListBrowseActivity.CONT_TYPE, FilterTypes.SeriesType.MANGA.ordinal());
                startActivity(intent);
                break;
            case R.id.user_manga_chaps_container:
                mPresenter.createSuperToast(this, String.format(getString(R.string.text_user_manga_chapters), mCurrentUser.getManga_chap()), R.drawable.ic_book_white_24dp, Style.TYPE_STANDARD);
                break;
        }
    }
}
