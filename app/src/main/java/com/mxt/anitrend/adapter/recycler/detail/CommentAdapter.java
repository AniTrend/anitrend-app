package com.mxt.anitrend.adapter.recycler.detail;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import com.bumptech.glide.Glide;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.databinding.AdapterCommentBinding;
import com.mxt.anitrend.model.entity.anilist.FeedReply;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtils;

import java.util.List;

import butterknife.OnClick;

/**
 * Created by max on 2017/12/03.
 * comment activity adapter
 */

public class CommentAdapter extends RecyclerViewAdapter<FeedReply> {

    public CommentAdapter(List<FeedReply> data, Context context) {
        super(data, context);
    }

    @Override
    public RecyclerViewHolder<FeedReply> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CommentViewHolder(AdapterCommentBinding.inflate(CompatUtil.getLayoutInflater(parent.getContext()), parent, false));
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    protected class CommentViewHolder extends RecyclerViewHolder<FeedReply> {

        private AdapterCommentBinding binding;

        /**
         * Default constructor which includes binding with butter knife
         *
         * @param binding
         */
        public CommentViewHolder(AdapterCommentBinding binding) {
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
        public void onBindViewHolder(FeedReply model) {
            binding.setModel(model);
            binding.widgetStatus.setModel(model);
            binding.widgetMention.setVisibility(View.GONE);

            binding.widgetFavourite.setRequestParams(KeyUtils.ACTIVITY_REPLY, model.getId());
            binding.widgetFavourite.setModel(model.getLikes());

            if(presenter.isCurrentUser(model.getUser().getId())) {
                binding.widgetDelete.setModel(model, KeyUtils.MUT_DELETE_FEED_REPLY);

                binding.widgetMention.setVisibility(View.GONE);
                binding.widgetEdit.setVisibility(View.VISIBLE);
                binding.widgetDelete.setVisibility(View.VISIBLE);
            }
            else {
                binding.widgetMention.setVisibility(View.VISIBLE);
                binding.widgetEdit.setVisibility(View.GONE);
                binding.widgetDelete.setVisibility(View.GONE);
            }
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
            binding.widgetStatus.onViewRecycled();
            binding.widgetDelete.onViewRecycled();
            binding.unbind();
        }

        /**
         * Handle any onclick events from our views
         * <br/>
         *
         * @param v the view that has been clicked
         * @see View.OnClickListener
         */
        @Override @OnClick({R.id.widget_edit, R.id.widget_users, R.id.user_avatar, R.id.widget_mention})
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
