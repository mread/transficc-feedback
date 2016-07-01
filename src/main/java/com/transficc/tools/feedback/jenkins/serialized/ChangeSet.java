package com.transficc.tools.feedback.jenkins.serialized;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class ChangeSet
{
    private Item[] items;

    @SuppressFBWarnings({"UWF_UNWRITTEN_FIELD", "EI_EXPOSE_REP"})
    public Item[] getItems()
    {
        return items;
    }
}
