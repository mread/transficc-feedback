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
var jobTemplate = require('./partials/job.mustache');
window.$ = window.jQuery = $;
var heartBeatInterval = null;
var webSocket;
var missedHeartBeats = 0;

$(document).ready(function() {

    function setIteration () {
        var iteration = prompt("Please enter an iteration number", "it123");
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

    function onOpen() {
        if (heartBeatInterval === null) {
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

    function onUpdate(message) {
        function onJobUpdate(job) {
            var $job = $('#' + job.name);

            if ($job.length === 0) {
                location.reload();
                return;
            }

            var currentJobStatus = $job.attr('data-job-status').toLowerCase();
            var newJobStatus = job.jobStatus.toLowerCase();
            var dataPriority = $job.attr('data-priority');
            var testResults = job.jobsTestResults;

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
                            skipCount: !!testResults && testResults.skipCount
                        }));

            if (dataPriority == 0) {
                if (currentJobStatus !== 'error' && newJobStatus === 'error') {
                    var firstJob = _.chain($('.job')).
                      filter(function(job) {
                        return $(job).attr('data-priority') == 0;
                      }).
                      head().
                      value();
                      //After $job had replacedWith called on it, we lose the reference to the job in the DOM, so just do this for now....
                      $('#' + job.name).parent().insertBefore($(firstJob).parent());
                } else if (currentJobStatus === 'error' && newJobStatus !== 'error') {
                    //move to alphabetical order
                    location.reload();
                }
                regulariseJobHeight();
            }
        }

        var data = JSON.parse(message.data);
        var type = data.type;

        if (type === 'jobUpdate') {
            onJobUpdate(data.value);
        } else if (type === 'statusUpdate') {
            onStatusUpdate(data.value.status);
        } else if (type === 'iterationUpdate') {
            $('#iteration').html(data.value.iteration);
        } else if (type === 'heartBeat') {
            missedHeartBeats = 0;
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
            onClose();
        }
    }

    setupWebSocket();
    $('#set-iteration').click(setIteration);
    $('#status-update').click(updateStatus);
    onStatusUpdate($('#update .message').text());
    regulariseJobHeight();
});