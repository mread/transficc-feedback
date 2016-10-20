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
var $ = require('jquery');
var _ = require('lodash');
var moment = require('moment');
var jobTemplate = require('./partials/job.mustache');
window.$ = window.jQuery = $;
var heartBeatInterval = null;
var webSocket;
var missedHeartBeats = 0;
var startUpTime;

var Jobs = {
    findFirst: function () {
        return _.chain($('.job')).
            filter(function(job) {
                return $(job).attr('data-priority') == 0;
            }).
            head().
            value();
    },

    findLast: function() {
        return _.chain($('.job')).
            filter(function(job) {
                return $(job).attr('data-priority') == 0;
            }).
            last().
            value();
    },

    findJobToFollow: function (jobName) {
        return _.chain($('.job')).
            filter(function(job) {
                return $(job).attr('data-priority') == 0 && $(job).attr('data-job-status').toLowerCase() !== 'error';
            }).
            find(function (job) {
                return $(job).attr('data-title') > jobName;
            }).
            value();
    }
}

$(document).ready(function() {

    function setIteration () {
        var iteration = prompt("Please enter an iteration number");
        if (iteration !== null) {
            $.ajax({
                url: '/iteration',
                type: 'PUT',
                contentType: 'application/json',
                data: JSON.stringify({
                    iteration: iteration
                }),
                success: function () {
                    $('#iteration').html(iteration);
                },
                error: function (xhr, textStatus, errorThrown) {
                    alert("Could not update iteration. Status:" + textStatus + " Error:" + errorThrown);
                }
            });
        }
    }

    function updateStatus () {
        var update = prompt("Please provide a status update", "");
        $.ajax({
            url: '/status',
            type: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify({
                message: update
            }),
            success: function () {
                $('#update .message').html(update);
            },
            error: function (xhr, textStatus, errorThrown) {
                alert("Could not update status. Status:" + textStatus + " Error:" + errorThrown);
            }
        });
    }

    function regulariseJobHeight() {
        //for the little builds
        var maxHeight = _.chain($('.col-md-2 .job')).
            map(function(job) {
                return $(job).height();
            }).
            max().
            value();

        _.each($('.col-md-2 .job'), function (job) {
            $(job).height(maxHeight);
        });
    }

    function onStatusUpdate(status) {
        if (status === null || status.length === 0) {
            $('#update').hide();
        } else {
            $('#update').show();
            $('#update .message').html(status);
        }
    }

    function getJobTimeDifference(jobTime, serverTimestamp) {
        return moment(jobTime).from(serverTimestamp);
    }

    function updateTimestamps(currentTime) {
        var serverTimestamp = moment(currentTime);
        $('.timestamp').each(function() {
            $(this).text(getJobTimeDifference($(this).data('timestamp'), serverTimestamp));
        });
    }

    function onOpen() {
        if (heartBeatInterval === null) {
            if ($('#disconnected').is(':visible')) {
                webSocket.send('snapshot');
            }
            missedHeartBeats = 0;
            $('#disconnected').hide();
            heartBeatInterval = setInterval(function() {
                try {
                    missedHeartBeats++;
                    if (missedHeartBeats > 3) {
                     throw new Error("Too many missed heartbeats");
                    }
                    webSocket.send('--heartbeat--');
                } catch (e) {
                    webSocket.close();
                    onClose();
                }
            }, 5000);
        }
    }

    function onClose() {
        clearInterval(heartBeatInterval);
        heartBeatInterval = null;
        $('#disconnected').show();
        setTimeout(function() {
            setupWebSocket()
        }, 10000);
    }

    function handleUpdateForOrdinaryJob($job, jobName, newJobStatus) {
        var currentJobStatus = $job.attr('data-job-status').toLowerCase();
        var dataPriority = $job.attr('data-priority');

        if (dataPriority == 0) {
            if (currentJobStatus !== 'error' && newJobStatus === 'error') {
                var firstJob = Jobs.findFirst()
                $job.parent().insertBefore($(firstJob).parent());
            } else if (currentJobStatus === 'error' && newJobStatus !== 'error') {
                //move to alphabetical order
                var jobToInsertBefore = Jobs.findJobToFollow(jobName);

                if (jobToInsertBefore === undefined) {
                    var lastJob = Jobs.findLast();
                    $job.parent().insertAfter($(lastJob).parent());
                } else {
                    $job.parent().insertBefore($(jobToInsertBefore).parent());
                }
            }
        }
    }

    function onJobUpdate(job) {
        var $job = $('#' + job.name);
        var isANewJob = $job.length === 0;
        if (isANewJob) {
            location.reload();
            return;
        }

        var newJobStatus = job.jobStatus.toLowerCase();
        var testResults = job.jobsTestResults;

        handleUpdateForOrdinaryJob($job, job.name, newJobStatus);

        $job.replaceWith(jobTemplate({
            name: job.name,
            jobStatus: newJobStatus,
            priority: job.priority,
            revision: job.revision,
            url: job.url,
            buildNumber: job.buildNumber,
            jobCompletionPercentage: job.jobCompletionPercentage,
            shouldHideProgressBar: job.shouldHideProgressBar,
            comments: job.comments,
            shouldHideTestReport: job.shouldHideTestResults,
            passCount: !!testResults && testResults.passCount,
            failCount: !!testResults && testResults.failCount,
            skipCount: !!testResults && testResults.skipCount,
            timestamp: job.timestamp,
            timeDifference: getJobTimeDifference(job.timestamp)
        }));
        regulariseJobHeight();
    }

    function onJobRemoved(jobName) {
        $job = $('#' + jobName).parent().remove();
    }

    function onUpdate(message) {
        var data = JSON.parse(message.data);
        var type = data.type;

        if (type === 'JOB_UPDATE') {
            onJobUpdate(data.value);
        } else if (type === 'STATUS_UPDATE') {
            onStatusUpdate(data.value.status);
        } else if (type === 'ITERATION_UPDATE') {
            $('#iteration').html(data.value.iteration);
        } else if (type === 'HEARTBEAT') {
            if (data.value.serverStartUpTime !== startUpTime) {
                console.log('Server update detected. Reloading client');
                location.reload();
            }
            missedHeartBeats = 0;
            updateTimestamps(data.value.currentServerTime);
        } else if (type === 'JOB_DELETED') {
            onJobRemoved(data.value);
        }
    }

    function webSocketProtocol() {
        var loc = window.location;
        if (loc.protocol === "https:")
        {
            return "wss:";
        } else {
            return "ws:";
        }
    }

    function setupWebSocket() {
        webSocket = new WebSocket(webSocketProtocol() + '//' + location.host + '/updates/websocket');
        webSocket.onopen = onOpen;
        webSocket.onclose = onClose;
        webSocket.onmessage = onUpdate;
        webSocket.onerror = function() {
            console.log('error detected');
        }
    }

    startUpTime = $('#start-up-time').data('value');
    updateTimestamps();
    setupWebSocket();
    $('#set-iteration').click(setIteration);
    $('#status-update').click(updateStatus);
    onStatusUpdate($('#update .message').text());
    $('#loading').hide();
    $('#jobs').show();
    regulariseJobHeight();
});
