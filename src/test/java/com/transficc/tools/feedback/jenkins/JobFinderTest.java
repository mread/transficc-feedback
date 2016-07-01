package com.transficc.tools.feedback.jenkins;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.transficc.infrastructure.collections.Result;
import com.transficc.tools.feedback.HttpClientFacade;
import com.transficc.tools.feedback.JobPrioritiesRepository;
import com.transficc.tools.feedback.JobRepository;
import com.transficc.tools.feedback.JobService;
import com.transficc.tools.feedback.JobStatus;
import com.transficc.tools.feedback.MessageBuilder;
import com.transficc.tools.feedback.jenkins.serialized.Job;
import com.transficc.tools.feedback.jenkins.serialized.Jobs;
import com.transficc.tools.feedback.messaging.MessageBus;
import com.transficc.tools.feedback.messaging.PublishableJob;
import com.transficc.tools.feedback.util.ClockService;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;

public class JobFinderTest
{
    private static final String URL = "blah";
    @Mock
    private HttpClientFacade httpClient;
    @Mock
    private ClockService clockService;
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
        given(scheduledExecutorService.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class))).willReturn(scheduledFuture);
        jobRepository = new JobRepository();
        final LinkedBlockingQueue<PublishableJob> messageBusQueue = new LinkedBlockingQueue<>();
        final MessageBus messageBus = new MessageBus(messageBusQueue,
                                                     null);
        jobFinder = new JobFinder(new JobService(jobRepository, messageBus, null, scheduledExecutorService),
                                  new Jenkins(clockService, httpClient, URL), new JobPrioritiesRepository(Collections.emptyMap()), "");
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

        given(httpClient.get(URL + "/api/json?tree=jobs[name,url,color,lastBuild[number,url]]", Jobs.class)).willReturn(Result.success(jobs1), Result.success(jobs2));

        jobFinder.run();
        jobFinder.run();

        final List<PublishableJob> publishableJobs = jobRepository.getPublishableJobs();

        assertThat(publishableJobs.size(), is(2));
        assertThat(publishableJobs.get(0).getName(), is("Chinar"));
        assertThat(publishableJobs.get(0).getUrl(), is("stuff.com"));
        assertThat(publishableJobs.get(0).getJobStatus(), Is.is(JobStatus.ERROR));
        assertThat(publishableJobs.get(1).getName(), is("Tom"));
        assertThat(publishableJobs.get(1).getUrl(), is("stuff.com"));
        assertThat(publishableJobs.get(1).getJobStatus(), is(JobStatus.SUCCESS));

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

        given(httpClient.get(URL + "/api/json?tree=jobs[name,url,color,lastBuild[number,url]]", Jobs.class)).willReturn(Result.success(jobs1), Result.success(jobs2));

        jobFinder.run();
        jobFinder.run();

        final List<PublishableJob> publishableJobs = jobRepository.getPublishableJobs();

        assertThat(publishableJobs.size(), is(2));
        assertThat(publishableJobs.get(0).getName(), is("Chinar"));
        assertThat(publishableJobs.get(0).getUrl(), is("stuff.com"));
        assertThat(publishableJobs.get(0).getJobStatus(), is(JobStatus.ERROR));
        assertThat(publishableJobs.get(1).getName(), is("Tom"));
        assertThat(publishableJobs.get(1).getUrl(), is("stuff.com"));
        assertThat(publishableJobs.get(1).getJobStatus(), is(JobStatus.SUCCESS));
    }
}