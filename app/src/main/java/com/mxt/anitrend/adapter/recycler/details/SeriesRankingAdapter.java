package com.mxt.anitrend.adapter.recycler.details;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mxt.anitrend.R;
import com.mxt.anitrend.api.model.Rank;
import com.mxt.anitrend.custom.RecyclerViewAdapter;
import com.mxt.anitrend.custom.RecyclerViewHolder;
import com.mxt.anitrend.event.InteractionListener;

import java.util.List;

/**
 * Created by Maxwell on 1/12/2017.
 */

public class SeriesRankingAdapter extends RecyclerViewAdapter<Rank> {

    private InteractionListener onSelection;

    public SeriesRankingAdapter(List<Rank> adapter, InteractionListener onSelection) {
        super(adapter);
        this.onSelection = onSelection;
    }

    @Override
    public RecyclerViewHolder<Rank> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder((LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_ranking_list, parent, false)));
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

    private class ViewHolder extends RecyclerViewHolder<Rank> {

        //declare all view controls here:
        private TextView rank_string;
        private ImageView rank_type;
        private View rank_parent;

        ViewHolder(View view) {
            super(view);
            rank_type = (ImageView) view.findViewById(R.id.rank_type);
            rank_string = (TextView) view.findViewById(R.id.rank_string);
            rank_parent = view.findViewById(R.id.rank_container);
            rank_parent.setOnClickListener(this);
        }

        @Override
        public void onBindViewHolder(Rank model) {
            rank_string.setText(model.getType_string());
            switch (model.getRanking_type()) {
                case "popular":
                    rank_type.setImageResource(R.drawable.ic_favorite_red_a700_48dp);
                    break;
                case "rated":
                    rank_type.setImageResource(R.drawable.ic_star_yellow_a700_48dp);
                    break;
            }
        }

        @Override
        public void onViewRecycled() {

        }

        @Override
        public void onClick(View view) {
            //TODO intercept the click of the item clicked
            //onSelection.onItemClick(getAdapterPosition());
        }
    }
}
