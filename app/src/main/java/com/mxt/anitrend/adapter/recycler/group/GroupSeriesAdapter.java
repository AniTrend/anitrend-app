package com.mxt.anitrend.adapter.recycler.group;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import com.bumptech.glide.Glide;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.shared.GroupTitleViewHolder;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.databinding.AdapterEntityGroupBinding;
import com.mxt.anitrend.databinding.AdapterSeriesBinding;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.group.RecyclerItem;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;

import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by max on 2017/12/31.
 */

public class GroupSeriesAdapter extends RecyclerViewAdapter<RecyclerItem> {

    public GroupSeriesAdapter(Context context) {
        super(context);
    }

    @NonNull
    @Override
    public RecyclerViewHolder<RecyclerItem> onCreateViewHolder(@NonNull ViewGroup parent, @KeyUtil.RecyclerViewType int viewType) {
        if (viewType == KeyUtil.RECYCLER_TYPE_HEADER)
            return new GroupTitleViewHolder(AdapterEntityGroupBinding.inflate(CompatUtil.INSTANCE.getLayoutInflater(parent.getContext()), parent, false));
        return new SeriesViewHolder(AdapterSeriesBinding.inflate(CompatUtil.INSTANCE.getLayoutInflater(parent.getContext()), parent, false));
    }

    @Override
    public @KeyUtil.RecyclerViewType
    int getItemViewType(int position) {
        return data.get(position).getContentType();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    protected class SeriesViewHolder extends RecyclerViewHolder<RecyclerItem> {

        private AdapterSeriesBinding binding;

        /**
         * Default constructor which includes binding with butter knife
         *
         * @param binding
         */
        public SeriesViewHolder(AdapterSeriesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Load image, text, buttons, etc. in this method from the given parameter
         * <br/>
         *
         * @param recyclerItem Is the model at the current adapter position
         */
        @Override
        public void onBindViewHolder(RecyclerItem recyclerItem) {
            MediaBase model = (MediaBase) recyclerItem;
            binding.setModel(model);
            binding.seriesTitle.setTitle(model);
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
            Glide.with(getContext()).clear(binding.seriesImage);
            binding.unbind();
        }

        @Override @OnClick(R.id.container)
        public void onClick(View v) {
            performClick(clickListener, data, v);
        }

        @Override @OnLongClick(R.id.container)
        public boolean onLongClick(View v) {
            return performLongClick(clickListener, data, v);
        }
    }
}
