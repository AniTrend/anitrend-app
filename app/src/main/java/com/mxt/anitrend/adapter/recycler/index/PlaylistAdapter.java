package com.mxt.anitrend.adapter.recycler.index;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mxt.anitrend.R;
import com.mxt.anitrend.api.hub.Playlist;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.view.base.activity.VideoPlayerActivity;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;

/**
 * Created by max on 2017/08/12.
 */

public class PlaylistAdapter extends RecyclerViewAdapter<Playlist> {

    public PlaylistAdapter(List<Playlist> mAdapter, FragmentActivity mContext) {
        super(mAdapter, mContext);
    }

    @Override
    public RecyclerViewHolder<Playlist> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_playlist_item, parent, false);
        return new ViewHolder(view);
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

    class ViewHolder extends RecyclerViewHolder<Playlist> {

        @BindView(R.id.playlist_image)
        ImageView mImage;
        @BindView(R.id.playlist_title)
        TextView mTitle;
        @BindView(R.id.playlist_episode_count)
        TextView mEpisodes;

        public ViewHolder(View view) {
            super(view);
            mImage.setOnClickListener(this);
        }

        /**
         * Load image, text, buttons, etc. in this method from the given parameter
         * <br/>
         *
         * @param model Is the model at the current adapter position
         * @see Playlist
         */
        @Override
        public void onBindViewHolder(Playlist model) {
            Glide.with(mContext).load(model.getThumbnail())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(mImage);

            mTitle.setText(model.getTitle());
            mEpisodes.setText(String.format(" %s", mContext.getString(R.string.text_anime_episodes, model.getVideos().size())));
        }

        /**
         * If any image views are used within the view holder, clear any pending async img requests
         * by using Glide.Clear(ImageView)
         * <br/>
         *
         * @see Glide
         */
        @Override
        public void onViewRecycled() {
            Glide.clear(mImage);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.playlist_image:
                    Toast.makeText(mContext, R.string.tba_placeholder, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(mContext, VideoPlayerActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(VideoPlayerActivity.URL_VIDEO_LINK, mAdapter.get(getAdapterPosition()));
                    //mContext.startActivity(intent);
                    break;
            }
        }
    }
}
