/**
 * Licensed to Cloudera, Inc. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  Cloudera, Inc. licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudera.flume.handlers.hbase;

import com.cloudera.flume.conf.Context;
import com.cloudera.flume.conf.SinkFactory;
import com.cloudera.flume.conf.SinkFactory.SinkBuilder;
import com.cloudera.flume.core.Event;
import com.cloudera.flume.core.EventSink;
import com.cloudera.util.Pair;
import com.google.common.base.Preconditions;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

/**
 * This generates an HBase output sink which puts event attributes into HBase record based on their names.
 * It is similar to {@link com.cloudera.flume.handlers.hbase.HBaseEventSink}, please refer to README.txt for basic steps.
 *
 * Sink has the next parameters: attr2hbase("table" [,"family"[,"attrPrefix"[,"writeBufferSize"[,"writeToWal"]]]]).
 * "table"           - HBase table name to perform output into.
 * "family"          - Column family's name which is used to store "system" data (event's timestamp, host).
 *                     In case this param is absent or ="" the sink doesn't write "system" data.
 * "attrPrefix"      - Attributes with this prefix in key will be placed into HBase table. Default value: "2hb_".
 *                     Attribute key should be in the following format: "<attrPrefix><columnFamily>:<qualifier>",
 *                     e.g. "2hb_user:name" means that its value will be placed into "user" column family with "name" qualifier
 *                     Attribute with key "<attrPrefix>" should contain row key for Put, otherwise (if attribute is absent) event's getNanos() used as row key value
 * "writeBufferSize" - If provided, autoFlush for the HTable set to "false", and writeBufferSize is set to its value.
 *                     This setting is valuable to boost HBase write speed.
 * "writeToWal"      - Determines whether WAL should be used during writing to HBase.
 *                     This setting is valuable to boost HBase write speed, but decreases reliability level. Use it if you know what it does.
 *
 * The Sink also implements method getSinkBuilders(), so it can be used as Flume's extension plugin (see flume.plugin.classes property of flume-site.xml config details)
 */
public class Attr2HBaseEventSink extends EventSink.Base {
  private final static Logger LOG = Logger.getLogger(Attr2HBaseEventSink.class.getName());
  public static final String USAGE = "usage: attr2hbase(\"table\" [,\"family\"[,\"attrPrefix\"[,\"writeBufferSize\"[,\"writeToWal\"]]]])";

  private String tableName;

  /**
   * Column family name to store system data like timestamp of event, host
   */
  private String familyName;
  private String attrPrefix = "2hb_";
  private long writeBufferSize = 0L;
  private boolean writeToWal = true;

  private Configuration config;
  private HTable table;

  public Attr2HBaseEventSink(String tableName, String familyName, String attrPrefix,
                                 long writeBufferSize, boolean writeToWal) {
    // You need a configuration object to tell the client where to connect.
    // When you create a HBaseConfiguration, it reads in whatever you've set
    // into your hbase-site.xml and in hbase-default.xml, as long as these can
    // be found on the CLASSPATH
    this(tableName, familyName, attrPrefix, writeBufferSize, writeToWal, HBaseConfiguration.create());
  }

  public Attr2HBaseEventSink(String tableName, String familyName, String attrPrefix,
                                 long writeBufferSize, boolean writeToWal, Configuration config)
  {
    Preconditions.checkNotNull(tableName);
    this.tableName = tableName;
    // familyName can be null or empty String, which means "don't store "system" data
    if (familyName != null && !"".equals(familyName)) {
      this.familyName = familyName;
    }
    if (attrPrefix!= null) {
      this.attrPrefix = attrPrefix;
    }

    this.writeBufferSize = writeBufferSize;
    this.writeToWal = writeToWal;

    this.config = config;
  }
  
  @Override
  public void append(Event e) throws IOException {
    Put p;
    // Attribute with key "<attrPrefix>" contains row key for Put
    if (e.getAttrs().containsKey(attrPrefix)) {
      p = new Put(e.getAttrs().get(attrPrefix));
    } else {
      p = new Put(Bytes.toBytes(e.getNanos()));
    }

    if (familyName != null) {
      p.add(Bytes.toBytes(familyName), Bytes.toBytes("timestamp"),
          Bytes.toBytes(e.getTimestamp()));
      p.add(Bytes.toBytes(familyName), Bytes.toBytes("host"),
          Bytes.toBytes(e.getHost()));
    }

//  Body shouldn't be usually stored. TODO: make configurable?
//    p.add(Bytes.toBytes(familyName), Bytes.toBytes("event"), e.getBody());

    for (Entry<String, byte[]> a : e.getAttrs().entrySet()) {
      if (a.getKey().startsWith(attrPrefix) && a.getKey().length() > attrPrefix.length()) {
        String[] col = a.getKey().substring(attrPrefix.length()).split(":"); // please see the javadoc for attrPrefix for more info
        p.add(Bytes.toBytes(col[0]), Bytes.toBytes(col[1]), a.getValue());
      }
    }

    if (p.getFamilyMap().size() > 0) {
      p.setWriteToWAL(writeToWal);
      table.put(p);
    }
  }

  @Override
  public void close() throws IOException {
    table.close(); // performs flushCommits() internally, so we are good when autoFlush=false
  }

  @Override
  public void open() throws IOException {
    // This instantiates an HTable object that connects you to
    // the tableName table.
    table = new HTable(config, tableName);
    if (writeBufferSize > 0) {
      table.setAutoFlush(false);
      table.setWriteBufferSize(writeBufferSize);
    }
  }

  public static SinkBuilder builder() {
    return new SinkBuilder() {

      @Override
      public EventSink build(Context context, String... argv) {
        Preconditions.checkArgument(argv.length >= 1,
                USAGE);

        // TODO: check that arguments has proper types

        return new Attr2HBaseEventSink(argv[0],
                                           argv.length >= 2 ? argv[1] : null,
                                           argv.length >= 3 ? argv[2] : null,
                                           argv.length >= 4 ? Long.valueOf(argv[3]) : 0,
                                           argv.length >= 5 ? Boolean.valueOf(argv[4]) : true);
      }

    };
  }

  public static List<Pair<String, SinkFactory.SinkBuilder>> getSinkBuilders() {
    return Arrays.asList(new Pair<String, SinkFactory.SinkBuilder>("attr2hbase", builder()));
  }
}
