package com.mxt.anitrend.adapter.recycler.index;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Filter;
import android.widget.TextView;

import com.mxt.anitrend.R;
import com.mxt.anitrend.api.model.StudioSmall;
import com.mxt.anitrend.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.view.detail.activity.StudioActivity;

import java.util.List;

import butterknife.BindView;

/**
 * Created by max on 2017/05/14.
 */

public class StudioAdapter extends RecyclerViewAdapter<StudioSmall> {

    public StudioAdapter(List<StudioSmall> adapter, FragmentActivity mContext) {
        super(adapter, mContext);
    }

    @Override
    public RecyclerViewHolder<StudioSmall> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder((LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_studio_list, parent, false)));
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

    class ViewHolder extends RecyclerViewHolder<StudioSmall> {

        @BindView(R.id.studio) TextView studio;
        @BindView(R.id.studio_container) View studio_container;

        ViewHolder(View view) {
            super(view);
            studio_container.setOnClickListener(this);
        }

        @Override
        public void onBindViewHolder(StudioSmall model) {
            studio.setText(model.getStudio_name());
        }

        @Override
        public void onViewRecycled() {

        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.studio_container:
                    Intent starter = new Intent(mContext, StudioActivity.class);
                    starter.putExtra(StudioActivity.STUDIO_PARAM, mAdapter.get(getAdapterPosition()));
                    mContext.startActivity(starter);
                    break;
            }
        }
    }
}