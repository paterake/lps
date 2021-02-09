package com.paterake.lps.address.cfg.reader

import com.paterake.lps.address.cfg.model.ModelCfgRegion

class CfgRegion (cfgName: String) extends CfgReader {
  private val cfg = readConfiguration[List[ModelCfgRegion]](cfgName)

  def this() {
    this("cfgRegion")
  }

  def getCfg(): List[ModelCfgRegion] = {
    cfg
  }

}
