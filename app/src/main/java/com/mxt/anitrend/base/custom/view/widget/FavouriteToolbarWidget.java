package com.mxt.anitrend.base.custom.view.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.databinding.WidgetToolbarFavouriteBinding;
import com.mxt.anitrend.model.entity.base.CharacterBase;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.base.StaffBase;
import com.mxt.anitrend.model.entity.base.StudioBase;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.graphql.AniGraphErrorUtilKt;
import com.mxt.anitrend.util.graphql.GraphUtil;
import com.mxt.anitrend.util.media.MediaUtil;

import io.github.wax911.library.model.request.QueryContainerBuilder;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by max on 2018/01/31.
 * Widget for handling favourite toggles
 */

public class FavouriteToolbarWidget extends FrameLayout implements CustomView, RetroCallback<ResponseBody>, View.OnClickListener {

    private WidgetPresenter<ResponseBody> presenter;
    private WidgetToolbarFavouriteBinding binding;

    private StaffBase staffBase;
    private MediaBase mediaBase;
    private StudioBase studioBase;
    private CharacterBase characterBase;

    private QueryContainerBuilder queryContainer;

    private final String TAG = FavouriteToolbarWidget.class.getSimpleName();

    public FavouriteToolbarWidget(@NonNull Context context) {
        super(context);
        onInit();
    }

    public FavouriteToolbarWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public FavouriteToolbarWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FavouriteToolbarWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        onInit();
    }

    @Override
    public void onInit() {
        presenter = new WidgetPresenter<>(getContext());
        binding = WidgetToolbarFavouriteBinding.inflate(CompatUtil.INSTANCE.getLayoutInflater(getContext()), this, true);
        queryContainer = GraphUtil.INSTANCE.getDefaultQuery(false)
                .putVariable(KeyUtil.arg_page_limit, KeyUtil.SINGLE_ITEM_LIMIT);
        binding.setOnClickEvent(this);
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {
        resetFlipperState();
        if(presenter != null)
            presenter.onDestroy();
    }

    private void resetFlipperState() {
        if(binding.widgetFlipper.getDisplayedChild() == WidgetPresenter.LOADING_STATE)
            binding.widgetFlipper.setDisplayedChild(WidgetPresenter.CONTENT_STATE);
    }

    public void setModel(StaffBase staffBase) {
        this.staffBase = staffBase;
        setIconType();
        queryContainer.putVariable(KeyUtil.arg_staffId, staffBase.getId());
        presenter.getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        binding.widgetFlipper.setVisibility(VISIBLE);
    }

    public void setModel(CharacterBase characterBase) {
        this.characterBase = characterBase;
        setIconType();
        queryContainer.putVariable(KeyUtil.arg_characterId, characterBase.getId());
        presenter.getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        binding.widgetFlipper.setVisibility(VISIBLE);
    }

    public void setModel(StudioBase studioBase) {
        this.studioBase = studioBase;
        setIconType();
        queryContainer.putVariable(KeyUtil.arg_studioId, studioBase.getId());
        presenter.getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        binding.widgetFlipper.setVisibility(VISIBLE);
    }

    public void setModel(MediaBase mediaBase) {
        this.mediaBase = mediaBase;
        setIconType();
        queryContainer.putVariable(MediaUtil.isAnimeType(mediaBase) ? KeyUtil.arg_animeId : KeyUtil.arg_mangaId, mediaBase.getId());
        presenter.getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        binding.widgetFlipper.setVisibility(VISIBLE);
    }

    public boolean isModelSet() {
        return staffBase != null || characterBase != null ||
                studioBase != null || mediaBase != null;
    }

    @Override
    public void onClick(View view) {
        if(presenter.getSettings().isAuthenticated())
            if (view.getId() == R.id.widget_flipper) {
                if (isModelSet()) {
                    if (binding.widgetFlipper.getDisplayedChild() == WidgetPresenter.CONTENT_STATE) {
                        binding.widgetFlipper.showNext();
                        presenter.requestData(KeyUtil.MUT_TOGGLE_FAVOURITE, getContext(), this);
                    } else
                        NotifyUtil.INSTANCE.makeText(getContext(), R.string.busy_please_wait, Toast.LENGTH_SHORT).show();
                } else
                    NotifyUtil.INSTANCE.makeText(getContext(), R.string.text_activity_loading, Toast.LENGTH_SHORT).show();
            }
            else
                NotifyUtil.INSTANCE.makeText(getContext(), R.string.info_login_req, R.drawable.ic_group_add_grey_600_18dp, Toast.LENGTH_SHORT).show();
    }

    private void setIconType() {
        boolean isFavourite = false, requiresTint = true;

        if(mediaBase != null) {
            isFavourite = mediaBase.isFavourite();
            requiresTint = false;
        }
        else if (studioBase != null)
            isFavourite = studioBase.isFavourite();
        else if (staffBase != null)
            isFavourite = staffBase.isFavourite();
        else if (characterBase != null)
            isFavourite = characterBase.isFavourite();

        if(isFavourite)
            binding.widgetLike.setImageDrawable(requiresTint ? CompatUtil.INSTANCE.getTintedDrawable(getContext(),
                    R.drawable.ic_favorite_white_24dp) : CompatUtil.INSTANCE.getDrawable(getContext(), R.drawable.ic_favorite_white_24dp));
        else
            binding.widgetLike.setImageDrawable(requiresTint ? CompatUtil.INSTANCE.getTintedDrawable(getContext(),
                    R.drawable.ic_favorite_border_white_24dp) : CompatUtil.INSTANCE.getDrawable(getContext(), R.drawable.ic_favorite_border_white_24dp));

        resetFlipperState();
    }

    @Override
    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
        try {
            if(response.isSuccessful()) {
                if(mediaBase != null)
                    mediaBase.toggleFavourite();
                else if (studioBase != null)
                    studioBase.toggleFavourite();
                else if (staffBase != null)
                    staffBase.toggleFavourite();
                else if (characterBase != null)
                    characterBase.toggleFavourite();
                setIconType();
            } else {
                Timber.tag(TAG).w(AniGraphErrorUtilKt.apiError(response));
                NotifyUtil.INSTANCE.makeText(getContext(), R.string.text_error_request, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
        try {
            Timber.w(throwable);
            resetFlipperState();
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}
