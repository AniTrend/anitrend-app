package com.mxt.anitrend.base.custom.view.editor;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Lifecycle;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.event.ItemClickListener;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.databinding.WidgetComposerBinding;
import com.mxt.anitrend.model.entity.anilist.FeedList;
import com.mxt.anitrend.model.entity.anilist.FeedReply;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.model.entity.giphy.Gif;
import com.mxt.anitrend.model.entity.giphy.Giphy;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.ErrorUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.MarkDown;
import com.mxt.anitrend.util.NotifyUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

import io.wax911.emojify.Emoji;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by max on 2017/12/02.
 * Composer widget for multiple feed types
 */

public class ComposerWidget extends FrameLayout implements CustomView, View.OnClickListener, RetroCallback<ResponseBody> {

    private WidgetComposerBinding binding;
    private @KeyUtil.RequestType int requestType;
    private WidgetPresenter<ResponseBody> presenter;
    private ItemClickListener<Object> itemClickListener;

    private UserBase recipient;
    private FeedList feedList;
    private FeedReply feedReply;

    private Lifecycle lifecycle;

    public ComposerWidget(@NonNull Context context) {
        super(context);
        onInit();
    }

    public ComposerWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public ComposerWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ComposerWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        onInit();
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

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        presenter = new WidgetPresenter<>(getContext());
        binding = WidgetComposerBinding.inflate(CompatUtil.getLayoutInflater(getContext()), this, true);
        binding.setOnClickListener(this);
    }

    private void resetFlipperState() {
        if(binding.widgetFlipper.getDisplayedChild() == WidgetPresenter.LOADING_STATE)
            binding.widgetFlipper.setDisplayedChild(WidgetPresenter.CONTENT_STATE);
    }

    public void setItemClickListener(ItemClickListener<Object> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setLifecycle(Lifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    public void setModel(FeedList feedList, @KeyUtil.RequestType int requestType) {
        this.feedList = feedList;
        this.requestType = requestType;
        // this.requestType = KeyUtils.MUT_SAVE_TEXT_FEED;
    }

    public void setModel(UserBase recipient, @KeyUtil.RequestType int requestType) {
        this.recipient = recipient;
        this.requestType = requestType;
        // this.requestType = KeyUtils.MUT_SAVE_MESSAGE_FEED;
    }

    public void setModel(FeedReply feedReply, @KeyUtil.RequestType int requestType) {
        this.feedReply = feedReply;
        this.requestType = requestType;
        // this.requestType = KeyUtils.MUT_SAVE_FEED_REPLY;
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        if(presenter != null)
            presenter.onDestroy();
        itemClickListener = null;
    }

    // TODO: 2018/03/25 Check how parameters are being passed
    @SuppressLint("SwitchIntDef")
    public void startRequestData() {
        if (binding.widgetFlipper.getDisplayedChild() == WidgetPresenter.CONTENT_STATE) {
            binding.widgetFlipper.showNext();

            QueryContainerBuilder queryContainer = GraphUtil.getDefaultQuery(false);

            switch (requestType) {
                case KeyUtil.MUT_SAVE_TEXT_FEED:
                    if(feedList != null) {
                        feedList.setValue(binding.comment.getFormattedText());
                        queryContainer.putVariable(KeyUtil.arg_id, feedList.getId());
                    }
                    queryContainer.putVariable(KeyUtil.arg_text, binding.comment.getFormattedText());
                    break;
                case KeyUtil.MUT_SAVE_FEED_REPLY:
                    if(feedReply != null) {
                        feedReply.setText(binding.comment.getFormattedText());
                        queryContainer.putVariable(KeyUtil.arg_activityId, feedReply.getId());
                    }
                    queryContainer.putVariable(KeyUtil.arg_text, binding.comment.getFormattedText());
                    break;
                case KeyUtil.MUT_SAVE_MESSAGE_FEED:
                    if(feedList != null) {
                        feedList.setValue(binding.comment.getFormattedText());
                        queryContainer.putVariable(KeyUtil.arg_id, feedList.getId());
                    }
                    queryContainer.putVariable(KeyUtil.arg_recipientId, recipient.getId());
                    queryContainer.putVariable(KeyUtil.arg_message, binding.comment.getFormattedText());
                    break;
            }

            presenter.getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
            presenter.requestData(requestType, getContext(), this);
        } else
            NotifyUtil.makeText(getContext(), R.string.busy_please_wait, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        if(itemClickListener != null) {
            itemClickListener.onItemClick(view, null);
            switch (view.getId()) {
                case R.id.widget_flipper:
                    if (!binding.comment.isEmpty()) startRequestData();
                    else NotifyUtil.makeText(getContext(), R.string.warning_empty_input, Toast.LENGTH_SHORT).show();
                    break;
            }
        } else
            NotifyUtil.makeText(getContext(), R.string.dialog_action_null, Toast.LENGTH_SHORT).show();
    }

    public boolean editBoxHasFocus(boolean releaseFocus) {
        final boolean hasFocus = binding.comment.hasFocus();
        if(hasFocus && releaseFocus)
            binding.comment.clearFocus();
        return hasFocus;
    }

    /**
     * Invoked for a received HTTP response.
     * <p>
     * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
     * Call {@link Response#isSuccessful()} to determine if the response indicates success.
     *
     * @param call     the origination requesting object
     * @param response the response from the network
     */
    @Override
    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
        if(lifecycle != null && lifecycle.getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            resetFlipperState();
            if(response.isSuccessful()) {
                binding.comment.getText().clear();

                if (requestType == KeyUtil.MUT_SAVE_TEXT_FEED) {
                    if(feedList != null)
                        presenter.notifyAllListeners(new BaseConsumer<>(requestType, feedList), false);
                    presenter.notifyAllListeners(new BaseConsumer<FeedList>(requestType), false);
                } else if (requestType == KeyUtil.MUT_SAVE_FEED_REPLY) {
                    if(feedReply != null)
                        presenter.notifyAllListeners(new BaseConsumer<>(requestType, feedReply), false);
                    presenter.notifyAllListeners(new BaseConsumer<FeedReply>(requestType), false);
                } else if (requestType == KeyUtil.MUT_SAVE_MESSAGE_FEED) {
                    if(feedList != null)
                        presenter.notifyAllListeners(new BaseConsumer<>(requestType, feedList), false);
                    presenter.notifyAllListeners(new BaseConsumer<FeedList>(requestType), false);
                }
            }
            else
                NotifyUtil.makeText(getContext(), ErrorUtil.getError(response), Toast.LENGTH_SHORT).show();
            presenter.onDestroy();
        }
    }

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     *
     * @param call      the origination requesting object
     * @param throwable contains information about the error
     */
    @Override
    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
        if(lifecycle != null && lifecycle.getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            resetFlipperState();
            throwable.printStackTrace();
            NotifyUtil.makeText(getContext(), throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onEmojiClicked(Emoji emoji) {
        EditText editor = binding.comment;
        int start = editor.getSelectionStart();
        editor.getEditableText().insert(start, emoji.getEmoji());
    }


    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onGiphyClicked(Giphy giphy) {
        String index = KeyUtil.GIPHY_LARGE_DOWN_SAMPLE;
        EditText editor = binding.comment;
        int start = editor.getSelectionStart();
        Gif gif = giphy.getImages().get(index);
        editor.getEditableText().insert(start, MarkDown.convertImage(gif.getUrl()));
    }

    public void appendText(String textValue) {
        int start = binding.comment.getSelectionStart();
        Editable editable = binding.comment.getEditableText();
        editable.insert(start, textValue);
    }

    public void setText(String textValue) {
        if(TextUtils.isEmpty(binding.comment.getText()))
            binding.comment.setText(textValue);
        else
            appendText(textValue);
    }

    public void mentionUserFrom(FeedReply feedReply) {
        String user = feedReply.getUser().getName();
        appendText(String.format(Locale.getDefault(), "@%s ", user));
    }

    public EditText getEditor() {
        return binding.comment;
    }
}
