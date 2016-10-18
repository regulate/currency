package org.baddev.currency.core.event;


import java.io.Serializable;
import java.util.Collection;

/**
 * Created by IPotapchuk on 6/17/2016.
 */
public interface Notifier extends Serializable {
    void doNotify(NotificationEvent event);
    boolean subscribe(NotificationListener listener);
    boolean unsubscribe(NotificationListener listener);
    Collection<NotificationListener> getSubscribers();
}
