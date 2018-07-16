package com.mxt.anitrend.base.custom.view.text;

import android.content.Context;
import android.content.Intent;
import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.databinding.CustomRatingWidgetBinding;
import com.mxt.anitrend.model.entity.anilist.MediaList;
import com.mxt.anitrend.model.entity.anilist.meta.MediaListOptions;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;

import java.util.Locale;

/**
 * Created by max on 2018/01/27.
 * Special text base rating view
 */

public class RatingTextView extends LinearLayout implements CustomView {

    private @Nullable MediaListOptions mediaListOptions;
    private CustomRatingWidgetBinding binding;

    public RatingTextView(Context context) {
        super(context);
        onInit();
    }

    public RatingTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public RatingTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RatingTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        onInit();
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        binding = CustomRatingWidgetBinding.inflate(CompatUtil.getLayoutInflater(getContext()), this, true);
        BasePresenter basePresenter = new BasePresenter(getContext());
        if(basePresenter.getApplicationPref().isAuthenticated())
            mediaListOptions = basePresenter.getDatabase().getCurrentUser().getMediaListOptions();
    }

    private void setFavourState(boolean isFavourite) {
        @ColorRes int colorTint = isFavourite ? R.color.colorStateYellow : R.color.white;
        Drawable drawable = CompatUtil.getDrawable(getContext(), R.drawable.ic_star_grey_600_24dp, colorTint);
        binding.ratingFavourState.setImageDrawable(drawable);
    }

    private void setRating(MediaList mediaList) {
        if(mediaListOptions != null)
            switch (mediaListOptions.getScoreFormat()) {
                case KeyUtil.POINT_10_DECIMAL:
                    binding.ratingValue.setText(String.format(Locale.getDefault(),"%.2f", mediaList.getScore()));
                    break;
                case KeyUtil.POINT_100:
                case KeyUtil.POINT_10:
                case KeyUtil.POINT_5:
                    binding.ratingValue.setText(String.format(Locale.getDefault(),"%d", (int)mediaList.getScore()));
                    break;
                case KeyUtil.POINT_3:
                        binding.ratingValue.setText("");
                        int score = (int)mediaList.getScore();
                        switch (score) {
                            case 0:
                            case 1:
                                binding.ratingValue.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.getDrawable(getContext(),
                                        R.drawable.ic_sentiment_dissatisfied_white_18dp), null, null, null);
                                break;
                            case 2:
                                binding.ratingValue.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.getDrawable(getContext(),
                                        R.drawable.ic_sentiment_neutral_white_18dp), null, null, null);
                                break;
                            case 3:
                                binding.ratingValue.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.getDrawable(getContext(),
                                        R.drawable.ic_sentiment_satisfied_white_18dp), null, null, null);
                                break;
                        }
                    break;
            }
        else
            binding.ratingValue.setText(String.format(Locale.getDefault(),"%d", (int)mediaList.getScore()));
    }

    private void setRating(MediaBase mediaBase) {
        float mediaScoreDefault = (float) mediaBase.getMeanScore() * 5 / 100f;
        if(mediaListOptions != null)
            switch (mediaListOptions.getScoreFormat()) {
                case KeyUtil.POINT_10_DECIMAL:
                    mediaScoreDefault = (mediaBase.getMeanScore() / 10);
                    binding.ratingValue.setText(String.format(Locale.getDefault(),"%.1f", mediaScoreDefault));
                    break;
                case KeyUtil.POINT_100:
                    binding.ratingValue.setText(String.format(Locale.getDefault(),"%d", mediaBase.getMeanScore()));
                    break;
                case KeyUtil.POINT_10:
                    binding.ratingValue.setText(String.format(Locale.getDefault(),"%d", mediaBase.getMeanScore() / 10));
                    break;
                case KeyUtil.POINT_5:
                    binding.ratingValue.setText(String.format(Locale.getDefault(),"%d", (int) mediaScoreDefault));
                    break;
                case KeyUtil.POINT_3:
                    binding.ratingValue.setText("");
                        if(mediaBase.getMeanScore() >= 0 && mediaBase.getMeanScore() <= 33)
                            binding.ratingValue.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.getDrawable(getContext(),
                                    R.drawable.ic_sentiment_dissatisfied_white_18dp), null, null, null);
                        else if (mediaBase.getMeanScore() >= 34 && mediaBase.getMeanScore() <= 66)
                            binding.ratingValue.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.getDrawable(getContext(),
                                    R.drawable.ic_sentiment_neutral_white_18dp), null, null, null);
                        else if (mediaBase.getMeanScore() >= 67 && mediaBase.getMeanScore() <= 100)
                            binding.ratingValue.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.getDrawable(getContext(),
                                    R.drawable.ic_sentiment_satisfied_white_18dp), null, null, null);
                    break;
            }
        else
            binding.ratingValue.setText(String.format(Locale.getDefault(),"%d", mediaBase.getMeanScore()));
    }

    @BindingAdapter("rating")
    public static void setAverageRating(RatingTextView view, MediaBase mediaBase) {
        //float rating = (float) mediaBase.getAverageScore() * MAX / 100;
        view.setRating(mediaBase);
        view.setFavourState(mediaBase.isFavourite());
    }

    @BindingAdapter("rating")
    public static void setAverageRating(RatingTextView view, MediaList mediaList) {
        //float rating = (float) mediaList.getScore() * MAX / 100;
        view.setRating(mediaList);
        view.setFavourState(mediaList.getMedia().isFavourite());
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {

    }
}
