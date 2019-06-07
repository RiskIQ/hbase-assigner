package com.riskiq.hbassigner.controller;

import com.google.common.collect.ImmutableMap;
import com.riskiq.hbassigner.hbase.HBaseConnectionManager;
import com.riskiq.hbassigner.hbase.RegionChecker;
import com.riskiq.hbassigner.hbase.model.RegionCheckerReport;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.TableName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Joe Linn
 * 05/31/2019
 */
@RestController
@RequestMapping(path = "/api/hbase")
public class HBaseController {
    @Autowired
    private RegionChecker regionChecker;
    @Autowired
    private HBaseConnectionManager connectionManager;


    @PostMapping(path = "_check-regions")
    public RegionCheckerReport checkRegions() throws IOException {
        return regionChecker.checkAllTables();
    }


    @PostMapping(path = "_check-regions/{table}")
    public Map<String, Object> checkRegions(@PathVariable String table) throws IOException {
        return ImmutableMap.of("results", ImmutableMap.of(table, regionChecker.checkTable(table)));
    }


    @GetMapping(path = "servers")
    public Map<String, Object> getServers() throws IOException {
        return connectionManager.doWithAdmin(admin -> {
            List<ServerName> servers = admin.getClusterStatus()
                    .getServers()
                    .stream()
                    .sorted(Comparator.comparing(ServerName::getHostname))
                    .collect(Collectors.toList());
            return ImmutableMap.of("count", servers.size(), "servers", servers);
        });
    }


    @GetMapping(path = "servers/{server:.+}")
    public ResponseEntity<? extends Map<String, ?>> getServer(@PathVariable String server) throws IOException {
        return connectionManager.doWithAdmin(admin -> admin.getClusterStatus()
                .getServers()
                .stream()
                .filter(name -> name.getHostname().equals(server)))
                .findFirst()
                .map(s -> ResponseEntity.ok(ImmutableMap.of("server", s)))
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping(path = "servers/{server:.+}/regions")
    public Map<String, Object> getServerRegions(@PathVariable String server) throws IOException {
        List<HRegionInfo> regions = connectionManager.doWithAdmin(admin -> admin.getOnlineRegions(ServerName.parseServerName(server)));
        return ImmutableMap.of("count", regions.size(), "regions", regions);
    }


    @GetMapping(path = "master")
    public ServerName getMaster() throws IOException {
        return connectionManager.doWithAdmin(admin -> admin.getClusterStatus().getMaster());
    }


    @GetMapping(path = "tables")
    public Map<String, Object> getTableNames() throws IOException {
        List<String> tableNames = connectionManager.doWithAdmin(admin -> Arrays.stream(admin.listTableNames()))
                .map(TableName::getNameAsString)
                .sorted()
                .collect(Collectors.toList());
        return ImmutableMap.of("count", tableNames.size(), "tables", tableNames);
    }


    @GetMapping(path = "tables/{table}")
    public HTableDescriptor getTableStats(@PathVariable String table) throws IOException {
        return connectionManager.doWithAdmin(admin -> admin.getTableDescriptor(TableName.valueOf(table)));
    }


    @GetMapping(path = "tables/{table}/regions")
    public Map<String, Object> getTableRegions(@PathVariable String table) throws IOException {
        List<HRegionInfo> regions = connectionManager.doWithAdmin(admin -> admin.getTableRegions(TableName.valueOf(table)));
        regions.sort(Comparator.comparing(HRegionInfo::getRegionNameAsString));
        return ImmutableMap.of("count", regions.size(), "regions", regions);
    }
}
