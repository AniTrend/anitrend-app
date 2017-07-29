package com.mxt.anitrend.presenter;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.view.View;

import com.bumptech.glide.manager.LifecycleListener;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.github.johnpersano.supertoasts.library.SuperToast;
import com.github.johnpersano.supertoasts.library.utils.PaletteUtils;
import com.mxt.anitrend.R;
import com.mxt.anitrend.api.model.UserSmall;
import com.mxt.anitrend.custom.event.LifeCycleListener;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.api.structure.Search;
import com.mxt.anitrend.custom.recycler.ScrollListener;
import com.mxt.anitrend.util.ApiPreferences;
import com.mxt.anitrend.util.ApplicationPrefs;
import com.mxt.anitrend.util.DefaultPreferences;
import com.tapadoo.alerter.Alerter;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import retrofit2.Callback;

/**
 * Created by max on 2017/03/06.
 */
public abstract class CommonPresenter <T> extends ScrollListener implements LifeCycleListener {

    private ApplicationPrefs applicationPrefs;
    private DefaultPreferences defaultPreferences;
    private Parcelable instance_position;
    private ApiPreferences api_preferences;
    private SuperToast mToast;
    private Alerter mAlerter;
    private Set<String> indexGenres;

    private UserSmall mCurrentUser;

    public CommonPresenter(Context context) {
        applicationPrefs = new ApplicationPrefs(context);
        defaultPreferences = new DefaultPreferences(context);
        api_preferences = new ApiPreferences(context);
        indexGenres = new android.support.v4.util.ArraySet<>();
        mCurrentUser = applicationPrefs.isAuthenticated()? applicationPrefs.getMiniUser():null;
    }

    public ApplicationPrefs getAppPrefs() {
        return applicationPrefs;
    }

    public ApiPreferences getApiPrefs() {
        return api_preferences;
    }

    public DefaultPreferences getDefaultPrefs() {
        return defaultPreferences;
    }

    public List<String> getGenres() {
        return Arrays.asList(KeyUtils.GenreTypes);
    }

    public void saveGenres(Integer[] which, CharSequence[] text, Activity mContext) {
        String genres = "";
        createSuperToast(mContext,
                mContext.getString(R.string.text_filter_applying),
                R.drawable.ic_reset,
                Style.TYPE_PROGRESS_BAR, Style.DURATION_MEDIUM,
                PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_CYAN));

        for (CharSequence temp: text) {
            if(genres.isEmpty())
                genres += String.format("%s",temp);
            else
                genres += String.format(",%s",temp);
        }

        for (Integer index: which)
            indexGenres.add(String.valueOf(index));

