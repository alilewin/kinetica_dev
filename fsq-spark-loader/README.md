# Kinetica Spark Loader Application

This project contains spark-submit applications written in Java and Scala. It contains a JUnit test framework that can run in 2 modes:

1. Spark Driver and worker running local. 
2. Spark Driver running local and worker running on a remote Spark master.

## Prerequisites

Before running the transform job [TransformPlaces.scala][TRANSFORM] you will need:

* A local installation of IntelliJ IDE. 
* Credentials to access a source parquet file in AWS S3.
* Write permissions on a Kinetica database.

[TRANSFORM]: <src/main/scala/com/kinetica/fsq/TransformPlaces.scala>

## Test Configuration

As a first step you will need to import the project into IntelliJ and build. You can run individual JUnit tests in [TestSpark.java][SPARK_TEST] directly from the UI.

There are 3 configuration files located in `src/test/resources` that are used by the SparkTest class.

* [test-spark-common.properties][COMMON]: Common configuration for local and remote. 

* [test-spark-creds.properties][CREDS]: Credentials for AWS and Kinetica.

* [test-spark-remote.properties][REMOTE]: Configuration for remote execution of workers. Parameters in this file will override the common configuration.

The parameter `spark.4sq.test.is-remote` controls if the worker is executed within the IDE or on a remote Spark master.

[SPARK_TEST]: <src/test/java/com/kinetica/fsq/TestSpark.java>
[COMMON]: <src/test/resources/test-spark-common.properties>
[CREDS]: <src/test/resources/test-spark-creds.properties>
[REMOTE]: <src/test/resources/test-spark-remote.properties>

### Local Test

In this mode you will run the Spark job entirely within the IDE. You will need to configure `test-spark-common.properties` and `test-spark-creds.properties` with:

* AWS credentials
* Kinetica credentials
* The source parquet file
* The destination table

If the destination table exists then it will be truncated. If it does not exist then DDL will be generated from the Dataframe. If the DDL is generated there will be a loss of `VARCHAR(N)` lengths. If this happens you can load the DDL from an [existing script][DDL] or update the columns in GAdmin.

[DDL]: <scripts/places.ddl>

### Remote Test

In this mode you will run the spark driver within the IDE and the spark worker will run on a remote master. To invoke this mode set `spark.4sq.test.is-remote = true`. This use case will require that the application JAR is compiled and that necessary JARs are available on the remote master. You will need to configure the following in `test-spark-remote.properties`:

* URL of remote spark master.
* IP of the local host as seen from the spark master.
* Location of the application JAR
* Location of jar dependencies (see below)

### JAR Dependencies

In order to reduce the size of the application jar some dependencies are copied to the remote host. If this is not done the application jar would be > 300M and it would need to be copied to the spark master before every job.

The dependencies can be downloaded from maven at the following locations. 

* [kinetica-jdbc-7.1.8.6-shaded.jar](https://nexus.kinetica.com/#browse/browse:releases:com%2Fkinetica%2Fkinetica-jdbc%2F7.1.8.6%2Fkinetica-jdbc-7.1.8.6-shaded.jar)
* [hadoop-aws-3.3.1.jar](https://mvnrepository.com/artifact/org.apache.hadoop/hadoop-aws/3.3.1)
* [aws-java-sdk-bundle-1.11.901.jar](https://nexus.kinetica.com/#browse/browse:releases:com%2Fkinetica%2Fkinetica-jdbc%2F7.1.8.6%2Fkinetica-jdbc-7.1.8.6-shaded.jar)

The jars should be copied to a location on the spark worker nodes and referenced in `spark.executor.extraClassPath`.

*Note: The Kinetica JDBC jar uses the `shaded` classifier to avoid a dependency version conflict with the Spark distribution.*

## References

See also: 

* [Kinetica Spark Guide](https://docs.kinetica.com/7.1/connectors/spark_guide/)

Apache Spark Documentation:

* [Scalar User Defined Functions (UDFs)](https://spark.apache.org/docs/latest/sql-ref-functions-udf-scalar.html)
* [JDBC To Other Databases](https://spark.apache.org/docs/latest/sql-data-sources-jdbc.html)
* [Integration with Cloud Infrastructures](https://spark.apache.org/docs/latest/cloud-integration.html)
* [Spark Configuration](https://spark.apache.org/docs/latest/configuration.html)
