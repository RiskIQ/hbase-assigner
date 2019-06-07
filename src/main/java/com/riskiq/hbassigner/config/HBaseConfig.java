package com.riskiq.hbassigner.config;

import com.riskiq.hbassigner.config.properties.HBaseProperties;
import com.riskiq.hbassigner.hbase.HBaseConnectionManager;
import com.riskiq.hbassigner.hbase.HBaseReportPublisher;
import com.riskiq.hbassigner.hbase.RegionChecker;
import com.riskiq.hbassigner.health.HBaseHealthIndicator;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

/**
 * @author Joe Linn
 * 05/30/2019
 */
@EnableConfigurationProperties(HBaseProperties.class)
@Configuration
public class HBaseConfig {
    @Bean
    public org.apache.hadoop.conf.Configuration hbaseConfiguration(HBaseProperties properties) {
        org.apache.hadoop.conf.Configuration config = HBaseConfiguration.create();
        config.set(HConstants.ZOOKEEPER_QUORUM, properties.getZookeeperHosts());
        config.setInt(HConstants.ZOOKEEPER_CLIENT_PORT, properties.getZookeeperPort());
        return config;
    }


    @Bean
    public HBaseConnectionManager hBaseConnectionManager(org.apache.hadoop.conf.Configuration config) {
        HBaseConnectionManager manager = new HBaseConnectionManager();
        manager.setConfiguration(config);
        return manager;
    }


    @Bean
    public HBaseHealthIndicator hBaseHealthIndicator(HBaseConnectionManager connectionManager) {
        HBaseHealthIndicator indicator = new HBaseHealthIndicator();
        indicator.setConnectionManager(connectionManager);
        return indicator;
    }


    @Bean
    public RegionChecker regionChecker(HBaseConnectionManager connectionManager) {
        RegionChecker checker = new RegionChecker();
        checker.setConnectionManager(connectionManager);
        return checker;
    }


    @Bean
    public Collection<HBaseReportPublisher> reportPublishers(ApplicationContext context) {
        return context.getBeansOfType(HBaseReportPublisher.class).values();
    }
}
