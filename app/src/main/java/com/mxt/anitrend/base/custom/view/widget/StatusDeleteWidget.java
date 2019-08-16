package com.mxt.anitrend.base.custom.view.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.databinding.WidgetDeleteBinding;
import com.mxt.anitrend.model.entity.anilist.FeedList;
import com.mxt.anitrend.model.entity.anilist.FeedReply;
import com.mxt.anitrend.model.entity.anilist.meta.DeleteState;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.ErrorUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.NotifyUtil;

import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

public class StatusDeleteWidget extends FrameLayout implements CustomView, RetroCallback<DeleteState>, View.OnClickListener {

    private WidgetDeleteBinding binding;
    private WidgetPresenter<DeleteState> presenter;
    private @KeyUtil.RequestType int requestType;
    private FeedList feedList;
    private FeedReply feedReply;
    private final String TAG = StatusDeleteWidget.class.getSimpleName();

    public StatusDeleteWidget(Context context) {
        super(context);
        onInit();
    }

    public StatusDeleteWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public StatusDeleteWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        presenter = new WidgetPresenter<>(getContext());
        binding = WidgetDeleteBinding.inflate(CompatUtil.INSTANCE.getLayoutInflater(getContext()), this, true);
        binding.widgetDelete.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.INSTANCE.getDrawable(getContext(),
                R.drawable.ic_delete_red_600_18dp),null, null, null);
        binding.setOnClickEvent(this);
    }

    private void setParameters(long feedId, @KeyUtil.RequestType int requestType) {
        this.requestType = requestType;
        QueryContainerBuilder queryContainer = GraphUtil.INSTANCE.getDefaultQuery(false)
                .putVariable(KeyUtil.arg_id, feedId);
        presenter.getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
    }

    public void setModel(FeedList feedList, @KeyUtil.RequestType int requestType) {
        setParameters(feedList.getId(), requestType);
        this.feedList = feedList;
    }

    public void setModel(FeedReply feedReply, @KeyUtil.RequestType int requestType) {
        setParameters(feedReply.getId(), requestType);
        this.feedReply = feedReply;
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {
        resetFlipperState();
        if(presenter != null)
            presenter.onDestroy();
        feedReply = null;
        feedList = null;
    }

    private void resetFlipperState() {
        if(binding.widgetFlipper.getDisplayedChild() == WidgetPresenter.LOADING_STATE)
            binding.widgetFlipper.setDisplayedChild(WidgetPresenter.CONTENT_STATE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.widget_flipper:
                if (binding.widgetFlipper.getDisplayedChild() == WidgetPresenter.CONTENT_STATE) {
                    binding.widgetFlipper.showNext();
                    presenter.requestData(requestType, getContext(), this);
                }
                else
                    NotifyUtil.makeText(getContext(), R.string.busy_please_wait, Toast.LENGTH_SHORT).show();
                break;
        }
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
    public void onResponse(@NonNull Call<DeleteState> call, @NonNull Response<DeleteState> response) {
        try {
            DeleteState deleteState;
            if(response.isSuccessful() && (deleteState = response.body()) != null) {
                resetFlipperState();
                if(deleteState.isDeleted()) {
                    if (requestType == KeyUtil.MUT_DELETE_FEED)
                        presenter.notifyAllListeners(new BaseConsumer<>(requestType, feedList), false);
                    else if (requestType == KeyUtil.MUT_DELETE_FEED_REPLY)
                        presenter.notifyAllListeners(new BaseConsumer<>(requestType, feedReply), false);
                } else
                    NotifyUtil.makeText(getContext(), R.string.text_error_request, Toast.LENGTH_SHORT).show();
            } else
                Timber.tag(TAG).e(ErrorUtil.INSTANCE.getError(response));
        } catch (Exception e) {
            e.printStackTrace();
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
    public void onFailure(@NonNull Call<DeleteState> call, @NonNull Throwable throwable) {
        try {
            Timber.tag(TAG).e(throwable.getLocalizedMessage());
            throwable.printStackTrace();
            resetFlipperState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
