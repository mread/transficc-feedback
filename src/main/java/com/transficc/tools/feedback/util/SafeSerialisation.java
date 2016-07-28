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

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SafeSerialisation
{
    private final ObjectMapper objectMapper;

    public SafeSerialisation(final ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    public <T> T deserialise(final String json, final Class<T> clazz)
    {
        try
        {
            return objectMapper.readValue(json, clazz);
        }
        catch (final IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    public <T> T deserialise(final Reader json, final Class<T> clazz)
    {
        try
        {
            return objectMapper.readValue(json, clazz);
        }
        catch (final IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    public <T> String serisalise(final T value)
    {
        try
        {
            return objectMapper.writeValueAsString(value);
        }
        catch (final JsonProcessingException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}
