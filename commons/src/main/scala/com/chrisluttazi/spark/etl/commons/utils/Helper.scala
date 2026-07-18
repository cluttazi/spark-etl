package com.chrisluttazi.spark.etl.commons.utils

import java.io.File

class Helper {

  def listFilesInDirectory(file: File, pattern: String): List[File] = {
    def isFileAndPattern(f: File): Boolean =
      f.isFile && f.getName.contains(pattern)

    val list = file.listFiles
    list.filter(isFileAndPattern).toList ++
      list.filter(_.isDirectory).flatMap(p => listFilesInDirectory(p, pattern))
  }
}

object Helper extends Helper
