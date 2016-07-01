package com.transficc.tools.feedback.routes;

import java.util.List;

import com.transficc.tools.feedback.IterationRepository;
import com.transficc.tools.feedback.JobRepository;
import com.transficc.tools.feedback.BreakingNewsService;
import com.transficc.tools.feedback.messaging.PublishableJob;


import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.TemplateEngine;

public class IndexViewRoute implements Handler<RoutingContext>
{
    private static final String TEMPLATE = "templates/index.hbs";
    private final TemplateEngine engine;
    private final JobRepository jobRepository;
    private final IterationRepository iterationRepository;
    private final BreakingNewsService breakingNewsService;

    public IndexViewRoute(final TemplateEngine engine, final JobRepository jobRepository,
                          final IterationRepository iterationRepository, final BreakingNewsService breakingNewsService)
    {
        this.engine = engine;
        this.jobRepository = jobRepository;
        this.iterationRepository = iterationRepository;
        this.breakingNewsService = breakingNewsService;
    }

    @Override
    public void handle(final RoutingContext routingContext)
    {
        final List<PublishableJob> jobs = jobRepository.getPublishableJobs();
        routingContext.put("jobs", jobs);
        routingContext.put("iteration", iterationRepository.iteration());
        routingContext.put("status", breakingNewsService.status());
        engine.render(routingContext, TEMPLATE,
                      res ->
                      {
                          if (res.succeeded())
                          {
                              routingContext.response().end(res.result());
                          }
                          else
                          {
                              routingContext.fail(res.cause());
                          }
                      });
    }
}
