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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.flume.conf.Context;
import com.cloudera.flume.conf.FlumeConfiguration;
import com.cloudera.flume.conf.SinkFactory.SinkDecoBuilder;
import com.cloudera.flume.core.Event;
import com.cloudera.flume.core.EventSink;
import com.cloudera.flume.core.EventSinkDecorator;
import com.cloudera.flume.reporter.ReportEvent;
import com.cloudera.flume.reporter.Reportable;
import com.cloudera.util.BackoffPolicy;
import com.cloudera.util.CappedExponentialBackoff;
import com.cloudera.util.CumulativeCappedExponentialBackoff;
import com.cloudera.util.MultipleIOException;
import com.google.common.base.Preconditions;

/**
 * This sink decorator attempts to retry appending for up to max millis. This in
 * conjunction with other decorators can make a sink never thrown an exception.
 * This is desired behavior on the retry side of a disk failover log.
 */
public class InsistentAppendDecorator<S extends EventSink> extends
    EventSinkDecorator<S> implements Reportable {
  static final Logger LOG = LoggerFactory
      .getLogger(InsistentAppendDecorator.class);
  final BackoffPolicy backoff;

  // attribute names
  final public static String A_INITIALSLEEP = "intialSleep";
  final public static String A_MAXSLEEP = "maxSleep";
  final public static String A_ATTEMPTS = "appendAttempts";
  final public static String A_REQUESTS = "appendRequests";
  final public static String A_SUCCESSES = "appendSuccessses";
  final public static String A_RETRIES = "appendRetries";
  final public static String A_GIVEUPS = "appendGiveups";

  long appendRequests = 0; // # of times append was called
  long appendAttempts = 0; // # of of times
  long appendSuccesses = 0; // # of times we successfully appended
  long appendRetries = 0; // # of times we tried to reappend
  long appendGiveups = 0; // # of times we gave up on waitingf

  public InsistentAppendDecorator(S s, BackoffPolicy bop) {
    super(s);
    this.backoff = bop;
  }

  /**
   * Creates a deco that has subsink s, and after failure initially waits for
   * 'initial' ms, exponentially backs off an individual sleep upto 'sleepCap'
   * ms, and fails after total backoff time has reached 'cumulativeCap' ms.
   */
  public InsistentAppendDecorator(S s, long initial, long sleepCap,
      long cumulativeCap) {
    super(s);
    this.backoff = new CumulativeCappedExponentialBackoff(initial, sleepCap,
        cumulativeCap);
  }

  /**
   * Creates a deco that has subsink s, and after failure initially waits for
   * 'initial' ms, exponentially backs off an individual sleep upto 'sleepCap'
   * ms. This has no cumulative cap and will never give up.
   */
  public InsistentAppendDecorator(S s, long initial, long sleepCap) {
    super(s);
    this.backoff = new CappedExponentialBackoff(initial, sleepCap);
  }

  /**
   * We have to be careful with this append method -- it has the potential to
   * block forever!
   */
  @Override
  public void append(Event evt) throws IOException {
    List<IOException> exns = new ArrayList<IOException>();
    int attemptRetries = 0;
    appendRequests++;
    while (!backoff.isFailed() && isOpen.get()
        && !Thread.currentThread().isInterrupted()) {
      try {
        appendAttempts++;
        super.append(evt);
        appendSuccesses++;
        backoff.reset(); // reset backoff counter;
        return;
      } catch (Exception e) {
        // this is an unexpected exception
        long waitTime = backoff.sleepIncrement();
        LOG.info("append attempt " + attemptRetries + " failed, backoff ("
            + waitTime + "ms): " + e.getMessage());
        LOG.debug(e.getMessage(), e);
        exns.add((e instanceof IOException) ? (IOException) e
            : new IOException(e));
        backoff.backoff();
        try {
          backoff.waitUntilRetryOk();
        } catch (InterruptedException e1) {
          // got an interrupted signal, bail out!
          exns.add(new IOException(e1));
          Thread.currentThread().interrupt();
          throw MultipleIOException.createIOException(exns);
        } finally {
          attemptRetries++;
          appendRetries++;
        }
      }
    }
    appendGiveups++;
    // failed to start
    IOException ioe = MultipleIOException.createIOException(exns);
    if (ioe == null) {
      return;
    }
    throw ioe;
  }

  public static SinkDecoBuilder builder() {
    return new SinkDecoBuilder() {
      @Override
      public EventSinkDecorator<EventSink> build(Context context,
          String... argv) {
        long initMs = FlumeConfiguration.get().getInsistentOpenInitBackoff();
        long cumulativeMaxMs = FlumeConfiguration.get()
            .getFailoverMaxCumulativeBackoff();
        long maxSingleMs = FlumeConfiguration.get()
            .getFailoverMaxSingleBackoff();

        Preconditions.checkArgument(argv.length <= 3,
            "usage: insistentAppend([maxSingle=" + maxSingleMs + "[,init="
                + initMs + "[,cumulativeMax=maxint]]])");

        if (argv.length >= 1) {
          maxSingleMs = Long.parseLong(argv[0]);
        }
        if (argv.length >= 2) {
          initMs = Long.parseLong(argv[1]);
        }
        if (argv.length == 3) {
          cumulativeMaxMs = Long.parseLong(argv[2]);
          // This one can give up
          return new InsistentAppendDecorator<EventSink>(null, initMs,
              maxSingleMs, cumulativeMaxMs);
        }

        // This one never gives up
        return new InsistentAppendDecorator<EventSink>(null, initMs,
            maxSingleMs);

      }

    };
  }

  @Override
  public String getName() {
    return "InsistentAppend";
  }

  @Override
  public ReportEvent getMetrics() {
    ReportEvent rpt = super.getMetrics();

    // counters
    rpt.setLongMetric(A_REQUESTS, appendRequests);
    rpt.setLongMetric(A_ATTEMPTS, appendAttempts);
    rpt.setLongMetric(A_SUCCESSES, appendSuccesses);
    rpt.setLongMetric(A_RETRIES, appendRetries);
    rpt.setLongMetric(A_GIVEUPS, appendGiveups);

    return rpt;
  }

  @Override
  public Map<String, Reportable> getSubMetrics() {
    Map<String, Reportable> map = super.getSubMetrics();
    map.put("backoffPolicy." + backoff.getName(), backoff);
    return map;
  }

}
