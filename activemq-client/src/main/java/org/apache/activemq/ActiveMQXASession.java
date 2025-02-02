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

package org.apache.activemq;

import jakarta.jms.JMSException;
import jakarta.jms.QueueSession;
import jakarta.jms.Session;
import jakarta.jms.TopicSession;
import jakarta.jms.TransactionInProgressException;
import jakarta.jms.XAQueueSession;
import jakarta.jms.XATopicSession;
import javax.transaction.xa.XAResource;

import org.apache.activemq.command.SessionId;

/**
 * The XASession interface extends the capability of Session by adding access
 * to a JMS provider's support for the  Java Transaction API (JTA) (optional).
 * This support takes the form of a javax.transaction.xa.XAResource object.
 * The functionality of this object closely resembles that defined by the
 * standard X/Open XA Resource interface.
 * <p/>
 * An application server controls the transactional assignment of an XASession
 * by obtaining its XAResource. It uses the XAResource to assign the session
 * to a transaction, prepare and commit work on the transaction, and so on.
 * <p/>
 * An XAResource provides some fairly sophisticated facilities for
 * interleaving work on multiple transactions, recovering a list of
 * transactions in progress, and so on. A JTA aware JMS provider must fully
 * implement this functionality. This could be done by using the services of a
 * database that supports XA, or a JMS provider may choose to implement this
 * functionality from scratch.
 * <p/>
 * A client of the application server is given what it thinks is a regular
 * JMS Session. Behind the scenes, the application server controls the
 * transaction management of the underlying XASession.
 * <p/>
 * The XASession interface is optional. JMS providers are not required to
 * support this interface. This interface is for use by JMS providers to
 * support transactional environments. Client programs are strongly encouraged
 * to use the transactional support  available in their environment, rather
 * than use these XA  interfaces directly.
 *
 * 
 * @see jakarta.jms.Session
 * @see jakarta.jms.QueueSession
 * @see jakarta.jms.TopicSession
 * @see jakarta.jms.XASession
 */
public class ActiveMQXASession extends ActiveMQSession implements QueueSession, TopicSession, XAQueueSession, XATopicSession {

    public ActiveMQXASession(ActiveMQXAConnection connection, SessionId sessionId, int theAcknowlegeMode, boolean dispatchAsync) throws JMSException {
        super(connection, sessionId, theAcknowlegeMode, dispatchAsync);
    }

    public void rollback() throws JMSException {
        checkClosed();
        throw new TransactionInProgressException("Cannot rollback() inside an XASession");
    }

    public void commit() throws JMSException {
        checkClosed();
        throw new TransactionInProgressException("Cannot commit() inside an XASession");
    }

    public Session getSession() throws JMSException {
        return this;
    }

    public XAResource getXAResource() {
        return getTransactionContext();
    }

    public QueueSession getQueueSession() throws JMSException {
        return new ActiveMQQueueSession(this);
    }

    public TopicSession getTopicSession() throws JMSException {
        return new ActiveMQTopicSession(this);
    }

    protected void doStartTransaction() throws JMSException {
        if (acknowledgementMode != SESSION_TRANSACTED) {
            // ok once the factory XaAckMode has been explicitly set to allow use outside an XA tx
        } else if (!getTransactionContext().isInXATransaction()) {
            throw new JMSException("Session's XAResource has not been enlisted in a distributed transaction.");
        }
    }

}
