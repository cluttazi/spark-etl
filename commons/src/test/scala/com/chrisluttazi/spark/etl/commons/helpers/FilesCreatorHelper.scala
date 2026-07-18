package com.chrisluttazi.spark.etl.commons.helpers

import java.io.File

import com.chrisluttazi.spark.etl.commons.enums.LoadType
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

/**
 * Creates files in order to execute the Suite
 */
class FilesCreatorHelper {
  val TEST_FILES_PREFIX: String = "numbers"
  val spark: SparkSession = SparkTestSession.get
  val resPath: String = this.getClass.getClassLoader.getResource(TEST_FILES_PREFIX).getPath

  def createTestFiles: Unit = {
    import spark.implicits._
    val rdd: RDD[Int] = spark.sparkContext.parallelize(1 to 10)
    val df: DataFrame = rdd.toDF(TEST_FILES_PREFIX)

    for (value <- LoadType.values.map(_.toString)) {
      val path = s"$resPath.$value"
      df.write.mode(SaveMode.Ignore).format(value).save(path)
    }
  }

  def getTestFiles: List[File] =
    // `numbers` on the classpath is an empty marker file; the datasets are
    // written next to it as `numbers.<format>` directories (Spark output
    // is always a directory of part files), so list the parent.
    new File(resPath).getParentFile.listFiles
      .filter(f => f.isDirectory && f.getName.startsWith(s"$TEST_FILES_PREFIX."))
      .toList

}

object FilesCreatorHelper extends FilesCreatorHelper
