/*
 * Copyright (c) 2023, Chad Juliano, Kinetica DB Inc.
 */

package com.kinetica.fsq

import org.apache.spark.sql.AnalysisException
import org.apache.spark.sql.execution.datasources.jdbc.{JDBCOptions, JdbcUtils}
import org.apache.spark.sql.jdbc.{JdbcDialect, JdbcType}
import org.apache.spark.sql.types.{DataType, LongType}

import java.sql.{Connection, Types}
import java.util.Locale

object KineticaDialect extends JdbcDialect {

    private var shardKeyIdx : Int = -1

    private var currentIdx : Int = -1

//    override def createConnectionFactory(options: JDBCOptions): Int => Connection = {
//        val result: Int => Connection = super.createConnectionFactory(options)
//        val conn: Connection = result(-1)
//        (partitionId: Int) => conn
//    }

    /**
     * Set the index of the shard key. This must be called before each time the jdbc DataFrameWriter
     * writes to a table.
     *
     * Note: Ideally there would be an API that would allow us to set a custom column type
     * that the JDBC writer would use to create the SQL DDL. Unfortunately this hack is required to work around
     * the issue.
     */
    def setShardKeyIdx(idx: Int): Unit = {
        shardKeyIdx = idx
        currentIdx = -1
    }

    override def canHandle(url: String): Boolean =
        url.toLowerCase(Locale.ROOT).startsWith("jdbc:kinetica")

    override def getJDBCType(dt: DataType): Option[JdbcType] = {
        // This is a hack to work around
        currentIdx += 1
        val isShardKey = shardKeyIdx == currentIdx

        val sqlType = dt match {
            //        case StringType => Some(JdbcType("VARCHAR(255)", Types.CHAR))
            case LongType if isShardKey => Some(JdbcType("BIGINT(shard_key)", Types.CHAR))
            //        case FloatType => Some(JdbcType("FLOAT4", Types.FLOAT))
            //        case DoubleType => Some(JdbcType("FLOAT8", Types.DOUBLE))
            //        case t: DecimalType => Some(JdbcType(s"NUMERIC(${t.precision},${t.scale})", java.sql.Types.NUMERIC))
            case _ => JdbcUtils.getCommonJDBCType(dt)
        }
        logDebug(s"getJDBCType: $dt => ${sqlType.get.databaseTypeDefinition}")
        sqlType
    }

    override def isCascadingTruncateTable(): Option[Boolean] = Some(false)

    override def getTableExistsQuery(table: String): String = {
        //s"""SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = '$table' LIMIT 1;"""
        s"SELECT * FROM $table limit 0"
    }

    override def getSchemaQuery(table: String): String = {
        s"SELECT * FROM $table limit 0"
    }

    override def getTruncateQuery(table: String, cascade: Option[Boolean] = isCascadingTruncateTable()): String = {
        s"TRUNCATE TABLE $table"
    }

    override def classifyException(message: String, e: Throwable): AnalysisException = {
        super.classifyException(message, e)
    }
}
