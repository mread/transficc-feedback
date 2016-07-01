package com.transficc.tools.feedback.jenkins.serialized;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Revision
{
    @JsonProperty("SHA1")
    private String sha1;

    public String getSha1()
    {
        return sha1;
    }
}
