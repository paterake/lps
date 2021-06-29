package com.paterake.lps.address.cfg.reader

import com.paterake.lps.address.cfg.model.ModelCfgBlank

class CfgBlank(cfgName: String) extends CfgReader {
  private val cfg = readConfiguration[List[ModelCfgBlank]](cfgName)

  def this() {
    this("cfgBlank")
  }

  def getCfg(): List[ModelCfgBlank] = {
    cfg
  }

  def getCfg(regionName: String, villageName: String, memberName: String): List[ModelCfgBlank] = {
    cfg.filter(p => {
      regionName.toLowerCase.split(" ").head.replaceAll(",", "").equals(p.regionName.toLowerCase) &&
        villageName.toLowerCase.equals(p.villageNameMarker.toLowerCase) &&
        memberName.toLowerCase.startsWith(p.memberNameMarker.toLowerCase)
    })
  }

  def getBlankPageCount(regionName: String, villageName: String, memberName: String): Int = {
    val entry = getCfg(regionName, villageName, memberName)
    if (entry.nonEmpty) {
      //println(regionName + ":" + villageName + ":" + memberName + "=" + entry.head.blankPageCount)
      entry.head.blankPageCount
    } else {
      0
    }
  }

}
