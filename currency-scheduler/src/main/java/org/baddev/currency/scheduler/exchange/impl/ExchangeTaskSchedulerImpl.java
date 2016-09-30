package org.baddev.currency.scheduler.exchange.impl;

import org.baddev.currency.core.event.ExchangeCompletionEvent;
import org.baddev.currency.core.notifier.Notifier;
import org.baddev.currency.exchanger.ExchangerService;
import org.baddev.currency.fetcher.service.ExchangeRateService;
import org.baddev.currency.jooq.schema.tables.interfaces.IExchangeOperation;
import org.baddev.currency.jooq.schema.tables.interfaces.IExchangeTask;
import org.baddev.currency.jooq.schema.tables.pojos.ExchangeOperation;
import org.baddev.currency.jooq.schema.tables.pojos.ExchangeTask;
import org.baddev.currency.scheduler.exchange.ExchangeTaskScheduler;
import org.baddev.currency.scheduler.exchange.service.ExchangeTaskService;
import org.joda.time.LocalDate;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

import static org.baddev.currency.jooq.schema.Tables.EXCHANGE_TASK;

/**
 * Created by IPOTAPCHUK on 6/8/2016.
 */
@Service
public class ExchangeTaskSchedulerImpl implements ExchangeTaskScheduler {

    private static final Logger log = LoggerFactory.getLogger(ExchangeTaskSchedulerImpl.class);

    @Autowired
    private ExchangerService exchangerService;
    @Autowired
    private ExchangeRateService rateService;
    @Autowired
    private DSLContext dsl;
    @Autowired
    private ThreadPoolTaskScheduler pool;
    @Autowired
    private Notifier notifier;
    @Autowired
    private ExchangeTaskService taskService;

    private Set<IExchangeTask> exchangeTasks = new HashSet<>();
    private Map<Long, ScheduledFuture> exchangeTasksJobsMap = new HashMap<>();

    private class ExchangeJob implements Runnable {

        private IExchangeTask taskData;
        private boolean success = true;

        ExchangeJob(final IExchangeTask taskData) {
            this.taskData = taskData;
        }

        @Override
        public void run() {
            IExchangeOperation exchOp = new ExchangeOperation();
            exchOp.setUserId(taskData.getUserId());
            exchOp.setFromCcy(taskData.getFromCcy());
            exchOp.setToCcy(taskData.getToCcy());
            exchOp.setFromAmount(taskData.getAmount());
            exchOp.setRatesDate(LocalDate.now());
            try {
                exchOp = exchangerService.exchange(exchOp, rateService.fetchCurrent());
            } catch (Exception e) {
                success = false;
                log.error("Error while performing exchange", e);
            } finally {
                if (!notifier.getSubscribers().isEmpty())
                    notifier.doNotify(new ExchangeCompletionEvent(this, exchOp, success));
            }
        }

    }

    @PostConstruct
    public void init() {
        Collection<ExchangeTask> tasks = dsl.selectFrom(EXCHANGE_TASK).fetchInto(ExchangeTask.class);
        tasks.forEach(t -> {
            ScheduledFuture task = scheduleTask(t);
            if (!t.getActive())
                task.cancel(false);
        });
        log.info("{} task(s) loaded and scheduled", tasks.size());
    }

    @Override
    public Long schedule(final IExchangeTask taskData) {
        IExchangeTask task = taskData.into(new ExchangeTask());
        task.setActive(true);
        if (exchangeTasks.contains(taskData)) {
            ScheduledFuture aged = exchangeTasksJobsMap.get(taskData.getId());
            if (aged != null) {
                if (!aged.isCancelled()) {
                    throw new IllegalArgumentException("Task " + taskData.getId() + " already exists");
                } else {
                    exchangeTasksJobsMap.remove(taskData.getId());
                }
            }
            taskService.update(task);
        } else {
            task = taskService.saveReturning(task);
        }
        scheduleTask(task);
        return task.getId();
    }

    private ScheduledFuture scheduleTask(final IExchangeTask op) {
        ScheduledFuture scheduled = pool.schedule(new ExchangeJob(op), new CronTrigger(op.getCron()));
        exchangeTasks.add(op);
        exchangeTasksJobsMap.put(op.getId(), scheduled);
        return scheduled;
    }

    @Override
    public void execute(IExchangeTask taskData) {
        pool.execute(new ExchangeJob(taskData));
    }

    @Override
    public void cancel(Long id, boolean remove) {
        IExchangeTask taskData = exchangeTasks.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .get();
        if (taskData == null) return;
        taskData.setActive(false);
        ScheduledFuture task = exchangeTasksJobsMap.remove(id);
        if (task != null) task.cancel(false);
        if (remove) {
            exchangeTasks.remove(taskData);
            taskService.delete(taskData.getId());
        } else taskService.update(taskData.into(new ExchangeTask()));
    }

    @Override
    public void cancelAll(boolean remove) {
        exchangeTasksJobsMap.values().forEach(t -> t.cancel(false));
        exchangeTasksJobsMap.clear();
        if (remove) {
            exchangeTasks.clear();
            dsl.deleteFrom(EXCHANGE_TASK).execute();
        } else dsl.update(EXCHANGE_TASK).set(EXCHANGE_TASK.ACTIVE, false).execute();
    }

    @Override
    public int getActiveCount() {
        return dsl.fetchCount(dsl.selectFrom(EXCHANGE_TASK).where(EXCHANGE_TASK.ACTIVE.eq(true)));
    }

}