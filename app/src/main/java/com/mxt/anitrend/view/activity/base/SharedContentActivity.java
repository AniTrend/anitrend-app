package com.mxt.anitrend.view.activity.base;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ShareCompat;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.base.custom.view.image.AppCompatTintImageView;
import com.mxt.anitrend.base.custom.view.text.SingleLineTextView;
import com.mxt.anitrend.base.interfaces.event.BottomSheetListener;
import com.mxt.anitrend.base.interfaces.event.ItemClickListener;
import com.mxt.anitrend.databinding.ActivityShareContentBinding;
import com.mxt.anitrend.model.entity.anilist.FeedList;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.ApplicationPref;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.DialogUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.MarkDown;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.view.sheet.BottomSheetEmoticon;
import com.mxt.anitrend.view.sheet.BottomSheetGiphy;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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

    /**
     * Some activities may have custom themes and if that's the case
     * override this method and set your own theme style, also if you wish
     * to apply the default navigation bar style for light themes
     * @see ActivityBase#setNavigationStyle() if running android Oreo +
     */
    @Override
    protected void configureActivity() {
        setTheme(new ApplicationPref(this).getTheme() == R.style.AppThemeLight ?
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
        bottomSheetBehavior.setPeekHeight(CompatUtil.dipToPx(KeyUtils.PEEK_HEIGHT));
        bottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.post_share_types, R.layout.adapter_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sharedResourceType.setAdapter(adapter);
        onActivityReady();
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onPause();
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
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
            toolbarState.setImageDrawable(CompatUtil.getTintedDrawable(this, R.drawable.ic_keyboard_arrow_down_grey_600_24dp));
        else
            toolbarState.setImageDrawable(CompatUtil.getTintedDrawable(this, R.drawable.ic_close_grey_600_24dp));
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
        binding.composerWidget.setRequestMode(KeyUtils.ACTIVITY_CREATE_REQ);
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
        toolbarState.setImageDrawable(CompatUtil.getTintedDrawable(this, R.drawable.ic_close_grey_600_24dp));
    }

    @Override
    public void onStateExpanded() {
        toolbarState.setImageDrawable(CompatUtil.getTintedDrawable(this, R.drawable.ic_keyboard_arrow_down_grey_600_24dp));
    }

    @Override @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onModelChanged(BaseConsumer<FeedList> consumer) {
        if(consumer.getRequestMode() == KeyUtils.ACTIVITY_CREATE_REQ) {
            NotifyUtil.makeText(this, R.string.text_compose_success, R.drawable.ic_insert_emoticon_white_24dp, Toast.LENGTH_SHORT).show();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    @OnClick(R.id.sheet_share_post_type_approve)
    public void getItemSelected() {
        String text = sharedResource.getText().toString();
        @KeyUtils.ShareType int position = sharedResourceType.getSelectedItemPosition();
        switch (position) {
            case KeyUtils.IMAGE_TYPE:
                binding.composerWidget.setText(MarkDown.convertImage(text));
                break;
            case KeyUtils.LINK_TYPE:
                binding.composerWidget.setText(MarkDown.convertLink(text));
                break;
            case KeyUtils.WEBM_TYPE:
                binding.composerWidget.setText(MarkDown.convertVideo(text));
                break;
            case KeyUtils.YOUTUBE_TYPE:
                binding.composerWidget.setText(MarkDown.convertYoutube(text));
                break;
            case KeyUtils.PLAIN_TYPE:
                binding.composerWidget.setText(text);
                break;
        }
    }

    /**
     * When the target view from {@link View.OnClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been clicked
     * @param data   the model that at the click index
     */
    @Override
    public void onItemClick(View target, Object data) {
        switch (target.getId()) {
            case R.id.insert_emoticon:
                mBottomSheet = new BottomSheetEmoticon.Builder()
                        .setTitle(R.string.text_emoticons_scroll)
                        .build();
                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());
                break;
            case R.id.insert_gif:
                mBottomSheet = new BottomSheetGiphy.Builder()
                        .setTitle(R.string.title_bottom_sheet_giphy)
                        .build();
                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());
                break;
            case R.id.widget_flipper:
                CompatUtil.hideKeyboard(this);
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
     * @param data   the model that at the long click index
     */
    @Override
    public void onItemLongClick(View target, Object data) {

    }
}
