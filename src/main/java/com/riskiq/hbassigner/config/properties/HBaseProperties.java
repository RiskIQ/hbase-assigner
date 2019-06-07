package com.riskiq.hbassigner.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Joe Linn
 * 05/30/2019
 */
@ConfigurationProperties(prefix = "hbase")
public class HBaseProperties {
    /**
     * comma-delimited list of Zookeeper hosts
     */
    private String zookeeperHosts;
    /**
     * zookeeper port
     */
    private int zookeeperPort;
    /**
     * Cron schedule on which the {@link com.riskiq.hbassigner.hbase.RegionCheckerTask} will run.
     */
    private String cron = "0 */30 * * * *";


    public String getZookeeperHosts() {
        return zookeeperHosts;
    }

    public void setZookeeperHosts(String zookeeperHosts) {
        this.zookeeperHosts = zookeeperHosts;
    }

    public int getZookeeperPort() {
        return zookeeperPort;
    }

    public void setZookeeperPort(int zookeeperPort) {
        this.zookeeperPort = zookeeperPort;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }
}
