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
package com.cloudera.flume.reporter.aggregator;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.flume.conf.Context;
import com.cloudera.flume.conf.ReportTestingContext;
import com.cloudera.flume.conf.SinkFactory.SinkBuilder;
import com.cloudera.flume.core.Event;
import com.cloudera.flume.core.EventSink;
import com.cloudera.flume.reporter.ReportEvent;
import com.cloudera.flume.reporter.ReportManager;
import com.google.common.base.Preconditions;

/**
 * This just counts the number of entries appended. Unlike the CounterSink, this
 * sink does not reset the count when opened/closed
 */
public class AccumulatorSink extends EventSink.Base {
  static final Logger LOG = LoggerFactory.getLogger(AccumulatorSink.class);

  AtomicLong cnt;
  String name;

  boolean isOpen = false;

  public AccumulatorSink(String name) {
    this.name = name;
    this.cnt = new AtomicLong();
  }

  @Override
  public void append(Event e) throws IOException, InterruptedException {
    Preconditions.checkState(isOpen,
        "Attempting to append to an accumulator that is not open!");
    cnt.incrementAndGet();
    super.append(e);
  }

  @Override
  public void close() throws IOException, InterruptedException {
    isOpen = false;
    LOG.info(name + " closed, accumulated " + cnt + " events");
    LOG.info("report: " + getMetrics().toText());
  }

  @Override
  public void open() throws IOException, InterruptedException {
    Preconditions.checkState(!isOpen);
    isOpen = true;
    // Do no reset counter.
    LOG.info("AccumulatorSink " + name + " opened");
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public ReportEvent getMetrics() {
    ReportEvent re = super.getMetrics();
    re.setLongMetric(name, cnt.get());
    return re;
  }

  @Deprecated
  @Override
  public ReportEvent getReport() {
    ReportEvent re = super.getReport();
    re.setLongMetric(name, cnt.get());
    return re;
  }

  public long getCount() {
    return cnt.get();
  }

  public static SinkBuilder builder() {

    return new SinkBuilder() {
      @Override
      public EventSink build(Context context, String... argv) {
        if (argv.length != 1) {
          throw new IllegalArgumentException("need only a name argument");
        }
        EventSink snk = new AccumulatorSink(argv[0]);

        if (context.getValue(ReportTestingContext.TESTING_REPORTS) != null) {
          ReportManager.get().add(snk);
        }

        return snk;
      }

    };
  }

}
