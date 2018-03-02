package com.mxt.anitrend.base.custom.view.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.databinding.WidgetToolbarFavouriteBinding;
import com.mxt.anitrend.model.entity.anilist.Character;
import com.mxt.anitrend.model.entity.anilist.Favourite;
import com.mxt.anitrend.model.entity.anilist.Series;
import com.mxt.anitrend.model.entity.anilist.Staff;
import com.mxt.anitrend.model.entity.anilist.Studio;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by max on 2018/01/31.
 */

public class FavouriteToolbarWidget extends FrameLayout implements CustomView, RetroCallback<ResponseBody>, View.OnClickListener {

    private WidgetPresenter<ResponseBody> presenter;
    private WidgetToolbarFavouriteBinding binding;
    private @KeyUtils.RequestMode int requestType;

    private Staff staff;
    private Series series;
    private Studio studio;
    private Character character;

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

    public FavouriteToolbarWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        onInit();
    }

    @Override
    public void onInit() {
        presenter = new WidgetPresenter<>(getContext());
        binding = WidgetToolbarFavouriteBinding.inflate(CompatUtil.getLayoutInflater(getContext()), this, true);
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

    public void setModel(Staff staff) {
        presenter.getParams().putLong(KeyUtils.arg_id, staff.getId());
        this.requestType = KeyUtils.STAFF_FAVOURITE_REQ;
        this.staff = staff;
        setIconType();
        binding.widgetFlipper.setVisibility(VISIBLE);
    }

    public void setModel(Character character) {
        presenter.getParams().putLong(KeyUtils.arg_id, character.getId());
        this.requestType = KeyUtils.CHARACTER_FAVOURITE_REQ;
        this.character = character;
        setIconType();
        binding.widgetFlipper.setVisibility(VISIBLE);
    }

    public void setModel(Studio studio) {
        presenter.getParams().putLong(KeyUtils.arg_id, studio.getId());
        this.requestType = KeyUtils.STUDIO_FAVOURITE_REQ;
        this.studio = studio;
        setIconType();
        binding.widgetFlipper.setVisibility(VISIBLE);
    }

    public void setModel(Series series) {
        presenter.getParams().putLong(KeyUtils.arg_id, series.getId());
        this.requestType = series.getSeries_type().equals(KeyUtils.SeriesTypes[KeyUtils.ANIME]) ?
                KeyUtils.ANIME_FAVOURITE_REQ : KeyUtils.MANGA_FAVOURITE_REQ;
        this.series = series;
        setIconType();
        binding.widgetFlipper.setVisibility(VISIBLE);
    }

    @Override
    public void onClick(View view) {
        if(presenter.getApplicationPref().isAuthenticated())
            switch (view.getId()) {
                case R.id.widget_flipper:
                    if (requestType != 0) {
                        if (binding.widgetFlipper.getDisplayedChild() == WidgetPresenter.CONTENT_STATE) {
                            binding.widgetFlipper.showNext();
                            presenter.requestData(requestType, getContext(), this);
                        }
                        else
                            NotifyUtil.makeText(getContext(), R.string.busy_please_wait, Toast.LENGTH_SHORT).show();
                    } else
                        NotifyUtil.makeText(getContext(), R.string.text_activity_loading, Toast.LENGTH_SHORT).show();
                    break;
            }
            else
                NotifyUtil.makeText(getContext(), R.string.info_login_req, R.drawable.ic_group_add_grey_600_18dp, Toast.LENGTH_SHORT).show();
    }

    private void setIconType() {
        boolean isFavourite, requiresTint = true;
        switch (requestType) {
            case KeyUtils.CHARACTER_FAVOURITE_REQ:
                isFavourite = character.isFavourite();
                break;
            case KeyUtils.STAFF_FAVOURITE_REQ:
                isFavourite = staff.isFavourite();
                break;
            case KeyUtils.STUDIO_FAVOURITE_REQ:
                isFavourite = studio.isFavourite();
                break;
            default:
                isFavourite = series.isFavourite();
                requiresTint = false;
                break;
        }

        if(isFavourite)
            binding.widgetLike.setImageDrawable(requiresTint ? CompatUtil.getTintedDrawable(getContext(),
                    R.drawable.ic_favorite_white_24dp) : CompatUtil.getDrawable(getContext(), R.drawable.ic_favorite_white_24dp));
        else
            binding.widgetLike.setImageDrawable(requiresTint ? CompatUtil.getTintedDrawable(getContext(),
                    R.drawable.ic_favorite_border_white_24dp) : CompatUtil.getDrawable(getContext(), R.drawable.ic_favorite_border_white_24dp));

        resetFlipperState();
    }

    @Override
    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
        try {
            if(response.isSuccessful()) {
                UserBase currentUser = presenter.getDatabase().getCurrentUser();
                Favourite favourite = presenter.getDatabase().getFavourite(currentUser.getId());
                if(favourite == null) {
                    favourite = new Favourite();
                    favourite.initCollections();
                }
                switch (requestType) {
                    case KeyUtils.ANIME_FAVOURITE_REQ:
                        series.setFavourite(!series.isFavourite());
                        if(favourite.getAnime() == null)
                            favourite.createAnimeCollection();
                        if(series.isFavourite())
                            favourite.getAnime().add(series);
                        else
                            favourite.getAnime().remove(series);
                        break;
                    case KeyUtils.MANGA_FAVOURITE_REQ:
                        series.setFavourite(!series.isFavourite());
                        if(favourite.getManga() == null)
                            favourite.createMangaCollection();
                        if(series.isFavourite())
                            favourite.getManga().add(series);
                        else
                            favourite.getManga().remove(series);
                        break;
                    case KeyUtils.CHARACTER_FAVOURITE_REQ:
                        character.setFavourite(!character.isFavourite());
                        if(favourite.getCharacter() == null)
                            favourite.createCharacterCollection();
                        if(character.isFavourite())
                            favourite.getCharacter().add(character);
                        else
                            favourite.getCharacter().remove(character);
                        break;
                    case KeyUtils.STAFF_FAVOURITE_REQ:
                        staff.setFavourite(!staff.isFavourite());
                        if(favourite.getStaff() == null)
                            favourite.createStaffCollection();
                        if(staff.isFavourite())
                            favourite.getStaff().add(staff);
                        else
                            favourite.getStaff().remove(staff);
                        break;
                    case KeyUtils.STUDIO_FAVOURITE_REQ:
                        studio.setFavourite(!studio.isFavourite());
                        if(favourite.getStudio() == null)
                            favourite.createStudioCollection();
                        if(studio.isFavourite())
                            favourite.getStudio().add(studio);
                        else
                            favourite.getStudio().remove(studio);
                        break;
                }
                presenter.getDatabase().saveFavourite(favourite);
                setIconType();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
        try {
            Log.e(toString(), throwable.getLocalizedMessage());
            throwable.printStackTrace();
            resetFlipperState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
