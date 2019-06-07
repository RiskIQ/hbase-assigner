package com.riskiq.hbassigner.hbase.model;

/**
 * @author Joe Linn
 * 05/30/2019
 */
public class TableAssignmentStatus {
    /**
     * The total number of regions which were found for a given table.
     */
    private int totalRegions;
    /**
     * Number of regions which had to be reassigned for this table.
     */
    private int assignedRegions;


    public TableAssignmentStatus() { }


    public TableAssignmentStatus(int totalRegions, int assignedRegions) {
        this.totalRegions = totalRegions;
        this.assignedRegions = assignedRegions;
    }


    public int getTotalRegions() {
        return totalRegions;
    }

    public void setTotalRegions(int totalRegions) {
        this.totalRegions = totalRegions;
    }

    public int getAssignedRegions() {
        return assignedRegions;
    }

    public void setAssignedRegions(int assignedRegions) {
        this.assignedRegions = assignedRegions;
    }
}
