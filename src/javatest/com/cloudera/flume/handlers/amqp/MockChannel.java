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

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Map;

/**
 * Mock implementation of an AMQP Channel that supports generation of a certain number of messages to a
 * consumer.
 * <p/>
 * Note that we only implement the basicConsume methods to keep track of the {@link com.rabbitmq.client.Consumer}.
 */
public class MockChannel implements Channel {

  private volatile int numberOfMessagesToGenerate;
  private volatile long millsBetweenMessages;
  private static final String CONSUMER_TAG = "tag";
  private volatile CallbackThread callbackThread;

  public MockChannel(int numberOfMessagesToGenerate) {
    this.numberOfMessagesToGenerate = numberOfMessagesToGenerate;
  }

  public MockChannel(int numberOfMessagesToGenerate, long millsBetweenMessages) {
    this.numberOfMessagesToGenerate = numberOfMessagesToGenerate;
    this.millsBetweenMessages = millsBetweenMessages;
  }

  public int getChannelNumber() {
    return 0;
  }

  public Connection getConnection() {
    return null;
  }

  public void close() throws IOException {
    if (callbackThread != null) {
      callbackThread.close();
    }
  }

  public void close(int closeCode, String closeMessage) throws IOException {
    if (callbackThread != null) {
      callbackThread.close();
    }
  }

  public AMQP.Channel.FlowOk flow(boolean active) throws IOException {
    return null;
  }

  public AMQP.Channel.FlowOk getFlow() {
    return null;
  }

  public void abort() throws IOException {

  }

  public void abort(int closeCode, String closeMessage) throws IOException {

  }

  public ReturnListener getReturnListener() {
    return null;
  }

  public void setReturnListener(ReturnListener listener) {

  }

  public FlowListener getFlowListener() {
    return null;
  }

  public void setFlowListener(FlowListener flowListener) {

  }

  public Consumer getDefaultConsumer() {
    return null;
  }

  public void setDefaultConsumer(Consumer consumer) {

  }

  public void basicQos(int prefetchSize, int prefetchCount, boolean global) throws IOException {

  }

  public void basicQos(int prefetchCount) throws IOException {

  }

  public void basicPublish(String exchange, String routingKey, AMQP.BasicProperties props, byte[] body) throws IOException {

  }

  public void basicPublish(String exchange, String routingKey, boolean mandatory, boolean immediate, AMQP.BasicProperties props, byte[] body) throws IOException {

  }

  public AMQP.Exchange.DeclareOk exchangeDeclare(String exchange, String type) throws IOException {
    return null;
  }

  public AMQP.Exchange.DeclareOk exchangeDeclare(String exchange, String type, boolean durable) throws IOException {
    return null;
  }

