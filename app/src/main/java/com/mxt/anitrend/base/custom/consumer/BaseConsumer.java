package com.mxt.anitrend.base.custom.consumer;

import com.mxt.anitrend.util.KeyUtil;

/**
 * Created by max on 2017/12/12.
 * A target specific publisher/subscriber holder, Since the model is of type T,
 * the only way to assure we don't get class cast exceptions is by using requestMode, see example
 * @see com.mxt.anitrend.view.fragment.list.MediaListFragment#onModelChanged(BaseConsumer)
 * @see org.greenrobot.eventbus.EventBus
 */

public class BaseConsumer<T> {

    private @KeyUtil.RequestType
    int requestMode;
    private T changeModel;

    public BaseConsumer(@KeyUtil.RequestType int requestMode, T changeModel) {
        this.requestMode = requestMode;
        this.changeModel = changeModel;
    }

    public BaseConsumer(int requestMode) {
        this.requestMode = requestMode;
    }

    public @KeyUtil.RequestType
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
