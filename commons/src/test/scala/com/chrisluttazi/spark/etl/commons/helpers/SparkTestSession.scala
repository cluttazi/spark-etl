package com.chrisluttazi.spark.etl.commons.helpers

import org.apache.spark.sql.SparkSession

class SparkTestSession {
  def get: SparkSession =
    SparkSession.builder
      .master("local")
      .appName("SparkTestSession")
      // The suite only reads/writes files; the native ORC implementation
      // avoids spinning up the embedded Hive metastore (Derby), which is
      // not writable on every CI runner.
      .config("spark.sql.orc.impl", "native")
      .getOrCreate()
}

object SparkTestSession extends SparkTestSession
