package com.mxt.anitrend.base.custom.view.widget;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.view.text.SingleLineTextView;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.databinding.WidgetVoteBinding;
import com.mxt.anitrend.model.entity.anilist.Review;
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

/**
 * Created by max on 2017/11/05.
 * Up Vote and Down Vote views
 */

public class VoteWidget extends LinearLayout implements CustomView, View.OnClickListener, RetroCallback<Review> {

    private WidgetPresenter<Review> presenter;
    private WidgetVoteBinding binding;
    private Review model;
    private final String TAG = VoteWidget.class.getSimpleName();
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

    private void setParameters(@KeyUtil.ReviewRating String ratingType) {
        QueryContainerBuilder queryContainer = GraphUtil.INSTANCE.getDefaultQuery(false)
                .putVariable(KeyUtil.arg_id, model.getId())
                .putVariable(KeyUtil.arg_rating, ratingType);
        presenter.getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        presenter.requestData(KeyUtil.MUT_RATE_REVIEW, getContext(), this);
    }

    @Override
    public void onClick(View view) {
        if(presenter.getSettings().isAuthenticated()) {
            switch (view.getId()) {
                case R.id.widget_thumb_up_flipper:
                    if (binding.widgetThumbUpFlipper.getDisplayedChild() == WidgetPresenter.CONTENT_STATE) {
                        binding.widgetThumbUpFlipper.showNext();
                        setParameters(CompatUtil.INSTANCE.equals(model.getUserRating(), KeyUtil.UP_VOTE) ? KeyUtil.NO_VOTE : KeyUtil.UP_VOTE);
                    } else
                        NotifyUtil.makeText(getContext(), R.string.busy_please_wait, Toast.LENGTH_SHORT).show();
                    break;
                case R.id.widget_thumb_down_flipper:
                    if (binding.widgetThumbDownFlipper.getDisplayedChild() == WidgetPresenter.CONTENT_STATE) {
                        binding.widgetThumbDownFlipper.showNext();
                        setParameters(CompatUtil.INSTANCE.equals(model.getUserRating(), KeyUtil.DOWN_VOTE) ? KeyUtil.NO_VOTE : KeyUtil.DOWN_VOTE);
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
        resetFlipperState();
        setReviewStatus();
    }

    public void applyColorStyleTo(SingleLineTextView singleLineTextView, @DrawableRes int drawableItem) {
        if(colorStyle != 0) {
            singleLineTextView.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.INSTANCE.getDrawable(getContext(),
                    drawableItem, colorStyle), null, null, null);
            singleLineTextView.setTextColor(CompatUtil.INSTANCE.getColor(getContext(), colorStyle));
        } else {
            singleLineTextView.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.INSTANCE.getDrawable(getContext(),
                    drawableItem, R.color.colorGrey600), null, null, null);
        }
    }

    public void setReviewStatus() {
        if(colorStyle != 0) {
            binding.widgetThumbUp.setTextColor(CompatUtil.INSTANCE.getColor(getContext(), colorStyle));
            binding.widgetThumbDown.setTextColor(CompatUtil.INSTANCE.getColor(getContext(), colorStyle));
        }
        switch (model.getUserRating()) {
            case KeyUtil.UP_VOTE:
                binding.widgetThumbUp.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.INSTANCE.getDrawable(getContext(),
                        R.drawable.ic_thumb_up_grey_600_18dp, R.color.colorStateGreen), null, null, null);
                applyColorStyleTo(binding.widgetThumbDown, R.drawable.ic_thumb_down_grey_600_18dp);
                break;
            case KeyUtil.DOWN_VOTE:
                binding.widgetThumbDown.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.INSTANCE.getDrawable(getContext(),
                    R.drawable.ic_thumb_down_grey_600_18dp, R.color.colorStateOrange), null, null, null);
                applyColorStyleTo(binding.widgetThumbUp, R.drawable.ic_thumb_up_grey_600_18dp);
                break;
            default:
                applyColorStyleTo(binding.widgetThumbUp, R.drawable.ic_thumb_up_grey_600_18dp);
                applyColorStyleTo(binding.widgetThumbDown, R.drawable.ic_thumb_down_grey_600_18dp);
                break;
        }

        binding.widgetThumbUp.setText(WidgetPresenter.convertToText(model.getRating()));
        final int downVotes = model.getRatingAmount() - model.getRating();
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
            Review model;
            if(response.isSuccessful() && (model = response.body()) != null) {
                this.model.setRating(model.getRating());
                this.model.setRatingAmount(model.getRatingAmount());
                this.model.setUserRating(model.getUserRating());
                setReviewStatus();
            } else {
                Timber.w(ErrorUtil.INSTANCE.getError(response));
                resetFlipperState();
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
            Timber.tag(TAG).e(throwable);
            throwable.printStackTrace();
            resetFlipperState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
