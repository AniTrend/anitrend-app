package com.mxt.anitrend.base.custom.view.widget;

import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.databinding.WidgetVoteBinding;
import com.mxt.anitrend.model.entity.anilist.Review;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by max on 2017/11/05.
 * Up Vote and Down Vote views
 */

public class VoteWidget extends LinearLayout implements CustomView, View.OnClickListener, RetroCallback<Review> {

    private WidgetPresenter<Review> presenter;
    private @KeyUtils.RequestType
    int seriesType;
    private WidgetVoteBinding binding;
    private Review model;

    private @ColorRes int colorStyle;

    public VoteWidget(@NonNull Context context) {
        super(context);
        onInit();
    }

    public VoteWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public VoteWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VoteWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        onInit();
    }

    @Override
    public void onClick(View view) {
        if(presenter.getApplicationPref().isAuthenticated()) {
            presenter.getParams().putInt(KeyUtils.arg_id, model.getId());
            switch (view.getId()) {
                case R.id.widget_thumb_up_flipper:
                    if (binding.widgetThumbUpFlipper.getDisplayedChild() == WidgetPresenter.CONTENT_STATE) {
                        binding.widgetThumbUpFlipper.showNext();
                        presenter.getParams().putInt(KeyUtils.arg_rating, KeyUtils.UP_VOTE);
                        presenter.requestData(seriesType, getContext(), this);
                    } else
                        NotifyUtil.makeText(getContext(), R.string.busy_please_wait, Toast.LENGTH_SHORT).show();
                    break;
                case R.id.widget_thumb_down_flipper:
                    if (binding.widgetThumbDownFlipper.getDisplayedChild() == WidgetPresenter.CONTENT_STATE) {
                        binding.widgetThumbDownFlipper.showNext();
                        presenter.getParams().putInt(KeyUtils.arg_rating, KeyUtils.DOWN_VOTE);
                        presenter.requestData(seriesType, getContext(), this);
                    } else
                        NotifyUtil.makeText(getContext(), R.string.busy_please_wait, Toast.LENGTH_SHORT).show();
                    break;
            }
        } else
            NotifyUtil.makeText(getContext(), R.string.info_login_req, R.drawable.ic_group_add_grey_600_18dp, Toast.LENGTH_SHORT).show();
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        presenter = new WidgetPresenter<>(getContext());
        binding = WidgetVoteBinding.inflate(LayoutInflater.from(getContext()), this, true);
        binding.setOnClickEvent(this);
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {
        if(presenter != null)
            presenter.onDestroy();
        model = null;
    }

    private void resetFlipperState() {
        if(binding.widgetThumbUpFlipper.getDisplayedChild() == WidgetPresenter.LOADING_STATE)
            binding.widgetThumbUpFlipper.setDisplayedChild(WidgetPresenter.CONTENT_STATE);

        if(binding.widgetThumbDownFlipper.getDisplayedChild() == WidgetPresenter.LOADING_STATE)
            binding.widgetThumbDownFlipper.setDisplayedChild(WidgetPresenter.CONTENT_STATE);
    }

    public void setModel(Review model, @ColorRes int colorStyle) {
        this.model = model; this.colorStyle = colorStyle;
        this.seriesType = model.getAnime() != null? KeyUtils.REVIEW_ANIME_RATE_REQ : KeyUtils.REVIEW_MANGA_RATE_REQ;
        resetFlipperState();
        setReviewStatus();
    }

    public void setReviewStatus() {
        switch (model.getUser_rating()) {
            case KeyUtils.UP_VOTE:
                binding.widgetThumbUp.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.getDrawable(getContext(),
                        R.drawable.ic_thumb_up_grey_600_18dp, R.color.colorStateGreen), null, null, null);
                break;
            case KeyUtils.DOWN_VOTE:
                binding.widgetThumbDown.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.getDrawable(getContext(),
                    R.drawable.ic_thumb_down_grey_600_18dp, R.color.colorStateOrange), null, null, null);
                break;
            default:
                if(colorStyle != 0) {
                    binding.widgetThumbUp.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.getDrawable(getContext(),
                            R.drawable.ic_thumb_up_grey_600_18dp, colorStyle), null, null, null);
                    binding.widgetThumbDown.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.getDrawable(getContext(),
                            R.drawable.ic_thumb_down_grey_600_18dp, colorStyle), null, null, null);

                    binding.widgetThumbUp.setTextColor(CompatUtil.getColor(getContext(), colorStyle));
                    binding.widgetThumbDown.setTextColor(CompatUtil.getColor(getContext(), colorStyle));
                } else {
                    binding.widgetThumbUp.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.getDrawable(getContext(),
                            R.drawable.ic_thumb_up_grey_600_18dp), null, null, null);
                    binding.widgetThumbDown.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.getDrawable(getContext(),
                            R.drawable.ic_thumb_down_grey_600_18dp), null, null, null);
                }
                break;
        }

        binding.widgetThumbUp.setText(WidgetPresenter.convertToText(model.getRating()));
        final int downVotes = model.getRating_amount()- model.getRating();
        binding.widgetThumbDown.setText(WidgetPresenter.convertToText(downVotes < 0 ? 0 : downVotes));
        resetFlipperState();
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
    public void onResponse(@NonNull Call<Review> call, @NonNull Response<Review> response) {
        try {
            Review reviewResponse = response.body();
            if(response.isSuccessful() && reviewResponse != null) {
                model.setRating(reviewResponse.getRating());
                model.setRating_amount(reviewResponse.getRating_amount());
                setReviewStatus();
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
    public void onFailure(@NonNull Call<Review> call, @NonNull Throwable throwable) {
        try {
                Log.e(toString(), throwable.getLocalizedMessage());
                throwable.printStackTrace();
                resetFlipperState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
