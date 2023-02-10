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
package org.apache.activemq.blob;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import jakarta.jms.JMSException;

import org.apache.activemq.command.ActiveMQBlobMessage;

/**
 * Represents a strategy of uploading a file/stream to some remote
 *
 * 
 */
public interface BlobUploadStrategy {

    URL uploadFile(ActiveMQBlobMessage message, File file) throws JMSException, IOException;

    URL uploadStream(ActiveMQBlobMessage message, InputStream in) throws JMSException, IOException;
}
