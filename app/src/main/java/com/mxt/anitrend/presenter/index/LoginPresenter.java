package com.mxt.anitrend.presenter.index;

import android.net.Uri;
import android.os.AsyncTask;

import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.utils.PaletteUtils;
import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.R;
import com.mxt.anitrend.api.call.UserModel;
import com.mxt.anitrend.api.model.User;
import com.mxt.anitrend.api.service.ServiceGenerator;
import com.mxt.anitrend.async.TokenReference;
import com.mxt.anitrend.presenter.CommonPresenter;
import com.mxt.anitrend.view.index.activity.LoginActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Maxwell on 11/14/2016.
 */

public class LoginPresenter extends CommonPresenter {

    private LoginActivity mActivity;
    private LoginCallback mCallback;
    private Authorize reference;
    private int notification_count = -1;

    public LoginPresenter(LoginActivity mActivity, LoginCallback mCallback) {
        super(mActivity);
        this.mActivity = mActivity;
        this.mCallback = mCallback;
    }

    public void handleLoginCallback(Uri uri) {
        //Use the parameter your API exposes for the code (mostly it's "code")
        String code = uri.getQueryParameter(BuildConfig.RESPONSE_TYPE);
        if(code != null) {
            TokenReference tokenReference = new TokenReference(mActivity);
            if(tokenReference.AuthorizeNewToken(code)) {
                getAppPrefs().setUserAuthenticated();
                ServiceGenerator.authStateChange(mActivity);
                reference = new Authorize();
                reference.execute();
            }
            else
                createSuperToast(mActivity, mActivity.getString(R.string.text_error_capture_login), R.drawable.ic_info_outline_white_18dp,
                        Style.TYPE_STANDARD, Style.DURATION_VERY_LONG,
                        PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_AMBER));
        } else if ((code = uri.getQueryParameter("error")) != null)
            createSuperToast(mActivity, code, R.drawable.ic_info_outline_white_18dp,
                    Style.TYPE_STANDARD, Style.DURATION_VERY_LONG,
                    PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_RED));
        else
            createSuperToast(mActivity, mActivity.getString(R.string.text_error_auth_login), R.drawable.ic_info_outline_white_18dp,
                    Style.TYPE_STANDARD, Style.DURATION_VERY_LONG,
                    PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_AMBER));
    }

    public void candlePendingLogin() {
        if(reference != null) {
            reference.cancel(true);
        }
    }

    private class Authorize extends AsyncTask<Void,Void,Call<User>> {

        @Override
        protected Call<User> doInBackground(Void... voids) {
            try {
                notification_count = ServiceGenerator.createService(UserModel.class, mActivity).fetchNotificationCount().execute().body();
                return ServiceGenerator.createService(UserModel.class, mActivity).fetchCurrentUser();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Call<User> user) {
            if(user != null) {
                if (!isCancelled()) {
                    user.enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            if (response.body() != null) {
                                getAppPrefs().saveMiniUserInfo(response.body());
                                mCallback.onLoginComplete(response.body(), notification_count);
                            }
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            t.printStackTrace();
                            if (t.getLocalizedMessage().startsWith("java.lang.IllegalStateException: Expected BEGIN_OBJECT")) {
                                createSuperToast(mActivity,
                                        mActivity.getString(R.string.text_login_success_with_error),
                                        R.drawable.ic_info_outline_white_18dp,
                                        Style.TYPE_STANDARD, Style.DURATION_VERY_LONG,
                                        PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_RED));
                            } else {
                                createSuperToast(mActivity, t.getLocalizedMessage(), R.drawable.ic_info_outline_white_18dp,
                                        Style.TYPE_STANDARD, Style.DURATION_VERY_LONG,
                                        PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_RED));
                            }
                            mCallback.onLoginComplete(null, -1);
                        }
                    });
                } else {
                    createSuperToast(mActivity, mActivity.getString(R.string.text_login_canceled), R.drawable.ic_info_outline_white_18dp,
                            Style.TYPE_STANDARD, Style.DURATION_VERY_LONG,
                            PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_AMBER));
                    mCallback.onLoginComplete(null, -1);
                }
            } else {
                createSuperToast(mActivity, mActivity.getString(R.string.text_error_login), R.drawable.ic_info_outline_white_18dp,
                        Style.TYPE_STANDARD, Style.DURATION_VERY_LONG,
                        PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_AMBER));
                mCallback.onLoginComplete(null, -1);
            }
        }
    }

    public interface LoginCallback {
        void onLoginComplete(User user, int notifications);
    }
}
