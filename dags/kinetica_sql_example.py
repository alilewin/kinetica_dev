##
# Copyright (c) 2023, Chad Juliano, Kinetica DB Inc.
##

import pendulum
from textwrap import dedent
from airflow.decorators import dag, task
from airflow.models.param import Param
from airflow.models.baseoperator import chain
from kinetica.operator.sql import KineticaSqlOperator


@dag(
    start_date=pendulum.datetime(2021, 1, 1, tz="UTC"),
    catchup=False,
    schedule=None,
    tags=["kinetica"],
    params={ "schema": Param(default="foursquare", type="string") }
)
def kinetica_sql_example():
    """
    ### Kinetica Example DAG
    This DAG contains 3 tasks for testing SQL connectivity to the Kinetica database and it has a custom param `schema`.

    ### Testing

    You can run the DAG directly from the web UI or you can execute tasks independently:
    
    ```
    $ airflow tasks test kinetica_sql_example kinetica_sql_ddl
    $ airflow tasks test kinetica_sql_example kinetica_sql_multi_line
    $ airflow tasks test kinetica_sql_example kinetica_sql_hook
    ```
    """

    kinetica_sql_ddl = KineticaSqlOperator(
        doc_md='Demonstrate DDL with templating.',
        task_id="kinetica_sql_ddl",
        sql='''
            CREATE OR REPLACE TABLE "{{ params.schema }}"."categories_test"
            (
                "categoryIdentifier" VARCHAR (primary_key, 256, dict) NOT NULL,
                "name" VARCHAR (64, dict),
                "logo_prefix" VARCHAR (1),
                "logo_suffix" VARCHAR (1),
                "logo_actualResolution_height" INTEGER,
                "logo_actualResolution_width" INTEGER,
                "parentCategory" VARCHAR (8, dict),
                "dt" DATETIME NOT NULL
            )
        ''',
        split_statements=True,
        return_last=False
    )

    kinetica_sql_multi_line = KineticaSqlOperator(
        doc_md='Demonstrate multi-line SQL',
        task_id="kinetica_sql_multi_line",
        sql='''
            SELECT 1; 
            SELECT '{{ ds }}';
        ''',
        split_statements=True,
        return_last=False
    )

    @task
    def kinetica_sql_hook(params={}):
        """
        ### Example Hook

        Use the KineticaHook to create a GPUdb connection and execute a SQL query.
        """
        import logging
        TLOG = logging.getLogger("airflow.task")
        TLOG.info(f"Got params: {params}")

        from dags.kinetica.hooks.sql import KineticaHook

        kinetica_hook = KineticaHook()
        kdbc = kinetica_hook.get_conn()

        KineticaHook.execute_sql(kdbc, f'''\
            CREATE OR REPLACE TABLE "{ params['schema'] }"."categories_test"
            (
                "categoryIdentifier" VARCHAR (primary_key, 256, dict) NOT NULL,
                "name" VARCHAR (64, dict),
                "logo_prefix" VARCHAR (1),
                "logo_suffix" VARCHAR (1),
                "logo_actualResolution_height" INTEGER,
                "logo_actualResolution_width" INTEGER,
                "parentCategory" VARCHAR (8, dict),
                "dt" DATETIME NOT NULL
            )
        ''')

    #kinetica_sql_ddl >> kinetica_sql_multi_line >> kinetica_sql_hook()
    chain(kinetica_sql_ddl, kinetica_sql_multi_line, kinetica_sql_hook())

dag = kinetica_sql_example()