        // Save the built list
        getApiPrefs().saveGenres(genres);
        getApiPrefs().saveGenresIndices(indexGenres);
        //notifyAllItems();
    }

    public Integer[] getSelectedGenres() {

        Set<String> set = getApiPrefs().getGenresIndices();
        if(set == null)
            return null;

        int index = 0;
        Integer[] mTemp = new Integer[set.size()];

        for (String s: set) {
            mTemp[index] = Integer.parseInt(s);
            index++;
        }
        return mTemp;
    }

    /**
     * Available for most calls throughout the application
     */
    public UserSmall getCurrentUser() {
        return mCurrentUser;
    }

    /**
     * Show super toast from an activity
     * <br/>
     * @param mContext Activity calling this method.
     * @param mText Desired message to display
     * @param mIcon Drawable preferred 24dp vectors will also work
     * @param mType Style.TYPE type of the toast
     *
     * <br/>
     * @see com.github.johnpersano.supertoasts.library.Style for toast duration & style
     */
    public void createSuperToast(Activity mContext, String mText, @DrawableRes int mIcon, int mType) {
        mToast = SuperActivityToast.create(mContext, new Style(), mType)
                .setText(" "+mText) //add a space for the icon
                .setTypefaceStyle(Typeface.NORMAL)
                .setIconPosition(Style.ICONPOSITION_LEFT)
                .setIconResource(mIcon)
                .setDuration(Style.DURATION_VERY_SHORT)
                .setFrame(Style.FRAME_LOLLIPOP)
                .setColor(PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_CYAN))
                .setAnimations(Style.ANIMATIONS_FLY);
        mToast.show();
    }

    /**
     * Show super toast from an activity
     * <br/>
     * @param mContext Activity calling this method.
     * @param mText Desired message to display
     * @param mIcon Drawable preferred 24dp vectors will also work
     * @param mType Style.TYPE type of the toast
     * @param mDuration Style.DURATION_LENGTH defines the duration toast should be visible for
     * @param mColor PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_RED) to get a color
     *
     * <br/>
     * @see com.github.johnpersano.supertoasts.library.Style for toast duration & style
     *
     * @see com.github.johnpersano.supertoasts.library.utils.PaletteUtils for colors
     */
    public void createSuperToast(Activity mContext, String mText, @DrawableRes int mIcon, int mType, int mDuration, int mColor) {
        mToast = SuperActivityToast.create(mContext, new Style(), mType)
                .setText(" "+mText)
                .setTypefaceStyle(Typeface.NORMAL)
                .setIconPosition(Style.ICONPOSITION_LEFT)
                .setIconResource(mIcon)
                .setDuration(mDuration)
                .setFrame(Style.FRAME_LOLLIPOP)
                .setColor(mColor)
                .setAnimations(Style.ANIMATIONS_FLY);
        mToast.show();
    }

    /**
     * Create an Alerter with an infinite duration
     */
    public void createAlerter(Activity mContext, String mTitle, String mText, @DrawableRes int mIcon, @ColorRes int mColor, View.OnClickListener onClickListener) {
        mAlerter = Alerter.create(mContext)
                .setTitle(mTitle)
                .setText(mText)
                .setIcon(mIcon)
                .setOnClickListener(onClickListener)
                .enableInfiniteDuration(true)
                .setBackgroundColor(mColor);
        mAlerter.show();
    }

    /**
     * Create an Alerter which will run for 6.5 seconds
     */
    public void createAlerter(Activity mContext, String mTitle, String mText, @DrawableRes int mIcon, @ColorRes int mColor) {
        mAlerter = Alerter.create(mContext)
                .setTitle(mTitle)
                .setText(mText)
                .setIcon(mIcon)
                .setDuration(6500)
                .setBackgroundColor(mColor);
        mAlerter.show();
    }

    /**
     * Create an Alerter which will run for x seconds
     * @param duration the amount of seconds e.g. 2, 5, 10 e.t.c
     */
    public void createAlerter(Activity mContext, String mTitle, String mText, @DrawableRes int mIcon, @ColorRes int mColor, int duration) {
        mAlerter = Alerter.create(mContext)
                .setTitle(mTitle)
                .setText(mText)
                .setIcon(mIcon)
                .setDuration(duration * 1000)
                .setBackgroundColor(mColor);
        mAlerter.show();
    }

    public void makeAlerterSuccess(Activity mContext, String text) {
        mAlerter = Alerter.create(mContext)
                .setText(text)
                .setIcon(R.drawable.ic_done_all_white_24dp)
                .setDuration(2500)
                .setBackgroundColor(R.color.colorAccent);
        mAlerter.show();
    }

    public void makeAlerterWarning(Activity mContext, String text) {
        mAlerter = Alerter.create(mContext)
                .setText(text)
                .setIcon(R.drawable.ic_warning_white_24dp)
                .setDuration(2500)
                .setBackgroundColor(R.color.colorStateOrange);
        mAlerter.show();
    }

    public void beginAsync(Callback<T> callback, int id) {
        //Empty method body, not mandatory to implement
    }

    public void beginAsync(Callback<T> callback) {
        //Empty method body, not mandatory to implement
    }

    public void beginAsync(Callback<T> callback, int id, int page) {
        //Empty method body, not mandatory to implement
    }

    public void beginAsync(Callback<T> callback, int page, String type) {
        //Empty method body, not mandatory to implement
    }

    public Parcelable getSavedParse() {
        return instance_position;
    }

    public void setParcelable(Parcelable parcelable) {
        instance_position = parcelable;
    }

    public void beginAsync(Callback<T> callback, Search searchModel) {

    }

    /**
     * Unregister any listeners from fragments or activities
     */
    @Override
    public void onPause() {

    }

    /**
     * Register any listeners from fragments or activities
     */
    @Override
    public void onResume() {

    }

    /**
     * Destroy any reference which maybe attached to
     * our context
     */
    @Override
    public void onDestroy() {
        if(mToast != null && mToast.isShowing()) {
            mToast.dismiss();
            mToast = null;
        }
    }
}
