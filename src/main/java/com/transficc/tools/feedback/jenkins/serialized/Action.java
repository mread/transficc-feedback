package com.transficc.tools.feedback.jenkins.serialized;

import java.util.Map;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings({"NP_UNWRITTEN_FIELD", "UWF_UNWRITTEN_FIELD"})
public class Action
{
    private Map<String, Branch> buildsByBranchName;

    public boolean hasData()
    {
        return buildsByBranchName != null;
    }

    public Branch get(final String name)
    {
        return buildsByBranchName.get(name);
    }
}
