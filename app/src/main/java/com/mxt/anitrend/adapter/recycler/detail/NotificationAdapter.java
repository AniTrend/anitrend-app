package com.mxt.anitrend.adapter.recycler.detail;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Filter;

import com.bumptech.glide.Glide;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.base.custom.view.image.AspectImageView;
import com.mxt.anitrend.databinding.AdapterNotificationBinding;
import com.mxt.anitrend.model.entity.base.NotificationBase;
import com.mxt.anitrend.model.entity.base.NotificationBase_;
import com.mxt.anitrend.model.entity.general.Notification;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtils;

import java.util.List;

import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by max on 2017/12/06.
 * Notification adapter
 */

public class NotificationAdapter extends RecyclerViewAdapter<Notification> {

    public NotificationAdapter(List<Notification> data, Context context) {
        super(data, context);
    }

    @Override
    public RecyclerViewHolder<Notification> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NotificationHolder(AdapterNotificationBinding.inflate(CompatUtil.getLayoutInflater(parent.getContext()), parent, false));
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
            NotificationBase notification = presenter.getDatabase()
                    .getBoxStore(NotificationBase.class).query()
                    .equal(NotificationBase_.id, model.getId())
                    .build().findFirst();

            if(notification != null)
                binding.notificationIndicator.setVisibility(notification.isRead()? View.GONE:View.VISIBLE);
            else
                binding.notificationIndicator.setVisibility(View.VISIBLE);

            binding.notificationTime.setText(model.getCreated_at());

            if(model.getObject_type() != KeyUtils.NOTIFICATION_AIRING)
                AspectImageView.setImage(binding.notificationImg, model.getUser().getImage_url_lge());
            else
                AspectImageView.setImage(binding.notificationImg, model.getSeries().getImage_url_lge());

            switch (model.getObject_type()) {
                case KeyUtils.NOTIFICATION_AIRING:
                    binding.notificationSubject.setText(R.string.notification_series);
                    binding.notificationHeader.setText(model.getSeries().getTitle_romaji());
                    binding.notificationContent.setText(context.getString(R.string.notification_episode, model.getMeta_data(), model.getSeries().getTitle_english()));
                    break;
                case KeyUtils.NOTIFICATION_COMMENT_FORUM:
                    binding.notificationSubject.setText(R.string.notification_user_comment_forum);
                    binding.notificationHeader.setText(model.getUser().getDisplay_name());
                    binding.notificationContent.setText(model.getComment().getComment());
                    break;
                case KeyUtils.NOTIFICATION_LIKE_FORUM:
                    binding.notificationSubject.setText(R.string.notification_user_like_forum);
                    binding.notificationHeader.setText(model.getUser().getDisplay_name());
                    binding.notificationContent.setText(String.format("%s", model.getMeta_data()));
                    break;
                case KeyUtils.NOTIFICATION_LIKE_ACTIVITY:
                    binding.notificationSubject.setText(R.string.notification_user_like_activity);
                    binding.notificationHeader.setText(model.getUser().getDisplay_name());
                    binding.notificationContent.setText(String.format("%s", model.getMeta_data()));
                    break;
                case KeyUtils.NOTIFICATION_REPLY_ACTIVITY:
                    binding.notificationSubject.setText(R.string.notification_user_reply_activity);
                    binding.notificationHeader.setText(model.getUser().getDisplay_name());
                    binding.notificationContent.setText(String.format("%s", model.getMeta_data()));
                    break;
                case KeyUtils.NOTIFICATION_REPLY_FORUM:
                    binding.notificationSubject.setText(R.string.notification_user_reply_forum);
                    binding.notificationHeader.setText(model.getUser().getDisplay_name());
                    binding.notificationContent.setText(String.format("%s", model.getComment().getComment()));
                    break;
                case KeyUtils.NOTIFICATION_FOLLOW_ACTIVITY:
                    binding.notificationSubject.setText(R.string.notification_user_follow_activity);
                    binding.notificationHeader.setText(model.getUser().getDisplay_name());
                    binding.notificationContent.setText(String.format("%s", model.getMeta_data()));
                    break;
                case KeyUtils.NOTIFICATION_DIRECT_MESSAGE:
                    binding.notificationSubject.setText(R.string.notification_user_activity_message);
                    binding.notificationHeader.setText(model.getUser().getDisplay_name());
                    binding.notificationContent.setText(String.format("%s", model.getMeta_data()));
                    break;
                case KeyUtils.NOTIFICATION_LIKE_ACTIVITY_REPLY:
                    binding.notificationSubject.setText(R.string.notification_user_like_activity);
                    binding.notificationHeader.setText(model.getUser().getDisplay_name());
                    binding.notificationContent.setText(String.format("%s", model.getMeta_data()));
                    break;
                case KeyUtils.NOTIFICATION_MENTION_ACTIVITY:
                    binding.notificationSubject.setText(R.string.notification_user_activity_mention);
                    binding.notificationHeader.setText(model.getUser().getDisplay_name());
                    binding.notificationContent.setText(String.format("%s", model.getMeta_data()));
                    break;
                case KeyUtils.NOTIFICATION_LIKE_FORUM_COMMENT:
                    binding.notificationSubject.setText(R.string.notification_user_like_comment);
                    binding.notificationHeader.setText(model.getUser().getDisplay_name());
                    binding.notificationContent.setText(String.format("%s", model.getMeta_data()));
                    break;
                default:
                    binding.notificationSubject.setText(R.string.notification_default);
                    if(model.getUser() != null)
                        binding.notificationHeader.setText(model.getUser().getDisplay_name());
                    else
                        binding.notificationHeader.setText(R.string.notification_default);
                    binding.notificationContent.setText(String.format("%s", model.getMeta_data()));
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

        /**
         * Handle any onclick events from our views
         * <br/>
         *
         * @param v the view that has been clicked
         * @see View.OnClickListener
         */
        @Override @OnClick({R.id.container, R.id.notification_img})
        public void onClick(View v) {
            int index;
            if((index = getAdapterPosition()) > -1)
                clickListener.onItemClick(v, data.get(index));
        }

        /**
         * Called when a view has been clicked and held.
         *
         * @param v The view that was clicked and held.
         * @return true if the callback consumed the long click, false otherwise.
         */
        @Override @OnLongClick(R.id.container)
        public boolean onLongClick(View v) {
            int index;
            if((index = getAdapterPosition()) > -1)
                clickListener.onItemLongClick(v, data.get(index));
            return true;
        }
    }
}
