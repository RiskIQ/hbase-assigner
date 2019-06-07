package com.riskiq.hbassigner.hbase;

import com.riskiq.hbassigner.hbase.model.RegionCheckerReport;

/**
 * Publishes the results of HBase region checking / assignment.
 * @author Joe Linn
 * 06/06/2019
 */
public interface HBaseReportPublisher {
    /**
     * Determines whether or not this publisher is enabled and should be used.  This will be checked each time result
     * publication is attempted.
     * @return true if this publisher is enabled; false otherwise
     */
    boolean isEnabled();


    /**
     * Publishes the given {@link RegionCheckerReport}.
     * @param report the results of a HBase region check run
     */
    void publishReport(RegionCheckerReport report);
}
