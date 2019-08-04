package com.mxt.anitrend.adapter.spinner;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.view.image.AppCompatTintImageView;
import com.mxt.anitrend.base.custom.view.text.SingleLineTextView;

import java.util.List;
import java.util.Map;

public class IconArrayAdapter extends ArrayAdapter<String> {

    private Map<Integer, Integer> indexIconMap;

    public IconArrayAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<String> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    /**
     * Set a map containing the index relative to the title containing a drawable int res
     * @param indexIconMap map of signature (position, R.drawable)
     */
    public void setIndexIconMap(Map<Integer, Integer> indexIconMap) {
        this.indexIconMap = indexIconMap;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        SingleLineTextView title = view.findViewById(R.id.spinner_text);
        AppCompatTintImageView icon = view.findViewById(R.id.spinner_icon);

        title.setText(getItem(position));
        icon.setTintDrawableAttr(indexIconMap.get(position), R.attr.titleColor);

        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);

        SingleLineTextView title = view.findViewById(R.id.spinner_text);
        AppCompatTintImageView icon = view.findViewById(R.id.spinner_icon);

        title.setText(getItem(position));
        icon.setTintDrawableAttr(indexIconMap.get(position), R.attr.titleColor);

        return view;
    }
}
