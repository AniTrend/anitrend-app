package com.mxt.anitrend.presenter.index;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.Html;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mxt.anitrend.R;
import com.mxt.anitrend.SplashActivity;
import com.mxt.anitrend.api.call.SeriesModel;
import com.mxt.anitrend.api.call.UserModel;
import com.mxt.anitrend.api.model.User;
import com.mxt.anitrend.api.service.ServiceGenerator;
import com.mxt.anitrend.api.structure.Genre;
import com.mxt.anitrend.custom.emoji4j.EmojiManager;
import com.mxt.anitrend.data.DataCentre;
import com.mxt.anitrend.util.ApplicationPrefs;
import com.mxt.anitrend.util.DialogManager;
import com.tapadoo.alerter.Alerter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Maxwell on 11/20/2016.
 */
public class SplashPresenter extends AsyncTask<Object, Object, Call<User>> implements Callback<List<Genre>> {

    private SplashActivity mContext;
    private onUserRetrieved callback;
    private Alerter mAlerter;

    public SplashPresenter(SplashActivity mContext, onUserRetrieved callback) {
        this.callback = callback;
        this.mContext = mContext;
    }

    public void fetchGenres() {
        Call<List<Genre>> response = ServiceGenerator.createService(SeriesModel.class, mContext).fetchGenres();
        response.enqueue(this);
    }

    private void persistGenres(List<Genre> genreList){
        if(genreList != null){
            DataCentre dc = new DataCentre(mContext);
            dc.addGenres(genreList);
            dc.closeConnection();
            new ApplicationPrefs(mContext).setGenresSaved();
        }
    }

    public void makeAlerterInfo(String text) {
        mAlerter = Alerter.create(mContext)
                .setText(text)
                .setIcon(R.drawable.ic_done_all_white_24dp)
                .setDuration(1500)
                .setBackgroundColor(R.color.colorAccent);
        mAlerter.show();
    }

    public void makeAlerterWarning(String text) {
        mAlerter = Alerter.create(mContext)
                .setText(text)
                .setIcon(R.drawable.ic_warning_white_24dp)
                .setDuration(2500)
                .setBackgroundColor(R.color.colorStateOrange);
        mAlerter.show();
    }

    @Override
    protected Call<User> doInBackground(Object... voids) {
        try {
            EmojiManager.initEmojiData(mContext);
            UserModel userModel = ServiceGenerator.createService(UserModel.class, mContext);
            return userModel.fetchCurrentUser();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Call<User> user) {
        callback.onFetchComplete(user);
    }

    @Override
    public void onResponse(Call<List<Genre>> call, Response<List<Genre>> response) {
        persistGenres(response.body());
        callback.onGenreFetchFinish();
    }

    @Override
    public void onFailure(Call<List<Genre>> call, Throwable t) {
        callback.onGenreFetchFinish();
    }

    public void requestAppReset() {
        new DialogManager(mContext).createDialogMessage("Authentication Error", Html.fromHtml(mContext.getString(R.string.app_splash_authenticating_message)),
                mContext.getString(R.string.Yes), mContext.getString(R.string.No), new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                switch (which) {
                    case POSITIVE:
                        makeAlerterInfo("Application has been reset!");
                        ServiceGenerator.authStateChange(mContext);
                        new ApplicationPrefs(mContext).setUserDeactivated();
                        mContext.onRefresh();
                        break;
                    case NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        });
    }

    public interface onUserRetrieved{
        void onFetchComplete(Call<User> user);
        void onGenreFetchFinish();
    }
}
