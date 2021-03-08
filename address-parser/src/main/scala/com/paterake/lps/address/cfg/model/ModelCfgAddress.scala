package com.paterake.lps.address.cfg.model

case class ModelCfgAddress(lineId: Int
                           , lineComment: String
                           , clcnLineElement: List[String]
                           , clcnParenthesis: List[String]
                           , elementSeparator: String
                           , fontCase: String
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
