package com.mxt.anitrend.adapter.recycler.details;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Filter;

import com.bumptech.glide.Glide;
import com.mxt.anitrend.R;
import com.mxt.anitrend.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.custom.emoji4j.Emoji;
import com.mxt.anitrend.event.InteractionListener;

import java.util.List;


/**
 * Created by max on 2017/04/23.
 */

public class EmoticonAdapter extends RecyclerViewAdapter<Emoji> {

    private InteractionListener mInteractionListener;

    public EmoticonAdapter(List<Emoji> mAdapter, Context mContext, InteractionListener mInteractionListener) {
        super(mAdapter, mContext);
        this.mInteractionListener = mInteractionListener;
    }

    @Override
    public RecyclerViewHolder<Emoji> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_emoticon, parent, false));
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

    private class ViewHolder extends RecyclerViewHolder<Emoji> {

        private AppCompatTextView emoticon;

        public ViewHolder(View view) {
            super(view);
            emoticon = (AppCompatTextView) view.findViewById(R.id.emoticon_holder);
            emoticon.setOnClickListener(this);
        }

        /**
         * Load image, text, buttons, etc. in this method from the given parameter
         * <br/>
         *
         * @param model Is the model at the current adapter position
         * @see Emoji
         */
        @Override
        public void onBindViewHolder(Emoji model) {
            emoticon.setText(model.getEmoji());
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
            emoticon.setText("");
        }

        @Override
        public void onClick(View v) {
            mInteractionListener.onItemClick(getAdapterPosition());
        }
    }
}
