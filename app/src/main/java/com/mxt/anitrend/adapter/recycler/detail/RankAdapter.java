package com.mxt.anitrend.adapter.recycler.detail;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import com.bumptech.glide.Glide;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.databinding.AdapterRankingBinding;
import com.mxt.anitrend.model.entity.anilist.MediaRank;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;

import java.util.List;

import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by max on 2018/01/01.
 */

public class RankAdapter extends RecyclerViewAdapter<MediaRank> {

    public RankAdapter(Context context) {
        super(context);
    }

    @NonNull
    @Override
    public RecyclerViewHolder<MediaRank> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RankViewHolder(AdapterRankingBinding.inflate(CompatUtil.getLayoutInflater(parent.getContext()), parent, false));
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    protected class RankViewHolder extends RecyclerViewHolder<MediaRank> {

        private AdapterRankingBinding binding;

        /**
         * Default constructor which includes binding with butter knife
         *
         * @param binding
         */
        public RankViewHolder(AdapterRankingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Load image, text, buttons, etc. in this method from the given parameter
         * <br/>
         *
         * @param model Is the model at the current adapter position
         */
        @Override
        public void onBindViewHolder(MediaRank model) {
            binding.setModel(model);
            binding.rankingType.setImageDrawable(CompatUtil.getDrawable(getContext(), CompatUtil.equals(model.getType(), KeyUtil.RATED) ?
                    R.drawable.ic_star_yellow_700_24dp : R.drawable.ic_favorite_red_700_24dp ));
            binding.executePendingBindings();
        }

        /**
         * If any image views are used within the view holder, clear any pending async img requests
         * by using Glide.clear(ImageView) or Glide.with(context).clear(view) if using Glide v4.0
         * <br/>
         *
         * @see Glide
         */
        @Override
        public void onViewRecycled() {
            binding.unbind();
        }

        /**
         * Handle any onclick events from our views
         * <br/>
         *
         * @param v the view that has been clicked
         * @see View.OnClickListener
         */
        @Override @OnClick({R.id.container, R.id.sub_container})
        public void onClick(View v) {
            int index;
            if((index = getAdapterPosition()) > -1)
                clickListener.onItemClick(v, data.get(index));
        }

        @Override @OnLongClick(R.id.container)
        public boolean onLongClick(View view) {
            int index;
            if((index = getAdapterPosition()) > -1)
                clickListener.onItemLongClick(view, data.get(index));
            return true;
        }
    }
}
