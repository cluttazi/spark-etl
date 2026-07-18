package com.chrisluttazi.spark.etl.commons

import com.chrisluttazi.spark.etl.commons.enums.LoadType
import com.chrisluttazi.spark.etl.commons.helpers.FilesCreatorHelper
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ExtractorSpec extends AnyFlatSpec with Matchers {
  "A Extractor" should "have files to extract" in {
    FilesCreatorHelper.createTestFiles
    FilesCreatorHelper.getTestFiles.size shouldBe LoadType.values.size
  }


}
