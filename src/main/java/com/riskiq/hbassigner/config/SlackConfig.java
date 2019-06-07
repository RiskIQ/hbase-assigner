package com.riskiq.hbassigner.config;

import com.riskiq.hbassigner.config.properties.SlackProperties;
import com.riskiq.hbassigner.hbase.SlackReportPublisher;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Joe Linn
 * 06/06/2019
 */
@EnableConfigurationProperties(SlackProperties.class)
@Configuration
public class SlackConfig {
    @Bean
    public SlackReportPublisher slackReportPublisher(SlackProperties properties) {
        SlackReportPublisher publisher = new SlackReportPublisher();
        publisher.setSlackProperties(properties);
        return publisher;
    }
}
