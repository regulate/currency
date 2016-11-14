package org.baddev.currency.mail;

import org.baddev.common.mail.ApplicationMailer;
import org.baddev.common.utils.Safe;
import org.baddev.currency.core.meta.Prod;
import org.slf4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Date;

/**
 * Created by IPotapchuk on 10/24/2016.
 */
@Component
@Prod
@Primary
public class AsyncMailer implements ApplicationMailer {

    @Autowired
    private Logger log;

    @Autowired
    private MailSender sender;
    @Autowired
    private ThreadPoolTaskExecutor pool;
    @Autowired
    private ObjectProvider<SimpleMailMessage> msgProvider;

    @Override
    public void sendMail(String to, String subject, String txt) {
        Assert.notNull(to, "recipient email address can't be null");
        Assert.notNull(subject, "subject can't be null");
        Assert.notNull(txt, "txt can't be null");
        pool.execute(() -> {
            Safe.tryCall(() -> {
                SimpleMailMessage msg = msgProvider.getIfAvailable();
                msg.setTo(to);
                msg.setSentDate(new Date());
                msg.setSubject(subject);
                msg.setText(txt);
                sender.send(msg);
                log.debug("Mail sent");
            });
        });
    }

}