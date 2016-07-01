package com.transficc.tools.feedback;

public class JobsTestResults
{
    private final int passCount;
    private final int failCount;
    private final int skipCount;
    private final double duration;

    public JobsTestResults(final int passCount, final int failCount, final int skipCount, final double duration)
    {
        this.passCount = passCount;
        this.failCount = failCount;
        this.skipCount = skipCount;
        this.duration = duration;
    }

    @Override
    public String toString()
    {
        return "TestResults{" +
               "passCount=" + passCount +
               ", failCount=" + failCount +
               ", skipCount=" + skipCount +
               ", duration=" + duration +
               '}';
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final JobsTestResults that = (JobsTestResults)o;

        if (passCount != that.passCount)
        {
            return false;
        }
        if (failCount != that.failCount)
        {
            return false;
        }
        if (skipCount != that.skipCount)
        {
            return false;
        }
        return Double.compare(that.duration, duration) == 0;

    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        result = passCount;
        result = 31 * result + failCount;
        result = 31 * result + skipCount;
        temp = Double.doubleToLongBits(duration);
        result = 31 * result + (int)(temp ^ (temp >>> 32));
        return result;
    }

    public int getPassCount()
    {

        return passCount;
    }

    public int getFailCount()
    {
        return failCount;
    }

    public int getSkipCount()
    {
        return skipCount;
    }

    public double getDuration()
    {
        return duration;
    }
}
