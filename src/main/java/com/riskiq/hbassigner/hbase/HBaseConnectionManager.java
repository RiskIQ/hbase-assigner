package com.riskiq.hbassigner.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * As the name suggests, this manages our connection to HBase and makes things a bit more user-friendly for dependency
 * injection.
 * @author Joe Linn
 * 05/30/2019
 */
public class HBaseConnectionManager implements DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(HBaseConnectionManager.class);

    private Configuration configuration;

    private Connection connection;
    private AtomicBoolean isCreated = new AtomicBoolean(false);


    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }


    @Override
    public void destroy() throws Exception {
        if (connection != null) {
            log.info("Closing HBase connection.");
            connection.close();
        }
    }


    /**
     * This wraps HBase's ugly static connection creation method so that we don't have to keep passing
     * the config around everywhere.
     * This connection will be closed when the application context shuts down, and should NOT be closed manually.
     * {@link org.apache.hadoop.hbase.client.Admin} and {@link org.apache.hadoop.hbase.client.Table} instances
     * obtained from this {@link Connection}, however, are not thread-safe, and should be used in the context of a
     * try-with-resources block or closed manually.
     * @return a live HBase connection
     * @throws IOException if we encounter an error when attempting to connect to HBase
     */
    public Connection getConnection() throws IOException {
        if (!isCreated.get()) {
            initConnection();
        }
        return connection;
    }


    /**
     * Perform work using a HBase {@link Admin} resource.
     * @param callback a callback containing the work to be done
     * @param <T> return type of the callback
     * @return return value of the callback.  Can be <code>null</code>.
     * @throws IOException if there is an issue communicating with HBase
     */
    @Nullable
    public <T> T doWithAdmin(HBaseCallback.AdminCallback<T> callback) throws IOException {
        try (Admin admin = getConnection().getAdmin()) {
            return callback.doThings(admin);
        }
    }


    /**
     * Perform work using a HBase {@link Table} resource.
     * @param tableName name of the table which will be accessed, including the table's namespace if applicable ([namespace]:[table])
     * @param callback a callback containing the work to be done
     * @param <T> the return type of the callback
     * @return return value of the callback. Can be <code>null</code>.
     * @throws IOException if there is an issue communicating with HBase
     */
    @Nullable
    public <T> T doWithTable(String tableName, HBaseCallback.TableCallback<T> callback) throws IOException {
        try (Table table = getConnection().getTable(TableName.valueOf(tableName))) {
            return callback.doThings(table);
        }
    }


    /**
     * Initializes our HBase connection.  This should only be done once in the lifecycle of our application.
     * @throws IOException if a HBase communication issue occurs
     */
    private synchronized void initConnection() throws IOException {
        if (!isCreated.get()) {
            log.info("Instantiating HBase connection.");
            connection = ConnectionFactory.createConnection(configuration);
            isCreated.set(true);
        }
    }
}
