package com.riskiq.hbassigner.hbase;

import com.riskiq.hbassigner.hbase.model.RegionCheckerReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

/**
 * A {@link Runnable} task which will perform a region check on all enabled HBase tables. This is intended to be run as
 * a scheduled task.
 * @author Joe Linn
 * 06/06/2019
 */
public class RegionCheckerTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(RegionCheckerTask.class);


    private RegionChecker regionChecker;
    private Collection<HBaseReportPublisher> publishers;


    public RegionCheckerTask(RegionChecker regionChecker, Collection<HBaseReportPublisher> publishers) {
        this.regionChecker = regionChecker;
        this.publishers = publishers;
    }


    @Override
    public void run() {
        RegionCheckerReport report;
        try {
            report = regionChecker.checkAllTables();
        } catch (IOException e) {
            log.error("Error performing HBase region check.", e);
            report = new RegionCheckerReport(e);
        }

        for (HBaseReportPublisher publisher : publishers) {
            if (publisher.isEnabled()) {
                publisher.publishReport(report);
            }
        }
    }
}
