FROM apache/airflow:2.5.1
USER root
RUN apt update
RUN apt install vim procps net-tools -y
RUN apt install default-jdk -y
USER airflow
RUN pip install --upgrade pip
RUN pip install --no-cache-dir gpudb
RUN pip install --no-cache-dir apache-airflow-providers-apache-spark
