/*
 * Copyright (c) 2023, Chad Juliano, Kinetica DB Inc.
 */

package com.kinetica.fsq;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

public class TestSpark {
    private static final Logger LOG = LoggerFactory.getLogger(TestSpark.class);
    private static final String TEST_COMMON_PROPERTIES = "/test-spark-common.properties";
    private static final String TEST_REMOTE_PROPERTIES = "/test-spark-remote.properties";
    private static final String TEST_CREDS_PROPERTIES = "/test-spark-creds.properties";

    @Test
    public void testTransformPlaces() {
        SparkSession.builder()
                .appName(TransformPlaces.class.getCanonicalName())
                .getOrCreate();
        TransformPlaces.main(null);
    }

    @Test
    public void testSimpleAppJava() {
        SimpleAppJava.main(null);
    }

    @Test
    public void testSimpleAppScala() {
        SimpleAppScala.main(null);
    }

    @Test
    public void testSparkSession() {
        SparkConf conf = new SparkConf();
        SparkSession.builder()
                .config(conf)
                .appName(this.getClass().getCanonicalName())
                .getOrCreate();
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        Properties sparkProps = new Properties();
        getCommonConfig(sparkProps);

        String isRemoteStr = sparkProps.getProperty("spark.4sq.test.is-remote", "false");
        boolean isRemote = Boolean.parseBoolean(isRemoteStr);

        if(isRemote) {
            appendConfig(sparkProps, TEST_REMOTE_PROPERTIES);
        }
        // we add the spark properties that will get picked up when the SparkConf is created.
        System.getProperties().putAll(sparkProps);
    }

    @After
    public void afterTest() {
        LOG.info("Cleanup any active session.");
        SparkSession.active().stop();
    }

    private static void getCommonConfig(Properties sparkProps) throws IOException {
        appendConfig(sparkProps, TEST_COMMON_PROPERTIES);
        appendConfig(sparkProps, TEST_CREDS_PROPERTIES);

        // the app name could be overridden by the spark application
        sparkProps.put("spark.app.name", TestSpark.class.getCanonicalName());
        sparkProps.put("spark.app.id", UUID.randomUUID().toString());
    }

    private static void appendConfig(Properties sparkProps, String fileName) throws IOException {
        try(InputStream is = TestSpark.class.getResourceAsStream(fileName)) {
            if(is == null) {
                throw new IOException("Not found: " + fileName);
            }
            sparkProps.load(is);
        }
    }
}
