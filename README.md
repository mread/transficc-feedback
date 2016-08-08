# TransFICC Feedback

Feedback on the current state of your Jenkins Continuous Delivery pipeline.

Requires Java 8 and NPM to build and run. Our compatibility with Jenkins is dependent on the [Jenkins Java Library], so check over there to see if we are compatible with the version of Jenkins you're running.

![screenshot]

## Why?

Feedback is good. We all want more feedback! We found that the visual displays provided via Jenkins plugins did not provide the detail we wanted at TransFICC (we had grown accustomed to the infamous Big Feedback over at LMAX Exchange), so decided to build our own.

We find it useful to see what stage of our pipeline various commits are, and what jobs are red/green/disabled.  We hope you do too.

## Build

```sh
./gradlew shadowJar
```

## Configure

TransFICC Feedback can be configured in two ways. You can either provide a configuration file on it's classpath called feedback.properties, or provide a properties file as a command line argument
for the jar when running. The following properties are available for configuration:


- `feedback.jenkins.url` is the address for your Jenkins server
- `feedback.jenkins.username` is the username used for authenticating with your Jenkins server (do not set if authentication is not setup)
- `feedback.jenkins.password` is the password used for authenticating with your Jenkins server (do not set if authentication is not setup)
- `feedback.port` the http port TransFICC Feedback GUI will be available on
- `feedback.job.name` a comma separated list of jobs that will have a specified priority. If a job does not have a specified priority then it appears on the lower half of the Feedback GUI, sorted in red/green/alphabetical order. (Note in the above screenshot the values are feedback.job.name=master-commit,integration-test,acceptance-test,venue-compatibility-test)
- `feedback.job.priority` the priority of the jobs listed above, ranked in descending order (the higher the number, the higher the given job appears on the Feedback GUI). Note that 1 is reserved for jobs that have not been prioritised (Note in the above screenshot the values are feedback.job.priority=5,4,3,2)
- `feedback.job.master` the name of your master commit job (usually unit tests, checkstyle, archiving). This job will have commit messages displayed (Note in the above screenshot the value is feedback.job.master=master-commit)



## Run

```sh
cd build/libs/
java -jar transficc-feedback-all.jar
```
Go to (or the port specified for `feedback.port`):
http://localhost:4567

## TODO

- Compatibility for SVN (currently only supports Git hashes)
- Persist iteration
- Persist test results (potentially just acceptance) for graphing / analysis
- Ability to block commits to repository if the master commit build is broken
- Fix the never ending green (boxes keep growing in height - apparently only in Chromium)
- Provide a history of failing/intermittent tests
- Support HTTPS (required for websocket to work in some corporate infrastructures)

License
----

Apache 2.0

   [Jenkins Java Library]: <https://github.com/jenkinsci/java-client-api>
   [screenshot]: screenshot.png?raw=true