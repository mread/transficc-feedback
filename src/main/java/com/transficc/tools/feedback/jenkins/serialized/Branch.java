package com.transficc.tools.feedback.jenkins.serialized;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings({"NP_UNWRITTEN_FIELD", "UWF_UNWRITTEN_FIELD"})
public class Branch
{
    private Revision revision;

    public String getRevision()
    {
        return revision.getSha1();
    }
}
