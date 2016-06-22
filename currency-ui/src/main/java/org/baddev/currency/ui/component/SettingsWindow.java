package org.baddev.currency.ui.component;

import com.vaadin.data.Property;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import org.baddev.currency.mail.ExchangeCompletionMailer;
import org.baddev.currency.notifier.listener.NotificationListener;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

import static org.baddev.currency.ui.MyUI.myUI;

/**
 * Created by IPotapchuk on 6/22/2016.
 */
@SpringComponent
public class SettingsWindow extends Window {

    @Autowired
    private ExchangeCompletionMailer mailListener;

    @PostConstruct
    public void init(){
        setCaption("Settings");
        setWidth(600.0f, Unit.PIXELS);
        setModal(true);
        setResizable(false);

        CheckBox mailNotifCb = new CheckBox("Mail on exchange task completion");
        mailNotifCb.addValueChangeListener(event -> valChange(event, mailListener));
        CheckBox uiNotifCb = new CheckBox("UI notification on exchange task completion");
        uiNotifCb.addValueChangeListener(event -> valChange(event, myUI()));

        final FormLayout notificationsTab = new FormLayout(uiNotifCb, mailNotifCb);
        notificationsTab.setMargin(true);
        notificationsTab.setSizeFull();

        TabSheet tabSheet = new TabSheet();
        tabSheet.addStyleName(ValoTheme.TABSHEET_FRAMED);
        tabSheet.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        tabSheet.addTab(notificationsTab, "Notifications");

        setContent(tabSheet);
        center();
    }

    private void valChange(Property.ValueChangeEvent e, NotificationListener l){
        if (e.getProperty().getValue().equals(Boolean.TRUE)) {
            myUI().registerListener(l);
        } else {
            myUI().unregisterListener(l);
        }
    }

}