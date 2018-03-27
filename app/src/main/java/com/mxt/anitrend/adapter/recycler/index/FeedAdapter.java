package com.mxt.anitrend.adapter.recycler.index;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import com.bumptech.glide.Glide;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.databinding.AdapterFeedProgressBinding;
import com.mxt.anitrend.databinding.AdapterFeedStatusBinding;
import com.mxt.anitrend.model.entity.anilist.FeedList;
import com.mxt.anitrend.util.KeyUtil;

import java.util.List;
import java.util.Objects;

import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by max on 2017/11/07.
 */

public class FeedAdapter extends RecyclerViewAdapter<FeedList> {

    private final int FEED_STATUS = 10, FEED_MESSAGE = 11, FEED_LIST = 20, FEED_PROGRESS = 21;

    public FeedAdapter(List<FeedList> data, Context context) {
        super(data, context);
    }

    @Override
    public RecyclerViewHolder<FeedList> onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == FEED_STATUS)
            return new StatusFeedViewHolder(AdapterFeedStatusBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        else if(viewType == FEED_MESSAGE)
            return new MessageFeedViewHolder(AdapterFeedStatusBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        else if(viewType == FEED_LIST)
            return new ListFeedViewHolder(AdapterFeedProgressBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        return new ProgressFeedViewHolder(AdapterFeedProgressBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    /**
     * Return the view type of the item at <code>position</code> for the purposes
     * of view recycling.
     * <p>
     * <p>The default implementation of this method returns 0, making the assumption of
     * a single view type for the adapter. Unlike ListView adapters, types need not
     * be contiguous. Consider using id resources to uniquely identify item view types.
     *
     * @param position position to query
     * @return integer value identifying the type of the view needed to represent the item at
     * <code>position</code>. Type codes need not be contiguous.
     */
    @Override
    public int getItemViewType(int position) {
        FeedList model = data.get(position);
        if(Objects.equals(model.getType(), KeyUtil.TEXT))
            return FEED_STATUS;
        else if(Objects.equals(model.getType(), KeyUtil.MESSAGE))
            return FEED_MESSAGE;
        else if(Objects.equals(model.getType(), KeyUtil.MEDIA_LIST) && model.getLikes() == null)
            return FEED_LIST;

        return FEED_PROGRESS;
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    protected class ProgressFeedViewHolder extends RecyclerViewHolder<FeedList> {

        private AdapterFeedProgressBinding binding;

        /**
         * Default constructor which includes binding with butter knife
         *
         * @param binding
         */
        public ProgressFeedViewHolder(AdapterFeedProgressBinding binding) {
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
        public void onBindViewHolder(FeedList model) {
            binding.setModel(model);
            binding.widgetFavourite.setRequestParams(KeyUtil.ACTIVITY, model.getId());
            binding.widgetFavourite.setModel(model.getLikes());
            binding.widgetComment.setReplyCount(model.getReplyCount());
            if(presenter.isCurrentUser(model.getUser())) {
                binding.widgetDelete.setModel(model, KeyUtil.MUT_DELETE_FEED);
                binding.widgetDelete.setVisibility(View.VISIBLE);
            }
            else
                binding.widgetDelete.setVisibility(View.GONE);
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
            Glide.with(getContext()).clear(binding.seriesImage);
            binding.widgetFavourite.onViewRecycled();
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
        @Override @OnClick({R.id.widget_users, R.id.user_avatar, R.id.widget_comment, R.id.series_image})
        public void onClick(View v) {
            int index;
            if((index = getAdapterPosition()) > -1)
                clickListener.onItemClick(v, data.get(index));
        }

        @Override @OnLongClick(R.id.series_image)
        public boolean onLongClick(View view) {
            int index;
            if((index = getAdapterPosition()) > -1)
                clickListener.onItemLongClick(view, data.get(index));
            return true;
        }
    }

    protected class StatusFeedViewHolder extends RecyclerViewHolder<FeedList> {

        private AdapterFeedStatusBinding binding;

        /**
         * Default constructor which includes binding with butter knife
         *
         * @param binding
         */
        public StatusFeedViewHolder(AdapterFeedStatusBinding binding) {
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
        public void onBindViewHolder(FeedList model) {
            binding.setModel(model);
            binding.widgetStatus.setModel(model);

            binding.widgetFavourite.setRequestParams(KeyUtil.ACTIVITY, model.getId());
            binding.widgetFavourite.setModel(model.getLikes());

            binding.widgetComment.setReplyCount(model.getReplyCount());

            if(presenter.isCurrentUser(model.getUser())) {
                binding.widgetDelete.setModel(model, KeyUtil.MUT_DELETE_FEED);

                binding.widgetEdit.setVisibility(View.VISIBLE);
                binding.widgetDelete.setVisibility(View.VISIBLE);
            }
            else {
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
            binding.widgetFavourite.onViewRecycled();
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
        @Override @OnClick({R.id.container, R.id.widget_edit, R.id.widget_users, R.id.user_avatar, R.id.widget_comment})
        public void onClick(View v) {
            int index;
            if((index = getAdapterPosition()) > -1) {
                FeedList model = data.get(index);
                if(isClickable(model))
                    clickListener.onItemClick(v, model);
            }
        }

        @Override @OnLongClick(R.id.container)
        public boolean onLongClick(View view) {
            int index = getAdapterPosition();
            return (index > -1 && !isLongClickable(data.get(index)));
        }
    }
    
    protected class MessageFeedViewHolder extends RecyclerViewHolder<FeedList> {

        private AdapterFeedStatusBinding binding;

        /**
         * Default constructor which includes binding with butter knife
         *
         * @param binding
         */
        public MessageFeedViewHolder(AdapterFeedStatusBinding binding) {
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
        public void onBindViewHolder(FeedList model) {
            binding.setModel(model);
            binding.widgetStatus.setModel(model);

            binding.widgetFavourite.setRequestParams(KeyUtil.ACTIVITY, model.getId());
            binding.widgetFavourite.setModel(model.getLikes());

            binding.widgetComment.setReplyCount(model.getReplyCount());

            if(presenter.isCurrentUser(model.getMessenger())) {
                binding.widgetDelete.setModel(model, KeyUtil.MUT_DELETE_FEED);

                binding.widgetEdit.setVisibility(View.VISIBLE);
                binding.widgetDelete.setVisibility(View.VISIBLE);
            }
            else {
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
        @Override @OnClick({R.id.widget_edit, R.id.widget_users, R.id.user_avatar, R.id.widget_comment})
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

    protected class ListFeedViewHolder extends RecyclerViewHolder<FeedList> {

        private AdapterFeedProgressBinding binding;

        /**
         * Default constructor which includes binding with butter knife
         *
         * @param binding
         */
        public ListFeedViewHolder(AdapterFeedProgressBinding binding) {
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
        public void onBindViewHolder(FeedList model) {
            binding.setModel(model);
            binding.widgetUsers.setVisibility(View.GONE);
            binding.widgetFavourite.setVisibility(View.GONE);
            binding.widgetComment.setReplyCount(model.getReplyCount());
            if(presenter.isCurrentUser(model.getUser())) {
                binding.widgetDelete.setModel(model, KeyUtil.MUT_DELETE_FEED);
                binding.widgetDelete.setVisibility(View.VISIBLE);
            }
            else
                binding.widgetDelete.setVisibility(View.GONE);
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
            Glide.with(getContext()).clear(binding.seriesImage);
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
        @Override @OnClick({R.id.user_avatar, R.id.widget_comment})
        public void onClick(View v) {
            int index;
            if((index = getAdapterPosition()) > -1)
                clickListener.onItemClick(v, data.get(index));
        }

        @Override
        public boolean onLongClick(View view) {
            int index;
            if((index = getAdapterPosition()) > -1)
                clickListener.onItemLongClick(view, data.get(index));
            return true;
        }
    }
}
