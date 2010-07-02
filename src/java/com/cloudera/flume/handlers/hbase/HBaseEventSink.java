// This Cloudera, Inc. source code, including without limit any
// human-readable computer programming code and associated documentation
// (together "Source Code"), contains valuable confidential, proprietary
// and trade secret information of Cloudera and is protected by the laws
// of the U.S. and other countries. Any use of the Source Code, including
// without limit any disclosure or reproduction, without the prior
// written authorization of Cloudera is strictly prohibited.
//
// Copyright (c) 2010 Cloudera, Inc.  All rights reserved.
package com.cloudera.flume.handlers.hbase;

import java.io.IOException;
import java.util.Map.Entry;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import com.cloudera.flume.conf.Context;
import com.cloudera.flume.conf.SinkFactory.SinkBuilder;
import com.cloudera.flume.core.Event;
import com.cloudera.flume.core.EventSink;
import com.google.common.base.Preconditions;

/**
 * This generates an hbase output sink. Nanos is used as the row name,
 * event timestamp, host, body and attributes are all added to the row.
 */
public class HBaseEventSink extends EventSink.Base {

  String tableName;
  String familyName;

  HBaseConfiguration config;
  HTable table;

  public HBaseEventSink(String tableName, String familyName) {
    // You need a configuration object to tell the client where to connect.
    // When you create a HBaseConfiguration, it reads in whatever you've set
    // into your hbase-site.xml and in hbase-default.xml, as long as these can
    // be found on the CLASSPATH
    this(tableName, familyName, new HBaseConfiguration());
  }

  public HBaseEventSink(String tableName, String familyName,
      HBaseConfiguration config)
  {
    Preconditions.checkNotNull(tableName);
    Preconditions.checkNotNull(familyName);
    this.tableName = tableName;
    this.familyName = familyName;

    this.config = config;
  }
  
  @Override
  public void append(Event e) throws IOException {
    Put p = new Put(Bytes.toBytes(e.getNanos()));
    
    p.add(Bytes.toBytes(familyName), Bytes.toBytes("timestamp"),
        Bytes.toBytes(e.getTimestamp()));
    p.add(Bytes.toBytes(familyName), Bytes.toBytes("host"),
        Bytes.toBytes(e.getHost()));
    p.add(Bytes.toBytes(familyName), Bytes.toBytes("event"), e.getBody());

    for (Entry<String, byte[]> a : e.getAttrs().entrySet()) {
      p.add(Bytes.toBytes(familyName), Bytes.toBytes(a.getKey()), a.getValue());
    }

    table.put(p);
  }

  @Override
  public void close() throws IOException {
    table.close();
  }

  @Override
  public void open() throws IOException {
    // This instantiates an HTable object that connects you to
    // the tableName table.
    table = new HTable(config, tableName);
  }

  public static SinkBuilder builder() {
    return new SinkBuilder() {

      @Override
      public EventSink build(Context context, String... argv) {
        Preconditions.checkArgument(argv.length == 2,
            "usage: hbase(table, family)");

        return new HBaseEventSink(argv[0], argv[1]);
      }

    };
  }
}
