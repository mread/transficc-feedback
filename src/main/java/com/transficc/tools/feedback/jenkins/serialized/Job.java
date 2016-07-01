package com.transficc.tools.feedback.jenkins.serialized;

public class Job
{
    private String name;
    private String url;
    private String color;
    private LastBuildInformation lastBuild;

    public String getName()
    {
        return name;
    }

    public String getUrl()
    {
        return url;
    }

    public String getColor()
    {
        return color;
    }

    public LastBuildInformation getLastBuild()
    {
        return lastBuild;
    }
}
