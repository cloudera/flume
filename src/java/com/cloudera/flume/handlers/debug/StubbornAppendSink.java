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
package com.cloudera.flume.handlers.debug;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.flume.conf.Context;
import com.cloudera.flume.conf.SinkFactory.SinkDecoBuilder;
import com.cloudera.flume.core.Event;
import com.cloudera.flume.core.EventSink;
import com.cloudera.flume.core.EventSinkDecorator;
import com.cloudera.flume.reporter.ReportEvent;
import com.cloudera.flume.reporter.Reportable;
import com.google.common.base.Preconditions;

/**
 * A sink is "stubborn" if it attempts to reopen a connections if an IO
 * exception is thrown during an append.
 * 
 * This only catches one failure, consecutive failures will still through
 * exception
 */
public class StubbornAppendSink<S extends EventSink> extends
    EventSinkDecorator<S> implements Reportable {
  public static final Logger LOG = LoggerFactory
      .getLogger(StubbornAppendSink.class);
  // attribute names
  final public static String A_SUCCESSES = "appendSuccess";
  final public static String A_FAILS = "appendFails";
  final public static String A_RECOVERS = "appendRecovers";

  AtomicLong appendSuccesses = new AtomicLong();
  AtomicLong appendFails = new AtomicLong();
  AtomicLong appendRecovers = new AtomicLong();

  public StubbornAppendSink(S s) {
    super(s);
  }

  @Override
  public void open() throws IOException, InterruptedException {
    Preconditions.checkNotNull(sink);
    LOG.debug("Opening Stubborn Append Sink {}", this);
    isOpen.set(true); // slightly different than default semantics -- open
    // always keeps it in open state.
    sink.open();
    LOG.debug("Opened Stubborn Append Sink {}", this);
  }

  @Override
  public void append(Event e) throws IOException, InterruptedException {
    try {
      super.append(e);
      appendSuccesses.incrementAndGet();
      return; // success case
    } catch (Exception ex) {
      LOG.info(ex.getMessage());
      appendFails.incrementAndGet();
      super.close(); // close

      if (Thread.currentThread().isInterrupted()) {
        throw new IOException(
            "throwing exception because stubborn append was interrupted");
      }

      open(); // attempt to reopen
      super.append(e); // resend
      appendSuccesses.incrementAndGet();
      // another exception may have been thrown at close/open/append
      appendRecovers.incrementAndGet();
    }
  }

  public static SinkDecoBuilder builder() {

    return new SinkDecoBuilder() {
      @Override
      public EventSinkDecorator<EventSink> build(Context context,
          String... argv) {
        Preconditions.checkArgument(argv.length == 0, "usage: stubborn");
        return new StubbornAppendSink<EventSink>(null);
      }

    };
  }

  @Override
  public String getName() {
    return "StubbornAppend";
  }

  @Override
  public ReportEvent getMetrics() {
    ReportEvent e = super.getMetrics();
    e.setLongMetric(A_SUCCESSES, appendSuccesses.get());
    e.setLongMetric(A_FAILS, appendFails.get());
    e.setLongMetric(A_RECOVERS, appendRecovers.get());
    return e;
  }

  @Deprecated
  @Override
  public ReportEvent getReport() {
    ReportEvent e = super.getReport();
    e.setLongMetric(A_SUCCESSES, appendSuccesses.get());
    e.setLongMetric(A_FAILS, appendFails.get());
    e.setLongMetric(A_RECOVERS, appendRecovers.get());
    return e;
  }
}
