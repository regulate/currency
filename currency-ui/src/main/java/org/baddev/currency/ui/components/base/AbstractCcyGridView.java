package org.baddev.currency.ui.components.base;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.renderers.HtmlRenderer;
import org.baddev.currency.ui.StringValueRenderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IPotapchuk on 4/5/2016.
 */
public abstract class AbstractCcyGridView<T> extends AbstractCcyView {

    protected Grid grid = new Grid();

    protected void setup(Class<T> type, Collection<T> items, String... excludeProps) {
        setup(type, excludeProps);
        refresh(items, null, null);
    }

    private void setup(Class<T> type, String... excludeProps) {
        grid.setSizeFull();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        BeanItemContainer<T> container = new BeanItemContainer<>(type);
        GeneratedPropertyContainer wrapperContainer = new GeneratedPropertyContainer(container);
        grid.setContainerDataSource(wrapperContainer);
        grid.setImmediate(true);
        if (excludeProps.length != 0)
            for (String prop : excludeProps)
                wrapperContainer.removeContainerProperty(prop);
    }

    private HorizontalLayout topBar() {
        HorizontalLayout topBar = new HorizontalLayout();
        topBar.setSizeUndefined();
        topBar.setWidth(100.0f, Unit.PERCENTAGE);
        topBar.setSpacing(true);
        customizeTopBar(topBar);
        return topBar;
    }

    private VerticalLayout gridWithBar() {
        VerticalLayout gridWithBar = new VerticalLayout();
        gridWithBar.addComponent(topBar());
        gridWithBar.addComponent(grid);
        gridWithBar.setMargin(true);
        gridWithBar.setSpacing(true);
        gridWithBar.setSizeFull();
        gridWithBar.setExpandRatio(grid, 1.0f);
        return gridWithBar;
    }

    @Override
    protected VerticalLayout contentRoot() {
        VerticalLayout content = super.contentRoot();
        VerticalLayout gridWithBar = gridWithBar();
        content.addComponent(gridWithBar);
        content.setExpandRatio(gridWithBar, 1.0f);
        return content;
    }

    protected Object getSelectedRow() {
        return ((com.vaadin.ui.Grid.SingleSelectionModel) grid.getSelectionModel()).getSelectedRow();
    }

    @SuppressWarnings("unchecked")
    protected BeanItemContainer<T> container() {
        return ((BeanItemContainer<T>) ((GeneratedPropertyContainer) grid.getContainerDataSource())
                .getWrappedContainer());
    }

    protected GeneratedPropertyContainer containerWrapper() {
        return (GeneratedPropertyContainer) grid.getContainerDataSource();
    }

    protected abstract void customizeTopBar(HorizontalLayout topBar);

    /**
     * Refreshes and sorts an underlying datasource container
     *
     * @param data       data to be stored
     * @param sortPropId if null then there is no sort applied
     * @param direction  if null and {@code sortPropId} not null, then asc
     */
    protected void refresh(Collection<T> data, String sortPropId, SortDirection direction) {
        container().removeAllItems();
        container().addAll(data);
        grid.clearSortOrder();
        if (sortPropId != null) {
            if (direction != null)
                grid.sort(sortPropId, direction);
            else
                grid.sort(sortPropId, SortDirection.ASCENDING);
        }
    }

    protected void filter(String text) {
        container().removeAllContainerFilters();
        if (!text.isEmpty()) {
            List<Container.Filter> filters = new ArrayList<>();
            container().getContainerPropertyIds()
                    .forEach(p -> filters.add(new SimpleStringFilter(p, text, true, false)));
            container().addContainerFilter(new Or(filters.stream().toArray(SimpleStringFilter[]::new)));
        }
    }

    /**
     * Adds button with static string value to grid
     *
     * @param propertyId
     * @param value
     * @param listener
     */
    protected void addGeneratedButton(String propertyId, String value, ClickableRenderer.RendererClickListener listener) {
        addGeneratedButton(propertyId, r -> value, listener);
    }

    /**
     * Adds button with dynamic string value to grid
     *
     * @param propertyId
     * @param r          decides which value to render
     * @param listener
     */
    protected void addGeneratedButton(String propertyId, StringValueRenderer r, ClickableRenderer.RendererClickListener listener) {
        containerWrapper().addGeneratedProperty(propertyId, new PropertyValueGenerator<String>() {
            @Override
            public String getValue(Item item, Object itemId, Object propertyId) {
                return r.render(itemId);
            }

            @Override
            public Class<String> getType() {
                return String.class;
            }
        });
        setupGenButton(propertyId, listener);
    }

    private void setupGenButton(String propId, ClickableRenderer.RendererClickListener listener) {
        grid.getColumn(propId).setRenderer(new ButtonRenderer(listener));
    }

    protected void addGeneratedStringProperty(String propertyId, boolean html, StringValueRenderer r) {
        containerWrapper().addGeneratedProperty(propertyId, new PropertyValueGenerator<String>() {
            @Override
            public String getValue(Item item, Object itemId, Object propertyId) {
                return r.render(itemId);
            }

            @Override
            public Class<String> getType() {
                return String.class;
            }
        });
        if (html) grid.getColumn(propertyId).setRenderer(new HtmlRenderer());
    }

}
