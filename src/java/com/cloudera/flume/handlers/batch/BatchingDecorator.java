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
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.flume.conf.Context;
import com.cloudera.flume.conf.SinkFactory.SinkDecoBuilder;
import com.cloudera.flume.core.Event;
import com.cloudera.flume.core.EventImpl;
import com.cloudera.flume.core.EventSink;
import com.cloudera.flume.core.EventSinkDecorator;
import com.cloudera.flume.handlers.hdfs.WriteableEvent;
import com.cloudera.flume.reporter.ReportEvent;
import com.cloudera.util.Clock;
import com.google.common.base.Preconditions;

/**
 * This takes many events and then batches them up into one batched event. If a
 * small number of events has been received and a specified latency has passed
 * since the last batch, the current batch is sent downstream..
 * 
 * TODO (jon) This should not have its own thread -- close flushes the batch and
 * only the roller (which closes) should have the thread.
 */
public class BatchingDecorator<S extends EventSink> extends
    EventSinkDecorator<S> {
  protected static final Logger LOG = LoggerFactory
      .getLogger(BatchingDecorator.class);

  public static final String BATCH_SIZE = "batchSize";
  public static final String BATCH_DATA = "batchData";

  // timeoutBatches + filledBatches + (emptyBatches) = triggeredBatches
  public static final String R_TIMEOUTS = "timeoutBatches";
  public static final String R_FILLED = "filledBatches";
  public static final String R_TRIGGERS = "triggeredBatches";
  public static final String R_EMPTY = "emptyBatches";

  AtomicLong timeoutCount = new AtomicLong(0);
  AtomicLong filledCount = new AtomicLong(0);
  AtomicLong totalCount = new AtomicLong(0);
  AtomicLong emptyCount = new AtomicLong(0);

  final int maxSize;
  final int maxLatency;

  List<Event> events;
  protected long lastBatchTime = 0;

  public BatchingDecorator(S s, int maxSize, int maxLatency) {
    super(s);
    this.maxSize = maxSize;
    this.maxLatency = maxLatency;
    events = new ArrayList<Event>(maxSize);
  }

  Event batchevent(List<Event> evts) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream(2 >> 15);
    DataOutput out = new DataOutputStream(baos);
    for (Event evt : events) {
      WriteableEvent we = new WriteableEvent(evt);
      we.write(out);
    }

    Event be = new EventImpl(new byte[0]);
    ByteBuffer b = ByteBuffer.allocate(4);
    b.putInt(events.size());
    be.set(BATCH_SIZE, b.array());
    be.set(BATCH_DATA, baos.toByteArray());
    return be;
  }

  public static boolean isBatch(Event e) {
    return e.get(BATCH_SIZE) != null && e.get(BATCH_DATA) != null;
  }

  /**
   * This is a method setup seems trivial but is used in testing.
   */
  protected void endBatchTimeout() throws IOException, InterruptedException {
    endBatch();
    timeoutCount.incrementAndGet();

  }

  protected synchronized void endBatch() throws IOException,
      InterruptedException {
    if (events.size() > 0) {
      Event be = batchevent(events);
      super.append(be);
      events.clear();
    } else {
      emptyCount.incrementAndGet();
    }
    lastBatchTime = Clock.unixTime();
    totalCount.incrementAndGet();
  }

  /**
   * This thread wakes up to check if a batch should be committed, no matter how
   * small it is.
   */
  class TimeoutThread extends Thread {
    final CountDownLatch doneLatch = new CountDownLatch(1);
    final CountDownLatch startedLatch = new CountDownLatch(1);
    volatile boolean timeoutThreadDone = false;

    TimeoutThread() {
      super("BatchingDecorator-TimeoutThread");
    }

    public synchronized void doShutdown() {
      timeoutThreadDone = true;
      interrupt();
      try {
        if (!doneLatch.await(maxLatency * 2L, TimeUnit.MILLISECONDS)) {
          LOG.warn("Batch timeout thread did not exit in a timely fashion");
        }
      } catch (InterruptedException e) {
        LOG.warn("Interrupted while waiting for batch timeout thread to finish");
      }
    }

    public synchronized void doStart() {
      timeoutThreadDone = false;
      this.start();
      try {
        if (!startedLatch.await(maxLatency * 2L, TimeUnit.MILLISECONDS)) {
          LOG.warn("Batch timeout thread did not start in a timely fashion");
        }
      } catch (InterruptedException e) {
        LOG.warn("Interrupted while waiting for batch timeout thread to start");
      }
    }

    public void run() {
      startedLatch.countDown();
      lastBatchTime = Clock.unixTime();
      while (!timeoutThreadDone) {
        long now = Clock.unixTime();
        long msUntilTimeout = 0;
        synchronized (this) {
          msUntilTimeout = lastBatchTime + maxLatency - now;
        }
        if (msUntilTimeout > 0) {
          try {
            LOG.debug("Batching timeout sleeping for " + msUntilTimeout + "ms");
            Clock.sleep(msUntilTimeout);
          } catch (InterruptedException e) {
            LOG.warn("TimeoutThread interrupted", e);
          }
          continue;
        }
        try {
          endBatchTimeout();
        } catch (IOException e) {
          LOG.error("IOException when ending batch!", e);
          timeoutThreadDone = true;
        } catch (InterruptedException e) {
          // TODO verify this is correct
          LOG.error("Interrupted exceptoin when ending batch", e);
          timeoutThreadDone = true;
        }
      }
      doneLatch.countDown();
    }
  }

  protected TimeoutThread timeoutThread = null;

  @Override
  public void open() throws IOException, InterruptedException {
    // TODO handle interruptions
    super.open();
    if (maxLatency > 0) {
      timeoutThread = new TimeoutThread();
      timeoutThread.doStart();
    }
  }

  /**
   * Synchronized so that events doesn't get manipulated by the TimeoutThread
   * calling endBatch.
   */
  @Override
  public synchronized void append(Event e) throws IOException,
      InterruptedException {
    events.add(e);
    if (events.size() >= maxSize) {
      // TODO handle interruptions
      endBatch();
      filledCount.incrementAndGet();
    }
  }

  @Override
  public void close() throws IOException, InterruptedException {
    // TODO handle interruptions

    // flush any left over events in queue.
    endBatch();
    if (timeoutThread != null) {
      timeoutThread.doShutdown();
    }
    super.close();
  }

  @Override
  public ReportEvent getMetrics() {
    ReportEvent rpt = super.getMetrics();
    rpt.setLongMetric(R_TIMEOUTS, timeoutCount.get());
    rpt.setLongMetric(R_FILLED, filledCount.get());
    rpt.setLongMetric(R_TRIGGERS, totalCount.get());
    rpt.setLongMetric(R_EMPTY, emptyCount.get());
    return rpt;
  }

  public static SinkDecoBuilder builder() {
    return new SinkDecoBuilder() {
      @Override
      public EventSinkDecorator<EventSink> build(Context context,
          String... argv) {
        Preconditions.checkArgument(argv.length >= 1 && argv.length < 3,
            "usage: batch(factor [,maxlatencymillis])");
        int factor = Integer.parseInt(argv[0]);
        int latency = 0;
        if (argv.length >= 2)
          latency = Integer.parseInt(argv[1]);
        return new BatchingDecorator<EventSink>(null, factor, latency);
      }
    };
  }

}
