package com.transficc.tools.feedback;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.transficc.functionality.Result;
import com.transficc.tools.feedback.messaging.MessageBus;
import com.transficc.tools.feedback.messaging.PublishableJob;
import com.transficc.tools.jenkins.Jenkins;
import com.transficc.tools.jenkins.domain.JobStatus;
import com.transficc.tools.jenkins.serialized.Job;
import com.transficc.tools.jenkins.serialized.Jobs;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JobFinderTest
{
    private static final String URL = "blah";
    @Mock
    private Jenkins jenkins;
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
                                  jenkins, new JobPrioritiesRepository(Collections.emptyMap()), "");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldOnlyAddEachJobOnce()
    {
        final Jobs jobs1 = new MessageBuilder(Jobs.class).
                setField("jobs", Arrays.<Job>asList(new MessageBuilder(Job.class).setField("name", "Tom").setField("url", "stuff.com").setField("color", "blue").build(),
                                                              new MessageBuilder(Job.class).setField("name", "Chinar").setField("url", "stuff.com").setField("color", "red").build())).
                build();

        final Jobs jobs2 = new MessageBuilder(Jobs.class).
                setField("jobs", Collections.singletonList(new MessageBuilder(Job.class).setField("name", "Tom").setField("url", "stuff.com").setField("color", "blue").build())).
                build();

        BDDMockito.given(jenkins.getAllJobs()).willReturn(Result.success(jobs1), Result.success(jobs2));

        jobFinder.run();
        jobFinder.run();

        final List<PublishableJob> publishableJobs = jobRepository.getPublishableJobs();

        MatcherAssert.assertThat(publishableJobs.size(), Is.is(2));
        MatcherAssert.assertThat(publishableJobs.get(0).getName(), Is.is("Chinar"));
        MatcherAssert.assertThat(publishableJobs.get(0).getUrl(), Is.is("stuff.com"));
        MatcherAssert.assertThat(publishableJobs.get(0).getJobStatus(), Is.is(JobStatus.ERROR));
        MatcherAssert.assertThat(publishableJobs.get(1).getName(), Is.is("Tom"));
        MatcherAssert.assertThat(publishableJobs.get(1).getUrl(), Is.is("stuff.com"));
        MatcherAssert.assertThat(publishableJobs.get(1).getJobStatus(), Is.is(JobStatus.SUCCESS));

    }

    @Test
    public void shouldAddJobAsTheyAreCreated()
    {
        final Jobs jobs1 = new MessageBuilder(Jobs.class).
                setField("jobs", Collections.singletonList(new MessageBuilder(Job.class).setField("name", "Tom").setField("url", "stuff.com").setField("color", "blue").build())).
                build();

        final Jobs jobs2 = new MessageBuilder(Jobs.class).
                setField("jobs", Collections.singletonList(new MessageBuilder(Job.class).setField("name", "Chinar").setField("url", "stuff.com").setField("color", "red").build())).
                build();

        BDDMockito.given(jenkins.getAllJobs()).willReturn(Result.success(jobs1), Result.success(jobs2));

        jobFinder.run();
        jobFinder.run();

        final List<PublishableJob> publishableJobs = jobRepository.getPublishableJobs();

        MatcherAssert.assertThat(publishableJobs.size(), Is.is(2));
        MatcherAssert.assertThat(publishableJobs.get(0).getName(), Is.is("Chinar"));
        MatcherAssert.assertThat(publishableJobs.get(0).getUrl(), Is.is("stuff.com"));
        MatcherAssert.assertThat(publishableJobs.get(0).getJobStatus(), Is.is(JobStatus.ERROR));
        MatcherAssert.assertThat(publishableJobs.get(1).getName(), Is.is("Tom"));
        MatcherAssert.assertThat(publishableJobs.get(1).getUrl(), Is.is("stuff.com"));
        MatcherAssert.assertThat(publishableJobs.get(1).getJobStatus(), Is.is(JobStatus.SUCCESS));
    }
}