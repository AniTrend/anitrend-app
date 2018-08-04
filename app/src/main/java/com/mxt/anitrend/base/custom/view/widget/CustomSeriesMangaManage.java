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
import com.mxt.anitrend.databinding.CustomActionMangaBinding;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.MediaListUtil;
import com.mxt.anitrend.util.NotifyUtil;

/**
 * Created by max on 2018/01/03.
 */

public class CustomSeriesMangaManage extends CustomSeriesManageBase {

    private CustomActionMangaBinding binding;

    public CustomSeriesMangaManage(Context context) {
        super(context);
    }

    public CustomSeriesMangaManage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSeriesMangaManage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CustomSeriesMangaManage(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        super.onInit();
        binding = CustomActionMangaBinding.inflate(CompatUtil.getLayoutInflater(getContext()), this, true);
    }

    /**
     * Saves the current views states into the model
     * and returns a bundle of the params
     * @see com.mxt.anitrend.util.MediaListUtil
     */
    @Override
    public Bundle persistChanges() {
        model.setProgress(binding.diaCurrentChapters.getProgressCurrent());
        model.setRepeat(binding.diaCurrentReread.getProgressCurrent());
        model.setProgressVolumes(binding.diaCurrentVolumes.getProgressCurrent());
        model.setScore(binding.diaCurrentScore.getProgressCurrent());
        model.setStartedAt(binding.diaCurrentStartedAt.getDate());
        model.setCompletedAt(binding.diaCurrentCompletedAt.getDate());
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

        if(model.getMedia().getVolumes() > 0)
            binding.diaCurrentVolumes.setProgressMaximum(model.getMedia().getVolumes());
        if(model.getMedia().getChapters() > 0)
            binding.diaCurrentChapters.setProgressMaximum(model.getMedia().getChapters());

        binding.diaCurrentScore.setProgressMaximum(100);
        binding.diaCurrentScore.setProgressCurrent((int)model.getScore());
        binding.diaCurrentChapters.setProgressCurrent(model.getProgress());
        binding.diaCurrentVolumes.setProgressCurrent(model.getProgressVolumes());
        binding.diaCurrentReread.setProgressCurrent(model.getRepeat());
        binding.diaCurrentStartedAt.setDate(model.getStartedAt());
        binding.diaCurrentCompletedAt.setDate(model.getCompletedAt());

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
                    NotifyUtil.makeText(getContext(), R.string.warning_manga_not_publishing, Toast.LENGTH_LONG).show();
                break;
            case KeyUtil.PLANNING:
                break;
            case KeyUtil.COMPLETED:
                if (!CompatUtil.equals(getSeriesModel().getStatus(), KeyUtil.FINISHED))
                    NotifyUtil.makeText(getContext(), R.string.warning_manga_publishing, Toast.LENGTH_LONG).show();
                else {
                    int total = getSeriesModel().getChapters();
                    model.setProgress(total);
                    binding.diaCurrentChapters.setProgressCurrent(total);
                    total = getSeriesModel().getVolumes();
                    if(total > 0) {
                        model.setProgressVolumes(total);
                        binding.diaCurrentVolumes.setProgressCurrent(total);
                    }
                }
                break;
            default:
                if (CompatUtil.equals(getSeriesModel().getStatus(), KeyUtil.NOT_YET_RELEASED))
                    NotifyUtil.makeText(getContext(), R.string.warning_manga_not_publishing, Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
