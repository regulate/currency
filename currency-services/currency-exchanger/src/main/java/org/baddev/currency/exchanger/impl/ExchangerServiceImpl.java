package org.baddev.currency.exchanger.impl;

import org.baddev.currency.core.action.ExchangeAction;
import org.baddev.currency.core.api.ExchangerService;
import org.baddev.currency.core.security.RoleEnum;
import org.baddev.currency.jooq.schema.Tables;
import org.baddev.currency.jooq.schema.tables.daos.ExchangeOperationDao;
import org.baddev.currency.jooq.schema.tables.interfaces.IExchangeOperation;
import org.baddev.currency.jooq.schema.tables.interfaces.IExchangeRate;
import org.baddev.currency.jooq.schema.tables.pojos.ExchangeOperation;
import org.baddev.currency.jooq.schema.tables.records.ExchangeOperationRecord;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Optional;

/**
 * Created by IPotapchuk on 3/14/2016.
 */
@Service
public class ExchangerServiceImpl implements ExchangerService {

    private final Logger               log;
    private final ExchangeOperationDao exchangeDao;
    private final ExchangeAction       exchangeAction;

    @Autowired
    public ExchangerServiceImpl(ExchangeOperationDao exchangeDao, ExchangeAction exchangeAction, Logger log) {
        Assert.notNull(exchangeDao, "exchangeDao can't be null");
        Assert.notNull(exchangeAction, "exchangeAction can't be null");
        Assert.notNull(log, "logger can't be null");
        this.exchangeDao = exchangeDao;
        this.exchangeAction = exchangeAction;
        this.log = log;
    }

    @Override
    @Transactional
    public IExchangeOperation exchange(IExchangeOperation operation,
                                                 Collection<? extends IExchangeRate> rates) {
        IExchangeOperation exchanged = exchangeAction.exchange(operation, rates);
        IExchangeOperation saved = DSL.using(exchangeDao.configuration())
                .insertInto(Tables.EXCHANGE_OPERATION)
                .set(exchanged.into(new ExchangeOperationRecord()))
                .returning()
                .fetchOne()
                .into(ExchangeOperation.class);
        log.info("Exchanged amount: [{}]{}, userId=[{}]", saved.getToAmount(), saved.getToCcy(), saved.getUserId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    @Secured({RoleEnum.ADMIN})
    public Collection<? extends IExchangeOperation> findAll() {
        return exchangeDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    @Secured({RoleEnum.USER, RoleEnum.ADMIN})
    public Collection<? extends IExchangeOperation> findForUser(Long key) {
        return exchangeDao.fetchByUserId(key);
    }

    @Override
    @Transactional(readOnly = true)
    @Secured({RoleEnum.ADMIN})
    public Collection<? extends IExchangeOperation> findById(Long... ids) {
        return exchangeDao.fetchById(ids);
    }

    @Override
    @Transactional(readOnly = true)
    @Secured({RoleEnum.ADMIN})
    public Optional<? extends IExchangeOperation> findOneById(Long id) {
        return Optional.ofNullable(exchangeDao.fetchOneById(id));
    }

    @Override
    @Transactional
    @Secured({RoleEnum.USER, RoleEnum.ADMIN})
    public void delete(Long... ids) {
        exchangeDao.deleteById(ids);
    }


}