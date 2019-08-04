package com.mxt.anitrend.base.custom.view.text;

import android.content.Context;
import androidx.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
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
        binding = CustomRatingWidgetBinding.inflate(CompatUtil.INSTANCE.getLayoutInflater(getContext()), this, true);
        BasePresenter basePresenter = new BasePresenter(getContext());
        if(basePresenter.getApplicationPref().isAuthenticated())
            mediaListOptions = basePresenter.getDatabase().getCurrentUser().getMediaListOptions();
    }

    private void setFavourState(boolean isFavourite) {
        @ColorRes int colorTint = isFavourite ? R.color.colorStateYellow : R.color.white;
        Drawable drawable = CompatUtil.INSTANCE.getDrawable(getContext(), R.drawable.ic_star_grey_600_24dp, colorTint);
        binding.ratingFavourState.setImageDrawable(drawable);
    }

    private void setListStatus(MediaBase mediaBase) {
        if(mediaBase.getMediaListEntry() != null) {
            binding.ratingListStatus.setVisibility(VISIBLE);
            switch (mediaBase.getMediaListEntry().getStatus()) {
                case KeyUtil.CURRENT:
                    binding.ratingListStatus.setTintDrawable(R.drawable.ic_remove_red_eye_white_18dp,
                            R.color.white);
                    break;
                case KeyUtil.PLANNING:
                    binding.ratingListStatus.setTintDrawable(R.drawable.ic_bookmark_white_24dp,
                            R.color.white);
                    break;
                case KeyUtil.COMPLETED:
                    binding.ratingListStatus.setTintDrawable(R.drawable.ic_done_all_grey_600_24dp,
                            R.color.white);
                    break;
                case KeyUtil.DROPPED:
                    binding.ratingListStatus.setTintDrawable(R.drawable.ic_delete_red_600_18dp,
                            R.color.white);
                    break;
                case KeyUtil.PAUSED:
                    binding.ratingListStatus.setTintDrawable(R.drawable.ic_pause_white_18dp,
                            R.color.white);
                    break;
                case KeyUtil.REPEATING:
                    binding.ratingListStatus.setTintDrawable(R.drawable.ic_repeat_white_18dp,
                            R.color.white);
                    break;
            }
        }
        else
            binding.ratingListStatus.setVisibility(GONE);
    }

    private void setListStatus() {
        binding.ratingListStatus.setVisibility(GONE);
    }

    private void setRating(MediaList mediaList) {
        if(mediaListOptions != null)
            switch (mediaListOptions.getScoreFormat()) {
                case KeyUtil.POINT_10_DECIMAL:
                    binding.ratingValue.setText(String.format(Locale.getDefault(),"%.1f", mediaList.getScore()));
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
                                binding.ratingValue.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.INSTANCE.getDrawable(getContext(),
                                        R.drawable.ic_face_white_18dp), null, null, null);
                                break;
                            case 1:
                                binding.ratingValue.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.INSTANCE.getDrawable(getContext(),
                                        R.drawable.ic_sentiment_dissatisfied_white_18dp), null, null, null);
                                break;
                            case 2:
                                binding.ratingValue.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.INSTANCE.getDrawable(getContext(),
                                        R.drawable.ic_sentiment_neutral_white_18dp), null, null, null);
                                break;
                            case 3:
                                binding.ratingValue.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.INSTANCE.getDrawable(getContext(),
                                        R.drawable.ic_sentiment_satisfied_white_18dp), null, null, null);
                                break;
                        }
                    break;
            }
        else
            binding.ratingValue.setText(String.format(Locale.getDefault(),"%d", (int)mediaList.getScore()));
    }

    private void setRating(MediaBase mediaBase) {
        float mediaScoreDefault = (float) mediaBase.getAverageScore() * 5 / 100f;
        if(mediaListOptions != null)
            switch (mediaListOptions.getScoreFormat()) {
                case KeyUtil.POINT_10_DECIMAL:
                    mediaScoreDefault = (mediaBase.getAverageScore() / 10f);
                    binding.ratingValue.setText(String.format(Locale.getDefault(),"%.1f", mediaScoreDefault));
                    break;
                case KeyUtil.POINT_100:
                    binding.ratingValue.setText(String.format(Locale.getDefault(),"%d", mediaBase.getAverageScore()));
                    break;
                case KeyUtil.POINT_10:
                    mediaScoreDefault = (mediaBase.getAverageScore() / 10f);
                    binding.ratingValue.setText(String.format(Locale.getDefault(),"%d", (int) mediaScoreDefault));
                    break;
                case KeyUtil.POINT_5:
                    binding.ratingValue.setText(String.format(Locale.getDefault(),"%d", (int) mediaScoreDefault));
                    break;
                case KeyUtil.POINT_3:
                    binding.ratingValue.setText("");
                        if(mediaBase.getAverageScore() == 0)
                            binding.ratingValue.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.INSTANCE.getDrawable(getContext(),
                                    R.drawable.ic_face_white_18dp), null, null, null);
                        if(mediaBase.getAverageScore() > 0 && mediaBase.getAverageScore() <= 33)
                            binding.ratingValue.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.INSTANCE.getDrawable(getContext(),
                                    R.drawable.ic_sentiment_dissatisfied_white_18dp), null, null, null);
                        else if (mediaBase.getAverageScore() >= 34 && mediaBase.getAverageScore() <= 66)
                            binding.ratingValue.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.INSTANCE.getDrawable(getContext(),
                                    R.drawable.ic_sentiment_neutral_white_18dp), null, null, null);
                        else if (mediaBase.getAverageScore() >= 67 && mediaBase.getAverageScore() <= 100)
                            binding.ratingValue.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.INSTANCE.getDrawable(getContext(),
                                    R.drawable.ic_sentiment_satisfied_white_18dp), null, null, null);
                    break;
            }
        else
            binding.ratingValue.setText(String.format(Locale.getDefault(),"%d", mediaBase.getAverageScore()));
    }

    @BindingAdapter("rating")
    public static void setAverageRating(RatingTextView view, MediaBase mediaBase) {
        //float rating = (float) mediaBase.getAverageScore() * MAX / 100;
        view.setRating(mediaBase);
        view.setListStatus(mediaBase);
        view.setFavourState(mediaBase.isFavourite());
    }

    @BindingAdapter("rating")
    public static void setAverageRating(RatingTextView view, MediaList mediaList) {
        //float rating = (float) mediaList.getScore() * MAX / 100;
        view.setListStatus();
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
