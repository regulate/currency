package org.baddev.currency.ui.components;

import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import org.baddev.currency.core.exchange.entity.ExchangeOperation;
import org.baddev.currency.ui.MyUI;

import static org.baddev.currency.core.exchange.entity.ExchangeOperation.*;

/**
 * Created by IPotapchuk on 4/8/2016.
 */
@SpringView(name = ExchangesView.NAME)
public class ExchangesView extends AbstractCcyGridView<ExchangeOperation> {

    public static final String NAME = "exchanges";

    @Override
    public void init() {
        setup(ExchangeOperation.class, MyUI.current().exchangeDao().loadAll(), P_ID);
        grid.setColumnOrder(P_DATE, P_AM_CD, P_EXC_AM_CD, P_AM, P_EXC_AM);
        grid.sort(P_DATE, SortDirection.DESCENDING);
    }

    @Override
    protected void customizeTopBar(HorizontalLayout topBar) {

    }

    @Override
    protected void customizeMenuBar(MenuBar menuBar) {
        menuBar.addItem("Rates", (MenuBar.Command) selectedItem -> MyUI.current().getNavigator().navigateTo(RatesView.NAME));
    }
}