  public AMQP.Exchange.DeclareOk exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, Map<String, Object> arguments) throws IOException {
    return null;
  }

  public AMQP.Exchange.DeclareOk exchangeDeclarePassive(String name) throws IOException {
    return null;
  }

  public AMQP.Exchange.DeleteOk exchangeDelete(String exchange, boolean ifUnused) throws IOException {
    return null;
  }

  public AMQP.Exchange.DeleteOk exchangeDelete(String exchange) throws IOException {
    return null;
  }

  public AMQP.Queue.DeclareOk queueDeclare() throws IOException {
    return null;
  }

  public AMQP.Queue.DeclareOk queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments) throws IOException {
    return null;
  }

  public AMQP.Queue.DeclareOk queueDeclarePassive(String queue) throws IOException {
    return null;
  }

  public AMQP.Queue.DeleteOk queueDelete(String queue) throws IOException {
    return null;
  }

  public AMQP.Queue.DeleteOk queueDelete(String queue, boolean ifUnused, boolean ifEmpty) throws IOException {
    return null;
  }

  public AMQP.Queue.BindOk queueBind(String queue, String exchange, String routingKey) throws IOException {
    return null;
  }

  public AMQP.Queue.BindOk queueBind(String queue, String exchange, String routingKey, Map<String, Object> arguments) throws IOException {
    return null;
  }

  public AMQP.Queue.UnbindOk queueUnbind(String queue, String exchange, String routingKey) throws IOException {
    return null;
  }

  public AMQP.Queue.UnbindOk queueUnbind(String queue, String exchange, String routingKey, Map<String, Object> arguments) throws IOException {
    return null;
  }

  public AMQP.Queue.PurgeOk queuePurge(String queue) throws IOException {
    return null;
  }

  public AMQP.Queue.PurgeOk queuePurge(String queue, boolean nowait) throws IOException {
    return null;
  }

  public GetResponse basicGet(String queue, boolean noAck) throws IOException {
    return null;
  }

  public void basicAck(long deliveryTag, boolean multiple) throws IOException {

  }

  public void basicReject(long l, boolean b) throws IOException {

  }

  public void basicCancel(String consumerTag) throws IOException {

  }

  public AMQP.Basic.RecoverOk basicRecover(boolean b) throws IOException {
    return null;
  }

  public void basicRecoverAsync(boolean requeue) throws IOException {

  }

  public AMQP.Tx.SelectOk txSelect() throws IOException {
    return null;
  }

  public AMQP.Tx.CommitOk txCommit() throws IOException {
    return null;
  }

  public AMQP.Tx.RollbackOk txRollback() throws IOException {
    return null;
  }

  public void addShutdownListener(ShutdownListener listener) {

  }

  public void removeShutdownListener(ShutdownListener listener) {

  }

  public ShutdownSignalException getCloseReason() {
    return null;
  }

  public void notifyListeners() {

  }

  public boolean isOpen() {
    return false;
  }

  public String basicConsume(String queue, Consumer callback) throws IOException {
    callbackThread = new CallbackThread(callback, numberOfMessagesToGenerate, millsBetweenMessages);
    callbackThread.start();
    return CONSUMER_TAG;
  }

  public String basicConsume(String queue, boolean noAck, Consumer callback) throws IOException {
    callbackThread = new CallbackThread(callback, numberOfMessagesToGenerate, millsBetweenMessages);
    callbackThread.start();
    return CONSUMER_TAG;
  }

  public String basicConsume(String queue, boolean noAck, String consumerTag, Consumer callback) throws IOException {
    callbackThread = new CallbackThread(callback, numberOfMessagesToGenerate, millsBetweenMessages);
    callbackThread.start();
    return CONSUMER_TAG;
  }

  public String basicConsume(String queue, boolean noAck, String consumerTag, boolean noLocal, boolean exclusive, Map<String, Object> stringObjectMap, Consumer callback) throws IOException {
    callbackThread = new CallbackThread(callback, numberOfMessagesToGenerate, millsBetweenMessages);
    callbackThread.start();
    return CONSUMER_TAG;
  }

  public String basicConsume(String queue, boolean noAck, String consumerTag, boolean noLocal, boolean exclusive, Consumer callback) throws IOException {
    callbackThread = new CallbackThread(callback, numberOfMessagesToGenerate, millsBetweenMessages);
    callbackThread.start();
    return CONSUMER_TAG;
  }

  public static class CallbackThread extends Thread {
    private final int numberOfMessagesToGenerate;
    private final long millsBetweenMessages;
    private final Consumer consumer;

    private CallbackThread(Consumer consumer, int numberOfMessagesToGenerate, long millsBetweenMessages) {
      this.consumer = consumer;
      this.numberOfMessagesToGenerate = numberOfMessagesToGenerate;
      this.millsBetweenMessages = millsBetweenMessages;
    }

    public void run() {

      for (int i = 0; i < numberOfMessagesToGenerate; ++i) {
        // long deliveryTag, boolean redeliver, String exchange, String routingKey
        Envelope envelope = new Envelope(0l, false, "exchangeName", "routingKey");
        try {
          consumer.handleDelivery(CONSUMER_TAG, envelope, new AMQP.BasicProperties(), ("Message" + i).getBytes());
        } catch (IOException e) {
          e.printStackTrace();
        }

        try {
          sleep(millsBetweenMessages);
        } catch (InterruptedException e) {
          break;
        }
      }
    }

    void close() {
      consumer.handleShutdownSignal("shutdown", new ShutdownSignalException(true, false, "mocked channel closed", null));
    }
  }
}
