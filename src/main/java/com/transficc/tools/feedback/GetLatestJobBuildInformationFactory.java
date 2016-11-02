package com.transficc.tools.feedback;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.transficc.tools.feedback.messaging.MessageBus;

public class GetLatestJobBuildInformationFactory
{
    private final JenkinsFacade jenkinsFacade;
    private final MessageBus messageBus;
    private final Set<String> jobNamesForTestResultsToPersist;

    public GetLatestJobBuildInformationFactory(final JenkinsFacade jenkinsFacade, final MessageBus messageBus, final String[] jobNamesForTestResultsToPersist)
    {
        this.jenkinsFacade = jenkinsFacade;
        this.messageBus = messageBus;
        this.jobNamesForTestResultsToPersist = new HashSet<>(jobNamesForTestResultsToPersist.length);
        Collections.addAll(this.jobNamesForTestResultsToPersist, jobNamesForTestResultsToPersist);
    }


    public GetLatestJobBuildInformation create(final Job job, final JobService jobService)
    {
        return new GetLatestJobBuildInformation(messageBus, jobService, job, jenkinsFacade, jobNamesForTestResultsToPersist.contains(job.getName()));
    }

}
