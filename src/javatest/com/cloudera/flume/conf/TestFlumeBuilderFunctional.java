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
package com.cloudera.flume.conf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.cloudera.flume.ExampleData;
import com.cloudera.flume.core.Event;
import com.cloudera.flume.core.EventImpl;
import com.cloudera.flume.core.EventSink;
import com.cloudera.flume.core.EventSource;
import com.cloudera.flume.core.EventUtil;
import com.cloudera.flume.core.connector.DirectDriver;
import com.cloudera.flume.handlers.debug.MemorySinkSource;
import com.cloudera.flume.reporter.ReportManager;
import com.cloudera.flume.reporter.aggregator.AccumulatorSink;
import com.cloudera.util.Pair;
import org.junit.Test;

/**
 * These are essentially the same tests as found in TestFactories, but use the
 * parser and builder infrastructure.
 * 
 * TODO (jon) eventually build code generator so we just test
 * parse/generate/parse.
 */
public class TestFlumeBuilderFunctional implements ExampleData {
  final static Logger LOG = Logger.getLogger(TestFlumeBuilderFunctional.class
      .getName());

  final String SOURCE = "asciisynth(25,100)";
  final static int LINES = 25;

  @Test
  public void testBuildConsole() throws IOException, FlumeSpecException,
      InterruptedException {

    EventSink snk = FlumeBuilder.buildSink(new Context(), "console");
    snk.open();
    snk.append(new EventImpl("test".getBytes()));
    snk.close();
  }

  @Test
  public void testBuildTextSource() throws IOException, FlumeSpecException,
      InterruptedException {
    Context ctx = LogicalNodeContext.testingContext();
    LOG.info("Working Dir path: " + new File(".").getAbsolutePath());
    EventSource src = FlumeBuilder.buildSource(ctx, SOURCE);
    src.open();
    Event e = null;
    int cnt = 0;
    while ((e = src.next()) != null) {
      LOG.info(e);
      cnt++;
    }
    src.close();
    assertEquals(LINES, cnt);
  }

  @Test
  public void testConnector() throws IOException, InterruptedException,
      FlumeSpecException {
    Context ctx = LogicalNodeContext.testingContext();
    EventSink snk = FlumeBuilder.buildSink(new Context(), "console");
    snk.open();

    EventSource src = FlumeBuilder.buildSource(ctx, SOURCE);
    src.open();

    DirectDriver conn = new DirectDriver(src, snk);
    conn.start();

    conn.join(Long.MAX_VALUE);

    snk.close();
    src.close();
    assertNull(conn.getError());
  }

  @Test
  public void testMultiSink() throws IOException, FlumeSpecException,
      InterruptedException {
    Context ctx = LogicalNodeContext.testingContext();
    LOG.info("== multi test start");
    String multi = "[ console , accumulator(\"count\") ]";
    EventSource src = FlumeBuilder.buildSource(ctx, SOURCE);
    EventSink snk = FlumeBuilder.buildSink(new ReportTestingContext(), multi);
    src.open();
    snk.open();
    EventUtil.dumpAll(src, snk);
    src.close();
    snk.close();
    AccumulatorSink cnt = (AccumulatorSink) ReportManager.get().getReportable(
        "count");
    assertEquals(LINES, cnt.getCount());
    LOG.info("== multi test stop");
  }

  @Test
  public void testDecorated() throws IOException, FlumeSpecException,
      InterruptedException {
    Context ctx = LogicalNodeContext.testingContext();

    LOG.info("== Decorated start");
    String decorated = "{ intervalSampler(5) =>  accumulator(\"count\")}";
    // String decorated = "{ intervalSampler(5) =>  console }";

    EventSource src = FlumeBuilder.buildSource(ctx, SOURCE);
    EventSink snk = FlumeBuilder.buildSink(new ReportTestingContext(),
        decorated);
    src.open();
    snk.open();
    EventUtil.dumpAll(src, snk);
    src.close();
    snk.close();
    AccumulatorSink cnt = (AccumulatorSink) ReportManager.get().getReportable(
        "count");
    assertEquals(LINES / 5, cnt.getCount());
    LOG.info("== Decorated stop");
  }

  @Test
  public void testFailover() throws IOException, FlumeSpecException,
      InterruptedException {
    Context ctx = LogicalNodeContext.testingContext();

    LOG.info("== failover start");
    // the primary is 90% flakey
    String multi = "< { flakeyAppend(.9,1337) => console } ? accumulator(\"count\") >";
    EventSource src = FlumeBuilder.buildSource(ctx, SOURCE);
    EventSink snk = FlumeBuilder.buildSink(new ReportTestingContext(), multi);
    src.open();
    snk.open();
    EventUtil.dumpAll(src, snk);
    src.close();
    snk.close();
    AccumulatorSink cnt = (AccumulatorSink) ReportManager.get().getReportable(
        "count");
    assertEquals(LINES, cnt.getCount());
    LOG.info("== failover stop");
  }

  @Test
  public void testNode() throws IOException, FlumeSpecException,
      InterruptedException {
    LOG.info("== node start");
    String multi = "localhost : "
        + SOURCE
        + " | < { flakeyAppend(.9,1337) => console } ? accumulator(\"count\") > ;";

    Map<String, Pair<EventSource, EventSink>> cfg = FlumeBuilder.build(
        new ReportTestingContext(), multi);
    for (Entry<String, Pair<EventSource, EventSink>> e : cfg.entrySet()) {
      // String name = e.getKey();
      EventSource src = e.getValue().getLeft();
      EventSink snk = e.getValue().getRight();
      src.open();
      snk.open();
      EventUtil.dumpAll(src, snk);
      src.close();
      snk.close();
    }
    AccumulatorSink cnt = (AccumulatorSink) ReportManager.get().getReportable(
        "count");
    assertEquals(LINES, cnt.getCount());
    LOG.info("== node stop");
  }
}
