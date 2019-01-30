package com.mxt.anitrend.worker;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.base.custom.async.WebTokenRequest;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.KeyUtil;

import java.util.concurrent.ExecutionException;

import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class AuthenticatorWorker extends Worker {

    private final BasePresenter presenter;
    private final Uri authenticatorUri;

    public AuthenticatorWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        authenticatorUri = Uri.parse(workerParams.getInputData()
                .getString(KeyUtil.arg_model));
        presenter = new BasePresenter(context);
    }

    /**
     * Override this method to do your actual background processing.  This method is called on a
     * background thread - you are required to <b>synchronously</b> do your work and return the
     * {@link Result} from this method.  Once you return from this
     * method, the Worker is considered to have finished what its doing and will be destroyed.  If
     * you need to do your work asynchronously on a thread of your own choice, see
     * {@link ListenableWorker}.
     * <p>
     * A Worker is given a maximum of ten minutes to finish its execution and return a
     * {@link Result}.  After this time has expired, the Worker will
     * be signalled to stop.
     *
     * @return The {@link Result} of the computation; note that
     * dependent work will not execute if you use
     * {@link Result#failure()} or
     * {@link Result#failure(Data)}
     */
    @NonNull
    @Override
    public Result doWork() {
        Data.Builder errorDataBuilder = new Data.Builder();
        try {
            String authorizationCode = authenticatorUri.getQueryParameter(BuildConfig.RESPONSE_TYPE);
            if (!TextUtils.isEmpty(authorizationCode)) {
                boolean isSuccess = WebTokenRequest.getToken(getApplicationContext(), authorizationCode);
                presenter.getApplicationPref().setAuthenticated(isSuccess);
                Data outputData = new Data.Builder()
                        .putBoolean(KeyUtil.arg_model, isSuccess)
                        .build();
                return Result.success(outputData);
            } else
                Log.e(toString(), "Authorization authenticatorUri was empty or null, cannot authenticate with the current state");
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            errorDataBuilder.putString(KeyUtil.arg_exception_error, e.getMessage());
        }
        Data workerErrorOutputData = errorDataBuilder
                .putString(KeyUtil.arg_uri_error, authenticatorUri
                        .getQueryParameter(KeyUtil.arg_uri_error))
                .putString(KeyUtil.arg_uri_error_description, authenticatorUri
                        .getQueryParameter(KeyUtil.arg_uri_error_description))
                .build();
        return Result.failure(workerErrorOutputData);
    }
}
