package com.mxt.anitrend.adapter.recycler.details;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Filter;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mxt.anitrend.R;
import com.mxt.anitrend.api.model.Review;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.base.interfaces.event.ReviewClickListener;
import com.mxt.anitrend.util.ApplicationPrefs;
import com.mxt.anitrend.view.index.activity.UserProfileActivity;

import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Maxwell on 11/8/2016.
 */
public class SeriesReviewAdapter extends RecyclerViewAdapter<Review> {

    private ReviewClickListener mListener;

    public SeriesReviewAdapter(List<Review> adapter, Context context, ReviewClickListener mListener) {
        super(adapter, context);
        this.mListener = mListener;
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_series_review, parent, false));
    }

    /**
     * <p>Returns a filter that can be used to constrain data with a filtering
     * pattern.</p>
     * <p>
     * <p>This method is usually implemented by {@link Adapter}
     * classes.</p>
     *
     * @return a filter used to constrain data
     */
    @Override
    public Filter getFilter() {
        return null;
    }

    private class CardViewHolder extends RecyclerViewHolder<Review> implements View.OnClickListener {

        //declare all view controls here:
        private ViewFlipper mUpFlipper, mDownFlipper;
        private TextView user_name, review_date, review_summary, review_up_score, review_down_score, review_read_more;
        private RatingBar review_rating;
        private CircleImageView user_image;
        private CardView review_card;

        CardViewHolder(View view) {
            super(view);
            mUpFlipper = (ViewFlipper) view.findViewById(R.id.mUpFlipper);
            mDownFlipper = (ViewFlipper) view.findViewById(R.id.mDownFlipper);
            user_name = (TextView) view.findViewById(R.id.review_user_name);
            review_date = (TextView) view.findViewById(R.id.review_date);
            review_summary = (TextView) view.findViewById(R.id.review_summary);
            review_up_score = (TextView) view.findViewById(R.id.review_up_score);
            review_down_score = (TextView) view.findViewById(R.id.review_down_score);
            review_rating = (RatingBar) view.findViewById(R.id.review_rating);
            user_image = (CircleImageView) view.findViewById(R.id.review_user_image);
            review_card = (CardView) view.findViewById(R.id.card_review);
            review_read_more = (TextView) view.findViewById(R.id.review_read_more);
            /*onClickListeners*/
            user_image.setOnClickListener(this);
            review_read_more.setOnClickListener(this);
            review_card.setOnClickListener(this);
            mUpFlipper.setOnClickListener(this);
            mDownFlipper.setOnClickListener(this);
        }

        @Override
        public void onBindViewHolder(Review model) {

            Glide.with(mContext)
                    .load(model.getUser().getImage_url_med())
                    .asBitmap()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(user_image);

            switch (model.getUser_rating()){
                case 0:
                    //no rating
                    review_up_score.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up_grey_600_18dp,0,0,0);
                    review_down_score.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_down_grey_600_18dp,0,0,0);
                    break;
                case 1:
                    //up vote
                    review_up_score.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up_green_600_18dp,0,0,0);
                    break;
                case 2:
                    //down vote
                    review_down_score.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_down_deep_orange_600_18dp,0,0,0);
                    break;
            }

            user_name.setText(String.format(Locale.getDefault(), mContext.getString(R.string.text_reviewed_by), model.getUser().getDisplay_name()));
            review_date.setText(model.getDate());
            review_summary.setText(model.getSummary());
            review_rating.setRating(model.getDoubleScore());

            review_up_score.setText(String.format(Locale.getDefault()," %d ",model.getRating()));
            review_down_score.setText(String.format(Locale.getDefault()," %d ",model.getRating_amount() - model.getRating()));

            //reset the view pager if it's still showing progress
            if(mDownFlipper.getDisplayedChild() != 0)
                mDownFlipper.showPrevious();
            if(mUpFlipper.getDisplayedChild() != 0)
                mUpFlipper.showPrevious();
        }

        @Override
        public void onViewRecycled() {
            Glide.clear(user_image);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            switch (view.getId()){
                case R.id.review_read_more:
                    mListener.onReadMore(position);
                    break;
                case R.id.card_review:
                    mListener.onCardClicked(position);
                    break;
                case R.id.mDownFlipper:
                    if (mDownFlipper.getDisplayedChild() == 0)
                        mDownFlipper.showNext();
                    else {
                        Toast.makeText(mContext, "Busy, please wait!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mListener.onDownvote(position);
                    break;
                case R.id.mUpFlipper:
                    if (mUpFlipper.getDisplayedChild() == 0)
                        mUpFlipper.showNext();
                    else {
                        Toast.makeText(mContext, "Busy, please wait!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mListener.onUpvote(position);
                    break;
                case R.id.review_user_image:
                    if(!new ApplicationPrefs(mContext).isAuthenticated()) {
                        Toast.makeText(mContext, "Please sign in first.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(mContext, UserProfileActivity.class);
                    intent.putExtra(UserProfileActivity.PROFILE_INTENT_KEY, mAdapter.get(getAdapterPosition()).getUser());
                    mContext.startActivity(intent);
                    break;
            }
        }
    }
}
