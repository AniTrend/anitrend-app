package com.mxt.anitrend.base.custom.consumer;

import com.mxt.anitrend.util.KeyUtils;

/**
 * Created by max on 2017/12/12.
 * A target specific publisher/subscriber holder
 * @see org.greenrobot.eventbus.EventBus
 */

public class BaseConsumer<T> {

    private @KeyUtils.RequestMode int requestMode;
    private T changeModel;

    public BaseConsumer(@KeyUtils.RequestMode int requestMode, T changeModel) {
        this.requestMode = requestMode;
        this.changeModel = changeModel;
    }

    public BaseConsumer(int requestMode) {
        this.requestMode = requestMode;
    }

    public @KeyUtils.RequestMode int getRequestMode() {
        return requestMode;
    }

    public T getChangeModel() {
        return changeModel;
    }

    public interface onRequestModelChange<T> {
        void onModelChanged(BaseConsumer<T> consumer);
    }
}
