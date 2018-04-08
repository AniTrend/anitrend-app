package com.mxt.anitrend.adapter.recycler.detail;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder;
import com.mxt.anitrend.util.CompatUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.wax911.emojify.Emoji;

/**
 * Created by max on 2017/12/07.
 */

public class EmoticonAdapter extends RecyclerViewAdapter<Emoji> {

    public EmoticonAdapter(List<Emoji> data, Context context) {
        super(data, context);
    }

    @NonNull
    @Override
    public RecyclerViewHolder<Emoji> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new EmoticonViewHolder(CompatUtil.getLayoutInflater(parent.getContext()).inflate(R.layout.adapter_emoticon, parent, false));
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                return null;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

            }
        };
    }

    protected class EmoticonViewHolder extends RecyclerViewHolder<Emoji> {

        protected @BindView(R.id.emoticon_holder) AppCompatTextView emoticon;

        /**
         * Default constructor which includes binding with butter knife
         *
         * @param view
         */
        public EmoticonViewHolder(View view) {
            super(view);
        }

        /**
         * Load image, text, buttons, etc. in this method from the given parameter
         * <br/>
         *
         * @param model Is the model at the current adapter position
         */
        @Override
        public void onBindViewHolder(Emoji model) {
            emoticon.setText(model.getEmoji(), TextView.BufferType.SPANNABLE);
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

        }

        /**
         * Handle any onclick events from our views
         * <br/>
         *
         * @param v the view that has been clicked
         * @see View.OnClickListener
         */
        @Override @OnClick(R.id.emoticon_holder)
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
