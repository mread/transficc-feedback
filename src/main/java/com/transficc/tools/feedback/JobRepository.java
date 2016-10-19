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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.transficc.tools.feedback.messaging.PublishableJob;
import com.transficc.tools.feedback.routes.JobStatusSnapshot;

public class JobRepository implements JobStatusSnapshot
{
    private final Map<String, Job> jobNameToJob = new ConcurrentHashMap<>();

    @Override
    public List<PublishableJob> getPublishableJobs()
    {
        return jobNameToJob.values().
                stream().
                map(Job::createPublishable).
                sorted((job1, job2) ->
                       {
                           final int comparePriority = Integer.compare(job2.getPriority(), job1.getPriority());
                           final int compareJobStatus = job1.getJobStatus().compareTo(job2.getJobStatus());
                           return comparePriority == 0 ? compareJobStatus != 0 && job1.getPriority() == 0 ? compareJobStatus : job1.getName().compareTo(job2.getName()) : comparePriority;
                       }).
                collect(Collectors.toList());
    }

    public boolean contains(final String jobName)
    {
        return jobNameToJob.containsKey(jobName);
    }

    public void put(final String jobName, final Job job)
    {
        jobNameToJob.put(jobName, job);
    }

    public void remove(final String jobName)
    {
        jobNameToJob.remove(jobName);
    }
}
