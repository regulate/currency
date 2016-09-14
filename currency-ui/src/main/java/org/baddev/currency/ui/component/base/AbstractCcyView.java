package org.baddev.currency.ui.component.base;

import com.google.common.eventbus.EventBus;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.VerticalLayout;
import org.baddev.currency.security.SecurityUtils;
import org.baddev.currency.ui.component.window.SettingsWindow;
import org.baddev.currency.ui.security.event.LogoutEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;

import static org.baddev.currency.ui.CurrencyUI.currencyUI;

/**
 * Created by IPotapchuk on 5/16/2016.
 */
public abstract class AbstractCcyView extends VerticalLayout implements View {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private SettingsWindow settingsWindow;
    @Autowired
    protected EventBus bus;

    @PostConstruct
    public void init() {
        setSizeFull();
        addComponent(contentRoot());
        log.debug("View created {}, {}", getClass().getName(), hashCode());
    }

    protected VerticalLayout contentRoot() {
        VerticalLayout content = new VerticalLayout();
        content.addComponent(menuBar());
        content.setSizeFull();
        return content;
    }

    protected MenuBar menuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setWidth(100.0f, Unit.PERCENTAGE);
        menuBar.addStyleName("small");
        String loggedIn = SecurityUtils.loggedInUserName();
        if (!StringUtils.isEmpty(loggedIn.trim())) {
            MenuBar.MenuItem parent = menuBar.addItem(loggedIn, FontAwesome.USER, null);
            parent.addItem("Settings", FontAwesome.GEAR, selectedItem -> currencyUI().addWindow(settingsWindow));
            parent.addItem("Logout", FontAwesome.SIGN_OUT, selectedItem -> bus.post(new LogoutEvent(this)));
        }
        customizeMenuBar(menuBar);
        return menuBar;
    }

    protected abstract void customizeMenuBar(MenuBar menuBar);

    protected final void navigateTo(String viewName) {
        currencyUI().getNavigator().navigateTo(viewName);
    }

    public static void attachComponents(AbstractOrderedLayout l, Component... cs) {
        Arrays.stream(cs).forEach(c -> {
            if (l.getComponentIndex(c) == -1)
                l.addComponent(c);
        });
    }

    public static void toggleVisible(boolean visible, Component... components) {
        Arrays.stream(components).forEach(c -> c.setVisible(visible));
    }

    public static void toggleEnabled(boolean enabled, Component... components) {
        Arrays.stream(components).forEach(c -> c.setEnabled(enabled));
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

    }

    @PreDestroy
    public void destroy() {
        log.debug("View destroyed {}", getClass().getName(), hashCode());
    }

}
