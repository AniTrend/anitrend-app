package com.mxt.anitrend.adapter.recycler.index;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Filter;
import android.widget.TextView;

import com.mxt.anitrend.R;
import com.mxt.anitrend.api.structure.LibraryRep;
import com.mxt.anitrend.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.custom.recycler.RecyclerViewHolder;

import java.util.List;

/**
 * Created by Maxwell on 12/30/2016.
 */

public class LibrariesAdapter extends RecyclerViewAdapter<LibraryRep> {

    public LibrariesAdapter(List<LibraryRep> mAdapter, Context mContext) {
        super(mAdapter, mContext);
    }

    @Override
    public RecyclerViewHolder<LibraryRep> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_about_libraries, parent, false);
        return new LibrariesAdapter.ViewHolder(view);
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

    private class ViewHolder extends RecyclerViewHolder<LibraryRep> {

        //declare all view controls here:
        private TextView name, description;
        private View container;

        ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.library_name);
            description = (TextView) itemView.findViewById(R.id.library_description);
            container = itemView.findViewById(R.id.about_container);
            container.setOnClickListener(this);
        }

        @Override
        public void onBindViewHolder(LibraryRep model) {
            name.setText(model.getName());
            description.setText(model.getDescription());
        }

        @Override
        public void onViewRecycled() {

        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mAdapter.get(getAdapterPosition()).getLink()));
            mContext.startActivity(intent);
        }
    }
}
