package com.riskiq.hbassigner.health;

import com.riskiq.hbassigner.hbase.HBaseConnectionManager;
import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.hadoop.hbase.client.Admin;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

/**
 * A <a href="https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#production-ready">Spring Boot Actuator</a>
 * health indicator which reports the status of our HBase connection and lists the servers in our cluster.
 * @author Joe Linn
 *         04/06/2017
 */
public class HBaseHealthIndicator extends AbstractHealthIndicator {
    private HBaseConnectionManager connectionManager;


    public void setConnectionManager(HBaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }


    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        ClusterStatus status = connectionManager.doWithAdmin(Admin::getClusterStatus);
        builder.up()
                .withDetail("master", status.getMaster())
                .withDetail("servers", status.getServers());
    }
}
