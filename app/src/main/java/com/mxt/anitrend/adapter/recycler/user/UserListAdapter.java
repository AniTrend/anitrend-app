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
import com.mxt.anitrend.api.model.UserSmall;
import com.mxt.anitrend.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.view.index.activity.UserProfileActivity;

import java.util.List;

import butterknife.BindView;

/**
 * Created by max on 2017/04/11.
 */
public class UserListAdapter extends RecyclerViewAdapter<UserSmall> {

    public UserListAdapter(List<UserSmall> adapter, Context context) {
        super(adapter, context);
    }

    @Override
    public RecyclerViewHolder<UserSmall> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_user_likes, parent, false));
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

    private class ViewHolder extends RecyclerViewHolder<UserSmall> {

        @BindView(R.id.user_group_holder) View container;
        @BindView(R.id.user_name) TextView user_name;
        @BindView(R.id.user_image) ImageView user_image;

        ViewHolder(View view) {
            super(view);
            container.setOnClickListener(this);
        }

        @Override
        public void onBindViewHolder(UserSmall model) {

            Glide.with(mContext).load(model.getImage_url_med())
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(user_image);

            user_name.setText(model.getDisplay_name());
        }

        @Override
        public void onViewRecycled() {
            Glide.clear(user_image);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.user_group_holder:
                    Intent intent = new Intent(mContext, UserProfileActivity.class);
                    intent.putExtra(UserProfileActivity.PROFILE_INTENT_KEY, mAdapter.get(getAdapterPosition()));
                    mContext.startActivity(intent);
                    break;
            }
        }
    }
}
