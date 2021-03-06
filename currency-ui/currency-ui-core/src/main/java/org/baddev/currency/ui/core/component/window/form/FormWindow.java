package org.baddev.currency.ui.core.component.window.form;

import com.vaadin.data.Validator;
import com.vaadin.server.Resource;
import com.vaadin.ui.Field;
import org.baddev.currency.ui.core.component.view.AbstractFormView;
import org.baddev.currency.ui.core.component.view.AbstractView;
import org.baddev.currency.ui.core.component.window.Showable;
import org.baddev.currency.ui.core.model.fieldgroup.FieldConfig;
import org.baddev.currency.ui.core.util.FieldGroupUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by IPotapchuk on 10/3/2016.
 */
public final class FormWindow {

    public enum Mode {
        NEW, EDIT, READONLY
    }

    private FormWindow() {
    }

    public static <T> void show(Config<T> config) {
        Showable formWindow = configure(config);
        try {
            formWindow.show(config.caption);
        } catch (Exception e) {
            throw new RuntimeException("Failed to show FormWindow", e);
        }
    }

    public static void show(AbstractFormView formView) {
        new SimpleFormWindow(formView).show(formView.getNameCaption());
    }

    public static void showTabs(String caption, AbstractView... views) {
        new TabsheetFormWindow(views).show(caption);
    }

    private static <T> Showable configure(Config<T> config) {
        AbstractFormWindow formWindow = null;

        if (config.beanClass != null) {
            formWindow = new BindableFormWindow<>(config.mode,
                    FieldGroupUtils.create(config.beanClass, config.formBean, config.fieldConfigs));
        } else if (!config.lhs.isEmpty() || !config.rhs.isEmpty()) {
            formWindow = new TwincolSelectWindow<>(config.lhs, config.rhs, config.itemCaptionProducer);
        }
        Objects.requireNonNull(formWindow);
        if (config.width != null) formWindow.withWidth(config.width);
        if (config.height != null) formWindow.withHeight(config.height);
        formWindow.withUIErrorHandling(config.uiErrorHandlingMode);
        formWindow.withErrorActionProvider(config.onCommitError);
        formWindow.withSuccessActionProvider(config.onCommitSuccess);
        if (config.successCaption != null) formWindow.withSuccessCaption(config.successCaption);
        if (config.successMsg != null) formWindow.withSuccessMsg(config.successMsg);
        formWindow.setResizable(config.resizable);
        formWindow.setModal(config.modal);
        return formWindow;
    }

    public static class Config<T> {

        private Mode mode;
        private String caption = "";
        private Consumer onCommitSuccess;
        private Consumer onCommitError;
        private Float width;
        private Float height;
        private Class<T> beanClass;
        private T formBean;
        private Set<FieldConfig> fieldConfigs = new LinkedHashSet<>();
        private boolean resizable;
        private boolean modal = true;
        private Collection<? extends T> rhs = Collections.emptyList();
        private Collection<? extends T> lhs = Collections.emptyList();
        private Function<T, String> itemCaptionProducer = Object::toString;
        private boolean uiErrorHandlingMode = true;
        private String successMsg;
        private String successCaption;

        public Config(Mode windowMode) {
            this.mode = windowMode;
        }

        public Config<T> setCaption(String caption) {
            this.caption = caption;
            return this;
        }

        public Config<T> setOnCommitSuccess(Consumer<Set<T>> onCommitSuccess) {
            this.onCommitSuccess = onCommitSuccess;
            return this;
        }

        public <E extends Exception> Config setOnCommitError(Consumer<E> onCommitError) {
            this.onCommitError = onCommitError;
            this.uiErrorHandlingMode = false;
            return this;
        }

        public Config<T> setWidth(Float width) {
            this.width = width;
            return this;
        }

        public Config<T> setHeight(Float height) {
            this.height = height;
            return this;
        }

        public Config<T> setBeanClass(Class<T> beanClass) {
            this.beanClass = beanClass;
            return this;
        }

        public Config<T> setFormBean(T formBean) {
            this.formBean = formBean;
            return this;
        }

        public Config<T> setFieldConfigs(Collection<FieldConfig> fieldConfigs) {
            this.fieldConfigs = new LinkedHashSet<>(fieldConfigs);
            return this;
        }

        public Config<T> addFieldConfig(String propId, String caption, Resource icon, Class<? extends Field> type){
            this.fieldConfigs.add(FieldGroupUtils.fConf(propId, caption, icon, type));
            return this;
        }

        public Config<T> addFieldConfig(String propId, String caption, Resource icon){
            this.fieldConfigs.add(FieldGroupUtils.fConf(propId, caption, icon));
            return this;
        }

        public Config<T> addFieldConfig(String propId, String caption){
            this.fieldConfigs.add(FieldGroupUtils.fConf(propId, caption));
            return this;
        }

        public Config<T> addFieldConfig(String propId, String caption, Class<? extends Field> type){
            this.fieldConfigs.add(FieldGroupUtils.fConf(propId, caption, type));
            return this;
        }

        public Config<T> addFieldConfig(String propId, String caption, Resource icon, Class<? extends Field> type, Validator validator){
            this.fieldConfigs.add(FieldGroupUtils.fConf(propId, caption, icon, type, validator));
            return this;
        }

        public Config<T> addFieldConfig(String propId, String caption, Resource icon, Validator validator){
            this.fieldConfigs.add(FieldGroupUtils.fConf(propId, caption, icon, validator));
            return this;
        }

        public Config<T> addFieldConfig(String propId, String caption, Validator validator){
            this.fieldConfigs.add(FieldGroupUtils.fConf(propId, caption, validator));
            return this;
        }

        public Config<T> addFieldConfig(String propId, String caption, Class<? extends Field> type, Validator validator){
            this.fieldConfigs.add(FieldGroupUtils.fConf(propId, caption, type, validator));
            return this;
        }

        public Config<T> setResizable(boolean resizable) {
            this.resizable = resizable;
            return this;
        }

        public Config<T> setModal(boolean modal) {
            this.modal = modal;
            return this;
        }

        public Config<T> setRhs(Collection<? extends T> rhs) {
            this.rhs = rhs;
            return this;
        }

        public Config<T> setLhs(Collection<? extends T> lhs) {
            this.lhs = lhs;
            return this;
        }

        public Config<T> setItemCaptionProducer(Function<T, String> captionProducer) {
            this.itemCaptionProducer = captionProducer;
            return this;
        }

        public Config<T> setSuccessMsg(String successMsg) {
            this.successMsg = successMsg;
            return this;
        }

        public Config<T> setSuccessCaption(String successCaption) {
            this.successCaption = successCaption;
            return this;
        }

        public Config<T> setSuccessNotification(String caption, String msg) {
            this.successCaption = caption;
            this.successMsg = msg;
            return this;
        }
    }

}
