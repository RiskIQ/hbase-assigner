package com.riskiq.hbassigner.hbase.model;

import java.util.Map;

/**
 * @author Joe Linn
 * 06/06/2019
 */
public class RegionCheckerReport {
    /**
     * Total number of regions which were assigned during the course of this check. 0 means all was well. > 0 means we had to manually assign regions.
     */
    private int totalAssignments;
    /**
     * A map of table names to {@link TableAssignmentStatus} records.
     */
    private Map<String, TableAssignmentStatus> tableAssignments;
    /**
     * Duration of the entire check in MILLISECONDS.
     */
    private long elapsed;
    /**
     * An error which occurred during the check, if any. If no error occurred, this will be null.
     */
    private Throwable error;


    public RegionCheckerReport() {}


    public RegionCheckerReport(Map<String, TableAssignmentStatus> map, long elapsed) {
        setTableAssignments(map);
        setTotalAssignments(map.values()
                .stream()
                .mapToInt(TableAssignmentStatus::getAssignedRegions)
                .sum());
        this.elapsed = elapsed;
    }


    public RegionCheckerReport(Throwable error) {
        this.error = error;
    }


    public int getTotalAssignments() {
        return totalAssignments;
    }

    public void setTotalAssignments(int totalAssignments) {
        this.totalAssignments = totalAssignments;
    }

    public Map<String, TableAssignmentStatus> getTableAssignments() {
        return tableAssignments;
    }

    public void setTableAssignments(Map<String, TableAssignmentStatus> tableAssignments) {
        this.tableAssignments = tableAssignments;
    }


    public long getElapsed() {
        return elapsed;
    }

    public void setElapsed(long elapsed) {
        this.elapsed = elapsed;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }
}
