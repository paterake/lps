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

  def getCfg(regionName: String): ModelCfgRegion = {
    val clcnCfgRegion = cfg.filter(p => p.regionName.toLowerCase.equals(regionName.toLowerCase))
    val cfgRegion = if (clcnCfgRegion.isEmpty || clcnCfgRegion.size == 0) {
      cfg.filter(p => p.regionName.toLowerCase.equals("default")).head
    } else {
      clcnCfgRegion.head
    }
    cfgRegion
  }

}
