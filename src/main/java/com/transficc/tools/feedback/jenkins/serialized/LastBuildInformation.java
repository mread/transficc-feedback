package com.transficc.tools.feedback.jenkins.serialized;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
public final class LastBuildInformation
{
    private int number;
    private String url;

    public int getNumber()
    {
        return number;
    }

    public String getUrl()
    {
        return url;
    }
}
