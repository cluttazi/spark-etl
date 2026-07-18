package com.chrisluttazi.spark.etl.commons

import java.nio.file.Files

import com.chrisluttazi.spark.etl.commons.enums.LoadType
import com.chrisluttazi.spark.etl.commons.helpers.SparkTestSession
import org.apache.spark.sql.SparkSession
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LoaderSpec extends AnyFlatSpec with Matchers with Loader {
  private val spark: SparkSession = SparkTestSession.get

  import spark.implicits._

  "load" should "write a DataFrame in the requested format" in {
    val path = Files.createTempDirectory("loader-spec-single").resolve("out").toString
    val df = (1 to 5).toDF("n")

    load(LoadType.PARQUET, path, df).isSuccess shouldBe true
    spark.read.parquet(path).count() shouldBe 5
  }

  // Regression: `case nil` (a variable pattern) used to match every list,
  // so loadList silently wrote nothing.
  "loadList" should "write every DataFrame in the list" in {
    val base = Files.createTempDirectory("loader-spec-list").toString
    val df = (1 to 5).toDF("n")

    val result = loadList(
      List(
        (LoadType.PARQUET, s"$base/parquet", df),
        (LoadType.JSON, s"$base/json", df)
      )
    )

    result.isSuccess shouldBe true
    spark.read.parquet(s"$base/parquet").count() shouldBe 5
    spark.read.json(s"$base/json").count() shouldBe 5
  }
}
