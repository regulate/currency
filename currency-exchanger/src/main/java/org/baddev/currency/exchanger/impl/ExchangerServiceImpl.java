package org.baddev.currency.exchanger.impl;

import org.baddev.currency.core.exception.ServiceException;
import org.baddev.currency.exchanger.ExchangerService;
import org.baddev.currency.jooq.schema.Tables;
import org.baddev.currency.jooq.schema.tables.daos.ExchangeOperationDao;
import org.baddev.currency.jooq.schema.tables.interfaces.IExchangeRate;
import org.baddev.currency.jooq.schema.tables.pojos.ExchangeOperation;
import org.baddev.currency.jooq.schema.tables.pojos.ExchangeRate;
import org.baddev.currency.jooq.schema.tables.records.ExchangeOperationRecord;
import org.joda.time.LocalDateTime;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
 * Created by IPotapchuk on 3/14/2016.
 */
@Service("exchanger")
public class ExchangerServiceImpl implements ExchangerService<ExchangeOperation, ExchangeRate> {

    private static final Logger log = LoggerFactory.getLogger(ExchangerServiceImpl.class);

    private ExchangeOperationDao exchangeDao;

    @Autowired
    public ExchangerServiceImpl(ExchangeOperationDao exchangeDao) {
        this.exchangeDao = exchangeDao;
    }

    @Override
    @Transactional
    public ExchangeOperation exchange(ExchangeOperation operation,
                                      Collection<ExchangeRate> rates) {
        operation.setPerformDatetime(LocalDateTime.now());
        if (operation.getFromCcy().equals("UAH")) {
            double rate = filterRate(rates, operation.getToCcy());
            operation.setToAmount(rate * operation.getFromAmount());
        } else {
            double fPairRate = filterRate(rates, operation.getFromCcy());
            double inDefaultBase = fPairRate * operation.getFromAmount();
            double sPairRate = filterRate(rates, operation.getToCcy());
            operation.setToAmount(inDefaultBase / sPairRate);
        }
        log.info("Exchanged amount: [{}]{}, userId=[{}]", operation.getToAmount(), operation.getToCcy(), operation.getUserId());
        return DSL.using(exchangeDao.configuration())
                .insertInto(Tables.EXCHANGE_OPERATION)
                .set(operation.into(new ExchangeOperationRecord()))
                .returning()
                .fetchOne()
                .into(ExchangeOperation.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ExchangeOperation> findAll() {
        return exchangeDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ExchangeOperation> findForUser(Long key) {
        return exchangeDao.fetchByUserId(key);
    }

    @Override
    @Transactional
    public void deleteById(Long... ids) {
        exchangeDao.deleteById(ids);
    }

    private static <E extends IExchangeRate> Double filterRate(Collection<E> rates, String ccy) {
        return rates.stream()
                .filter(r -> r.getCcy().equals(ccy))
                .mapToDouble(IExchangeRate::getRate)
                .findFirst()
                .orElseThrow(ServiceException::new);
    }

}
