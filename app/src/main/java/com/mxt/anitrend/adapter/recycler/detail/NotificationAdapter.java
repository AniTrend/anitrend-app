package com.mxt.anitrend.adapter.recycler.detail;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Filter;

import com.bumptech.glide.Glide;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.shared.UnresolvedViewHolder;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.base.custom.view.image.AspectImageView;
import com.mxt.anitrend.databinding.AdapterNotificationBinding;
import com.mxt.anitrend.databinding.CustomRecyclerUnresolvedBinding;
import com.mxt.anitrend.model.entity.anilist.Notification;
import com.mxt.anitrend.model.entity.base.NotificationHistory;
import com.mxt.anitrend.model.entity.base.NotificationHistory_;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.DateUtil;
import com.mxt.anitrend.util.KeyUtil;

import butterknife.OnClick;
import butterknife.OnLongClick;
import io.objectbox.Box;

/**
 * Created by max on 2017/12/06.
 * Notification adapter
 */

public class NotificationAdapter extends RecyclerViewAdapter<Notification> {

    private Box<NotificationHistory> historyBox;

    public NotificationAdapter(Context context) {
        super(context);
        historyBox = presenter.getDatabase().getBoxStore(NotificationHistory.class);
    }

    @NonNull
    @Override
    public RecyclerViewHolder<Notification> onCreateViewHolder(@NonNull ViewGroup parent, @KeyUtil.RecyclerViewType int viewType) {
        if (viewType == KeyUtil.RECYCLER_TYPE_CONTENT)
            return new NotificationHolder(AdapterNotificationBinding.inflate(CompatUtil.getLayoutInflater(parent.getContext()), parent, false));
        return new UnresolvedViewHolder<>(CustomRecyclerUnresolvedBinding.inflate(CompatUtil.getLayoutInflater(parent.getContext()), parent, false));
    }

    /**
     * Return the view type of the item at <code>position</code> for the purposes
     * of view recycling.
     *
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
        final Notification notification = data.get(position);
        if (!CompatUtil.equals(notification.getType(), KeyUtil.AIRING) && notification.getUser() == null)
            return KeyUtil.RECYCLER_TYPE_ERROR;
        return KeyUtil.RECYCLER_TYPE_CONTENT;
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

    protected class NotificationHolder extends RecyclerViewHolder<Notification> {

        private AdapterNotificationBinding binding;

        /**
         * Default constructor which includes binding with butter knife
         *
         * @param binding
         */
        public NotificationHolder(AdapterNotificationBinding binding) {
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
        public void onBindViewHolder(Notification model) {
            NotificationHistory notificationHistory = historyBox.query()
                    .equal(NotificationHistory_.id, model.getId())
                    .build().findFirst();

            if(notificationHistory != null)
                binding.notificationIndicator.setVisibility(View.GONE);
            else
                binding.notificationIndicator.setVisibility(View.VISIBLE);

            binding.notificationTime.setText(DateUtil.getPrettyDateUnix(model.getCreatedAt()));

            if(!CompatUtil.equals(model.getType(), KeyUtil.AIRING)) {
                if (model.getUser() != null && model.getUser().getAvatar() != null)
                    AspectImageView.setImage(binding.notificationImg, model.getUser().getAvatar().getLarge());
            }
            else
                AspectImageView.setImage(binding.notificationImg, model.getMedia().getCoverImage().getLarge());

            switch (model.getType()) {
                case KeyUtil.ACTIVITY_MESSAGE:
                    binding.notificationSubject.setText(R.string.notification_user_activity_message);
                    binding.notificationHeader.setText(model.getUser().getName());
                    binding.notificationContent.setText(model.getContext());
                    break;
                case KeyUtil.FOLLOWING:
                    binding.notificationSubject.setText(R.string.notification_user_follow_activity);
                    binding.notificationHeader.setText(model.getUser().getName());
                    binding.notificationContent.setText(model.getContext());
                    break;
                case KeyUtil.ACTIVITY_MENTION:
                    binding.notificationSubject.setText(R.string.notification_user_activity_mention);
                    binding.notificationHeader.setText(model.getUser().getName());
                    binding.notificationContent.setText(model.getContext());
                    break;
                case KeyUtil.THREAD_COMMENT_MENTION:
                    binding.notificationSubject.setText(R.string.notification_user_comment_forum);
                    binding.notificationHeader.setText(model.getUser().getName());
                    binding.notificationContent.setText(model.getContext());
                    break;
                case KeyUtil.THREAD_SUBSCRIBED:
                    binding.notificationSubject.setText(R.string.notification_user_comment_forum);
                    binding.notificationHeader.setText(model.getUser().getName());
                    binding.notificationContent.setText(model.getContext());
                    break;
                case KeyUtil.THREAD_COMMENT_REPLY:
                    binding.notificationSubject.setText(R.string.notification_user_comment_forum);
                    binding.notificationHeader.setText(model.getUser().getName());
                    binding.notificationContent.setText(model.getContext());
                    break;
                case KeyUtil.AIRING:
                    binding.notificationSubject.setText(R.string.notification_series);
                    binding.notificationHeader.setText(model.getMedia().getTitle().getUserPreferred());
                    binding.notificationContent.setText(context.getString(R.string.notification_episode,
                            String.valueOf(model.getEpisode()), model.getMedia().getTitle().getUserPreferred()));
                    break;
                case KeyUtil.ACTIVITY_LIKE:
                    binding.notificationSubject.setText(R.string.notification_user_like_activity);
                    binding.notificationHeader.setText(model.getUser().getName());
                    binding.notificationContent.setText(model.getContext());
                    break;
                case KeyUtil.ACTIVITY_REPLY:
                case KeyUtil.ACTIVITY_REPLY_SUBSCRIBED:
                    binding.notificationSubject.setText(R.string.notification_user_reply_activity);
                    binding.notificationHeader.setText(model.getUser().getName());
                    binding.notificationContent.setText(model.getContext());
                    break;
                case KeyUtil.ACTIVITY_REPLY_LIKE:
                    binding.notificationSubject.setText(R.string.notification_user_like_reply);
                    binding.notificationHeader.setText(model.getUser().getName());
                    binding.notificationContent.setText(model.getContext());
                    break;
                case KeyUtil.THREAD_LIKE:
                    binding.notificationSubject.setText(R.string.notification_user_like_activity);
                    binding.notificationHeader.setText(model.getUser().getName());
                    binding.notificationContent.setText(model.getContext());
                    break;
                case KeyUtil.THREAD_COMMENT_LIKE:
                    binding.notificationSubject.setText(R.string.notification_user_like_comment);
                    binding.notificationHeader.setText(model.getUser().getName());
                    binding.notificationContent.setText(model.getContext());
                    break;
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
            Glide.with(getContext()).clear(binding.notificationImg);
            binding.unbind();
        }

        @Override @OnClick({R.id.container, R.id.notification_img})
        public void onClick(View v) {
            performClick(clickListener, data, v);
        }

        @Override @OnLongClick(R.id.container)
        public boolean onLongClick(View v) {
            return performLongClick(clickListener, data, v);
        }
    }
}
