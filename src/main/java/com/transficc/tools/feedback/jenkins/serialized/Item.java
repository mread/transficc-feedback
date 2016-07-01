package com.transficc.tools.feedback.jenkins.serialized;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
public class Item
{
    private String id;
    private String comment;

    public String getId()
    {
        return id;
    }

    public String getComment()
    {
        return comment;
    }
}
