/*
 * Copyright 2016 TransFICC Ltd.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.  The ASF licenses this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the specific language governing permissions and limitations under the License.
 */
package com.transficc.tools.feedback.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingThreadFactory implements ThreadFactory
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingThreadFactory.class);
    private final LoggingThreadFactory.LoggingUncaughtExceptionHandler uncaughtExceptionHandler = new LoggingThreadFactory.LoggingUncaughtExceptionHandler();
    private final AtomicInteger threadCount = new AtomicInteger();
    private final String threadNamePrefix;
    private final int maxThreads;

    public LoggingThreadFactory(final String threadNamePrefix)
    {
        this.threadNamePrefix = threadNamePrefix;
        this.maxThreads = Integer.MAX_VALUE;
    }

    @Override
    public Thread newThread(final Runnable runnable)
    {
        if (threadCount.incrementAndGet() > maxThreads)
        {
            throw new IllegalStateException("This thread factory allows only " + maxThreads + " thread.");
        }
        final Thread thread = new Thread(runnable);
        thread.setName(threadNamePrefix + "-thread-" + threadCount.get());
        thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        return thread;
    }

    private static class LoggingUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler
    {
        @Override
        public void uncaughtException(final Thread t, final Throwable e)
        {
            LOGGER.error("ExceptionCaught: ThreadId: " + t.getId(), e);
        }
    }
}
