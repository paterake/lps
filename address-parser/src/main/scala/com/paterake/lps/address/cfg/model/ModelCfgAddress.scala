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
                           , singleInd: Boolean
                           , singleIdx: Int
                           , clcnLineElementRight: List[String]
                           , elementSeparatorRight: String
                           , fontRight: String
                           , fontSizeRight: Int
                           , textAlignmentRight: String
                           , singleIndRight: Boolean
                           , singleIdxRight: Int
                           , indexInd: Boolean
                           , dropSurname: Boolean
                          )
