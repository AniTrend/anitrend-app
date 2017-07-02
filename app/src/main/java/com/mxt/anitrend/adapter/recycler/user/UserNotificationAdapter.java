package com.mxt.anitrend.adapter.recycler.user;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mxt.anitrend.R;
import com.mxt.anitrend.api.structure.UserNotification;
import com.mxt.anitrend.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.event.NotificationClickListener;
import com.mxt.anitrend.util.ApplicationPrefs;
import com.mxt.anitrend.view.index.activity.UserProfileActivity;

import java.util.List;
import java.util.Locale;

/**
 * Created by Maxwell on 1/9/2017.
 */
public class UserNotificationAdapter extends RecyclerViewAdapter<UserNotification> {

    private ApplicationPrefs mPrefs;
    private NotificationClickListener mCallback;

    public UserNotificationAdapter(List<UserNotification> adapter, Context context, ApplicationPrefs prefs, NotificationClickListener callback) {
        super(adapter, context);
        mCallback = callback;
        mPrefs = prefs;
    }

    @Override
    public RecyclerViewHolder<UserNotification> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_user_notification, parent, false));
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

    private class ViewHolder extends RecyclerViewHolder<UserNotification> {

        //declare all view controls here:
        private TextView notificationSubject ,notificationContent, notificationHeader, notificationTime;
        private ImageView notificationImg;

        ViewHolder(View view) {
            super(view);
            notificationSubject = (TextView) view.findViewById(R.id.notification_subject);
            notificationTime = (TextView) view.findViewById(R.id.notification_time);
            notificationHeader = (TextView) view.findViewById(R.id.notification_header);
            notificationContent = (TextView) view.findViewById(R.id.notification_content);
            notificationImg = (ImageView) view.findViewById(R.id.notification_img);
            view.setOnClickListener(this);
            notificationImg.setOnClickListener(this);
        }

        @Override
        public void onBindViewHolder(UserNotification model) {
            if(model.getObject_type() != NotificationClickListener.TYPE_AIRING) {
                Glide.with(mContext).load(mPrefs.isHD()?model.getUser().getImage_url_lge():model.getUser().getImage_url_med())
                        .centerCrop().crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(notificationImg);
            } else {
                Glide.with(mContext).load(mPrefs.isHD()?model.getSeries().getImage_url_lge():model.getSeries().getImage_url_med())
                        .centerCrop().crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(notificationImg);
            }

            notificationTime.setText(String.format(Locale.getDefault(), "%s ", model.getCreated_at()));
            switch (model.getObject_type()) {
                case NotificationClickListener.TYPE_AIRING:
                    notificationSubject.setText(mContext.getString(R.string.notification_series));
                    notificationHeader.setText(model.getSeries().getTitle_romaji());
                    notificationContent.setText(mContext.getString(R.string.notification_episode, model.getMeta_data(), model.getSeries().getTitle_english()));
                    break;
                case NotificationClickListener.TYPE_COMMENT_FORUM:
                    notificationSubject.setText(mContext.getString(R.string.notification_user_comment_forum));
                    notificationHeader.setText(model.getUser().getDisplay_name());
                    notificationContent.setText(String.format("%s", model.getComment().getComment()));
                    break;
                case NotificationClickListener.TYPE_LIKE_FORUM:
                    notificationSubject.setText(mContext.getString(R.string.notification_user_like_forum));
                    notificationHeader.setText(model.getUser().getDisplay_name());
                    notificationContent.setText(String.format("%s", model.getMeta_value()));
                    break;
                case NotificationClickListener.TYPE_LIKE_ACTIVITY:
                    notificationSubject.setText(mContext.getString(R.string.notification_user_like_activity));
                    notificationHeader.setText(model.getUser().getDisplay_name());
                    notificationContent.setText(String.format("%s", model.getMeta_value()));
                    break;
                case NotificationClickListener.TYPE_REPLY_ACTIVITY:
                    notificationSubject.setText(mContext.getString(R.string.notification_user_reply_activity));
                    notificationHeader.setText(model.getUser().getDisplay_name());
                    notificationContent.setText(String.format("%s", model.getMeta_value()));
                    break;
                case NotificationClickListener.TYPE_REPLY_FORUM:
                    notificationSubject.setText(mContext.getString(R.string.notification_user_reply_forum));
                    notificationHeader.setText(model.getUser().getDisplay_name());
                    notificationContent.setText(String.format("%s", model.getComment().getComment()));
                    break;
                case NotificationClickListener.TYPE_FOLLOW_ACTIVITY:
                    notificationSubject.setText(mContext.getString(R.string.notification_user_follow_activity));
                    notificationHeader.setText(model.getUser().getDisplay_name());
                    notificationContent.setText(String.format("%s", model.getMeta_value()));
                    break;
                case NotificationClickListener.TYPE_DIRECT_MESSAGE:
                    notificationSubject.setText(mContext.getString(R.string.notification_user_activity_message));
                    notificationHeader.setText(model.getUser().getDisplay_name());
                    notificationContent.setText(String.format("%s", model.getMeta_value()));
                    break;
                case NotificationClickListener.TYPE_LIKE_ACTIVITY_REPLY:
                    notificationSubject.setText(mContext.getString(R.string.notification_user_like_activity));
                    notificationHeader.setText(model.getUser().getDisplay_name());
                    notificationContent.setText(String.format("%s", model.getMeta_value()));
                    break;
                case NotificationClickListener.TYPE_MENTION_ACTIVITY:
                    notificationSubject.setText(mContext.getString(R.string.notification_user_activity_mention));
                    notificationHeader.setText(model.getUser().getDisplay_name());
                    notificationContent.setText(String.format("%s", model.getMeta_value()));
                    break;
                case NotificationClickListener.TYPE_LIKE_FORUM_COMMENT:
                    notificationSubject.setText(mContext.getString(R.string.notification_user_like_comment));
                    notificationHeader.setText(model.getUser().getDisplay_name());
                    notificationContent.setText(String.format("%s", model.getMeta_value()));
                    break;
                default:
                    notificationSubject.setText(mContext.getString(R.string.notification_default));
                    if(model.getUser() != null)
                        notificationHeader.setText(model.getUser().getDisplay_name());
                    else
                        notificationHeader.setText(mContext.getString(R.string.notification_default));
                    notificationContent.setText(String.format("%s", model.getMeta_value()));
                    break;
            }
        }

        @Override
        public void onViewRecycled() {
            Glide.clear(notificationImg);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.notification_img:
                    UserNotification model = mAdapter.get(getAdapterPosition());
                    if(model.getObject_type() != NotificationClickListener.TYPE_AIRING) {
                        Intent intent = new Intent(mContext, UserProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(UserProfileActivity.PROFILE_INTENT_KEY, model.getUser());
                        mContext.startActivity(intent);
                    }
                    return;
            }
            mCallback.onNotificationClick(getAdapterPosition());
        }
    }
}
