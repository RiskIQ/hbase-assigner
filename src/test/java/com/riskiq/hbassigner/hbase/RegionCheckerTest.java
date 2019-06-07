package com.riskiq.hbassigner.hbase;

import com.github.charithe.hbase.HBaseJunitRule;
import com.riskiq.hbassigner.hbase.model.TableAssignmentStatus;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Joe Linn
 * 05/31/2019
 */
public class RegionCheckerTest {
    @ClassRule
    public static HBaseJunitRule hBaseJunitRule = new HBaseJunitRule();

    private HBaseConnectionManager connectionManager;
    private RegionChecker checker;


    @Before
    public void setUp() throws Exception {
        connectionManager = new HBaseConnectionManager();
        connectionManager.setConfiguration(hBaseJunitRule.getHBaseConfiguration());

        checker = new RegionChecker();
        checker.setConnectionManager(connectionManager);
    }


    @Test
    public void testGetTableRegions() throws Exception {
        final String tableName = "foo";
        final byte[] columnFamily = Bytes.toBytes("a");
        byte[][] splits = new byte[26][];
        char splitKey = 'a';
        for (char i = 0; i < splits.length; i++) {
            splits[i] = Bytes.toBytes(splitKey + i);
        }
        connectionManager.doWithAdmin(admin -> {
            admin.createTable(new HTableDescriptor(TableName.valueOf(tableName)).addFamily(new HColumnDescriptor(columnFamily)), splits);
            return null;
        });
        connectionManager.doWithTable(tableName, table -> {
            List<Put> puts = new ArrayList<>(splits.length);
            for (byte[] split : splits) {
                puts.add(new Put(split).addColumn(columnFamily, split, split));
            }
            table.put(puts);
            return null;
        });

        TableAssignmentStatus status = checker.checkTable(tableName);
        assertThat(status.getTotalRegions(), equalTo(splits.length + 1));
        assertThat(status.getAssignedRegions(), equalTo(0));
    }
}