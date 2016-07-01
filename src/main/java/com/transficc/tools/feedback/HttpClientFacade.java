package com.transficc.tools.feedback;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.transficc.infrastructure.collections.Result;
import com.transficc.tools.feedback.util.SafeSerisalisation;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientFacade
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientFacade.class);
    private final HttpClient httpClient;
    private final SafeSerisalisation safeSerisalisation;

    public HttpClientFacade(final HttpClient httpClient, final SafeSerisalisation safeSerisalisation)
    {
        this.httpClient = httpClient;
        this.safeSerisalisation = safeSerisalisation;
    }

    public <T> Result<Integer, T> get(final String url, final Class<T> response)
    {
        final HttpGet request = new HttpGet(url);
        try
        {
            final HttpResponse httpResponse = httpClient.execute(request);
            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200)
            {
                return Result.success(safeSerisalisation.deserialise(new InputStreamReader(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8), response));
            }
            return Result.error(statusCode);
        }
        catch (final IOException e)
        {
            LOGGER.error("Error occurred making request", e);
            throw new RuntimeException(e);
        }
        finally
        {
            request.releaseConnection();
        }
    }
}
