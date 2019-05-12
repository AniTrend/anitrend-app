package com.mxt.anitrend.base.custom.view.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.databinding.WidgetButtonStateBinding;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.ErrorUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.NotifyUtil;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by max on 2017/11/16.
 * widget that represents the state of an
 * external user, either following or not
 */

public class FollowStateWidget extends FrameLayout implements CustomView, View.OnClickListener, RetroCallback<UserBase> {

    private UserBase model;
    private WidgetButtonStateBinding binding;
    private WidgetPresenter<UserBase> presenter;

    public FollowStateWidget(Context context) {
        super(context);
        onInit();
    }

    public FollowStateWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public FollowStateWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        binding = WidgetButtonStateBinding.inflate(CompatUtil.INSTANCE.getLayoutInflater(getContext()),this, true);
        presenter = new WidgetPresenter<>(getContext());
        binding.setOnClickListener(this);
    }

    public void setUserModel(UserBase model) {
        this.model = model;
        if(presenter.getApplicationPref().isAuthenticated())
            if(!presenter.isCurrentUser(model))
                setControlText();
            else
                setVisibility(GONE);
        else
            setVisibility(GONE);
    }

    private void setControlText() {
        if (model.isFollowing()) {
            binding.buttonStateContainer.setCardBackgroundColor(CompatUtil.INSTANCE.getColor(getContext(), R.color.colorAccentDark));
            binding.buttonStateText.setText(R.string.following);
        } else {
            binding.buttonStateContainer.setCardBackgroundColor(CompatUtil.INSTANCE.getColor(getContext(), R.color.colorAccent));
            binding.buttonStateText.setText(R.string.follow);
        }
        resetFlipperState();
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {
        setVisibility(VISIBLE);
        if(presenter != null)
            presenter.onDestroy();
        resetFlipperState();
    }

    private void resetFlipperState() {
        if (binding.widgetFlipper.getDisplayedChild() == WidgetPresenter.LOADING_STATE)
            binding.widgetFlipper.setDisplayedChild(WidgetPresenter.CONTENT_STATE);
    }

            @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.widget_flipper:
                if (binding.widgetFlipper.getDisplayedChild() == WidgetPresenter.CONTENT_STATE) {
                    binding.widgetFlipper.showNext();
                    QueryContainerBuilder queryContainer = GraphUtil.INSTANCE.getDefaultQuery(false)
                            .putVariable(KeyUtil.arg_userId, model.getId());
                    presenter.getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
                    presenter.requestData(KeyUtil.MUT_TOGGLE_FOLLOW, getContext(), this);
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
    public void onResponse(@NonNull Call<UserBase> call, @NonNull Response<UserBase> response) {
        try {
            if(response.isSuccessful()) {
                model.toggleFollow();
                presenter.notifyAllListeners(new BaseConsumer<>(KeyUtil.MUT_TOGGLE_FOLLOW, model), false);
                setControlText();
            } else {
                Log.e(this.toString(), ErrorUtil.INSTANCE.getError(response));
                setControlText();
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
    public void onFailure(@NonNull Call<UserBase> call, @NonNull Throwable throwable) {
        try {
            throwable.printStackTrace();
            setControlText();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
