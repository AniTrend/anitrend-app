package com.mxt.anitrend.adapter.recycler.details;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Filter;
import android.widget.TextView;

import com.mxt.anitrend.R;
import com.mxt.anitrend.api.structure.ExternalLink;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.base.interfaces.event.InteractionListener;

import java.util.List;

/**
 * Created by Maxwell on 1/12/2017.
 */

public class AnimeLinksAdapter extends RecyclerViewAdapter<ExternalLink> {

    private InteractionListener mListener;

    public AnimeLinksAdapter(List<ExternalLink> mData, InteractionListener mListener) {
        super(mData);
        this.mListener = mListener;
    }

    @Override
    public RecyclerViewHolder<ExternalLink> onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflater = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_links_list, parent, false);
        return new ViewHolder(inflater);
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

    private class ViewHolder extends RecyclerViewHolder<ExternalLink> {

        //declare all view controls here:
        private TextView link;

        ViewHolder(View view) {
            super(view);
            link = (TextView) view.findViewById(R.id.anime_details_link_action);
            link.setOnClickListener(this);
        }

        @Override
        public void onBindViewHolder(ExternalLink model) {
            link.setText(model.getSite());
        }

        @Override
        public void onViewRecycled() {

        }

        @Override
        public void onClick(View view) {
            mListener.onItemClick(getAdapterPosition());
        }
    }
}
