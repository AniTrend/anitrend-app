package com.mxt.anitrend.view.base.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.johnpersano.supertoasts.library.Style;
import com.mxt.anitrend.R;
import com.mxt.anitrend.api.model.Review;
import com.mxt.anitrend.api.model.SeriesSmall;
import com.mxt.anitrend.base.custom.Payload;
import com.mxt.anitrend.base.custom.async.RequestApiAction;
import com.mxt.anitrend.databinding.ActivityReviewReaderBinding;
import com.mxt.anitrend.presenter.detail.GenericPresenter;
import com.mxt.anitrend.util.ErrorHandler;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.PatternMatcher;
import com.mxt.anitrend.viewmodel.activity.DefaultActivity;
import com.zzhoujay.richtext.RichText;
import com.zzhoujay.richtext.RichTextConfig;
import com.zzhoujay.richtext.RichType;
import com.zzhoujay.richtext.ig.DefaultImageGetter;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by max on 2017/08/18.
 */

public class ReviewReaderActivity extends DefaultActivity {

    public static final String REVIEW_INSTANCE = "review_item";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.review_text)
    TextView reviewText;
    @BindView(R.id.review_avatar)
    CircleImageView avatar;
    @BindView(R.id.review_heading)
    TextView reviewHeading;
    @BindView(R.id.review_series_img)
    ImageView reviewSeries;

    private RichText mRichText;
    private Review mReview;
    private ActivityReviewReaderBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_review_reader);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mPresenter = new GenericPresenter(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        try {
            if(getIntent().hasExtra(REVIEW_INSTANCE))
                mReview = getIntent().getParcelableExtra(REVIEW_INSTANCE);
            binding.setPresenter(mPresenter);
            binding.setModel(mReview);
            updateUI();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_review_reader, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Payload.ReviewActions payload;
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_review_like:
                Toast.makeText(getApplicationContext(), mReview.getUser_rating() == 0?"Up voting review":"Removing previous rating", Toast.LENGTH_SHORT).show();
                payload = new Payload.ReviewActions(mReview.getId(), mReview.getUser_rating() == 0? 1:0);
                new RequestApiAction.ReviewBasedActions(getApplicationContext(), new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(isAlive())
                            if(response.isSuccessful() && response.body() != null)
                                mPresenter.makeAlerterSuccess(ReviewReaderActivity.this, getString(R.string.completed_success));
                            else
                                Toast.makeText(getApplicationContext(), ErrorHandler.getError(response).toString(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        try {
                            Toast.makeText(getApplicationContext(), t.getCause().getMessage(), Toast.LENGTH_SHORT).show();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }, mReview.getAnime() != null?KeyUtils.ActionType.REVIEW_ANIME_RATE : KeyUtils.ActionType.REVIEW_MANGA_RATE, payload).execute();
                break;
            case R.id.action_review_dislike:
                Toast.makeText(getApplicationContext(), mReview.getUser_rating() == 0?"Down voting review":"Removing previous rating", Toast.LENGTH_SHORT).show();
                payload = new Payload.ReviewActions(mReview.getId(), mReview.getUser_rating() == 0? 2:0);
                new RequestApiAction.ReviewBasedActions(getApplicationContext(), new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(isAlive())
                            if(response.isSuccessful() && response.body() != null)
                                mPresenter.makeAlerterSuccess(ReviewReaderActivity.this, getString(R.string.completed_success));
                            else
                                Toast.makeText(getApplicationContext(), ErrorHandler.getError(response).toString(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        try {
                            Toast.makeText(getApplicationContext(), t.getCause().getMessage(), Toast.LENGTH_SHORT).show();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }, mReview.getAnime() != null?KeyUtils.ActionType.REVIEW_ANIME_RATE : KeyUtils.ActionType.REVIEW_MANGA_RATE, payload).execute();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(REVIEW_INSTANCE, mReview);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null)
            mReview = savedInstanceState.getParcelable(REVIEW_INSTANCE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mRichText != null) {
            mRichText.clear();
            mRichText = null;
        }
    }

    @Override
    protected void updateUI() {
        mToolbar.setTitle(getString(R.string.text_reviewed_by, mReview.getUser().getDisplay_name()));
        RichTextConfig.RichTextConfigBuild builder = RichText
                .from(PatternMatcher.convertToStandardMarkdown(mReview.getTextRaw()))
                .imageGetter(new DefaultImageGetter())
                .type(RichType.MARKDOWN);
        mRichText = builder.into(reviewText);

        Glide.with(this).load(mReview.getUser()
                .getImage_url_med()).centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(avatar);

        SeriesSmall anime = mReview.getAnime();
        SeriesSmall manga = mReview.getManga();

        boolean isAnime = anime != null;

        switch (mPresenter.getApiPrefs().getTitleLanguage()) {
            case "romaji":
                reviewHeading.setText(isAnime? anime.getTitle_romaji() : manga.getTitle_romaji());
                break;
            case "english":
                reviewHeading.setText(isAnime? anime.getTitle_english() : manga.getTitle_english());
                break;
            case "japanese":
                reviewHeading.setText(isAnime? anime.getTitle_japanese() : manga.getTitle_japanese());
                break;
        }

        Glide.with(this).load(isAnime?anime.getImage_url_lge():manga.getImage_url_lge()).centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(reviewSeries);

        mPresenter.createSuperToast(this, getString(R.string.text_processing), R.drawable.ic_info_outline_white_18dp,
                Style.TYPE_PROGRESS_BAR, Style.DURATION_VERY_LONG,
                ContextCompat.getColor(this, R.color.colorStateBlue));
    }
}