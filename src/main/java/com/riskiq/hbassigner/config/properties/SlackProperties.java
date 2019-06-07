package com.riskiq.hbassigner.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Joe Linn
 * 06/06/2019
 */
@ConfigurationProperties(prefix = "slack")
public class SlackProperties {
    /**
     * Slack webhook URL. If null or empty, Slack publication will be disabled.
     */
    private String url;
    /**
     * Target Slack channel for messages. If null or empty, Slack publication will be disabled.
     */
    private String channel;
    /**
     * If true, a report will be sent every time the region check is run on a scheduled basis.  If false, a report
     * will only be sent if an error occurred during the check of if one or more regions was assigned by the checker.
     */
    private boolean sendOnGood = true;


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }


    public boolean isSendOnGood() {
        return sendOnGood;
    }

    public void setSendOnGood(boolean sendOnGood) {
        this.sendOnGood = sendOnGood;
    }
}
