# Kinetica Provider for Apache Airflow

This package provides SQL and Spark connectivity services for Kinetica.

- [Kinetica SQL Operator](#kinetica-sql-operator)
    - [Prerequsites](#prerequsites)
    - [Kinetica Connection Setup](#kinetica-connection-setup)
- [Kinetica Spark Operator](#kinetica-spark-operator)
    - [Prerequsites](#prerequsites-1)
    - [Spark Connection Setup](#spark-connection-setup)
    - [Task Configuration](#task-configuration)
- [Docker](#docker)
- [See Also](#see-also)

## Kinetica SQL Operator

Relevant files are:

| File      | Description |
| ----------- | ----------- |
| [dags/kinetica_sql_example.py](dags/kinetica_sql_example.py)    | Example DAG |
| [dags/kinetica/operator/sql.py](dags/kinetica/operator/sql.py)  | Contains KineticaSqlOperator |
| [dags/kinetica/hooks/sql.py](dags/kinetica/operator/sql.py)     | Contains KineticaSqlOperator |

### Prerequsites

To execute this operator you will need to have:
1. Credentials to insert into a table in a Kinetica database.

### Kinetica Connection Setup

You can create a default connection with the following syntax:

```
$ airflow connections add 'kinetica_default' \
    --conn-type 'kinetica' \
    --conn-login 'admin' \
    --conn-password '???'  \
    --conn-host 'g-p100-300-301-u29.tysons.kinetica.com' \
    --conn-port '9191' \
    --conn-schema 'schema'
```

## Kinetica Spark Operator

The `KineticaSparkOperator` operator executes a `SparkSubmitHook` that will run the Kinetica import job `com.kinetica.fsq.TransformPlaces`. It is used for ingesting data from a parquet file in AWS to a Kinetica table.

Relevant files are:

| File      | Description |
| ----------- | ----------- |
| [dags/kinetica_spark_example.py](dags/kinetica_spark_example.py)    | Example DAG |
| [dags/kinetica/operator/spark.py](dags/kinetica/operator/spark.py)  | Contains KineticaSparkOperator |
| [dags/kinetica_spark.conf](dags/kinetica_spark.conf)  | Configuration file |

### Prerequsites

To execute this operator you will need to have:

1. Connection to a spark cluster.
2. Credentials to insert into a table in a Kinetica database.
3. Credentials to read a parquet file in Amazon S3

### Spark Connection Setup

The Spark REST API active on the master. You can do this with `spark.master.rest.enabled = true`. The default port for this is 6066 (not the legacy 7077 for the master). 

You will need to create a connection in Airflow of type `spark`. You can create a default connection with the following syntax:

```
$ airflow connections add 'spark_default' \
    --conn-type 'spark' \
    --conn-host 'spark://172.31.31.29:6066' \
    --conn-extra '{"deploy-mode": "cluster"}'
```

### Task Configuration

This task will look for a configuration file `kinetica_spark.conf` in the same location as the DAG. In this file you will need to configure:

* Location of appliction JAR
* Location of JAR dependencies
* Kinetica credentials
* AWS credentials

Important: Relevant JAR files will need to be copied to a location on the Spark master.

## Docker

When running the docker image you can create the airflow user.

```
useradd \
    --base-dir /home \
    --gid docker \
    --create-home \
    --comment 'PIN Shared User' \
    --uid 50000 \
    airflow
```

## See Also

* [Kinetica Python API](https://docs.kinetica.com/7.1/api/python/)
* [Managing Connections](https://airflow.incubator.apache.org/docs/apache-airflow/stable/howto/connection.html)
* [SQL Operators](https://airflow.apache.org/docs/apache-airflow-providers-common-sql/stable/operators.html)
* [Kinetica SQL](https://docs.kinetica.com/7.1/sql/)
