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
package com.cloudera.flume.handlers.amqp;

import com.cloudera.flume.core.EventUtil;
import com.cloudera.flume.reporter.aggregator.CounterSink;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestAmqpEventSource {

  private ConnectionFactory connectionFactory;
  private Connection connection;
  private int numberOfMessagesToGenerate = 10;
  private MockChannel mockChannel;


  @Before
  public void setUp() throws IOException {
    connectionFactory = mock(ConnectionFactory.class);
    connection = mock(Connection.class);

    when(connectionFactory.newConnection()).thenReturn(connection);
  }

  @Test
  public void testNEventsDelivered() throws InterruptedException, IOException {
    // this will generate 10 messages for this channel
    mockChannel = new MockChannel(numberOfMessagesToGenerate);
    when(connection.createChannel()).thenReturn(mockChannel);

    final AmqpEventSource source = new AmqpEventSource(connectionFactory, "exchangeName", "queueName", "binding1");
    source.open();

    final CounterSink sink = new CounterSink("test");
    sink.open();
    Thread t = new Thread() {
      public void run() {
        try {
          // NOTE that we say specifically how many to dump
          EventUtil.dumpN(numberOfMessagesToGenerate, source, sink);
        } catch (IOException e) {
          fail(e.getMessage());
        }
      }
    };
    t.start();
    Thread.sleep(2000);

    source.close();
    sink.close();

    // closing the source will stop the consumer thread - give it 2 seconds to shutdown
    source.consumerThread.join(2000);

    assertEquals(numberOfMessagesToGenerate, sink.getCount());
    assertFalse(source.consumerThread.isAlive());
  }

  @Test
  public void testCloseWhileOnNext() throws IOException, InterruptedException {
    // this will generate 10 messages for this channel, but wait 3 seconds between messages
    mockChannel = new MockChannel(numberOfMessagesToGenerate, 3000);
    when(connection.createChannel()).thenReturn(mockChannel);

    final AmqpEventSource source = new AmqpEventSource(connectionFactory, "exchangeName", "queueName", "binding1");
    source.open();

    final CounterSink sink = new CounterSink("test");
    sink.open();
    Thread t = new Thread() {
      public void run() {
        try {
          // NOTE we dump ALL Of the messages
          EventUtil.dumpAll(source, sink);
        } catch (IOException e) {
          fail(e.getMessage());
        }
      }
    };
    t.start();

    Thread.sleep(5000);
    // closing the source closes down the connection
    source.close();

    // closing the source will stop the consumer thread - give it 2 seconds to shutdown
    source.consumerThread.join(2000);

    // there should be some events delivered
    assertTrue(sink.getCount() > 0);
    assertFalse(source.consumerThread.isAlive());
  }
}
