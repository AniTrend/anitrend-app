package com.mxt.anitrend.base.custom.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.databinding.WidgetFavouriteBinding;
import com.mxt.anitrend.model.entity.base.UserBase;
import io.github.wax911.library.model.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.graphql.AniGraphErrorUtilKt;
import com.mxt.anitrend.util.graphql.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.NotifyUtil;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by max on 2017/10/29.
 * Like or favourite view which manages state independently
 */
public class FavouriteWidget extends FrameLayout implements CustomView, RetroCallback<List<UserBase>>, View.OnClickListener {

    private WidgetPresenter<List<UserBase>> presenter;
    private WidgetFavouriteBinding binding;
    private @Nullable List<UserBase> model;
    private final String TAG = FavouriteWidget.class.getSimpleName();

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
        binding = WidgetFavouriteBinding.inflate(CompatUtil.INSTANCE.getLayoutInflater(getContext()), this, true);
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

    public void setRequestParams(@KeyUtil.LikeType String likeType, long modelId) {
        QueryContainerBuilder queryContainer = GraphUtil.INSTANCE.getDefaultQuery(false)
                .putVariable(KeyUtil.arg_id, modelId)
                .putVariable(KeyUtil.arg_type, likeType);
        presenter.getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.widget_flipper:
                if (binding.widgetFlipper.getDisplayedChild() == WidgetPresenter.CONTENT_STATE) {
                    binding.widgetFlipper.showNext();
                    presenter.requestData(KeyUtil.MUT_TOGGLE_LIKE, getContext(), this);
                }
                else
                    NotifyUtil.INSTANCE.makeText(getContext(), R.string.busy_please_wait, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void setIconType() {
        if(!CompatUtil.INSTANCE.isEmpty(model) && model.contains(presenter.getDatabase().getCurrentUser()))
            binding.widgetLike.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.INSTANCE.getDrawable(getContext(),
                    R.drawable.ic_favorite_grey_600_18dp, R.color.colorStateRed), null, null, null);
        else
            binding.widgetLike.setCompoundDrawablesWithIntrinsicBounds(CompatUtil.INSTANCE.getDrawable(getContext(),
                    R.drawable.ic_favorite_grey_600_18dp), null, null, null);
        binding.widgetLike.setText(WidgetPresenter.convertToText(CompatUtil.INSTANCE.sizeOf(model)));
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
    public void onResponse(@NonNull Call<List<UserBase>> call, @NonNull Response<List<UserBase>> response) {
        try {
            if(response.isSuccessful()) {
                if(!CompatUtil.INSTANCE.isEmpty(model) && model.contains(presenter.getDatabase().getCurrentUser()))
                    model.remove(presenter.getDatabase().getCurrentUser());
                else
                    model.add(presenter.getDatabase().getCurrentUser());
                setIconType();
            } else {
                Timber.tag(TAG).w(AniGraphErrorUtilKt.apiError(response));
                resetFlipperState();
            }
        } catch (Exception e) {
            Timber.tag(TAG).w(e);
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
    public void onFailure(@NonNull Call<List<UserBase>> call, @NonNull Throwable throwable) {
        try {
            Timber.tag(TAG).e(throwable);
            resetFlipperState();
        } catch (Exception e) {
            Timber.tag(TAG).e(throwable);
            e.printStackTrace();
        }
    }
}
