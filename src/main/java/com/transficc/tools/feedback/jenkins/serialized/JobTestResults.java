package com.transficc.tools.feedback.jenkins.serialized;

import com.transficc.tools.feedback.JobsTestResults;

public class JobTestResults
{
    private int passCount;
    private int failCount;
    private int skipCount;
    private double duration;

    public JobsTestResults convert()
    {
        return new JobsTestResults(passCount, failCount, skipCount, duration);
    }
}
