package com.riskiq.hbassigner.hbase;

import com.google.common.base.Strings;
import com.riskiq.hbassigner.config.properties.SlackProperties;
import com.riskiq.hbassigner.hbase.model.RegionCheckerReport;
import com.riskiq.hbassigner.hbase.model.TableAssignmentStatus;
import humanize.Humanize;
import in.ashwanthkumar.slack.webhook.Slack;
import in.ashwanthkumar.slack.webhook.SlackAttachment;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Publishes the results of a HBase region checking run to the configured Slack channel via Slack's
 * <a href="https://slack.com/apps/A0F7XDUAZ-incoming-webhooks">incoming</a>
 * <a href="https://api.slack.com/incoming-webhooks">webhooks</a> functionality. See {@link SlackProperties} for
 * configuration options.
 * @author Joe Linn
 * 06/06/2019
 */
public class SlackReportPublisher implements HBaseReportPublisher {
    private static final Logger log = LoggerFactory.getLogger(SlackReportPublisher.class);


    private SlackProperties slackProperties;


    public void setSlackProperties(SlackProperties slackProperties) {
        this.slackProperties = slackProperties;
    }


    @Override
    public boolean isEnabled() {
        return !Strings.isNullOrEmpty(slackProperties.getUrl()) && !Strings.isNullOrEmpty(slackProperties.getChannel());
    }


    @Override
    public void publishReport(RegionCheckerReport report) {
        if (report.getError() != null) {
            // task failed
            sendErrorToSlack(report.getError());
        } else {
            // task succeeded
            sendReportToSlack(report);
        }
    }


    /**
     * Sends the given report to Slack.
     * @param report the results of a HBase region check run
     */
    private void sendReportToSlack(RegionCheckerReport report) {
        boolean greatSuccess = report.getTotalAssignments() == 0;

        if (greatSuccess && !slackProperties.isSendOnGood()) {
            if (log.isDebugEnabled()) {
                log.debug("No region assignments performed. Nothing to report via Slack.");
            }
            return;
        }

        final String successMessage = "HBase region assignment check completed successfully. Took " + Humanize.duration(report.getElapsed() / 1000) + ". Assigned " + report.getTotalAssignments() + " regions.";
        SlackAttachment attachment = new SlackAttachment(successMessage)
                .color(greatSuccess ? "good" : "warning");
        StringBuilder fallbackBuilder = new StringBuilder().append(successMessage);
        for (Map.Entry<String, TableAssignmentStatus> entry : report.getTableAssignments().entrySet()) {
            fallbackBuilder.append("\n")
                    .append(entry.getKey())
                    .append(": ")
                    .append("Reassigned ")
                    .append(entry.getValue().getAssignedRegions())
                    .append(" of ")
                    .append(entry.getValue().getTotalRegions())
                    .append(" regions");
            attachment.addField(new SlackAttachment.Field(entry.getKey(), entry.getValue().getAssignedRegions() + " / " + entry.getValue().getTotalRegions(), true));
        }
        attachment.fallback(fallbackBuilder.toString());

        try {
            new Slack(slackProperties.getUrl())
                    .sendToChannel(slackProperties.getChannel())
                    .displayName("HBase Region Checker")
                    .icon(greatSuccess ? ":thumbsup_all:" : ":suspect:")
                    .push(attachment);
        } catch (IOException e) {
            log.error("Error sending report to Slack.", e);
        }
    }


    /**
     * Sends an error report to Slack.  This will be called in the event of a failure to communicate with HBase during a
     * check / reassignment operation.
     * @param error the error which caused the HBase region check to fail
     */
    private void sendErrorToSlack(Throwable error) {
        String message = "Error performing HBase region check: " + error.getMessage() + "```" + ExceptionUtils.getStackTrace(error) + "```";
        try {
            new Slack(slackProperties.getUrl())
                    .sendToChannel(slackProperties.getChannel())
                    .displayName("HBase Region Checker")
                    .icon(":poop:")
                    .push(new SlackAttachment(message)
                            .color("danger")
                            .fallback("Error performing HBase region check.")
                            .addMarkdownIn("text"));
        } catch (IOException e1) {
            log.error("Error sending failure message to slack.", error);
        }
    }
}
