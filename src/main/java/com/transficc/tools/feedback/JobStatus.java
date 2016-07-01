package com.transficc.tools.feedback;

public enum JobStatus
{
    //This order is important (Enum.compareTo is used in JobRepository)
    ERROR(),
    DISABLED(),
    SUCCESS();

    public static JobStatus parse(final String color)
    {
        switch (color)
        {
            case "red":
            case "red_anime":
            case "yellow":
            case "yellow_anime":
            case "aborted":
            case "aborted_anime":
                return ERROR;
            case "blue":
            case "blue_anime":
                return SUCCESS;
            case "grey":
            case "grey_anime":
            case "disabled":
            case "disabled_anime":
            case "notbuilt":
            case "notbuilt_anime":
            default:
                return DISABLED;
        }
    }
}
