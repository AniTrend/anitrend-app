package com.mxt.anitrend.adapter.recycler.details;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mxt.anitrend.R;
import com.mxt.anitrend.api.structure.Channel;
import com.mxt.anitrend.api.structure.Episode;
import com.mxt.anitrend.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.event.InteractionListener;

import java.util.Locale;

/**
 * Created by Maxwell on 2/11/2017.
 */
public class AnimeEpisodeAdapter extends RecyclerViewAdapter<Episode> {

    private InteractionListener mListener;

    public AnimeEpisodeAdapter(InteractionListener mListener, Context mContext, Channel mChannel) {
        super(mChannel.getEpisode(), mContext);
        this.mListener = mListener;
    }

    @Override
    public RecyclerViewHolder<Episode> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_anime_episode, parent, false));
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


    private class ViewHolder extends RecyclerViewHolder<Episode> {

        private ImageView mImage;
        private TextView mTitle, mDuration;

        public ViewHolder(View view) {
            super(view);
            mImage = (ImageView)view.findViewById(R.id.episode_image);
            mTitle = (TextView)view.findViewById(R.id.episode_title);
            mDuration = (TextView)view.findViewById(R.id.episode_duration);
            mImage.setOnClickListener(this);
            mTitle.setOnClickListener(this);
        }

        @Override
        public void onBindViewHolder(Episode model) {

            if(model.getThumbnail() != null) {
                Glide.with(mContext).load(model.getThumbnail().get(0).getUrl())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(mImage);
            }

            mTitle.setText(model.getTitle());
            if(model.getContent().getDuration() != null) {
                int time = Integer.valueOf(model.getContent().getDuration());
                mDuration.setText(String.format(Locale.getDefault(), "%d:%d ", time / 60, time % 60));
            } else {
                mDuration.setText("N/A");
            }
        }

        @Override
        public void onViewRecycled() {
            Glide.clear(mImage);
        }

        @Override
        public void onClick(View view) {
            mListener.onItemClick(getAdapterPosition());
        }
    }
}
