package com.mxt.anitrend.base.custom.view.widget;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.databinding.CustomActionAnimeBinding;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.MediaListUtil;
import com.mxt.anitrend.util.NotifyUtil;

/**
 * Created by max on 2018/01/03.
 */

public class CustomSeriesAnimeManage extends CustomSeriesManageBase {

    private CustomActionAnimeBinding binding;

    public CustomSeriesAnimeManage(Context context) {
        super(context);
    }

    public CustomSeriesAnimeManage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSeriesAnimeManage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CustomSeriesAnimeManage(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        super.onInit();
        binding = CustomActionAnimeBinding.inflate(CompatUtil.getLayoutInflater(getContext()), this, true);
    }

    /**
     * Saves the current views states into the model
     * and returns a bundle of the params
     * @see com.mxt.anitrend.util.MediaListUtil
     */
    @Override
    public Bundle persistChanges() {
        model.setProgress(binding.diaCurrentProgress.getProgressCurrent());
        model.setRepeat(binding.diaCurrentRewatch.getProgressCurrent());
        model.setScore(binding.diaCurrentScore.getProgressCurrent());
        model.setHidden(binding.diaCurrentPrivacy.isChecked());
        model.setNotes(binding.diaCurrentNotes.getFormattedText());
        model.setStatus(KeyUtil.MediaListStatus[binding.diaCurrentStatus.getSelectedItemPosition()]);
        return MediaListUtil.getMediaListParams(model);
    }

    @Override
    protected void populateFields() {
        binding.setModel(model);
        binding.executePendingBindings();
    }

    @Override
    protected void bindFields() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.media_list_status, R.layout.adapter_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        binding.diaCurrentStatus.setAdapter(adapter);

        if(!TextUtils.isEmpty(model.getStatus()))
            binding.diaCurrentStatus.setSelection(CompatUtil.constructListFrom(KeyUtil.MediaListStatus).indexOf(model.getStatus()));
        else
            binding.diaCurrentStatus.setSelection(CompatUtil.constructListFrom(KeyUtil.MediaListStatus).indexOf(KeyUtil.PLANNING));

        binding.diaCurrentPrivacy.setChecked(model.isHidden());
        if(model.getMedia().getEpisodes() > 0)
            binding.diaCurrentProgress.setProgressMaximum(model.getMedia().getEpisodes());

        binding.diaCurrentScore.setProgressMaximum(100);
        binding.diaCurrentScore.setProgressCurrent(model.getScore());
        binding.diaCurrentProgress.setProgressCurrent(model.getProgress());
        binding.diaCurrentRewatch.setProgressCurrent(model.getRepeat());

        binding.diaCurrentStatus.setOnItemSelectedListener(this);
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {
        super.onViewRecycled();
        if(binding != null)
            binding.unbind();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        model.setStatus(KeyUtil.MediaListStatus[i]);
        switch (KeyUtil.MediaListStatus[i]) {
            case KeyUtil.CURRENT:
                if (CompatUtil.equals(getSeriesModel().getStatus(), KeyUtil.NOT_YET_RELEASED))
                    NotifyUtil.makeText(getContext(), R.string.warning_anime_not_airing, Toast.LENGTH_LONG).show();
                break;
            case KeyUtil.PLANNING:
                break;
            case KeyUtil.COMPLETED:
                if (!CompatUtil.equals(getSeriesModel().getStatus(), KeyUtil.FINISHED))
                    NotifyUtil.makeText(getContext(), R.string.warning_anime_is_airing, Toast.LENGTH_LONG).show();
                else {
                    int total = getSeriesModel().getEpisodes();
                    model.setProgress(total);
                    binding.diaCurrentProgress.setProgressCurrent(total);
                    //binding.diaCurrentProgress.setText(String.valueOf(total));
                }
                break;
            default:
                if (CompatUtil.equals(getSeriesModel().getStatus(), KeyUtil.NOT_YET_RELEASED))
                    NotifyUtil.makeText(getContext(), R.string.warning_anime_not_airing, Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}