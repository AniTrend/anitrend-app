package com.mxt.anitrend.base.interfaces.event;

/**
 * Created by max on 2017/06/18.
 * Planned use will involve firing events other classes via publisher/subscriber pattern
 * @see org.greenrobot.eventbus.EventBus
 */

public interface PublisherListener<T> {

    /**
     * Responds to published events, be sure to add subscribe annotation
     * @see org.greenrobot.eventbus.Subscribe
     *
     * @param param passed event
     */
    void onEventPublished(T param);
}
