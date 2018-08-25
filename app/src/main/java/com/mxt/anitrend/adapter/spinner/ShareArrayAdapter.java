package com.mxt.anitrend.adapter.spinner;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.view.image.AppCompatTintImageView;
import com.mxt.anitrend.base.custom.view.text.SingleLineTextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShareArrayAdapter extends ArrayAdapter<String> {

    private final Map<Integer, Integer> titleIconList = new HashMap<Integer, Integer>() {{
        put(0, R.drawable.ic_textsms_white_24dp);
        put(1, R.drawable.ic_link_white_24dp); put(2, R.drawable.ic_crop_original_white_24dp);
        put(3, R.drawable.ic_youtube); put(4, R.drawable.ic_slow_motion_video_white_24dp);
    }};

    public ShareArrayAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<String> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        SingleLineTextView title = view.findViewById(R.id.spinner_text);
        AppCompatTintImageView icon = view.findViewById(R.id.spinner_icon);

        title.setText(getItem(position));
        icon.setTintDrawableAttr(titleIconList.get(position), R.attr.titleColor);

        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);

        SingleLineTextView title = view.findViewById(R.id.spinner_text);
        AppCompatTintImageView icon = view.findViewById(R.id.spinner_icon);

        title.setText(getItem(position));
        icon.setTintDrawableAttr(titleIconList.get(position), R.attr.titleColor);

        return view;
    }
}
