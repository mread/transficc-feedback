/*
 * Copyright 2016 TransFICC Ltd.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.  The ASF licenses this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the specific language governing permissions and limitations under the License.
 */
package com.transficc.tools.feedback;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offbytwo.jenkins.JenkinsServer;
import com.transficc.tools.feedback.dao.IterationDao;
import com.transficc.tools.feedback.messaging.MessageBus;
import com.transficc.tools.feedback.messaging.MessageSubscriber;
import com.transficc.tools.feedback.messaging.PublishableJob;
import com.transficc.tools.feedback.routes.Routes;
import com.transficc.tools.feedback.routes.WebSocketPublisher;
import com.transficc.tools.feedback.util.FeedbackProperties;
import com.transficc.tools.feedback.util.LoggingThreadFactory;
import com.transficc.tools.feedback.util.SafeSerialisation;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.FilterComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcConnectionPool;


import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;

public class FeedbackMain
{

    private static final String SERVICE_NAME = "transficc-feedback";

    static
    {
        configureLogging(SERVICE_NAME);
    }

    public static void main(final String[] args) throws IOException
    {
        final Optional<File> propertiesFile = args.length == 1 ? Optional.of(new File(args[0])) : Optional.empty();
        final FeedbackProperties feedbackProperties = new FeedbackProperties(propertiesFile);
        System.setProperty("vertx.cacheDirBase", feedbackProperties.getVertxCacheDir());
        final Vertx vertx = Vertx.vertx();
        final HttpServer server = vertx.createHttpServer();
        final ObjectMapper mapper = Json.mapper;
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        final SafeSerialisation safeSerialisation = new SafeSerialisation(mapper);
        final WebSocketPublisher webSocketPublisher = new WebSocketPublisher(vertx.eventBus(), safeSerialisation);
        final JenkinsServer jenkins = createJenkinsServer(feedbackProperties);
        final BlockingQueue<PublishableJob> messageQueue = new LinkedBlockingQueue<>();

        final JdbcConnectionPool dataSource = JdbcConnectionPool.create("jdbc:h2:~/data/feedback", "feedback", "");

        final Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.migrate();

        final ThreadFactory threadFactory = new LoggingThreadFactory(SERVICE_NAME);
        final ExecutorService statusCheckerService = Executors.newFixedThreadPool(1, threadFactory);
        final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4, threadFactory);
        statusCheckerService.submit(new MessageSubscriber(messageQueue, webSocketPublisher));
        final JobRepository jobRepository = new JobRepository();
        final MessageBus messageBus = new MessageBus(messageQueue, webSocketPublisher);
        final JenkinsFacade jenkinsFacade = new JenkinsFacade(jenkins, new JobPrioritiesRepository(feedbackProperties.getJobsWithPriorities()), feedbackProperties.getMasterJobName(),
                                                              System::currentTimeMillis, feedbackProperties.getVersionControl());
        final JobService jobService = new JobService(jobRepository, messageBus, jenkinsFacade, scheduledExecutorService);
        final IterationRepository iterationRepository = new IterationRepository(messageBus, new IterationDao(dataSource));
        Routes.setup(server, jobRepository, iterationRepository, new BreakingNewsService(messageBus), webSocketPublisher, Router.router(vertx));
        final JobFinder jobFinder = new JobFinder(jobService, jenkinsFacade);

        scheduledExecutorService.scheduleAtFixedRate(jobFinder, 0, 5, TimeUnit.MINUTES);
        server.listen(feedbackProperties.getFeedbackPort());
    }

    private static JenkinsServer createJenkinsServer(final FeedbackProperties feedbackProperties)
    {
        final JenkinsServer jenkins;
        if (feedbackProperties.getJenkinsUsername() == null)
        {
            jenkins = new JenkinsServer(URI.create(feedbackProperties.getJenkinsUrl()));
        }
        else
        {
            jenkins = new JenkinsServer(URI.create(feedbackProperties.getJenkinsUrl()), feedbackProperties.getJenkinsUsername(), feedbackProperties.getJenkinsPassword());
        }
        return jenkins;
    }

    private static void configureLogging(final String serviceName)
    {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");

        final ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        //Set Log4js own log level
        builder.setStatusLevel(Level.ERROR);
        builder.setConfigurationName("TransFICC default service log configuration");

        final String stdOutAppender = "stdoutAppender";
        final LayoutComponentBuilder patternLayout = builder.newLayout("PatternLayout").addAttribute("pattern", "%d %c{1} [%t] %-5level: %msg%n%throwable");
        //only log fatal or worse to the console
        final FilterComponentBuilder filterComponent = builder.newFilter("ThresholdFilter", Filter.Result.NEUTRAL, Filter.Result.DENY).addAttribute("level", Level.FATAL);
        final String logPath = "log/";

        final AppenderComponentBuilder consoleAppender = builder.newAppender(stdOutAppender, "CONSOLE").add(filterComponent).
                add(patternLayout).addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
        final AppenderComponentBuilder fileAppender = builder.newAppender("fileAppender", "RollingFile").add(patternLayout).
                addAttribute("fileName", new File(logPath + serviceName + ".log").getAbsolutePath()).addAttribute("filePattern", logPath + serviceName + ".%i.log").
                addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", "20 MB"));

        builder.add(consoleAppender);
        builder.add(fileAppender);

        builder.add(builder.newLogger("com.transficc", Level.INFO).addAttribute("additivity", false).add(builder.newAppenderRef(stdOutAppender)).add(builder.newAppenderRef("fileAppender")));
        builder.add(builder.newRootLogger(Level.INFO).add(builder.newAppenderRef(stdOutAppender)).add(builder.newAppenderRef("fileAppender")));
        Configurator.initialize(builder.build());
    }
}
