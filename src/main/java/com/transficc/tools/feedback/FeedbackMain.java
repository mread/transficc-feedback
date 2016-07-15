package com.transficc.tools.feedback;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transficc.insfrastructure.threading.ExceptionLoggingThreadFactory;
import com.transficc.logging.LoggingService;
import com.transficc.tools.feedback.messaging.MessageBus;
import com.transficc.tools.feedback.messaging.MessageSubscriber;
import com.transficc.tools.feedback.messaging.PublishableJob;
import com.transficc.tools.feedback.routes.Routes;
import com.transficc.tools.feedback.routes.WebSocketPublisher;
import com.transficc.tools.feedback.util.FeedbackProperties;
import com.transficc.tools.jenkins.Jenkins;
import com.transficc.tools.jenkins.JenkinsBuilder;
import com.transficc.tools.jenkins.SafeSerialisation;


import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;

public class FeedbackMain
{

    static
    {
        LoggingService.configureLogging("tools-feedback");
    }

    public static void main(final String[] args) throws IOException
    {
        final Properties properties = createProperties();
        final FeedbackProperties feedbackProperties = new FeedbackProperties(properties);
        System.setProperty("vertx.cacheDirBase", feedbackProperties.getVertxCacheDir());
        final String jenkinsUrl = feedbackProperties.getJenkinsUrl();
        final Vertx vertx = Vertx.vertx();
        final HttpServer server = vertx.createHttpServer();
        final EventBus eventBus = vertx.eventBus();
        final ObjectMapper mapper = Json.mapper;
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        final SafeSerialisation safeSerialisation = new SafeSerialisation(mapper);
        final WebSocketPublisher webSocketPublisher = new WebSocketPublisher(eventBus, safeSerialisation);
        final Jenkins jenkins = JenkinsBuilder.newInstance().jenkinsUrl(jenkinsUrl).build();
        final BlockingQueue<PublishableJob> messageQueue = new LinkedBlockingQueue<>();

        final ExecutorService statusCheckerService = Executors.newFixedThreadPool(1);
        final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4, new ExceptionLoggingThreadFactory("tools-feedback"));
        statusCheckerService.submit(new MessageSubscriber(messageQueue, webSocketPublisher));
        final JobRepository jobRepository = new JobRepository();
        final MessageBus messageBus = new MessageBus(messageQueue, webSocketPublisher);
        final JenkinsFacade jenkinsFacade = new JenkinsFacade(jenkins, new JobPrioritiesRepository(feedbackProperties.getJobsWithPriorities()), feedbackProperties.getMasterJobName());
        final JobService jobService = new JobService(jobRepository, messageBus, jenkinsFacade, scheduledExecutorService);
        final IterationRepository iterationRepository = new IterationRepository(messageBus);
        Routes.setup(server, jobRepository, iterationRepository, new BreakingNewsService(messageBus), webSocketPublisher, Router.router(vertx), safeSerialisation);
        final JobFinder jobFinder = new JobFinder(jobService, jenkinsFacade
        );

        scheduledExecutorService.scheduleAtFixedRate(jobFinder, 0, 5, TimeUnit.MINUTES);
        server.listen(feedbackProperties.getFeedbackPort());
    }

    private static Properties createProperties() throws IOException
    {
        final ClassLoader classLoader = FeedbackMain.class.getClassLoader();
        final InputStream serviceProperties = classLoader.getResourceAsStream("feedback.properties");
        final Properties properties = new Properties();
        properties.load(serviceProperties);
        return properties;
    }
}
