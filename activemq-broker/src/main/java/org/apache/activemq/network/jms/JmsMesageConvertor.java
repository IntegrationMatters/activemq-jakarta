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
package org.apache.activemq.network.jms;

import jakarta.jms.Connection;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;

/**
 * Converts Message from one JMS to another
 */
public interface JmsMesageConvertor {

    /**
     * Convert a foreign JMS Message to a native ActiveMQ Message
     *
     * @param message
     *      The target message to convert to a native ActiveMQ message
     *
     * @return the converted message
     * @throws JMSException
     */
    Message convert(Message message) throws JMSException;

    /**
     * Convert a foreign JMS Message to a native ActiveMQ Message - Inbound or
     * visa-versa outbound.  If the replyTo Destination instance is not null
     * then the Message is configured with the given replyTo value.
     *
     * @param message
     *      The target message to convert to a native ActiveMQ message
     * @param replyTo
     *      The replyTo Destination to set on the converted Message.
     *
     * @return the converted message
     * @throws JMSException
     */
    Message convert(Message message, Destination replyTo) throws JMSException;

    void setConnection(Connection connection);

}
