package me.flash.distributedbatch.producer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.amqp.dsl.AmqpInboundChannelAdapterSMLCSpec;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ShutdownListener implements JobExecutionListener {

    @Autowired
    private List<DisposableBean> disposableBeans;

    @Autowired
    private AmqpInboundChannelAdapterSMLCSpec channelAdapterSMLCSpec;

    @Override
    public void beforeJob(JobExecution jobExecution) {
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> {
            channelAdapterSMLCSpec.stop();
            for (DisposableBean disposableBean : disposableBeans) {
                try {
                    disposableBean.destroy();
                } catch (Exception e) {
                    log.warn("Can't destroy {}", disposableBean);
                }
            }
            scheduler.shutdown();
        }, 10, TimeUnit.SECONDS);
    }
}
