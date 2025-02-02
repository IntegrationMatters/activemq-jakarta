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
package org.apache.activemq.command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import jakarta.jms.JMSException;
import jakarta.jms.MessageNotWriteableException;
import jakarta.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.util.ByteArrayInputStream;
import org.apache.activemq.util.ByteArrayOutputStream;
import org.apache.activemq.util.ByteSequence;
import org.apache.activemq.util.JMSExceptionSupport;
import org.apache.activemq.util.MarshallingSupport;
import org.apache.activemq.wireformat.WireFormat;

/**
 * @openwire:marshaller code="28"
 *
 */
public class ActiveMQTextMessage extends ActiveMQMessage implements TextMessage {

    public static final byte DATA_STRUCTURE_TYPE = CommandTypes.ACTIVEMQ_TEXT_MESSAGE;

    protected String text;

    @Override
    public Message copy() {
        ActiveMQTextMessage copy = new ActiveMQTextMessage();
        copy(copy);
        return copy;
    }

    private void copy(ActiveMQTextMessage copy) {
        super.copy(copy);
        copy.text = text;
    }

    @Override
    public byte getDataStructureType() {
        return DATA_STRUCTURE_TYPE;
    }

    @Override
    public String getJMSXMimeType() {
        return "jms/text-message";
    }

    @Override
    public void setText(String text) throws MessageNotWriteableException {
        checkReadOnlyBody();
        this.text = text;
        setContent(null);
    }

    @Override
    public String getText() throws JMSException {
        ByteSequence content = getContent();

        if (text == null && content != null) {
            text = decodeContent(content);
            setContent(null);
            setCompressed(false);
        }
        return text;
    }

    private String decodeContent(ByteSequence bodyAsBytes) throws JMSException {
        String text = null;
        if (bodyAsBytes != null) {
            InputStream is = null;
            try {
                is = new ByteArrayInputStream(bodyAsBytes);
                if (isCompressed()) {
                    is = new InflaterInputStream(is);
                }
                DataInputStream dataIn = new DataInputStream(is);
                text = MarshallingSupport.readUTF8(dataIn);
                dataIn.close();
            } catch (IOException ioe) {
                throw JMSExceptionSupport.create(ioe);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
        return text;
    }

    @Override
    public void beforeMarshall(WireFormat wireFormat) throws IOException {
        super.beforeMarshall(wireFormat);
        storeContentAndClear();
    }

    @Override
    public void storeContentAndClear() {
        storeContent();
        text=null;
    }

    @Override
    public void storeContent() {
        try {
            ByteSequence content = getContent();
            String text = this.text;
            if (content == null && text != null) {
                ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
                OutputStream os = bytesOut;
                ActiveMQConnection connection = getConnection();
                if (connection != null && connection.isUseCompression()) {
                    compressed = true;
                    os = new DeflaterOutputStream(os);
                }
                DataOutputStream dataOut = new DataOutputStream(os);
                MarshallingSupport.writeUTF8(dataOut, text);
                dataOut.close();
                setContent(bytesOut.toByteSequence());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // see https://issues.apache.org/activemq/browse/AMQ-2103
    // and https://issues.apache.org/activemq/browse/AMQ-2966
    @Override
    public void clearUnMarshalledState() throws JMSException {
        super.clearUnMarshalledState();
        this.text = null;
    }

    @Override
    public boolean isContentMarshalled() {
        return content != null || text == null;
    }

    /**
     * Clears out the message body. Clearing a message's body does not clear its
     * header values or property entries. <p/>
     * <P>
     * If this message body was read-only, calling this method leaves the
     * message body in the same state as an empty body in a newly created
     * message.
     *
     * @throws JMSException if the JMS provider fails to clear the message body
     *                 due to some internal error.
     */
    @Override
    public void clearBody() throws JMSException {
        super.clearBody();
        this.text = null;
    }

    @Override
    public int getSize() {
        String text = this.text;
        if (size == 0 && content == null && text != null) {
            size = getMinimumMessageSize();
            if (marshalledProperties != null) {
                size += marshalledProperties.getLength();
            }
            size += text.length() * 2;
        }
        return super.getSize();
    }

    @Override
    public String toString() {
        try {
            String text = this.text;
            if( text == null ) {
                text = decodeContent(getContent());
            }
            if (text != null) {
                text = MarshallingSupport.truncate64(text);
                HashMap<String, Object> overrideFields = new HashMap<String, Object>();
                overrideFields.put("text", text);
                return super.toString(overrideFields);
            }
        } catch (JMSException e) {
        }
        return super.toString();
    }
}
