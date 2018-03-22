package com.mxt.anitrend.base.custom.view.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.databinding.WidgetFavouriteBinding;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.NotifyUtil;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by max on 2017/10/29.
 * Like or favourite view which manages independently
 */
public class FavouriteWidget extends FrameLayout implements CustomView, RetroCallback<ResponseBody>, View.OnClickListener {

    private WidgetPresenter<ResponseBody> presenter;
    private @KeyUtils.RequestType
    int requestType;
    private WidgetFavouriteBinding binding;
    private List<UserBase> model;

    public FavouriteWidget(Context context) {
        super(context);
        onInit();
    }

    public FavouriteWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public FavouriteWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        presenter = new WidgetPresenter<>(getContext());
        binding = WidgetFavouriteBinding.inflate(CompatUtil.getLayoutInflater(getContext()), this, true);
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
        model = null;
    }

    private void resetFlipperState() {
        if(binding.widgetFlipper.getDisplayedChild() == WidgetPresenter.LOADING_STATE)
            binding.widgetFlipper.setDisplayedChild(WidgetPresenter.CONTENT_STATE);
    }

    public void setModel(List<UserBase> model) {
         this.model = model;
        setIconType();
    }

    public void setRequestParams(@KeyUtils.RequestType int requestType, int modelId) {
        presenter.getParams().putInt(KeyUtils.arg_id, modelId);
        this.requestType = requestType;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.widget_flipper:
                if (binding.widgetFlipper.getDisplayedChild() == WidgetPresenter.CONTENT_STATE) {
                    binding.widgetFlipper.showNext();
                    presenter.requestData(requestType, getContext(), this);
                }
                else
                    NotifyUtil.makeText(getContext(), R.string.busy_please_wait, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void setIconType() {
        if(model.contains(presenter.getDatabase().getCurrentUser()))
            binding.widgetLike.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.getDrawable(getContext(),
                    R.drawable.ic_favorite_grey_600_18dp, R.color.colorStateRed), null, null, null);
        else
            binding.widgetLike.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.getDrawable(getContext(),
                    R.drawable.ic_favorite_grey_600_18dp), null, null, null);
        binding.widgetLike.setText(WidgetPresenter.convertToText(model.size()));
        resetFlipperState();
    }

    /**
     * Invoked for a received HTTP response.
     * <p>
     * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
     * Call {@link Response#isSuccessful()} to determine if the response indicates success.
     *
     * @param call     the origination requesting object
     * @param response the response from the network
     */
    @Override
    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
        try {
            if(response.isSuccessful()) {
                if(model.contains(presenter.getDatabase().getCurrentUser()))
                    model.remove(presenter.getDatabase().getCurrentUser());
                else
                    model.add(presenter.getDatabase().getCurrentUser());
                setIconType();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     *
     * @param call      the origination requesting object
     * @param throwable contains information about the error
     */
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
