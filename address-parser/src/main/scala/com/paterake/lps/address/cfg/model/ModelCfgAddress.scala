package com.paterake.lps.address.cfg.model

case class ModelCfgAddress(lineId: Int
                           , clcnLineElement: List[String]
                           , clcnParenthesis: List[String]
                           , elementSeparator: String
                           , font: String
                           , fontSize: Int
                           , textAlignment: String
                           , clcnLineElementRight: List[String]
                           , elementSeparatorRight: String
                           , fontRight: String
                           , fontSizeRight: Int
                           , textAlignmentRight: String
                           , indexInd: Boolean
                          )
