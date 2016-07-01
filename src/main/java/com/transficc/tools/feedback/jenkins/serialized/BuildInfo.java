package com.transficc.tools.feedback.jenkins.serialized;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class BuildInfo
{
    private long estimatedDuration;
    private String url;
    private long timestamp;
    private boolean building;
    private ChangeSet changeSet;
    private Action[] actions;

    public long getEstimatedDuration()
    {
        return estimatedDuration;
    }

    public String getUrl()
    {
        return url;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public boolean isBuilding()
    {
        return building;
    }

    public ChangeSet getChangeSet()
    {
        return changeSet;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Action[] getActions()
    {
        return actions;
    }
}
