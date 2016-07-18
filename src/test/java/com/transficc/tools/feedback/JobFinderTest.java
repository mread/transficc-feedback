package com.transficc.tools.feedback;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.offbytwo.jenkins.JenkinsServer;
import com.transficc.tools.feedback.messaging.MessageBus;
import com.transficc.tools.feedback.messaging.PublishableJob;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import static com.transficc.tools.feedback.JenkinsFacade.JobStatus;

public class JobFinderTest
{
    private static final String URL = "blah";
    @Mock
    private JenkinsServer jenkins;
    @Mock
    private ScheduledExecutorService scheduledExecutorService;
    @Mock
    private ScheduledFuture scheduledFuture;
    private JobFinder jobFinder;
    private JobRepository jobRepository;

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
        BDDMockito.given(scheduledExecutorService.scheduleAtFixedRate(Matchers.any(Runnable.class), Matchers.anyLong(), Matchers.anyLong(), Matchers.any(TimeUnit.class))).willReturn(scheduledFuture);
        jobRepository = new JobRepository();
        final LinkedBlockingQueue<PublishableJob> messageBusQueue = new LinkedBlockingQueue<>();
        final MessageBus messageBus = new MessageBus(messageBusQueue,
                                                     null);
        jobFinder = new JobFinder(new JobService(jobRepository, messageBus, null, scheduledExecutorService),
                                  new JenkinsFacade(jenkins, new JobPrioritiesRepository(Collections.emptyMap()), "", () -> 10));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldOnlyAddEachJobOnce() throws IOException
    {
        final Map<String, com.offbytwo.jenkins.model.Job> result1 = new HashMap<>();
        result1.put("Tom", new MessageBuilder(com.offbytwo.jenkins.model.Job.class).setField("name", "Tom").setField("url", "stuff.com").build());
        result1.put("Chinar", new MessageBuilder(com.offbytwo.jenkins.model.Job.class).setField("name", "Chinar").setField("url", "stuff.com").build());
        final Map<String, com.offbytwo.jenkins.model.Job> result2 = new HashMap<>();
        result2.put("Tom", new MessageBuilder(com.offbytwo.jenkins.model.Job.class).setField("name", "Tom").setField("url", "stuff.com").build());
        BDDMockito.given(jenkins.getJobs()).willReturn(result1, result2);

        jobFinder.run();
        jobFinder.run();

        final List<PublishableJob> publishableJobs = jobRepository.getPublishableJobs();

        MatcherAssert.assertThat(publishableJobs.size(), Is.is(2));
        MatcherAssert.assertThat(publishableJobs.get(0).getName(), Is.is("Chinar"));
        MatcherAssert.assertThat(publishableJobs.get(0).getUrl(), Is.is("stuff.com"));
        MatcherAssert.assertThat(publishableJobs.get(0).getJobStatus(), Is.is(JobStatus.DISABLED));
        MatcherAssert.assertThat(publishableJobs.get(1).getName(), Is.is("Tom"));
        MatcherAssert.assertThat(publishableJobs.get(1).getUrl(), Is.is("stuff.com"));
        MatcherAssert.assertThat(publishableJobs.get(1).getJobStatus(), Is.is(JobStatus.DISABLED));

    }

    @Test
    public void shouldAddJobAsTheyAreCreated() throws IOException
    {
        final Map<String, com.offbytwo.jenkins.model.Job> result1 = new HashMap<>();
        result1.put("Tom", new MessageBuilder(com.offbytwo.jenkins.model.Job.class).setField("name", "Chinar").setField("url", "stuff.com").build());
        final Map<String, com.offbytwo.jenkins.model.Job> result2 = new HashMap<>();
        result2.put("Tom", new MessageBuilder(com.offbytwo.jenkins.model.Job.class).setField("name", "Tom").setField("url", "stuff.com").build());
        BDDMockito.given(jenkins.getJobs()).willReturn(result1, result2);

        jobFinder.run();
        jobFinder.run();

        final List<PublishableJob> publishableJobs = jobRepository.getPublishableJobs();

        MatcherAssert.assertThat(publishableJobs.size(), Is.is(2));
        MatcherAssert.assertThat(publishableJobs.get(0).getName(), Is.is("Chinar"));
        MatcherAssert.assertThat(publishableJobs.get(0).getUrl(), Is.is("stuff.com"));
        MatcherAssert.assertThat(publishableJobs.get(0).getJobStatus(), Is.is(JobStatus.DISABLED));
        MatcherAssert.assertThat(publishableJobs.get(1).getName(), Is.is("Tom"));
        MatcherAssert.assertThat(publishableJobs.get(1).getUrl(), Is.is("stuff.com"));
        MatcherAssert.assertThat(publishableJobs.get(1).getJobStatus(), Is.is(JobStatus.DISABLED));
    }
}