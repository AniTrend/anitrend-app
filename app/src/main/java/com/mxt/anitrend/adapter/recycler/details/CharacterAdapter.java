package com.mxt.anitrend.adapter.recycler.details;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
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
import com.mxt.anitrend.api.model.CharacterSmall;
import com.mxt.anitrend.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.view.detail.activity.CharacterActivity;

import java.util.List;
import java.util.Locale;

/**
 * Created by max on 2017/05/17.
 */

public class CharacterAdapter extends RecyclerViewAdapter<CharacterSmall> {

    public CharacterAdapter(List<CharacterSmall> adapter, FragmentActivity context) {
        super(adapter, context);
    }

    @Override
    public RecyclerViewHolder<CharacterSmall> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_character_staff_v2, parent, false));
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

    private class ViewHolder extends RecyclerViewHolder<CharacterSmall> {

        //declare all view controls here:
        private TextView name;
        private ImageView model_image;

        ViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.character_model_name);
            model_image = (ImageView) view.findViewById(R.id.character_model_image);
            model_image.setOnClickListener(this);
        }

        @Override
        public void onBindViewHolder(CharacterSmall model) {

            Glide.with(mContext).load(model.getImage_url_lge())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(model_image);

            name.setText(String.format(Locale.getDefault(), "%s %s", model.getName_first(), model.getName_last()));
        }

        @Override
        public void onViewRecycled() {
            Glide.clear(model_image);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.character_model_image:
                    Intent intent = new Intent(mContext, CharacterActivity.class);
                    intent.putExtra(CharacterActivity.CHARACTER_OBJECT_PARAM, mAdapter.get(getAdapterPosition()));
                    mContext.startActivity(intent);
                    break;
            }
        }
    }
}