package com.transficc.tools.feedback;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildChangeSet;
import com.offbytwo.jenkins.model.BuildResult;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.transficc.logging.LoggingService;
import com.transficc.tools.feedback.messaging.MessageBus;
import com.transficc.tools.feedback.messaging.PublishableJob;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

public class GetLatestJobBuildInformationTest
{

    static
    {
        LoggingService.configureLoggingForUnitTests();
    }

    @Mock
    private JenkinsServer jenkins;
    @Mock
    private JobWithDetails jobWithDetails;
    @Mock
    private Build lastBuild;
    private GetLatestJobBuildInformation jobChecker;
    private BlockingQueue<PublishableJob> messageBusQueue;
    private String jobName;

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
        messageBusQueue = new LinkedBlockingQueue<>();
        jobName = "Tom is the best";
        given(jobWithDetails.isBuildable()).willReturn(true);
        final MessageBus messageBus = new MessageBus(messageBusQueue, null);
        this.jobChecker = new GetLatestJobBuildInformation(messageBus, null, new Job(jobName, "tom-url", 0, JenkinsFacade.JobStatus.SUCCESS, false), new JenkinsFacade(jenkins, null, null,
                                                                                                                                                                       () -> 10));
    }

    @Test
    public void shouldPushJobUpdateToTheMessageBus() throws Exception
    {
        final String jobUrl = "tom-url";
        final String revision = "5435dsd";
        final Map<Object, Object> revisionActions = new HashMap<>();
        final Map<Object, Object> revisions = new HashMap<>();
        revisions.put("SHA1", revision);
        revisionActions.put("lastBuiltRevision", revisions);
        final Map<Object, Object> testResults = new HashMap<>();
        testResults.put("failCount", 1);
        testResults.put("skipCount", 2);
        testResults.put("totalCount", 4);
        testResults.put("urlName", "testReport");
        final List<Map<Object, Object>> actions = Arrays.asList(revisionActions, testResults);
        final BuildChangeSet buildChangeSet = new BuildChangeSet();
        buildChangeSet.setItems(Collections.emptyList());

        given(jenkins.getJob(jobName)).willReturn(jobWithDetails);
        given(jobWithDetails.getLastBuild()).willReturn(lastBuild);
        given(lastBuild.details()).willReturn(new MessageBuilder(BuildWithDetails.class)
                                                      .setField("actions", actions)
                                                      .setField("building", false)
                                                      .setField("changeSet", buildChangeSet)
                                                      .setField("result", BuildResult.SUCCESS)
                                                      .setField("timestamp", 5)
                                                      .setField("estimatedDuration", 10)
                                                      .build());
        //when

        jobChecker.run();

        //then

        final PublishableJob actualJob = messageBusQueue.take();
        assertThat(actualJob, is(new PublishableJob(jobName, jobUrl, 0, revision, JenkinsFacade.JobStatus.SUCCESS, 0, 50.0, new String[0], false,
                                                    new JenkinsFacade.TestResults(1, 1, 2))));
    }

    @Test
    public void shouldNotPublishAnUpdateIfNothingHasChanged() throws Exception
    {
        //given
        final String revision = "";
        final Map<Object, Object> revisionActions = new HashMap<>();
        final Map<Object, Object> revisions = new HashMap<>();
        revisions.put("SHA1", revision);
        revisionActions.put("lastBuiltRevision", revisions);
        final List<Map<Object, Object>> actions = Collections.singletonList(revisionActions);
        final BuildChangeSet buildChangeSet = new BuildChangeSet();
        buildChangeSet.setItems(Collections.emptyList());

        given(jenkins.getJob(jobName)).willReturn(jobWithDetails);
        given(jobWithDetails.getLastBuild()).willReturn(lastBuild);
        given(lastBuild.details()).willReturn(new MessageBuilder(BuildWithDetails.class)
                                                      .setField("actions", actions)
                                                      .setField("building", false)
                                                      .setField("changeSet", buildChangeSet)
                                                      .setField("result", BuildResult.SUCCESS)
                                                      .setField("timestamp", 10)
                                                      .setField("estimatedDuration", 1)
                                                      .build());

        //when

        jobChecker.run();

        //then

        assertThat(messageBusQueue.size(), is(0));
    }
}