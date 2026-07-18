package com.chrisluttazi.spark.etl.commons

import org.apache.spark.sql.DataFrame

import scala.annotation.tailrec
import scala.util.Try

trait Transformer {

  /**
   * Renames all the columns of a [[DataFrame]]
   *
   * @param cols
   *   : A list of type String -> String
   * @param df
   *   : SE
   * @return
   */
  def renameColumns(cols: List[(String, String)], df: DataFrame): Try[DataFrame] = {
    def renameColumn(tuple: (String, String), df: DataFrame): DataFrame =
      df.withColumnRenamed(tuple._1, tuple._2)

    Try(applyToDF(cols, renameColumn, df))
  }

  /**
   * Applies a function of type (A, [[DataFrame]]) to a [[List]] of the same type.
   *
   * @param list
   *   : SE
   * @param f
   *   : SE
   * @param df
   *   : SE
   * @tparam A
   *   : The type of the list
   * @return
   */
  @tailrec
  final def applyToDF[A](list: List[A], f: (A, DataFrame) => DataFrame, df: DataFrame): DataFrame =
    list match {
      case Nil => df
      case head :: tail => applyToDF(tail, f, f(head, df))
    }
}
