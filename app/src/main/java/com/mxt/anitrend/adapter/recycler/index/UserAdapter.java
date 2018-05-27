package com.mxt.anitrend.adapter.recycler.index;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.databinding.AdapterUserBinding;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.util.CompatUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;

/**
 * Created by max on 2017/11/10.
 */

public class UserAdapter extends RecyclerViewAdapter<UserBase> {

    public UserAdapter(Context context) {
        super(context);
    }

    @NonNull
    @Override
    public RecyclerViewHolder<UserBase> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(AdapterUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if(CompatUtil.isEmpty(clone))
                    clone = data;
                String filter = constraint.toString();
                if(TextUtils.isEmpty(filter)) {
                    results.values = new ArrayList<>(clone);
                    clone = null;
                }
                else {
                    results.values = new ArrayList<>(Stream.of(clone)
                            .filter((model) -> model.getName().toLowerCase().contains(filter))
                            .toList());
                }
                return results;
            }

            @Override @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if(results.values != null) {
                    data = (List<UserBase>) results.values;
                    notifyDataSetChanged();
                }
            }
        };
    }

    protected class UserViewHolder extends RecyclerViewHolder<UserBase> {

        private AdapterUserBinding binding;

        /**
         * Default constructor which includes binding with butter knife
         *
         * @param binding
         */
        public UserViewHolder(AdapterUserBinding binding) {
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
        public void onBindViewHolder(UserBase model) {
            binding.setModel(model);
            binding.userFollowStateWidget.setUserModel(model);
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
            Glide.with(getContext()).clear(binding.userAvatar);
            binding.userFollowStateWidget.onViewRecycled();
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