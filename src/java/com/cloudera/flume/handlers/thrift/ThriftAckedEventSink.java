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

import java.io.IOException;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.flume.conf.Context;
import com.cloudera.flume.conf.FlumeConfiguration;
import com.cloudera.flume.conf.SinkFactory.SinkBuilder;
import com.cloudera.flume.core.Event;
import com.cloudera.flume.core.EventImpl;
import com.cloudera.flume.core.EventSink;
import com.cloudera.flume.handlers.thrift.ThriftFlumeEventServer.Client;

/**
 * This is a sink that sends events to a remote host/port. The event append rpc
 * has a return value so this is stop-and-wait (now sliding window). This
 * however is the simplest way to get network backpressure that does not
 * overwhelm the receiver.
 */
public class ThriftAckedEventSink extends EventSink.Base {

  static final Logger LOG = LoggerFactory.getLogger(ThriftAckedEventSink.class);

  String host;
  int port;
  Client client;
  TTransport transport;
  boolean nonblocking;

  public ThriftAckedEventSink(String host, int port, boolean nonblocking) {
    this.host = host;
    this.port = port;
    this.nonblocking = nonblocking;
  }

  public ThriftAckedEventSink(String host, int port) {
    this(host, port, false);
  }

  @Override
  public void append(Event e) throws IOException, InterruptedException {
    ThriftFlumeEvent tfe = ThriftEventAdaptor.convert(e);

    try {
      EventStatus res = client.ackedAppend(tfe);
      super.append(e);
      if (res == EventStatus.ACK || res == EventStatus.COMMITED) {
        return;
      }
      throw new IOException(
          "Append return withs ERR status (received by dropped)");

    } catch (TException e1) {
      e1.printStackTrace();
      throw new IOException("Append failed " + e);
    }
  }

  @Override
  public void close() throws IOException {
    if (transport != null) {
      transport.close();
      transport = null;
      LOG.info("ThriftEventSink on port " + port + " closed");
    }
  }

  @Override
  public void open() throws IOException {

    try {
      if (nonblocking) {
        // non blocking must use "Framed transport"
        transport = new TSocket(host, port);
        transport = new TFramedTransport(transport);
      } else {
        transport = new TSocket(host, port);
      }

      TProtocol protocol = new TBinaryProtocol(transport);
      transport.open();
      client = new Client(protocol);
      LOG.info("ThriftEventSink open on port " + port + " opened");

    } catch (TTransportException e) {
      e.printStackTrace();
      throw new IOException("Failed to open thrift event sink at " + host + ":"
          + port + " : " + e);
    }
  }

  public static void main(String argv[]) {
    FlumeConfiguration conf = FlumeConfiguration.get();
    ThriftAckedEventSink sink = new ThriftAckedEventSink("localhost", conf
        .getCollectorPort());
    try {
      sink.open();

      for (int i = 0; i < 100; i++) {
        Event e = new EventImpl(("This is a test " + i).getBytes());
        sink.append(e);
        Thread.sleep(200);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }

  public static SinkBuilder builder() {
    return new SinkBuilder() {
      @Override
      public EventSink build(Context context, String... args) {
        if (args.length > 2) {
          throw new IllegalArgumentException(
              "usage: thrift([hostname, [portno]]) ");
        }
        String host = FlumeConfiguration.get().getCollectorHost();
        int port = FlumeConfiguration.get().getCollectorPort();
        if (args.length >= 1) {
          host = args[0];
        }

        if (args.length >= 2) {
          port = Integer.parseInt(args[1]);
        }

        return new ThriftAckedEventSink(host, port);
      }
    };
  }

}
