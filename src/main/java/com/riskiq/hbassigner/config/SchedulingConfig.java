package com.riskiq.hbassigner.config;

import com.riskiq.hbassigner.config.properties.HBaseProperties;
import com.riskiq.hbassigner.hbase.HBaseReportPublisher;
import com.riskiq.hbassigner.hbase.RegionChecker;
import com.riskiq.hbassigner.hbase.RegionCheckerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.Collection;

/**
 * @author Joe Linn
 * 06/06/2019
 */
@EnableScheduling
@Configuration
public class SchedulingConfig implements SchedulingConfigurer {
    private static final Logger log = LoggerFactory.getLogger(SchedulingConfig.class);

    @Autowired
    private HBaseProperties properties;
    @Autowired
    private RegionChecker regionChecker;
    @Autowired
    private Collection<HBaseReportPublisher> publishers;


    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        log.info("Scheduling HBase region checker using cron expression: " + properties.getCron());
        taskRegistrar.addCronTask(new CronTask(new RegionCheckerTask(regionChecker, publishers), properties.getCron()));
    }
}
