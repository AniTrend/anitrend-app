package com.mxt.anitrend.base.custom.consumer;

import com.mxt.anitrend.util.KeyUtils;

/**
 * Created by max on 2017/12/12.
 * A target specific publisher/subscriber holder
 * @see org.greenrobot.eventbus.EventBus
 */

public class BaseConsumer<T> {

    private @KeyUtils.RequestType
    int requestMode;
    private T changeModel;

    public BaseConsumer(@KeyUtils.RequestType int requestMode, T changeModel) {
        this.requestMode = requestMode;
        this.changeModel = changeModel;
    }

    public BaseConsumer(int requestMode) {
        this.requestMode = requestMode;
    }

    public @KeyUtils.RequestType
    int getRequestMode() {
        return requestMode;
    }

    public T getChangeModel() {
        return changeModel;
    }

    public boolean hasModel() {
        return changeModel != null;
    }

    public interface onRequestModelChange<T> {
        void onModelChanged(BaseConsumer<T> consumer);
    }
}
