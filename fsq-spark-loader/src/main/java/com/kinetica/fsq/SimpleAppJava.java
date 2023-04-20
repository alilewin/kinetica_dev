/*
 * Copyright (c) 2023, Chad Juliano, Kinetica DB Inc.
 */

package com.kinetica.fsq;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;

public class SimpleAppJava {
    private static SparkSession spark;

    public static void main(String[] args) {
        spark = SparkSession.builder()
                .appName(SimpleAppJava.class.getCanonicalName())
                .getOrCreate();

        Dataset<Row> peopleDf = createPeopleDf();
        peopleDf.show();
        peopleDf.groupBy("age").count().show();

        spark.stop();
    }

    private static Dataset<Row> createPeopleDf() {
        List<Object[]> stringAsList = new ArrayList<>();
        stringAsList.add(new Object[] { "Michael", null });
        stringAsList.add(new Object[] { "Andy", 30 });
        stringAsList.add(new Object[] { "Justin", 19 });

        JavaSparkContext sparkContext = new JavaSparkContext(spark.sparkContext());
        JavaRDD<Row> rowRDD = sparkContext.parallelize(stringAsList).map(RowFactory::create);

        // Creates schema
        StructType schema = DataTypes.createStructType(new StructField[] {
                        DataTypes.createStructField("name", DataTypes.StringType, false),
                        DataTypes.createStructField("age", DataTypes.IntegerType, true) });

        return spark.sqlContext().createDataFrame(rowRDD, schema).toDF();
    }
}
