from gpudb.gpudb import GPUdb
import random
from datetime import datetime, timedelta

from airflow import DAG
from airflow.operators.python import PythonOperator
#from fsq.airflow.dag_tag import FsqDagTag
#from fsq.airflow.fsq_default_args import fsq_default_args

#default_args = {**fsq_default_args, "owner": "ali", "start_date": datetime(2019, 1, 1)}
default_args = {"owner": "ali", "start_date": datetime(2019, 1, 1)}

#dag = DAG("kinetica-example-dag", default_args=default_args, schedule_interval=timedelta(days=1), tags=[FsqDagTag.EXAMPLE.name])
dag = DAG("kinetica-example-dag", default_args=default_args, schedule_interval=timedelta(days=1), tags=["kinetica"])

with dag:
    first_operator = PythonOperator(
        task_id="first-operator", python_callable=lambda: random.choice(["factual", "4sq", "placed", "unfolded"])
    )
    second_operator = PythonOperator(
        task_id="second-operator",
        python_callable=lambda x: print(x),  # pylint: disable=unnecessary-lambda
        op_args=[first_operator.output],
    )

    use_str_for_backwards_compat = PythonOperator(
        task_id="using-str",
        python_callable=lambda x: print(x),  # pylint: disable=unnecessary-lambda
        op_args=[str(first_operator.output) + " is the best!"],
    )

    # note that using str(task.output) eliminates the automatic dependency
    second_operator >> use_str_for_backwards_compat
