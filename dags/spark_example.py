from airflow.models import DAG
from airflow.providers.apache.spark.operators.spark_submit import SparkSubmitOperator
import pendulum

args = {
    'owner': 'Airflow',
}

with DAG(
    doc_md='Demonstrate SparkSubmitOperator with SparkPi example',
    dag_id='example_spark_dag',
    catchup=False,
    default_args=args,
    schedule=None,
    start_date=pendulum.datetime(2021, 1, 1, tz="UTC"),
    tags=['spark'],
) as dag:

    # execute CLI test:
    # airflow tasks test example_spark_dag example_spark_submit
    #
    # Requires connection to REST API (e.g. spark://172.31.31.29:6066)
    # Also {"deploy-mode": "cluster"}

    python_submit_job = SparkSubmitOperator(
        doc_md='Demonstrate SparkSubmitOperator with SparkPi example',
        task_id="example_spark_submit",

        # this must be a file on the master.
        application="/mnt/data/spark/spark-3.3.1-bin-hadoop3/examples/jars/spark-examples_2.12-3.3.1.jar", 
        #application="/opt/airflow/dags/spark/examples/jars/spark-examples_2.12-3.3.1.jar", 
        
        java_class="org.apache.spark.examples.SparkPi",
        name="spark-test",
        num_executors=2,
        driver_memory="512m",
        executor_memory="512m",
        verbose=False,

        # override the binary (if we are not in REST mode)
        #spark_binary="/opt/airflow/dags/spark/bin/spark-submit",

        conf={
            # REST mode must also be enabled on the master.
            "spark.master.rest.enabled" : "true"

            # We only need this if we are not running in rest mode.
            #"spark.driver.port" : "9201",
            #"spark.driver.host" : "localhost",
            #"spark.driver.bindAddress" : "0.0.0.0",
        }
    )

    python_submit_job
