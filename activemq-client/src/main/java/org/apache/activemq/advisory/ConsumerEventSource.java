/*
 * Copyright (c) 2023.  Integration Matters GmbH
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.activemq.advisory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.jms.Connection;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.Session;

import org.apache.activemq.ActiveMQMessageConsumer;
import org.apache.activemq.Service;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.command.ConsumerId;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.command.RemoveInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An object which can be used to listen to the number of active consumers
 * available on a given destination.
 * 
 * 
 */
public class ConsumerEventSource implements Service, MessageListener {
    private static final Logger LOG = LoggerFactory.getLogger(ConsumerEventSource.class);

    private final Connection connection;
    private final ActiveMQDestination destination;
    private ConsumerListener listener;
    private AtomicBoolean started = new AtomicBoolean(false);
    private AtomicInteger consumerCount = new AtomicInteger();
    private Session session;
    private ActiveMQMessageConsumer consumer;

    public ConsumerEventSource(Connection connection, Destination destination) throws JMSException {
        this.connection = connection;
        this.destination = ActiveMQDestination.transform(destination);
    }

    public void setConsumerListener(ConsumerListener listener) {
        this.listener = listener;
    }
    
    public String getConsumerId() {
        return consumer != null ? consumer.getConsumerId().toString() : "NOT_SET";
    }

    public void start() throws Exception {
        if (started.compareAndSet(false, true)) {
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            ActiveMQTopic advisoryTopic = AdvisorySupport.getConsumerAdvisoryTopic(destination);
            consumer = (ActiveMQMessageConsumer) session.createConsumer(advisoryTopic);
            consumer.setMessageListener(this);
        }
    }

    public void stop() throws Exception {
        if (started.compareAndSet(true, false)) {
            if (session != null) {
                session.close();
            }
        }
    }

    public void onMessage(Message message) {
        if (message instanceof ActiveMQMessage) {
            ActiveMQMessage activeMessage = (ActiveMQMessage)message;
            Object command = activeMessage.getDataStructure();
            int count = 0;
            if (command instanceof ConsumerInfo) {
                count = consumerCount.incrementAndGet();
                count = extractConsumerCountFromMessage(message, count);
                fireConsumerEvent(new ConsumerStartedEvent(this, destination, (ConsumerInfo)command, count));
            } else if (command instanceof RemoveInfo) {
                RemoveInfo removeInfo = (RemoveInfo)command;
                if (removeInfo.isConsumerRemove()) {
                    count = consumerCount.decrementAndGet();
                    count = extractConsumerCountFromMessage(message, count);
                    fireConsumerEvent(new ConsumerStoppedEvent(this, destination, (ConsumerId)removeInfo.getObjectId(), count));
                }
            } else {
                LOG.warn("Unknown command: " + command);
            }
        } else {
            LOG.warn("Unknown message type: " + message + ". Message ignored");
        }
    }

    /**
     * Lets rely by default on the broker telling us what the consumer count is
     * as it can ensure that we are up to date at all times and have not
     * received messages out of order etc.
     */
    protected int extractConsumerCountFromMessage(Message message, int count) {
        try {
            Object value = message.getObjectProperty("consumerCount");
            if (value instanceof Number) {
                Number n = (Number)value;
                return n.intValue();
            }
            LOG.warn("No consumerCount header available on the message: " + message);
        } catch (Exception e) {
            LOG.warn("Failed to extract consumerCount from message: " + message + ".Reason: " + e, e);
        }
        return count;
    }

    protected void fireConsumerEvent(ConsumerEvent event) {
        if (listener != null) {
            listener.onConsumerEvent(event);
        }
    }

}
