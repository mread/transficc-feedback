package com.transficc.tools.feedback;

import com.transficc.tools.feedback.messaging.MessageBus;

import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class GetLatestJobBuildInformationFactoryTest
{
    private final JenkinsFacade jenkinsFacade = Mockito.mock(JenkinsFacade.class);
    private final MessageBus messageBus = Mockito.mock(MessageBus.class);
    private final GetLatestJobBuildInformationFactory factory = new GetLatestJobBuildInformationFactory(jenkinsFacade, messageBus, new String[]{"SpecialJob"});

    @Test
    public void shouldCreateARunnableThatShouldPersistsTestInformation()
    {
        final GetLatestJobBuildInformation buildInformation = factory.create(new Job("SpecialJob", "google.com", 1, JenkinsFacade.JobStatus.SUCCESS, false, VersionControl.GIT), null);

        assertThat(buildInformation.isShouldPersistTestResults(), is(true));
    }

    @Test
    public void shouldCreateARunnableThatShouldNotPersistsTestInformation()
    {
        final GetLatestJobBuildInformation buildInformation = factory.create(new Job("AnotherJob", "google.com", 1, JenkinsFacade.JobStatus.SUCCESS, false, VersionControl.GIT), null);

        assertThat(buildInformation.isShouldPersistTestResults(), is(false));
    }
}