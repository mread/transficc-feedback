package com.transficc.tools.feedback.jenkins;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

class Build
{
    private final String revision;
    private final boolean building;
    private final String[] comments;
    private final long timestamp;
    private final long estimatedDuration;

    Build(final String revision, final String[] comments, final long timestamp, final long estimatedDuration, final boolean building)
    {
        this.revision = revision;
        this.building = building;
        this.comments = comments.clone();
        this.timestamp = timestamp;
        this.estimatedDuration = estimatedDuration;
    }

    public String getRevision()
    {
        return revision;
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "The build object does not escape the lambda it is created in; comments has already been cloned so is safe.")
    public String[] getComments()
    {
        return comments;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public long getEstimatedDuration()
    {
        return estimatedDuration;
    }

    public boolean isBuilding()
    {
        return building;
    }
}
