package org.baddev.currency.notifier.event;

import org.baddev.currency.core.Identity;

/**
 * Created by IPotapchuk on 6/17/2016.
 */
public class BaseNotificationEvent<T extends Identity> implements NotificationEvent<T>{

    private Object source;
    private T eventData;

    public BaseNotificationEvent(Object source, T eventData) {
        this.source = source;
        this.eventData = eventData;
    }

    @Override
    public T getEventData() {
        return eventData;
    }

    @Override
    public Object getSource() {
        return source;
    }

}
