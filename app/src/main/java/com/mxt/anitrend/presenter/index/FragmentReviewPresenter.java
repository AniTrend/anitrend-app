package com.mxt.anitrend.presenter.index;

import android.content.Context;

import com.mxt.anitrend.api.structure.ReviewType;
import com.mxt.anitrend.async.AsyncTaskFetch;
import com.mxt.anitrend.presenter.CommonPresenter;

import retrofit2.Callback;

/**
 * Created by max on 2017/05/02.
 */

public class FragmentReviewPresenter extends CommonPresenter<ReviewType> {

    private Context context;

    public FragmentReviewPresenter(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void beginAsync(Callback<ReviewType> callback, int page, String type) {
        final AsyncTaskFetch<ReviewType> requestTask = new AsyncTaskFetch<>(callback, context, type, page);
        requestTask.execute(AsyncTaskFetch.RequestType.SERIES_REVIEWS_REQ);
    }
}
