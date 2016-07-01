package com.transficc.tools.feedback.util;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SafeSerisalisation
{
    private final ObjectMapper objectMapper;

    public SafeSerisalisation(final ObjectMapper objectMapper)
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
