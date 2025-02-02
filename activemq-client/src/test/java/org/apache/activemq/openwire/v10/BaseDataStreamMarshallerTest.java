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
package org.apache.activemq.openwire.v10;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import jakarta.jms.MessageFormatException;

import org.junit.Test;

public class BaseDataStreamMarshallerTest {

    private static final int MAX_MESSAGE_LENGTH = 1024;

    @Test
    public void cutMessageIfNeeded() throws MessageFormatException {
        char[] message = new char[2056];
        Arrays.fill(message, '1');
        String cutMessage = (new WireFormatInfoMarshaller()).cutMessageIfNeeded(String.valueOf(message));
        assertEquals("Expected length " + MAX_MESSAGE_LENGTH, MAX_MESSAGE_LENGTH, cutMessage.length());
        assertTrue("Expected message tail ...", cutMessage.endsWith("..."));
    }
}
