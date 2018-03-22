package com.mxt.anitrend.view.sheet;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase;
import com.mxt.anitrend.base.custom.view.editor.ComposerWidget;
import com.mxt.anitrend.base.interfaces.event.ItemClickListener;
import com.mxt.anitrend.model.entity.anilist.FeedList;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.DialogUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by max on 2017/12/13.
 */

public class BottomSheetComposer extends BottomSheetBase implements ItemClickListener<Object>, BaseConsumer.onRequestModelChange<FeedList> {

    protected @BindView(R.id.composer_widget) ComposerWidget composerWidget;

    private @KeyUtils.RequestType
    int requestType;

    private BottomSheetBase mBottomSheet;

    private FeedList feedList;

    private User user;

    public static BottomSheetComposer newInstance(Bundle bundle) {
        BottomSheetComposer fragment = new BottomSheetComposer();
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * Set up your custom bottom sheet and check for arguments if any
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            feedList = getArguments().getParcelable(KeyUtils.arg_model);
            requestType = getArguments().getInt(KeyUtils.arg_request_type);
            user = getArguments().getParcelable(KeyUtils.arg_user_model);
        }
    }

    /**
     * Setup your view un-binder here as well as inflating other views as needed
     * into your view stub
     *
     * @param savedInstanceState
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        View contentView = View.inflate(getContext(), R.layout.bottom_sheet_composer, null);
        dialog.setContentView(contentView);
        unbinder = ButterKnife.bind(this, dialog);
        createBottomSheetBehavior(contentView);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        switch (requestType) {
            case KeyUtils.ACTIVITY_EDIT_REQ:
                composerWidget.setModel(feedList);
                composerWidget.setText(feedList.getValue());
                break;
            case KeyUtils.DIRECT_MESSAGE_SEND_REQ:
                toolbarTitle.setText(getString(mTitle, user.getName()));
                composerWidget.setModel(user);
                break;
            case KeyUtils.DIRECT_MESSAGE_EDIT_REQ:
                toolbarTitle.setText(getString(mTitle, user.getName()));
                composerWidget.setText(feedList.getValue());
                composerWidget.setModel(user);
                break;
        }
        composerWidget.setItemClickListener(this);
        composerWidget.setRequestMode(requestType);
        composerWidget.setLifecycle(getLifecycle());
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
                if(getActivity() != null)
                    mBottomSheet.show(getActivity().getSupportFragmentManager(), mBottomSheet.getTag());
                break;
            case R.id.insert_gif:
                mBottomSheet = new BottomSheetGiphy.Builder()
                        .setTitle(R.string.title_bottom_sheet_giphy)
                        .build();
                if(getActivity() != null)
                    mBottomSheet.show(getActivity().getSupportFragmentManager(), mBottomSheet.getTag());
                break;
            case R.id.widget_flipper:
                CompatUtil.hideKeyboard(getActivity());
                break;
            default:
                DialogUtil.createDialogAttachMedia(target.getId(), composerWidget.getEditor(), getContext());
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

    @SuppressLint("SwitchIntDef")
    @Override @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onModelChanged(BaseConsumer<FeedList> consumer) {
        NotifyUtil.createAlerter(getActivity(), R.string.text_post_information, R.string.completed_success, R.drawable.ic_insert_emoticon_white_24dp, R.color.colorStateGreen);
        closeDialog();
    }

    /**
     * Remove dialog.
     */
    @Override
    public void onDestroyView() {
        if(composerWidget != null)
            composerWidget.onViewRecycled();
        if(mBottomSheet != null)
            mBottomSheet.closeDialog();
        super.onDestroyView();
    }

    public static class Builder extends BottomSheetBuilder {
        @Override
        public BottomSheetBase build() {
            return newInstance(bundle);
        }

        public Builder setRequestMode(@KeyUtils.RequestType int requestType) {
            bundle.putInt(KeyUtils.arg_request_type, requestType);
            return this;
        }

        public Builder setUserActivity(FeedList feedList) {
            bundle.putParcelable(KeyUtils.arg_model, feedList);
            return this;
        }

        public Builder setUserModel(User userModel) {
            bundle.putParcelable(KeyUtils.arg_user_model, userModel);
            return this;
        }
    }
}
