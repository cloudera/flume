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
package com.cloudera.flume.handlers.thrift;

import org.apache.thrift.transport.TMemoryBuffer;
import org.apache.thrift.transport.TTransportException;
import org.junit.Assert;
import org.junit.Test;

/**
 * This tests the TStatsTransport and verifies that counters are updated
 * properly.
 */
public class TestTStatsTransport {

  /**
   * Tests writes
   */
  @Test
  public void testTStatsWritten() throws TTransportException {
    TMemoryBuffer mb = new TMemoryBuffer(1 << 20); // 1 MB memory buffer
    TStatsTransport stats = new TStatsTransport(mb);

    stats.write(new byte[100]);
    Assert.assertEquals(stats.getBytesRead(), 0);
    Assert.assertEquals(stats.getBytesWritten(), 100);

    stats.write(new byte[42]);
    Assert.assertEquals(stats.getBytesRead(), 0);
    Assert.assertEquals(stats.getBytesWritten(), 142);
  }

  /**
   * Does a write to fill the buffer and then tests reads.
   */
  @Test
  public void testTStatsRead() throws TTransportException {
    TMemoryBuffer mb = new TMemoryBuffer(1 << 20); // 1 MB memory buffer
    TStatsTransport stats = new TStatsTransport(mb);

    stats.write(new byte[200]);

    stats.read(new byte[100], 0, 100);
    Assert.assertEquals(stats.getBytesRead(), 100);
    Assert.assertEquals(stats.getBytesWritten(), 200);

    stats.read(new byte[42], 0, 42);
    Assert.assertEquals(stats.getBytesRead(), 142);
    Assert.assertEquals(stats.getBytesWritten(), 200);

    // buffer can be under filled but says by how much
    int count = stats.read(new byte[100], 0, 100);
    Assert.assertEquals(58, count);
    Assert.assertEquals(stats.getBytesRead(), 200);
    Assert.assertEquals(stats.getBytesWritten(), 200);
  }

}
