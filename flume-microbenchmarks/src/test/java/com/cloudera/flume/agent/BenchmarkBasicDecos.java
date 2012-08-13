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

import java.io.IOException;

import org.junit.Test;

import com.cloudera.flume.FlumeBenchmarkHarness;
import com.cloudera.flume.conf.FlumeSpecException;

/**
 * Benchmark tests on basic decorators.
 */
public class BenchmarkBasicDecos {
  String[] decos = { "nullDeco", "ackInjector", "ackChecker", };

  String[] batching = { "batch(10)", "batch(100)", "batch(1000)" };

  // TODO (jon) new decorator chaining notation
  // String[] gzips = { "batch(10) gzip", "batch(100) gzip", "batch(1000) gzip"
  // };

  @Test
  public void basicDecorator() throws FlumeSpecException, IOException,
      InterruptedException {
    for (String d : decos) {
      FlumeBenchmarkHarness.doDecoBenchmark(d,
          FlumeBenchmarkHarness.createVariedMsgBytesCases());
      FlumeBenchmarkHarness.doDecoBenchmark(d,
          FlumeBenchmarkHarness.createVariedNumAttrsCases());
      FlumeBenchmarkHarness.doDecoBenchmark(d,
          FlumeBenchmarkHarness.createVariedValSizeCases());
    }
  }
}
