package com.chrisluttazi.spark.etl.commons

import com.chrisluttazi.spark.etl.commons.enums.LoadType
import com.chrisluttazi.spark.etl.commons.enums.LoadType.LoadType
import org.apache.spark.sql.DataFrame

import scala.annotation.tailrec
import scala.util.Try

trait Loader {

  /**
   * Based on a list of ([[LoadType]] , [[String]], [[DataFrame]]) tries to write the dataframe
   *
   * @param list
   *   : list of ([[LoadType]] , [[String]], [[DataFrame]])
   * @return
   */
  @tailrec
  final def loadList(list: List[(LoadType, String, DataFrame)]): Try[Unit] = list match {
    case Nil => Try(()) // done
    case head :: tail =>
      load(head._1, head._2, head._3) // failures are ignored; keep loading the rest
      loadList(tail)
  }

  /**
   * Load a single [[DataFrame]]
   *
   * @param loadType
   *   : SE see [[LoadType]]
   * @param path
   *   : SE
   * @param df
   *   : SE
   * @return
   */
  def load(loadType: LoadType, path: String, df: DataFrame): Try[Unit] = Try(
    loadType match {
      case LoadType.ORC => df.write.orc(path)
      case LoadType.PARQUET => df.write.parquet(path)
      case LoadType.JSON => df.write.json(path)
      case LoadType.CSV => df.write.csv(path)
    }
  )
}
