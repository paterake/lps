package com.paterake.lps.address.cfg.reader

import com.paterake.lps.address.cfg.model.ModelCfgRegion

class CfgRegion(cfgName: String) extends CfgReader {
  private val cfg = readConfiguration[List[ModelCfgRegion]](cfgName)

  def this() {
    this("cfgRegion")
  }

  def getCfg(): List[ModelCfgRegion] = {
    cfg
  }

  def getCfg(regionName: String): List[ModelCfgRegion] = {
    cfg.filter(p => regionName.toLowerCase.startsWith(p.regionName))

  }

  def getCfgRename(regionName: String): String = {
    val clcnCfgRegion = getCfg(regionName)
    val newName = if (clcnCfgRegion.isEmpty || clcnCfgRegion.isEmpty || clcnCfgRegion.head.rename == null) {
      regionName
    } else {
      clcnCfgRegion.head.rename
    }
    newName
  }

  def getCfgBlankPages(regionName: String): Int = {
    val clcnCfgRegion = getCfg(regionName)
    val cfgRegion = if (clcnCfgRegion.isEmpty || clcnCfgRegion.isEmpty) {
      cfg.filter(p => p.regionName.toLowerCase.equals("default")).head
    } else {
      clcnCfgRegion.head
    }
    cfgRegion.blankPageCount
  }

}
