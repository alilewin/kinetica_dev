/*
 * Copyright (c) 2023, Chad Juliano, Kinetica DB Inc.
 */

package com.kinetica.fsq

import com.kinetica.fsq.SimpleAppScala.spark
import com.uber.h3core.H3CoreV3
import org.apache.spark.internal.Logging
import org.apache.spark.sql._
import org.apache.spark.sql.functions.{col, to_json, udf}
import org.apache.spark.sql.jdbc.JdbcDialects
import org.apache.spark.sql.types.StringType

import scala.collection.mutable.ListBuffer

/**
 * Spark-submit application for loading of the places table.
 */
object TransformPlaces extends Logging {
    private var spark: SparkSession = _
    private val colVals = new ListBuffer[Column]()
//    private val colSchema = new ListBuffer[String]()

    /**
     * Entry point for spark application.
     */
    def main(args: Array[String]): Unit = {
        spark = SparkSession.builder
          //.appName(this.getClass.getCanonicalName)
          .getOrCreate()

        spark.sparkContext.setLogLevel("DEBUG")

        val parquetSourcePath = spark.conf.get("spark.4sq.transform.source")
        val destTableName = spark.conf.get("spark.4sq.transform.dest-table")
        val limitStr: Option[String] = spark.conf.getOption("spark.4sq.transform.limit")
        val rowLimit : Option[Int] = limitStr.map(_.toInt)

        val inputDf = readParquet(parquetSourcePath, rowLimit)
        addH3Column()
        val outputDf = convertColumnsToJson(inputDf)
        writeJdbc(outputDf, destTableName, spark.conf)

        spark.stop()
    }

    /**
     * Read a DataFrame from a parquet source file
     * @param parquetPath S3 bucket with parquet file
     * @param rowLimit      optional row result limit
     * @return
     */
    private def readParquet(parquetPath: String, rowLimit: Option[Int]): DataFrame = {
        logInfo(s"Reading data from: $parquetPath")
        var df = spark.read.parquet(parquetPath)
        logInfo(s"Input rowcount: ${df.count()}")

        if (rowLimit.isDefined) {
            logInfo(s"Limiting results to: ${rowLimit.get} rows")
            df = df.limit(rowLimit.get)
        }

        logInfo(s"Input schema:\n ${df.schema.treeString(Int.MaxValue)}")
        df
    }

    private val h3Inst: H3CoreV3 = H3CoreV3.newInstance

    private def addH3Column(): Unit = {
        val h3Resolution = 3

        // this UDF will be used to calculate the H3 geospatial hash
        val udfGetH3Hash = udf((lat: Double, lon: Double) => h3Inst.geoToH3(lat, lon, h3Resolution))

        // here we specify the columns used to generate the H3 hash
        colVals += udfGetH3Hash(
            col("geocodes.mainEntry.latitude"),
            col("geocodes.mainEntry.longitude"))
          .alias("h3hash")

        // Let the dialect know the column index of the shard key (which is h3hash)
        KineticaDialect.setShardKeyIdx(0)
    }

    /**
     * Convert some column to JSON
     * @return
     */
    private def convertColumnsToJson(inputDf: DataFrame) = {
//        def getJdbcDDL(colType: StructField, newType: String): String = {
//            val nullString = if (colType.nullable) "" else " NOT NULL"
//            s"${quoteIfNeeded(colType.name)} $newType$nullString"
//        }

        inputDf.schema.foreach(colType => {
            if (!colType.dataType.isInstanceOf[StringType]) {
                colVals += to_json(col(colType.name)).alias(colType.name)
            }
            else {
                colVals += col(colType.name)

                // Set to the maximum kinetica varchar. This is a waste or RAM so
                // Don't forget to reduce these columns in Kinetica later with the stats tool.
                //colSchema += getJdbcDDL(colType, "VARCHAR(255)")
            }
        })

        val outputDf = inputDf.select(colVals: _*)

        logInfo(s"Output spark schema:\n ${outputDf.schema.treeString(Int.MaxValue)}")
        outputDf
    }

    private def writeJdbc(df: DataFrame, tableName: String, conf: RuntimeConfig): Unit = {
        val jdbcOpts = Map(
            "driver" -> "com.kinetica.jdbc.Driver",
            "url" -> conf.get("spark.4sq.jdbc.url"),
            "UID" -> conf.get("spark.4sq.jdbc.user"),
            "PWD" -> conf.get("spark.4sq.jdbc.password.key"),
            "UpdateOnExistingPk" -> "true"
        )
        logInfo(s"JDBC URL: ${jdbcOpts.get("url")}")

        logInfo(s"Writing data to table: $tableName")
        JdbcDialects.registerDialect(KineticaDialect)

        df.write
          .format("jdbc")
          .options(jdbcOpts)
          .option("dbtable", tableName)
          //.option("createTableColumnTypes", colSchema.mkString(","))
          .option("batchsize", 1000)
          .option("truncate", "true")
          .mode(SaveMode.Overwrite)
          .save()
    }
}
