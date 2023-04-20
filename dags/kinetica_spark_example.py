##
# Copyright (c) 2023, Chad Juliano, Kinetica DB Inc.
##

import pendulum
from airflow.decorators import dag, task
from kinetica.operator.spark import KineticaSparkOperator
from airflow.models.baseoperator import chain
from airflow.models.param import Param

@dag(
    start_date=pendulum.datetime(2021, 1, 1, tz="UTC"),
    catchup=False,
    schedule=None,
    tags=["kinetica"],
    params={ 
        "s3_prefix": Param(default="s3a://4sq-partner-grapheval/datasets/generated/foursquareweb", type="string"),
        "schema": Param(default="foursquare", type="string"),
    }
)
def kinetica_spark_example():
    """
    ### Kinetica Spark Example 

    This example excecutes a `SparkSubmitHook` that will run the Kinetica import job `com.kinetica.fsq.TransformPlaces`. 

    ### Testing

    You can run this command for a unit test:
    
    ```
    $ airflow tasks test kinetica_spark_example kinetica_spark_operator
    ```
    """

    kinetica_spark_operator = KineticaSparkOperator(
        doc_md='Demonstrate Spark submit.',
        task_id="kinetica_spark_operator",
        dest_table = "{{params.schema}}.places_test",
        source_file = "{{params.s3_prefix}}/fsq-graph-place/place/dt=2023-03-14/",
        limit = 1000,
        parallelism = 4,
        verbose = True,
    )

    kinetica_spark_operator

dag = kinetica_spark_example()
