package com.chrisluttazi.spark.etl.commons

import com.chrisluttazi.spark.etl.commons.helpers.SparkTestSession
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TransformerSpec extends AnyFlatSpec with Matchers with Transformer {
  private val spark: SparkSession = SparkTestSession.get

  import spark.implicits._

  // Regression: `case nil` (a variable pattern) used to match every list,
  // so renameColumns silently returned the DataFrame unchanged.
  "renameColumns" should "rename every listed column" in {
    val df = Seq((1, "a")).toDF("old1", "old2")
    val renamed = renameColumns(List("old1" -> "new1", "old2" -> "new2"), df)

    renamed.isSuccess shouldBe true
    renamed.get.columns.toList shouldBe List("new1", "new2")
  }

  "applyToDF" should "apply the function once per list element" in {
    val df = Seq(1, 2, 3).toDF("n")
    val addCol: (String, DataFrame) => DataFrame = (name, d) => d.withColumn(name, d("n"))

    val result = applyToDF(List("a", "b"), addCol, df)

    result.columns.toList shouldBe List("n", "a", "b")
  }

  it should "return the DataFrame unchanged for an empty list" in {
    val df = Seq(1).toDF("n")

    applyToDF(Nil, (_: String, d: DataFrame) => d.drop("n"), df).columns.toList shouldBe List("n")
  }
}
