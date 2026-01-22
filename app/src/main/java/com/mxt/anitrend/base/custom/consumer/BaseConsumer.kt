package com.mxt.anitrend.base.custom.consumer

import com.mxt.anitrend.util.KeyUtil

/**
 * Created by max on 2017/12/12.
 * A target specific publisher/subscriber holder, Since the model is of type T,
 * the only way to assure we don't get class cast exceptions is by using requestMode, see example
 * @see com.mxt.anitrend.view.fragment.list.MediaListFragment#onModelChanged(BaseConsumer)
 * @see org.greenrobot.eventbus.EventBus
 */
class BaseConsumer<T> @JvmOverloads constructor(
    @field:KeyUtil.RequestType val requestMode: Int,
    val changeModel: T? = null
) {

    fun hasModel(): Boolean = changeModel != null

    interface onRequestModelChange<T> {
        fun onModelChanged(consumer: BaseConsumer<T>)
    }
}
