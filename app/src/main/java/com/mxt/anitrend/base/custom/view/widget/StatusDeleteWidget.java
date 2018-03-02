package com.mxt.anitrend.base.custom.view.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.databinding.WidgetDeleteBinding;
import com.mxt.anitrend.model.entity.anilist.UserActivity;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.model.entity.general.UserActivityReply;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class StatusDeleteWidget extends FrameLayout implements CustomView, RetroCallback<ResponseBody>, View.OnClickListener {

    private WidgetDeleteBinding binding;
    private WidgetPresenter<ResponseBody> presenter;
    private @KeyUtils.RequestMode int requestType;
    private UserActivity userActivity;
    private UserActivityReply userActivityReply;

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
        binding = WidgetDeleteBinding.inflate(CompatUtil.getLayoutInflater(getContext()), this, true);
        binding.widgetDelete.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.getDrawable(getContext(),
                R.drawable.ic_delete_red_600_18dp),null, null, null);
        binding.setOnClickEvent(this);
    }


    public void setModel(UserActivity userActivity, @KeyUtils.RequestMode int requestType) {
        this.requestType = requestType;
        this.userActivity = userActivity;
        presenter.getParams().putInt(KeyUtils.arg_id, userActivity.getId());
    }

    public void setModel(UserActivityReply userActivityReply, @KeyUtils.RequestMode int requestType) {
        this.requestType = requestType;
        this.userActivityReply = userActivityReply;
        presenter.getParams().putInt(KeyUtils.arg_id, userActivityReply.getId());
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {
        resetFlipperState();
        if(presenter != null)
            presenter.onDestroy();
        userActivityReply = null;
        userActivity = null;
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
    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
        try {
            if(response.isSuccessful()) {
                resetFlipperState();
                if(requestType == KeyUtils.ACTIVITY_DELETE_REQ)
                    presenter.notifyAllListeners(new BaseConsumer<>(requestType, userActivity), false);
                else if (requestType == KeyUtils.ACTIVITY_REPLY_DELETE_REQ)
                    presenter.notifyAllListeners(new BaseConsumer<>(requestType, userActivityReply), false);
            }
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
    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
        try {
            Log.e(toString(), throwable.getLocalizedMessage());
            throwable.printStackTrace();
            resetFlipperState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
