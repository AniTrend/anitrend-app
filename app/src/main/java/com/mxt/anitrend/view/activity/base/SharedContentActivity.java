package com.mxt.anitrend.view.activity.base;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textfield.TextInputEditText;
import androidx.core.app.ShareCompat;
import androidx.appcompat.widget.AppCompatImageView;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import com.annimon.stream.IntPair;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.spinner.IconArrayAdapter;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.base.custom.view.image.AppCompatTintImageView;
import com.mxt.anitrend.base.custom.view.text.SingleLineTextView;
import com.mxt.anitrend.base.interfaces.event.BottomSheetListener;
import com.mxt.anitrend.base.interfaces.event.ItemClickListener;
import com.mxt.anitrend.databinding.ActivityShareContentBinding;
import com.mxt.anitrend.model.entity.anilist.FeedList;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.Settings;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.DialogUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.MarkDownUtil;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.view.sheet.BottomSheetGiphy;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by max on 2017/12/14.
 * share content intent activity
 */

public class SharedContentActivity extends ActivityBase<FeedList, BasePresenter> implements BottomSheetListener,
        BaseConsumer.onRequestModelChange<FeedList>, ItemClickListener<Object> {

    private ActivityShareContentBinding binding;
    private BottomSheetBehavior bottomSheetBehavior;
    private BottomSheetBehavior.BottomSheetCallback bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, @BottomSheetBehavior.State int newState) {
            if(isAlive()) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        finish();
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        onStateCollapsed();
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        onStateExpanded();
                        break;
                }
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

        }
    };

    protected @BindView(R.id.toolbar_title) SingleLineTextView toolbarTitle;
    protected @BindView(R.id.toolbar_state) AppCompatImageView toolbarState;
    protected @BindView(R.id.toolbar_search) AppCompatImageView toolbarSearch;

    protected @BindView(R.id.sheet_shared_resource) TextInputEditText sharedResource;
    protected @BindView(R.id.sheet_share_post_type) Spinner sharedResourceType;
    protected @BindView(R.id.sheet_share_post_type_approve) AppCompatTintImageView sharedResourceApprove;

    private Map<Integer, Integer> indexIconMap = new HashMap<Integer, Integer>() {{
        put(0, R.drawable.ic_textsms_white_24dp);
        put(1, R.drawable.ic_link_white_24dp); put(2, R.drawable.ic_crop_original_white_24dp);
        put(3, R.drawable.ic_youtube); put(4, R.drawable.ic_slow_motion_video_white_24dp);
    }};

    /**
     * Some activities may have custom themes and if that's the case
     * override this method and set your own theme style, also if you wish
     * to apply the default navigation bar style for light themes
     * @see ActivityBase#setNavigationStyle() if running android Oreo +
     */
    @Override
    protected void configureActivity() {
        setTheme(new Settings(this).getTheme() == R.style.AppThemeLight ?
                R.style.AppThemeLight_Translucent: R.style.AppThemeDark_Translucent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_share_content);
        bottomSheetBehavior = BottomSheetBehavior.from(binding.designBottomSheet);
        setPresenter(new BasePresenter(getApplicationContext()));
        ButterKnife.bind(this);
        setViewModel(true);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        bottomSheetBehavior.setPeekHeight(CompatUtil.INSTANCE.dipToPx(KeyUtil.PEEK_HEIGHT));
        bottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        IconArrayAdapter iconArrayAdapter = new IconArrayAdapter(this,
                R.layout.adapter_spinner_item, R.id.spinner_text,
                CompatUtil.INSTANCE.getStringList(this, R.array.post_share_types));
        iconArrayAdapter.setIndexIconMap(indexIconMap);
        sharedResourceType.setAdapter(iconArrayAdapter);
        onActivityReady();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    @Override
    protected void onActivityReady() {
        toolbarSearch.setVisibility(View.GONE);
        toolbarTitle.setText(R.string.menu_title_new_activity_post);
        if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            toolbarState.setImageDrawable(CompatUtil.INSTANCE.getTintedDrawable(this, R.drawable.ic_keyboard_arrow_down_grey_600_24dp));
        else
            toolbarState.setImageDrawable(CompatUtil.INSTANCE.getTintedDrawable(this, R.drawable.ic_close_grey_600_24dp));
        toolbarState.setOnClickListener(view -> {
            switch (bottomSheetBehavior.getState()) {
                case BottomSheetBehavior.STATE_EXPANDED:
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    break;
                default:
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    break;
            }
        });
        binding.composerWidget.setItemClickListener(this);
        binding.composerWidget.setLifecycle(getLifecycle());
        binding.composerWidget.setRequestType(KeyUtil.MUT_SAVE_TEXT_FEED);
        updateUI();
    }

    @Override
    protected void updateUI() {
        ShareCompat.IntentReader reader = intentBundleUtil.getSharedIntent();
        if(reader != null) {
            sharedResource.setText(reader.getText());
            if(!reader.getText().equals(reader.getSubject())) {
                if(binding != null && binding.composerWidget != null)
                    binding.composerWidget.setText(reader.getSubject());
            }
        }
    }

    @Override
    protected void makeRequest() {

    }

    @Override
    public void onStateCollapsed() {
        toolbarState.setImageDrawable(CompatUtil.INSTANCE.getTintedDrawable(this, R.drawable.ic_close_grey_600_24dp));
    }

    @Override
    public void onStateExpanded() {
        toolbarState.setImageDrawable(CompatUtil.INSTANCE.getTintedDrawable(this, R.drawable.ic_keyboard_arrow_down_grey_600_24dp));
    }

    @Override @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onModelChanged(BaseConsumer<FeedList> consumer) {
        if(consumer.getRequestMode() == KeyUtil.MUT_SAVE_TEXT_FEED) {
            NotifyUtil.makeText(this, R.string.text_compose_success, R.drawable.ic_insert_emoticon_white_24dp, Toast.LENGTH_SHORT).show();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    @OnClick(R.id.sheet_share_post_type_approve)
    public void getItemSelected() {
        String text = sharedResource.getText().toString();
        @KeyUtil.ShareType int position = sharedResourceType.getSelectedItemPosition();
        switch (position) {
            case KeyUtil.IMAGE_TYPE:
                binding.composerWidget.setText(MarkDownUtil.INSTANCE.convertImage(text));
                break;
            case KeyUtil.LINK_TYPE:
                binding.composerWidget.setText(MarkDownUtil.INSTANCE.convertLink(text));
                break;
            case KeyUtil.WEBM_TYPE:
                binding.composerWidget.setText(MarkDownUtil.INSTANCE.convertVideo(text));
                break;
            case KeyUtil.YOUTUBE_TYPE:
                binding.composerWidget.setText(MarkDownUtil.INSTANCE.convertYoutube(text));
                break;
            case KeyUtil.PLAIN_TYPE:
                binding.composerWidget.setText(text);
                break;
        }
    }

    /**
     * When the target view from {@link View.OnClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been clicked
     * @param data   the model that at the clicked index
     */
    @Override
    public void onItemClick(View target, IntPair<Object> data) {
        switch (target.getId()) {
            case R.id.insert_emoticon:
                break;
            case R.id.insert_gif:
                mBottomSheet = new BottomSheetGiphy.Builder()
                        .setTitle(R.string.title_bottom_sheet_giphy)
                        .build();
                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());
                break;
            case R.id.widget_flipper:
                CompatUtil.INSTANCE.hideKeyboard(this);
                break;
            default:
                DialogUtil.createDialogAttachMedia(target.getId(), binding.composerWidget.getEditor(), this);
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
    public void onItemLongClick(View target, IntPair<Object> data) {

    }
}
