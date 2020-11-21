package com.paterake.lps.address.cfg.reader

import com.paterake.lps.address.cfg.model.ModelCfgAddress

class CfgAddress(cfgName: String) extends CfgReader {
  private val cfg = readConfiguration[List[ModelCfgAddress]](cfgName)

  def this() {
    this("cfgAddress")
  }

  def getCfg(): List[ModelCfgAddress] = {
    cfg
  }

}
