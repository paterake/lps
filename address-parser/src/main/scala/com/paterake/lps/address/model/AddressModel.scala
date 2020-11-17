package com.paterake.lps.address.model

import scala.collection.mutable.ListBuffer

class AddressModel {
  val addressCfg = scala.io.Source.fromInputStream(this.getClass.getResourceAsStream("/addressCfg.txt"))
  val clcnAddressLine = ListBuffer.empty[(Int, Array[String])]

  def getAddressCfg(): List[(Int, Array[String])] = {
    addressCfg.getLines().foreach(line => {
      val tidyLine = line.replaceAll(" ", "")
      clcnAddressLine.append((tidyLine.split(",").head.toInt
        , tidyLine.split(",").tail))
    })
    clcnAddressLine.toList
  }
}
