package com.chrisluttazi.spark.etl.commons

import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.Try

trait Extractor {

  /**
   * Tries to read from a path and create a [[DataFrame]] The order is orc, parquet, json and csv
   *
   * @param spark
   *   : [[SparkSession]]
   * @param paths
   *   : List of paths, commaseparated
   * @return
   */
  def extract(spark: SparkSession, paths: String*): Try[DataFrame] =
    Try(spark.read.format("orc").load(paths: _*))
      .orElse(Try(spark.read.parquet(paths: _*)))
      .orElse(Try(spark.read.json(paths: _*)))
      .orElse(Try(spark.read.csv(paths: _*)))
}
