package com.paterake.lps.address.parse

import com.itextpdf.io.font.{PdfEncodings, TrueTypeCollection}
import com.itextpdf.kernel.font.{PdfFont, PdfFontFactory}

import java.io.File

object FontBuilder {


  def getFont(): PdfFont = {
    //val font_gujarati_location = "/home/paterake/Documents/__cfg/fonts/noto_gujarati/NotoSansGujarati-Regular.ttf"
    ///private val font_gujarati_location = "/home/paterake/Documents/__cfg/fonts/lohit-gujarati.ttf"
    //private val font_gujarati_location = "/home/paterake/Documents/__cfg/fonts/Ekatrafonts/Ekatra-N_240114.ttf"
    val font_gujarati_location = "/home/paterake/Documents/__cfg/fonts/GujaratiSangamMN.ttf"

    val font_gujarati = PdfFontFactory.createFont(font_gujarati_location, PdfEncodings.IDENTITY_H)
    font_gujarati
  }

  def listFontCollection(ttc:TrueTypeCollection): Unit = {
    val ttcSize = ttc.getTTCSize
    println(ttcSize)
    for (i <- 0 to ttcSize - 1) {
      val fontProgram = ttc.getFontByTccIndex(i)
      val fontName = fontProgram.getFontNames
      fontName.getFullName.foreach(x => x.foreach(println(_)))
    }
  }

  def getFontCollctionFont(): PdfFont = {
    val ttcLocation = "/home/paterake/Documents/__cfg/fonts/Collected_Fonts/Gujarati_Sangam_MN/Gujarati_Sangam_MN.ttc"
    val ttcLocation2 = "/home/paterake/Documents/__cfg/fonts/Collected_Fonts/Gujarati_MT/GujaratiMT.ttc"
    val ttc = new TrueTypeCollection(ttcLocation)
    //listFontCollection(ttc)
    val fontProgram = ttc.getFontByTccIndex(0)
    PdfFontFactory.createFont(fontProgram)
  }


}
