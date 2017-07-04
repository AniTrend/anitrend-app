package com.mxt.anitrend.adapter.recycler.details;

import android.content.Context;
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
import com.mxt.anitrend.api.model.StaffSmall;
import com.mxt.anitrend.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.event.InteractionListener;

import java.util.List;
import java.util.Locale;

/**
 * Created by Maxwell on 10/23/2016.
 */
public class SeriesStaffAdapter extends RecyclerViewAdapter<StaffSmall> {

    private InteractionListener mListener;

    public SeriesStaffAdapter(List<StaffSmall> mAdapter, Context mContext, InteractionListener mListener) {
        super(mAdapter, mContext);
        this.mListener = mListener;
    }

    @Override
    public RecyclerViewHolder<StaffSmall> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_character_staff_list, parent, false));
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

    private class ViewHolder extends RecyclerViewHolder<StaffSmall> {

        //declare all view controls here:
        private TextView name, role;
        private ImageView model_image;

        ViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.character_model_name);
            role = (TextView) view.findViewById(R.id.character_model_role);
            model_image = (ImageView) view.findViewById(R.id.character_model_image);
            model_image.setOnClickListener(this);
        }

        @Override
        public void onBindViewHolder(StaffSmall model) {

            Glide.with(mContext).load(model.getImage_url_lge())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(model_image);

            name.setText(String.format(Locale.getDefault(), "%s %s", model.getName_first(), model.getName_last()));
            if(model.getRole() != null)
                role.setText(model.getRole());
        }

        @Override
        public void onViewRecycled() {
            Glide.clear(model_image);
        }

        @Override
        public void onClick(View v) {
            mListener.onItemClick(getAdapterPosition());
        }
    }
}
