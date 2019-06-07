package com.riskiq.hbassigner.hbase;

import com.riskiq.hbassigner.hbase.model.RegionCheckerReport;
import com.riskiq.hbassigner.hbase.model.TableAssignmentStatus;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Checks HBase regions to ensure that they are assigned to a region server, and forces HBase to assign unassigned regions.
 * @author Joe Linn
 * 05/30/2019
 */
public class RegionChecker {
    private static final Logger log = LoggerFactory.getLogger(RegionChecker.class);


    private HBaseConnectionManager connectionManager;


    public void setConnectionManager(HBaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }


    /**
     * Performs a region assignment check on all enabled HBase tables.  Any unassigned regions will be assigned.
     * @return a report containing the results of this check
     * @throws IOException if an error is encountered when communicating with HBase
     */
    public RegionCheckerReport checkAllTables() throws IOException {
        final long startTime = System.currentTimeMillis();
        List<String> tableNames = connectionManager.doWithAdmin(admin -> Arrays.stream(admin.listTableNames())
                .filter(tableName -> {
                    try {
                        return admin.isTableEnabled(tableName);
                    } catch (IOException e) {
                        log.warn("Unable to determine if table " + tableName + " is enabled.", e);
                        return false;
                    }
                }))
                .map(TableName::getNameAsString)
                .collect(Collectors.toList());
        Map<String, TableAssignmentStatus> statusMap = new LinkedHashMap<>(tableNames.size());
        for (String tableName : tableNames) {
            statusMap.put(tableName, checkTable(tableName));
        }
        final long elapsed = System.currentTimeMillis() - startTime;
        return new RegionCheckerReport(statusMap, elapsed);
    }


    /**
     * Performs a region assignment check on a single HBase table.
     * @param tableName the name of the table to be checked, including the table's namespace if applicable ([namespace]:[table])
     * @return the result of the check
     * @throws IOException if an error is encountered when communicating with HBase
     */
    public TableAssignmentStatus checkTable(String tableName) throws IOException {
        Map<String, String> tableRegions = getTableRegions(tableName);
        log.info("Checking {} regions for table {}", tableRegions.size(), tableName);
        return connectionManager.doWithTable(tableName, table -> {
            int assignments = 0;
            for (Map.Entry<String, String> entry : tableRegions.entrySet()) {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Checking region {} of table {}", entry.getValue(), tableName);
                    }
                    table.getScanner(new Scan(Bytes.toBytes(entry.getKey()))
                            .setMaxResultSize(1)
                            .setFilter(new PageFilter(1)));
                } catch (IOException e) {
                    log.warn("Could not scan region " + entry.getValue() + " of table " + tableName + ". Attempting to assign.", e);
                    if (assignRegion(entry.getValue())) {
                        assignments++;
                    }
                }
            }
            return new TableAssignmentStatus(tableRegions.size(), assignments);
        });
    }


    /**
     * Builds a map of region start keys to region IDs for the given table.
     * @param tableName name of the desired table
     * @return start keys to region IDs map
     * @throws IOException
     */
    private Map<String, String> getTableRegions(String tableName) throws IOException {
        return connectionManager.doWithTable(TableName.META_TABLE_NAME.getNameAsString(), table -> {
            Map<String, String> regionInfo = new HashMap<>();
            for (Result result : table.getScanner(new Scan().setRowPrefixFilter(Bytes.toBytes(tableName)))) {
                final String rowString = Bytes.toString(result.getRow());
                List<String> strings = splitToList(rowString, ",");
                String startKey = strings.get(1);
                try {
                    String regionID = splitToList(strings.get(strings.size() - 1), "\\.").get(1);
                    regionInfo.put(startKey, regionID);
                } catch (IndexOutOfBoundsException e) {
                    log.error("Unable to extract region ID from string " + strings.get(strings.size() - 1) + " (row: " + rowString + ") for table " + tableName, e);
                }
            }
            return regionInfo;
        });
    }


    /**
     * Attempt to force assignment of the region with the given ID.
     * @param region region ID
     * @return true if successful; false if errors were thrown
     * @throws IOException
     */
    private boolean assignRegion(String region) throws IOException {
        return connectionManager.doWithAdmin(admin -> {
            try {
                admin.assign(Bytes.toBytes(region));
                log.info("Successfully reassigned region {}", region);
                return true;
            } catch (IOException e) {
                log.error("Failed to assign region " + region, e);
                throw e;
            }
        });
    }


    private List<String> splitToList(String str, String delimiter) {
        return Arrays.asList(str.split(delimiter));
    }
}
