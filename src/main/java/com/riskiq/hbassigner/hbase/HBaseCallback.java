package com.riskiq.hbassigner.hbase;

import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Table;

import java.io.IOException;

/**
 * Represents a callback which will do work with a given {@link HBaseResource}.  Opening and closing of the resource
 * will be handled externally.
 * @author Joe Linn
 * 05/31/2019
 */
public interface HBaseCallback<T, HBaseResource> {
    /**
     * Perform work with the given resource.
     * @param resource a HBase resource ({@link Admin}, {@link Table}, etc.)
     * @return result of the work done. Can be <code>null</code>.
     * @throws IOException if a HBase error occurs during the course of the work done in the callback
     */
    T doThings(HBaseResource resource) throws IOException;


    public interface AdminCallback<T> extends HBaseCallback<T, Admin> {}


    public interface TableCallback<T> extends HBaseCallback<T, Table> {}
}
