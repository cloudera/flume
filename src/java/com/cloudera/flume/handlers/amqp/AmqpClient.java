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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownSignalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * This class provides access to an AMQP broker via a {@link com.rabbitmq.client.Channel}. Besides the
 * blocking {@link #getChannel()} method, it provides helper methods for closing down a connection to said
 * broker also.
 * <p/>
 * The {@link #getChannel()} method uses an exponential backoff algorithm for trying to connect to said broker. Note
 * that it will continue to try and get a connection unless the {@link #running} is set to false.
 */
abstract class AmqpClient {

  private static final Logger LOG = LoggerFactory.getLogger(AmqpClient.class);

  private static final int CONNECTION_RETRY_TIME = 1000;
  private static final int MAX_RETRY_TIME = CONNECTION_RETRY_TIME * 60;

  private final ConnectionFactory connectionFactory;

  /**
   * True if the client is in a running state. This is used in the {@link #getChannel()} method to determine whether
   * it should continue trying to create a channel or stop.
   */
  private volatile boolean running;

  protected AmqpClient() {
    this(new ConnectionFactory());
  }

  protected AmqpClient(String host, int port) {
    connectionFactory = new ConnectionFactory();
    connectionFactory.setHost(host);
    connectionFactory.setPort(port);
  }

  protected AmqpClient(String host, int port, String virtualHost, String username, String password) {
    connectionFactory = new ConnectionFactory();
    connectionFactory.setHost(host);
    connectionFactory.setPort(port);
    connectionFactory.setVirtualHost(virtualHost);
    connectionFactory.setUsername(username);
    connectionFactory.setPassword(password);
  }

  protected AmqpClient(ConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  protected void setRunning(boolean running) {
    if(this.running != running) {

      LOG.info("Setting running to {}", running);
      this.running = running;

      synchronized (this) {
        notify();
      }
    }
  }

  /**
   * This flag can be set to false when you want to client to be shut down. Note that it is volatile
   * since it is accessed by multiple threads.
   *
   * @return true if the client is running
   */
  protected boolean isRunning() {
    return running;
  }

  /**
   * This method will block until a connection and channel can be established to the AMQP broker as specified
   * by the {@link #connectionFactory}.
   *
   * @return Channel to the broker
   * @throws InterruptedException if any thread has interrupted the current thread.
   */
  protected Channel getChannel() throws InterruptedException {
    Channel channel = null;
    Thread thread = Thread.currentThread();
    int numberTimesConnectionLost = 0;

    while (channel == null && isRunning() && !thread.isInterrupted()) {
      Connection conn = null;
      try {
        LOG.info("Connecting to broker at {}...",connectionFactory.getHost());
        conn = connectionFactory.newConnection();
        LOG.info("Connected to broker at {}", connectionFactory.getHost());

        channel = conn.createChannel();
        // reset backoff time
        numberTimesConnectionLost = 0;
      } catch (IOException e) {
        LOG.info("IOException caught. Closing connection to broker and waiting to reconnect", e);
        closeConnectionSilently(conn);

        numberTimesConnectionLost++;
        waitToRetryConnection(numberTimesConnectionLost);
      }
    }

    if (thread.isInterrupted()) {
      throw new InterruptedException();
    }

    return channel;
  }

  private void waitToRetryConnection(int numberTimesConnectionLost) throws InterruptedException {
    long backOffTime = (long) (numberTimesConnectionLost * 2) * CONNECTION_RETRY_TIME;
    // limit the possible wait time to MAX_RETRY_TIME
    long waitTime = backOffTime > MAX_RETRY_TIME ? MAX_RETRY_TIME : backOffTime;

    LOG.debug("Waiting {} milliseconds before re-connect to broker...", waitTime);

    synchronized (this) {
      wait(waitTime);
    }
  }

  /**
   * Tries to close the channel ignoring any {@link java.io.IOException}s that may occur while doing so.
   *
   * @param channel channel to close
   */
  protected void closeChannelSilently(Channel channel) {
    if (channel != null) {
      try {
        channel.close();
      } catch (IOException e) {
        LOG.warn("Problem closing down channel", e);
      } catch (ShutdownSignalException e) {
        // we can ignore this since we are shutting down
        LOG.debug("Got a shutdown signal while closing channel", e);
      }
      closeConnectionSilently(channel.getConnection());
    }
  }

  private void closeConnectionSilently(Connection connection) {
    if (connection != null) {
      try {
        connection.close();
      } catch (IOException e) {
        LOG.warn("Problem closing down connection", e);
      } catch (ShutdownSignalException e) {
        // we can ignore this since we are shutting down
        LOG.debug("Got a shutdown signal while closing connection", e);
      }
    }
  }
}
