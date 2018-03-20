package com.mxt.anitrend.presenter.activity;

import android.content.Context;
import android.text.TextUtils;

import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.base.custom.async.WebTokenRequest;
import com.mxt.anitrend.base.custom.presenter.CommonPresenter;
import com.mxt.anitrend.model.entity.base.MessageBase;

import java.util.concurrent.ExecutionException;

/**
 * Created by max on 2017/11/01.
 * Login presenter
 */

public class LoginPresenter extends CommonPresenter {

    public LoginPresenter(Context context) {
        super(context);
    }

    public boolean handleIntentCallback(MessageBase messageBase) {
        String response = messageBase.getQueryParam(BuildConfig.RESPONSE_TYPE);
        try {
            if(!TextUtils.isEmpty(response) && WebTokenRequest.getToken(getContext(), response)) {
                getApplicationPref().setAuthenticated(true);
                return true;
            }
        } catch (ExecutionException | InterruptedException  e) {
            e.printStackTrace();
        }
        return false;
    }
}
