// This Cloudera, Inc. source code, including without limit any
// human-readable computer programming code and associated documentation
// (together "Source Code"), contains valuable confidential, proprietary
// and trade secret information of Cloudera and is protected by the laws
// of the U.S. and other countries. Any use of the Source Code, including
// without limit any disclosure or reproduction, without the prior
// written authorization of Cloudera is strictly prohibited.
//
// Copyright (c) 2010 Cloudera, Inc.  All rights reserved.
package com.cloudera.flume.hbase;

import java.io.IOException;

import junit.framework.Assert;

import org.apache.hadoop.hbase.HBaseTestCase;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cloudera.flume.core.Event;
import com.cloudera.flume.core.EventImpl;
import com.cloudera.flume.core.EventSink;
import com.cloudera.flume.core.Event.Priority;
import com.cloudera.util.Clock;

/**
 * Test the hbase sink writes events to a table/family properly
 */
public class TestHBaseSink {
  private static HBaseTestEnv hbaseEnv;
  
  @BeforeClass
  public static void setup() throws Exception {
    // expensive, so just do it once for all tests, just make sure
    // that tests don't overlap (use diff tables for each test)
    hbaseEnv = new HBaseTestEnv();
    hbaseEnv.conf.set(HBaseTestCase.TEST_DIRECTORY_KEY, "build/test/data");
    hbaseEnv.setUp();
  }
  
  @AfterClass
  public static void teardown() throws Exception {
    hbaseEnv.tearDown();
  }
  
  /**
   * Write events to a sink directly, verify by scanning HBase table.
   */
  @Test
  public void testSink() throws IOException {
    final String tableName = "testSink";
    final String tableFamily = "testSinkFamily";
    
    // create the table and column family to be used by sink
    HTableDescriptor desc = new HTableDescriptor(tableName);
    desc.addFamily(new HColumnDescriptor(tableFamily));
    HBaseAdmin admin = new HBaseAdmin(hbaseEnv.conf);
    admin.createTable(desc);
    
    // explicit constructor rather than builder - we want to control the conf
    EventSink snk = new HBaseEventSink(tableName, tableFamily, hbaseEnv.conf);
    snk.open();
    try {
      long day_millis = 1000 * 60 * 60 * 24;
      Event e1 = new EventImpl("message0".getBytes(), Clock.unixTime(),
          Priority.INFO, 0, "localhost");
      Event e2 = new EventImpl("message1".getBytes(),
          e1.getTimestamp() + day_millis, Priority.INFO, 1, "localhost");
      Event e3 = new EventImpl("message2".getBytes(),
          e1.getTimestamp() + 2 * day_millis, Priority.INFO, 2, "localhost");
      snk.append(e1);
      snk.append(e2);
      snk.append(e3);
    } finally {
      snk.close();
    }
    
    // verify that the events made it into hbase
    HTable table = new HTable(hbaseEnv.conf, tableName);
    try {
      for(long i = 0; i <=2; i++) {
        Result r = table.get(new Get(Bytes.toBytes(i)));
        System.out.println("result " + r);
        byte [] value = r.getValue(Bytes.toBytes(tableFamily),
          Bytes.toBytes("event"));
        Assert.assertEquals("Matching body", "message" + i, Bytes.toString(value));
      }
    } finally {
      table.close();
    }
  }

  /** Useful for debugging purposes. Scan table and dump to stdout */
  private void dumpByTimestamp(HTable table, String tableFamily, String colName)
    throws IOException
  {
    Scan s = new Scan();
    s.addColumn(Bytes.toBytes(tableFamily), Bytes.toBytes(colName));
    ResultScanner scanner = table.getScanner(s);
    System.out.println("Scanning " + table.toString() + " " + tableFamily);
    try {
      for (Result rr = scanner.next(); rr != null; rr = scanner.next()) {
        System.out.println("Found row: " + rr);
      }

    } finally {
      scanner.close();
    }
  }

}
