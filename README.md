[![Build Status](https://travis-ci.org/RiskIQ/hbase-assigner.svg?branch=master)](https://travis-ci.org/RiskIQ/hbase-assigner)

# HBase Region Assigner
During the course of normal HBase operation, table regions will occasionally become unassigned when regions are 
transitioned from one regionserver to another.  As long a region remains unassigned, the data contained therein will 
be inaccessible.  While reassigning an unassigned region is not difficult (`assign regionName` via `hbase shell`), 
HBase will not assign these wayward regions automatically.  This can result in late-night pages and grumpy engineers. 

In an effort to combat grumpiness, we created this application, which will run region assignment checks on every enabled 
HBase table on a scheduled basis. If any unassigned regions are encountered, we will instruct HBase to assign them to a 
regionserver.

## Requirements
Java 8 or higher is required.

## Development
This application is built using [Gradle](https://gradle.org/) and [Spring Boot](https://spring.io/projects/spring-boot). 
To build and run this application from source, clone the repository and run: 
```bash
./gradlew bootRun -Dhbase.zookeeper-hosts=<comma-delimited list of hostnames> -Dhbase.zoo-keeper-port=<zk port>
```

## Deployment / Installation
The application can be packaged into an executable jar by running `./gradlew bootJar`.  The resulting jar file can be found 
in `build/lib`.  The jar can then be run via 
```bash
java -jar path/to/jar.jar
```
or, on most Linux systems, an [init.d](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment-install.html#deployment-initd-service) 
or [systemd](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment-install.html#deployment-systemd-service) 
service can be created.

### Configuration
By default, the application will look for an `application.properties` file in `/etc/hbase-assigner`.  This location can 
be changed by adding the following command line option when starting the application: 
`spring.config.additional-location=/path/to/config`. The properties file should follow standard 
[properties file formatting](https://docs.oracle.com/cd/E23095_01/Platform.93/ATGProgGuide/html/s0204propertiesfileformat01.html).

#### Configuration Parameters
These parameters can be located in the `application.properties` file described above, or they can be passed as command line 
parameters (`-D<name>=<value>`). 

| Name | Description | Required? | Default |
|------|-------------|-----------|---------|
| hbase.zookeeper-hosts | A comma-delimited list of Zookeeper hosts. These should be the same hosts which manage your HBase cluster. | yes | N/A |
| hbase.zookeeper-port | The port on which Zookeeper listens on the configured hosts. | yes | N/A |
| hbase.cron | A [Quartz-style](https://www.freeformatter.com/cron-expression-generator-quartz.html) cron expression which will determine the frequency of automated region assignment checks. | no | `0 */30 * * * *` (every 30 minutes) |
| slack.url | A Slack incoming webhook URL which will be used for report publication. If unconfigured, Slack publication will be disabled. | no | none |
| slack.channel | The name of the Slack channel to which the reports will be published. If unconfigured, Slack publication will be disabled. | no | none |
| server.port | Port on which the application will listen for incoming HTTP requests. | no | 8080 |

## REST API
For the most part, the region assigner is intended to run its scheduled checks in the background, report on its activity, 
and otherwise be left alone. There are, however, several convenient REST endpoints available:

| Path | Method | Description |
|------|--------|-------------|
| `/api/hbase/_check-regions` | POST | Runs a region check on all enabled HBase tables. The results of the check are returned in JSON form, but are not published to Slack. |
| `/api/hbase/_check-regions/{table}` | POST | Runs a region check only on the given table. |
| `/api/hbase/servers` | GET | Returns a list of the currently available HBase servers. |
| `/api/hbase/servers/{server}` | GET | Returns details regarding the server running on the given host. | 
| `/api/hbase/servers/{server}/regions` | GET | Retrieves information regarding all of the regions which are assigned to the given server. |
| `/api/hbase/master` | GET | Basic information about the current HBase master. |
| `/api/hbase/tables` | GET | A listing of all existing HBase tables. |
| `/api/hbase/tables/{table}/` | GET | Details regarding the table with the given name. |
| `/api/hbase/tables/{table}/regions` | GET | A listing of the regions belonging to the given table. |