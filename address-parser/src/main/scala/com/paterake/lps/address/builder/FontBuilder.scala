package com.paterake.lps.address.builder

import com.itextpdf.io.font.{PdfEncodings, TrueTypeCollection}
import com.itextpdf.kernel.font.{PdfFont, PdfFontFactory}
import com.paterake.lps.address.parse.Location

object FontBuilder {

  def getFont(): PdfFont = {
    val font_gujarati_location = Location.font_gujarati_location

    val font_gujarati = PdfFontFactory.createFont(font_gujarati_location, PdfEncodings.IDENTITY_H)
    font_gujarati
  }

  def listFontCollection(ttc: TrueTypeCollection): Unit = {
    val ttcSize = ttc.getTTCSize
    println(ttcSize)
    for (i <- 0 to ttcSize - 1) {
      val fontProgram = ttc.getFontByTccIndex(i)
      val fontName = fontProgram.getFontNames
      fontName.getFullName.foreach(x => x.foreach(println(_)))
    }
  }

  def getFontCollctionFont(): PdfFont = {
    val ttcLocation = Location.font_collection_location
    val ttc = new TrueTypeCollection(ttcLocation)
    //listFontCollection(ttc)
    val fontProgram = ttc.getFontByTccIndex(0)
    PdfFontFactory.createFont(fontProgram)
  }


}
