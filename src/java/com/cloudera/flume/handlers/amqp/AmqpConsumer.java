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

import com.cloudera.flume.core.Event;
import com.cloudera.flume.core.EventImpl;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Subclasses of {@link com.cloudera.flume.handlers.amqp.AmqpClient} that will bind to a queue and collect
 * messages from said queue and create {@link Event}s from them. These events are made available via the
 * blocking method {@link #getNextEvent(long, java.util.concurrent.TimeUnit)} ()}.
 * <p/>
 * The cancellation/shutdown policy for the consumer is to either interrupt the thread, or set the
 * {@link #running} flag to false.
 *
 * @see com.cloudera.flume.handlers.amqp.AmqpClient
 */
class AmqpConsumer extends AmqpClient implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(AmqpConsumer.class);

  private static final String DIRECT_EXCHANGE = "direct";
  static final String DEFAULT_EXCHANGE_TYPE = DIRECT_EXCHANGE;

  /**
   * Exchange level properties
   */
  private final String exchangeName;
  private String exchangeType = DEFAULT_EXCHANGE_TYPE;
  /**
   * true if we are declaring a durable exchange (the exchange will survive a server restart)
   */
  private boolean durableExchange;


  /**
   * Queue level properties
   */

  /**
   * if left unspecified, the server chooses a name and provides this to the client. Generally, when
   * applications share a message queue they agree on a message queue name beforehand, and when an
   * application needs a message queue for its own purposes, it lets the server provide a name.
   */
  private String queueName;
  /**
   * if set, the message queue remains present and active when the server restarts. It may lose transient
   * messages if the server restarts.
   */
  private boolean durable;
  /**
   * if set, the queue belongs to the current connection only, and is deleted when the connection closes.
   */
  private boolean exclusive;
  /**
   * true if we are declaring an autodelete queue (server will delete it when no longer in use)
   */
  private boolean autoDelete;
  private final String [] bindings;

  private Channel channel;

  private final BlockingQueue<Event> events = new LinkedBlockingQueue<Event>();

  public AmqpConsumer(String host, int port, String virutalHost, String userName, String password,
                      String exchangeName, String exchangeType, boolean durableExchange,
                      String queueName, boolean durable, boolean exclusive, boolean autoDelete, String...bindings) {
    super(host, port, virutalHost, userName, password);

    this.exchangeName = exchangeName;
    this.exchangeType = exchangeType;
    this.durableExchange = durableExchange;
    this.queueName = queueName;
    this.durable = durable;
    this.exclusive = exclusive;
    this.autoDelete = autoDelete;
    this.bindings = bindings;
  }

  public AmqpConsumer(ConnectionFactory connectionFactory, String exchangeName, String queueName, String...bindings) {
    super(connectionFactory);

    this.exchangeName = exchangeName;
    this.queueName = queueName;
    this.bindings = bindings;
  }

  public Event getNextEvent(long timeout, TimeUnit unit) throws InterruptedException {
    return events.poll(timeout, unit);
  }

  @Override
  public void run() {
    setRunning(true);

    try {
      runConsumeLoop();
    } finally {
      closeChannelSilently(channel);
    }

    LOG.info("AMQP Consumer has shut down successfully.");
  }

  /**
   * Main run loop for consuming messages
   */
  private void runConsumeLoop() {
    QueueingConsumer consumer = null;

    Thread currentThread = Thread.currentThread();

    while (isRunning() && !currentThread.isInterrupted()) {

      try {
        if (channel == null) {
          // this will block until a channel is established or we are told to shutdown
          channel = getChannel();

          if (channel == null) {
            // someone set the running flag to false
            break;
          }
          // setup exchange, queue and binding
          channel.exchangeDeclare(exchangeName, exchangeType, durableExchange);
          // named queue?
          if (queueName == null) {
            queueName = channel.queueDeclare().getQueue();

          } else {
            channel.queueDeclare(queueName, durable, exclusive, autoDelete, null);
          }

          if(bindings != null) {
            // multiple bindings
            for(String binding : bindings) {
              channel.queueBind(queueName, exchangeName, binding);
            }
          } else {
            // no binding given - this could be the case if it is a fanout exchange
            channel.queueBind(queueName, exchangeName, "");
          }


          consumer = new QueueingConsumer(channel);
          boolean noAck = false;
          String consumerTag = channel.basicConsume(queueName, noAck, consumer);
          LOG.info("Starting new consumer. Server generated {} as consumerTag", consumerTag);
        }

        QueueingConsumer.Delivery delivery = consumer.nextDelivery();

        Event event = new EventImpl(delivery.getBody());

        // add to queue
        events.add(event);

        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
      } catch (InterruptedException e) {
        LOG.info("Consumer Thread was interrupted, shutting down...");
        setRunning(false);

      } catch (IOException e) {
        LOG.info("IOException caught in Consumer Thread. Closing channel and waiting to reconnect", e);
        closeChannelSilently(channel);
        channel = null;

      } catch (ShutdownSignalException e) {
        LOG.info("Consumer Thread caught ShutdownSignalException, shutting down...");
        setRunning(false);
      }
    }
  }
}
