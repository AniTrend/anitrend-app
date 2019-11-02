package com.mxt.anitrend.base.custom.view.widget;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.SnapHelper;

import com.annimon.stream.IntPair;
import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.detail.ImagePreviewAdapter;
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter;
import com.mxt.anitrend.base.interfaces.event.ItemClickListener;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.databinding.WidgetStatusBinding;
import com.mxt.anitrend.model.entity.anilist.FeedList;
import com.mxt.anitrend.model.entity.anilist.FeedReply;
import com.mxt.anitrend.util.CenterSnapUtil;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.markdown.RegexUtil;
import com.mxt.anitrend.view.activity.base.ImagePreviewActivity;
import com.mxt.anitrend.view.activity.base.VideoPlayerActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Created by max on 2017/11/25.
 */
@Deprecated
public class StatusContentWidget extends LinearLayout implements CustomView, ItemClickListener<String>, CenterSnapUtil.PositionChangeListener {

    private List<String> contentLinks, contentTypes;
    private WidgetStatusBinding binding;

    public StatusContentWidget(@NonNull Context context) {
        super(context);
        onInit();
    }

    public StatusContentWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public StatusContentWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public StatusContentWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        onInit();
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        binding = WidgetStatusBinding.inflate(LayoutInflater.from(getContext()), this, true);
        binding.widgetStatusRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.widgetStatusRecycler.setNestedScrollingEnabled(true);
        SnapHelper snapHelper = new CenterSnapUtil(this);
        snapHelper.attachToRecyclerView(binding.widgetStatusRecycler);
    }

    public void setModel(FeedList model) {
        binding.widgetStatusText.setMarkDownText(model.getText());
        findMediaAttachments(model.getText());
    }

    public void setModel(FeedReply model) {
        binding.widgetStatusText.setMarkDownText(model.getReply());
        findMediaAttachments(model.getReply());
    }

    public void setTextData(@Nullable String textData) {
        binding.widgetStatusText.setMarkDownText(textData);
        findMediaAttachments(textData);
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {
        contentLinks = null; contentTypes = null;
    }

    private void findMediaAttachments(@Nullable String value) {
        if(!TextUtils.isEmpty(value)) {
            Matcher matcher = RegexUtil.INSTANCE.findMedia(value);
            contentLinks = new ArrayList<>();
            contentTypes = new ArrayList<>();
            while (matcher.find()) {
                int gc = matcher.groupCount();
                String tag = matcher.group(gc - 1);
                String media = matcher.group(gc);
                contentTypes.add(tag);
                if (tag.equals(RegexUtil.KEY_YOU))
                    contentLinks.add(RegexUtil.INSTANCE.buildYoutube(media.replace("(", "").replace(")", "")));
                else
                    contentLinks.add(media.replace("(", "").replace(")", ""));
            }
        }
        constructAdditionalViews();
    }

    private void constructAdditionalViews() {
        if(!CompatUtil.INSTANCE.isEmpty(contentLinks)) {
            RecyclerViewAdapter<String> previewAdapter = new ImagePreviewAdapter(contentTypes,  getContext());
            previewAdapter.onItemsInserted(contentLinks);
            previewAdapter.setClickListener(this);
            binding.widgetStatusRecycler.setAdapter(previewAdapter);

            if(previewAdapter.getItemCount() < 2)
                binding.widgetStatusIndicator.setVisibility(GONE);
            else {
                binding.widgetStatusIndicator.setVisibility(VISIBLE);
                binding.widgetStatusIndicator.setMaximum(previewAdapter.getItemCount());
                binding.widgetStatusIndicator.setCurrentPosition(1);
            }
            binding.widgetSlideHolder.setVisibility(VISIBLE);
        } else
            binding.widgetSlideHolder.setVisibility(GONE);
    }

    @Override
    public void onPageChanged(int currentPage) {
        binding.widgetStatusIndicator.setCurrentPosition(currentPage);
    }

    /**
     * When the target view from {@link OnClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been clicked
     * @param data   the model that at the clicked index
     */
    @Override
    public void onItemClick(View target, IntPair<String> data) {
        Intent intent;
        switch (contentTypes.get(data.getFirst()).toLowerCase()) {
            case RegexUtil.KEY_IMG:
                intent = new Intent(getContext(), ImagePreviewActivity.class);
                intent.putExtra(KeyUtil.arg_model, data.getSecond());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
                break;
            case RegexUtil.KEY_WEB:
                intent = new Intent(getContext(), VideoPlayerActivity.class);
                intent.putExtra(KeyUtil.arg_model, data.getSecond());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
                break;
            case RegexUtil.KEY_YOU:
                try {
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(data.getSecond()));
                    getContext().startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    /**
     * When the target view from {@link OnLongClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been long clicked
     * @param data   the model that at the long clicked index
     */
    @Override
    public void onItemLongClick(View target, IntPair<String> data) {

    }
}
