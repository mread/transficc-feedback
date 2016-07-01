package com.transficc.tools.feedback;

import java.lang.reflect.Field;

public final class MessageBuilder
{
    private final Object instance;

    public MessageBuilder(final Class clazz)
    {
        try
        {
            this.instance = clazz.newInstance();
        }
        catch (final IllegalAccessException | InstantiationException e)
        {
            throw new RuntimeException(e);
        }
    }

    public MessageBuilder setField(final String field, final Object value)
    {
        try
        {
            final Field actualField = instance.getClass().getDeclaredField(field);
            final boolean accessible = actualField.isAccessible();
            actualField.setAccessible(true);
            actualField.set(instance, value);
            actualField.setAccessible(accessible);
            return this;
        }
        catch (final NoSuchFieldException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"unchecked", "TypeParameterUnusedInFormals"})
    public <T> T build()
    {
        return (T)instance;
    }
}
