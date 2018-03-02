package com.mxt.anitrend.adapter.recycler.index;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import com.bumptech.glide.Glide;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.databinding.AdapterStudioBinding;
import com.mxt.anitrend.model.entity.anilist.Favourite;
import com.mxt.anitrend.model.entity.base.StudioBase;
import com.mxt.anitrend.util.CompatUtil;

import java.util.List;

import butterknife.OnClick;

/**
 * Created by max on 2017/12/20.
 */

public class StudioAdapter extends RecyclerViewAdapter<StudioBase> {

    private List<StudioBase> favouriteStudios;

    public StudioAdapter(List<StudioBase> data, Context context) {
        super(data, context);
        Favourite favourite = presenter.getFavourites();
        if(favourite != null)
            favouriteStudios = favourite.getStudio();
    }

    @Override
    public RecyclerViewHolder<StudioBase> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new StudioViewHolder(AdapterStudioBinding.inflate(CompatUtil.getLayoutInflater(parent.getContext()), parent, false));
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    protected class StudioViewHolder extends RecyclerViewHolder<StudioBase> {

        private AdapterStudioBinding binding;

        /**
         * Default constructor which includes binding with butter knife
         *
         * @param binding
         */
        public StudioViewHolder(AdapterStudioBinding binding) {
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
        public void onBindViewHolder(StudioBase model) {
            if(favouriteStudios != null)
                model.setFavourite(favouriteStudios.contains(model));
            binding.setModel(model);
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
        @Override @OnClick(R.id.container)
        public void onClick(View v) {
            int index;
            if((index = getAdapterPosition()) > -1)
                clickListener.onItemClick(v, data.get(index));
        }

        @Override
        public boolean onLongClick(View view) {
            return false;
        }
    }
}
