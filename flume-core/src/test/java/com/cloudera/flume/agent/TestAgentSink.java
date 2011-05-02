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
package com.cloudera.flume.agent;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.flume.conf.Context;
import com.cloudera.flume.conf.FlumeBuilder;
import com.cloudera.flume.conf.FlumeConfiguration;
import com.cloudera.flume.conf.FlumeSpecException;
import com.cloudera.flume.conf.LogicalNodeContext;
import com.cloudera.flume.conf.SinkFactoryImpl;
import com.cloudera.flume.conf.SinkFactory.SinkBuilder;
import com.cloudera.flume.core.Event;
import com.cloudera.flume.core.EventImpl;
import com.cloudera.flume.core.EventSink;
import com.cloudera.flume.core.EventSource;
import com.cloudera.flume.core.EventUtil;
import com.cloudera.flume.handlers.avro.AvroJsonOutputFormat;
import com.cloudera.flume.handlers.debug.ConsoleEventSink;
import com.cloudera.flume.handlers.debug.MemorySinkSource;
import com.cloudera.util.FileUtil;

/**
 * This just tests the creation of agent sinks based on their configuration
 * strings.
 */
public class TestAgentSink {
  public static final Logger LOG = LoggerFactory.getLogger(TestAgentSink.class);
  FlumeNode node;
  MockMasterRPC mock;
  File tmpdir = null;

  // TODO (jon) the perf suit patch fixes this problem.
  @Before
  public void setUp() {
    // change config so that the write ahead log dir is in a new uniq place
    try {
      tmpdir = FileUtil.mktempdir();
    } catch (Exception e) {
      Assert.fail("mk temp dir failed");
    }
    FlumeConfiguration conf = FlumeConfiguration.get();
    conf.set(FlumeConfiguration.AGENT_LOG_DIR_NEW, tmpdir.getAbsolutePath());

    // This will register the FlumeNode with a MockMasterRPC so it doesn't go
    // across the network
    mock = new MockMasterRPC();
    node = new FlumeNode(mock, false /* starthttp */, false /* oneshot */);
  }

  @Test
  public void testBuilder() throws FlumeSpecException {
    String snk = " agentSink";
    FlumeBuilder.buildSink(LogicalNodeContext.testingContext(), snk);

    String snk2 = "agentSink(\"localhost\")";
    FlumeBuilder.buildSink(LogicalNodeContext.testingContext(), snk2);

    String snk3 = "agentSink(\"localhost\", 12345)";
    FlumeBuilder.buildSink(LogicalNodeContext.testingContext(), snk3);
    try {
      String snk4 = "agentSink(\"localhost\", 12345, \"fail\")";
      FlumeBuilder.buildSink(LogicalNodeContext.testingContext(), snk4);
    } catch (Exception e) {
      return;
    }
    Assert.fail("unexpected fall through");
  }

  @Test
  public void testDiskFailoverBuilder() throws FlumeSpecException {
    String snk = " agentFailoverSink";
    FlumeBuilder.buildSink(LogicalNodeContext.testingContext(), snk);

    String snk2 = "agentFailoverSink(\"localhost\")";
    FlumeBuilder.buildSink(LogicalNodeContext.testingContext(), snk2);

    String snk3 = "agentFailoverSink(\"localhost\", 12345)";
    FlumeBuilder.buildSink(LogicalNodeContext.testingContext(), snk3);
    try {
      String snk4 = "agentFailoverSink(\"localhost\", 12345, \"fail\")";
      FlumeBuilder.buildSink(LogicalNodeContext.testingContext(), snk4);
    } catch (Exception e) {
      return;
    }
    Assert.fail("unexpected fall through");
  }

  @Test
  public void testBestEffortBuilder() throws FlumeSpecException {
    String snk = " agentBestEffortSink";
    FlumeBuilder.buildSink(new Context(), snk);

    String snk2 = "agentBestEffortSink(\"localhost\")";
    FlumeBuilder.buildSink(new Context(), snk2);

    String snk3 = "agentBestEffortSink(\"localhost\", 12345)";
    FlumeBuilder.buildSink(new Context(), snk3);
    try {
      String snk4 = "agentBestEffortSink(\"localhost\", 12345, \"fail\")";
      FlumeBuilder.buildSink(new Context(), snk4);
    } catch (Exception e) {
      return;
    }
    Assert.fail("unexpected fall through");
  }

  /**
   * This test makes sure that opening and closing in rapid succession does not
   * cause an exception due to resource contention (ports) or race conditions.
   * 
   * @throws InterruptedException
   */
  @Test
  public void testAgentSink() throws FlumeSpecException, IOException,
      InterruptedException {
    String snkcfg = "agentSink(\"localhost\", 12345)";

    EventSource src = FlumeBuilder.buildSource(LogicalNodeContext
        .testingContext(), "collectorSource(12345)");
    src.open();

    for (int i = 0; i < 100; i++) {
      EventSink snk = FlumeBuilder.buildSink(LogicalNodeContext
          .testingContext(), snkcfg);
      snk.open();
      snk.close();
    }

  }


}
