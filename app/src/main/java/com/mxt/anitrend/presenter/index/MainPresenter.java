package com.mxt.anitrend.presenter.index;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.R;
import com.mxt.anitrend.api.call.RepoModel;
import com.mxt.anitrend.api.service.ServiceGenerator;
import com.mxt.anitrend.api.structure.Search;
import com.mxt.anitrend.event.ApplicationInitListener;
import com.mxt.anitrend.presenter.CommonPresenter;
import com.mxt.anitrend.util.AppVersionTracking;
import com.mxt.anitrend.util.DateTimeConverter;
import com.mxt.anitrend.util.ErrorHandler;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Maxwell on 10/7/2016.
 */
public class MainPresenter extends CommonPresenter {

    private Context mContext;
    //private DataCentre dc;
    private ApplicationInitListener callbackBase;

    public MainPresenter(Context context) {
        super(context);
        mContext = context;
    }

    public void setOnInitCallback(ApplicationInitListener callback){
        callbackBase = callback;
    }

    public void notifyAllItems(String text) {
        EventBus.getDefault().post(text);
    }

    public void startExecution() {
        new Initializer().execute();
    }

    public Search getSearchModel() {
        //"id" || "score" || "popularity" || "start_date" || "end_date" Sorts results, default ascending order. Append "-desc" for descending order e.g. "id-desc"
        return new Search("anime", /*anime or manga*/
                getApiPrefs().getYear(), /*year*/
                DateTimeConverter.getSeason(), /*season*/
                getApiPrefs().getShowType(), /*Type e.g. TV or Movie e.t.c*/
                getApiPrefs().getStatus(), /*status*/
                getApiPrefs().getGenres(), /*genre */
                getApiPrefs().getExcluded(), /*genre exclude*/
                getApiPrefs().getSort(), /*sort*/
                getApiPrefs().getOrder(), /*order*/
                true, /*airing data*/
                false, /*full page*/
                null);/*page*/
    }

    private class Initializer extends AsyncTask<Void,Void,Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            FirebaseApp.initializeApp(mContext);
            MobileAds.initialize(mContext.getApplicationContext(), mContext.getResources().getString(R.string.app_id));
            FirebaseAnalytics.getInstance(mContext).setAnalyticsCollectionEnabled(true);
            FirebaseAnalytics.getInstance(mContext).setMinimumSessionDuration(2000);
            //dc.getGenres(null, null);

            AppVersionTracking current = new AppVersionTracking(BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME);

            if(!getAppPrefs().checkState()){
                getAppPrefs().saveOrUpdateVersionNumber(current);
                return false;
            }
            AppVersionTracking saved = getAppPrefs().getSavedVersions();
            if((BuildConfig.VERSION_CODE > saved.getCode())) {
                getAppPrefs().saveOrUpdateVersionNumber(current);
                return true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean desc) {
            //dc.closeConnection();
            if(isCancelled())
                return;

            try {
                if(desc != null)
                    if (desc) {
                        callbackBase.onUpdatedVersion();
                    } else {
                        callbackBase.onNewInstallation();
                    }

                RepoModel repo = ServiceGenerator.createRepoService();
                repo.checkVersion().enqueue(new Callback<AppVersionTracking>() {
                    @Override
                    public void onResponse(Call<AppVersionTracking> call, Response<AppVersionTracking> response) {
                        if(response.isSuccessful() && response.body() != null) {
                                AppVersionTracking repo_version = response.body();
                                if (repo_version != null && repo_version.checkAgainstCurrent()) {
                                    getAppPrefs().saveRepoVersion(repo_version);
                                    callbackBase.onNormalStart();
                                }
                        } else
                            Log.e("MainPresenter{onPost}", ErrorHandler.getError(response).toString());
                    }

                    @Override
                    public void onFailure(Call<AppVersionTracking> call, Throwable t) {
                        t.printStackTrace();
                        if(mContext != null)
                            Toast.makeText(mContext, R.string.text_update_check_failed, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                FirebaseCrash.report(ex);
                if(mContext != null)
                    Toast.makeText(mContext, R.string.text_update_check_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }
}