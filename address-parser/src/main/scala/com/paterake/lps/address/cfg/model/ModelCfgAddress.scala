package com.paterake.lps.address.cfg.model

case class ModelCfgAddress(lineId: Int
                           , clcnLineElement: List[String]
                           , clcnLineElementRight: List[String]
                           , clcnParenthesis: List[String]
                           , elementSeparator: String
                           , elementSeparatorRight: String
                           , font: String
                           , fontSize: Int
                           , textAlignment: String
                          )
