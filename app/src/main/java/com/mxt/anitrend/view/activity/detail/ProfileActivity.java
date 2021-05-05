package com.mxt.anitrend.view.activity.detail;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.ViewPager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.pager.detail.ProfilePageAdapter;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.base.custom.view.image.WideImageView;
import com.mxt.anitrend.databinding.ActivityProfileBinding;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.TutorialUtil;
import com.mxt.anitrend.util.graphql.GraphUtil;
import com.mxt.anitrend.view.sheet.BottomSheetComposer;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.wax911.library.model.request.QueryContainerBuilder;

/**
 * Created by max on 2017/11/14.
 * Profile activity
 */

public class ProfileActivity extends ActivityBase<UserBase, BasePresenter> implements View.OnClickListener {

    protected @BindView(R.id.toolbar) Toolbar toolbar;
    protected @BindView(R.id.page_container) ViewPager viewPager;
    protected @BindView(R.id.smart_tab) SmartTabLayout smartTabLayout;

    private ActivityProfileBinding binding;
    private String userName;
    private UserBase model;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);
        setPresenter(new BasePresenter(getApplicationContext()));
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        disableToolbarTitle();
        setViewModel(true);
        if(getIntent().hasExtra(KeyUtil.arg_id))
            id = getIntent().getLongExtra(KeyUtil.arg_id, -1);
        if(getIntent().hasExtra(KeyUtil.arg_userName))
            userName = getIntent().getStringExtra(KeyUtil.arg_userName);
        if(getIntent().hasExtra(KeyUtil.arg_mediaType)) {
            Intent intent = new Intent(this, MediaListActivity.class);
            intent.putExtras(getIntent().getExtras());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mActionBar.setHomeAsUpIndicator(CompatUtil.INSTANCE.getDrawable(this, R.drawable.ic_arrow_back_white_24dp));
        ProfilePageAdapter profilePageAdapter = new ProfilePageAdapter(getSupportFragmentManager(), getApplicationContext());
        profilePageAdapter.setParams(getIntent().getExtras());
        viewPager.setAdapter(profilePageAdapter);
        viewPager.setOffscreenPageLimit(offScreenLimit);
        smartTabLayout.setViewPager(viewPager);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(model == null)
            onActivityReady();
        else
            updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        if(!getPresenter().isCurrentUser(id, userName))
            menu.findItem(R.id.action_notification).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_notification:
                startActivity(new Intent(ProfileActivity.this, NotificationActivity.class));
                //CompatUtil.startRevealAnim(this, item.getActionView(), new Intent(ProfileActivity.this, NotificationActivity.class));
                return true;
            case R.id.action_message:
                if(model != null) {
                    if (getPresenter().isCurrentUser(model.getId()))
                        startActivity(new Intent(ProfileActivity.this, MessageActivity.class));
                    else {
                        mBottomSheet = new BottomSheetComposer.Builder().setUserModel(model)
                                .setRequestMode(KeyUtil.MUT_SAVE_MESSAGE_FEED)
                                .setTitle(R.string.text_message_to)
                                .build();
                        mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());
                    }
                } else
                    NotifyUtil.INSTANCE.makeText(this, R.string.text_activity_loading, Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    @Override
    protected void onActivityReady() {
        if(id == -1 && userName == null)
            NotifyUtil.INSTANCE.createAlerter(this, R.string.text_user_model, R.string.layout_empty_response, R.drawable.ic_warning_white_18dp, R.color.colorStateRed);
        else
            makeRequest();
    }

    @Override
    protected void updateUI() {
        binding.setOnClickListener(this);
        binding.profileStatsWidget.setParams(getIntent().getExtras());
        WideImageView.setImage(binding.profileBanner, model.getBannerImage());
        if(getPresenter().isCurrentUser(model.getId())) {
            new TutorialUtil().setContext(this)
                    .setFocalColour(R.color.colorGrey600)
                    .setTapTarget(KeyUtil.KEY_NOTIFICATION_TIP)
                    .setSettings(getPresenter().getSettings())
                    .showTapTarget(R.string.tip_notifications_title,
                            R.string.tip_notifications_text, R.id.action_notification);
        } else {
            new TutorialUtil().setContext(this)
                    .setFocalColour(R.color.colorGrey600)
                    .setTapTarget(KeyUtil.KEY_MESSAGE_TIP)
                    .setSettings(getPresenter().getSettings())
                    .showTapTarget(R.string.tip_compose_message_title,
                            R.string.tip_compose_message_text, R.id.action_message);
        }

        getPresenter().notifyAllListeners(new BaseConsumer<>(KeyUtil.USER_BASE_REQ, model), false);
    }

    @Override
    protected void makeRequest() {
        QueryContainerBuilder queryContainer = GraphUtil.INSTANCE.getDefaultQuery(false)
                .putVariable(KeyUtil.arg_userName, userName);
        if(id > 0)
            queryContainer.putVariable(KeyUtil.arg_id, id);

        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.USER_BASE_REQ, getApplicationContext());
    }

    /**
     * Called when the model state is changed.
     *
     * @param model The new data
     */
    @Override
    public void onChanged(@Nullable UserBase model) {
        super.onChanged(model);
        if(model != null) {
            this.id = model.getId();
            this.model = model;
            updateUI();
        } else
            NotifyUtil.INSTANCE.createAlerter(this, R.string.text_user_model, R.string.layout_empty_response, R.drawable.ic_warning_white_18dp, R.color.colorStateRed);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.profile_banner:
                CompatUtil.INSTANCE.imagePreview(view, model.getBannerImage(), R.string.image_preview_error_profile_banner);
                break;
        }
    }
}