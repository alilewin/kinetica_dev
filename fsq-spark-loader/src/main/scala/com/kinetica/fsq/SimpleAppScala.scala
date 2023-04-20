/*
 * Copyright (c) 2023, Chad Juliano, Kinetica DB Inc.
 */

package com.kinetica.fsq

import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

object SimpleAppScala {
    private var spark: SparkSession = _

    def main(args: Array[String]): Unit = {
        spark = SparkSession.builder
          .appName(this.getClass.getCanonicalName)
          .getOrCreate()

        val personDf = createPeopleDf()
        personDf.show()
        personDf.groupBy("age").count().show()

        spark.stop()
    }

    def createPeopleDf(): DataFrame = {
        val someData = Seq(
            Row("Michael", null),
            Row("Andy", 30),
            Row("Justin", 19)
        )

        val someSchema = List(
            StructField("name", StringType, nullable = false),
            StructField("age", IntegerType, nullable = true)
        )

        val someDF = spark.createDataFrame(
            spark.sparkContext.parallelize(someData),
            StructType(someSchema)
        )

        someDF
    }
}
