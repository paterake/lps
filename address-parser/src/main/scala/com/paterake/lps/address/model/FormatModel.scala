package com.paterake.lps.address.model

import scala.collection.mutable.ListBuffer

class FormatModel {
  val formatCfg = scala.io.Source.fromInputStream(this.getClass.getResourceAsStream("/formatCfg.txt"))
  val clcnFormat = ListBuffer.empty[String]

  def getFormatCfg(): List[String] = {
    formatCfg.getLines().foreach(line => {
      val tidyLine = line.replaceAll(" ", "")
      clcnFormat.append(tidyLine)
    })
    clcnFormat.toList
  }


}
