package com.transficc.tools.feedback;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.transficc.infrastructure.collections.Result;
import com.transficc.logging.LoggingService;
import com.transficc.tools.feedback.jenkins.Jenkins;
import com.transficc.tools.feedback.jenkins.LatestBuildInformation;
import com.transficc.tools.feedback.jenkins.serialized.JobTestResults;
import com.transficc.tools.feedback.messaging.MessageBus;
import com.transficc.tools.feedback.messaging.PublishableJob;
import com.transficc.tools.feedback.util.ClockService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;

public class GetLatestJobBuildInformationTest
{

    static
    {
        LoggingService.configureLoggingForUnitTests();
    }


    @Mock
    private ClockService clockService;
    @Mock
    private Jenkins jenkins;
    private GetLatestJobBuildInformation jobChecker;
    private BlockingQueue<PublishableJob> messageBusQueue;
    private String jobName;

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
        messageBusQueue = new LinkedBlockingQueue<>();
        jobName = "Tom is the best";
        final MessageBus messageBus = new MessageBus(messageBusQueue,
                                                     null);
        this.jobChecker = new GetLatestJobBuildInformation(messageBus, jenkins, null, new Job(jobName, "tom-url", 0, JobStatus.parse("blue"), false));
    }

    @Test
    public void shouldPushJobUpdateToTheMessageBus() throws Exception
    {
        final String jobUrl = "tom-url";
        final String revision = "5435dsd";
        final String color = "red";
        final long timestamp = 2;
        final int buildNumber = 123;

        //given
        final JobTestResults testResults = new MessageBuilder(JobTestResults.class).setField("passCount", 1).setField("failCount", 1).setField("skipCount", 2).setField("duration", 1.2).build();
        given(jenkins.getLatestBuildInformation(jobUrl + "/api/json?tree=name,url,color,lastBuild[number,url]")).willReturn(
                Result.success(new LatestBuildInformation(revision, JobStatus.ERROR, buildNumber, 50.0, new String[0], false, Optional.of(testResults))));
        given(clockService.currentMillis()).willReturn(timestamp + 2);

        //when

        jobChecker.run();

        //then

        final PublishableJob actualJob = messageBusQueue.take();
        assertThat(actualJob, is(new PublishableJob(jobName, jobUrl, 0, revision, JobStatus.parse(color), buildNumber, 50.0, new String[0], false, new JobsTestResults(1, 1, 2, 1.2))));
    }

    @Test
    public void shouldNotPublishAnUpdateIfNothingHasChanged() throws Exception
    {
        final String jobUrl = "tom-url";
        final String revision = "";

        //given
        given(jenkins.getLatestBuildInformation(jobUrl + "/api/json?tree=name,url,color,lastBuild[number,url]")).willReturn(Result.success(
                new LatestBuildInformation(revision, JobStatus.SUCCESS, 1, 0.0, new String[0], false, Optional.empty())));

        //when

        jobChecker.run();

        //then

        assertThat(messageBusQueue.size(), is(0));
    }
}