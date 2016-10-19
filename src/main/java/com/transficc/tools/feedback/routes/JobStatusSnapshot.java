package com.transficc.tools.feedback.routes;

import java.util.List;

import com.transficc.tools.feedback.messaging.PublishableJob;

public interface JobStatusSnapshot
{
    List<PublishableJob> getPublishableJobs();
}
