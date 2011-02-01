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
package com.cloudera.flume.handlers.batch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPOutputStream;

import com.cloudera.flume.conf.Context;
import com.cloudera.flume.conf.SinkFactory.SinkDecoBuilder;
import com.cloudera.flume.core.Event;
import com.cloudera.flume.core.EventImpl;
import com.cloudera.flume.core.EventSink;
import com.cloudera.flume.core.EventSinkDecorator;
import com.cloudera.flume.handlers.hdfs.WriteableEvent;
import com.cloudera.flume.reporter.ReportEvent;
import com.google.common.base.Preconditions;

/**
 * This gzips each event as it passes through the decorator.
 */
public class GzipDecorator<S extends EventSink> extends EventSinkDecorator<S> {

  public static final String R_EVENTCOUNT = "eventsCount";
  public static final String R_EVENTSIZE = "eventsSize";
  public static final String R_GZIPSIZE = "gzippedSize";

  AtomicLong eventCount = new AtomicLong(0);
  AtomicLong eventSize = new AtomicLong(0);
  AtomicLong gzipSize = new AtomicLong(0);

  public GzipDecorator(S s) {
    super(s);
  }

  @Override
  public void append(Event e) throws IOException, InterruptedException {
    WriteableEvent we = new WriteableEvent(e);
    byte[] bs = we.toBytes();
    eventSize.addAndGet(bs.length);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    GZIPOutputStream gzos = new GZIPOutputStream(baos);
    gzos.write(bs);
    gzos.close();

    Event gze = new EventImpl(new byte[0]);
    byte[] compressed = baos.toByteArray();
    gze.set(GunzipDecorator.GZDOC, compressed);
    super.append(gze);
    gzipSize.addAndGet(compressed.length);
    eventCount.incrementAndGet();
  }

  @Override
  public ReportEvent getMetrics() {
    ReportEvent rpt = super.getMetrics();
    rpt.setLongMetric(R_EVENTCOUNT, eventCount.get());
    rpt.setLongMetric(R_EVENTSIZE, eventSize.get());
    rpt.setLongMetric(R_GZIPSIZE, gzipSize.get());
    return rpt;
  }

  public static SinkDecoBuilder builder() {
    return new SinkDecoBuilder() {
      @Override
      public EventSinkDecorator<EventSink> build(Context context,
          String... argv) {
        Preconditions.checkArgument(argv.length == 0, "usage: gzip");
        return new GzipDecorator<EventSink>(null);
      }
    };
  }
}
