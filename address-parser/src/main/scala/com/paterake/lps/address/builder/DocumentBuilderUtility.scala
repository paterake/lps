package com.paterake.lps.address.builder

import com.paterake.lps.address.cfg.model.ModelCfgIndex
import org.apache.poi.xwpf.usermodel.{XWPFDocument, XWPFParagraph}

import java.awt.{Font, GraphicsEnvironment}
import java.io.FileInputStream
import scala.collection.mutable.ListBuffer

object DocumentBuilderUtility {
  val fontDefault = "Noto Sans"
  //val fontGujarati = "Noto Sans Gujarati"
  val fontGujarati = "Gujarati Sangam MN"

  var pageCount = 104
  var lineCount = 0
  val clcnNameIdx = new ListBuffer[ModelCfgIndex]()


  def registerFont(fontLocation: String): Unit = {
    val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
    val is = new FileInputStream(fontLocation)
    val fontGuj = Font.createFont(Font.TRUETYPE_FONT, is)
    is.close()
    ge.registerFont(fontGuj)
  }

  def registerFont(): Unit = {
    registerFont("/home/paterake/.fonts/GujaratiSangamMN.ttf")
    registerFont("/home/paterake/.fonts/NotoSans-Regular.ttf")
    registerFont("/home/paterake/.fonts/NotoSans-Bold.ttf")
    registerFont("/home/paterake/.fonts/NotoSansGujarati-Bold.ttf")
    registerFont("/home/paterake/.fonts/NotoSansGujarati-Regular.ttf")
  }

  def getNewDocument(): XWPFDocument = {
    val doc = new XWPFDocument()
    registerFont()
    /*
    val document = doc.getDocument
    val body = document.getBody

    if (!body.isSetSectPr) body.addNewSectPr
    val section = body.getSectPr

    if (!section.isSetPgSz) section.addNewPgSz
    val pageSize = section.getPgSz
    //pageSize.setOrient(STPageOrientation.PORTRAIT)
    pageSize.setH(BigInteger.valueOf(595))
    pageSize.setW(BigInteger.valueOf(420))
    */
    doc
  }

  def getParagraph(document: XWPFDocument): XWPFParagraph = {
    val paragraph = document.createParagraph()
    if (paragraph.getCTP.getPPr == null) paragraph.getCTP.addNewPPr()
    if (paragraph.getCTP.getPPr.getShd != null) paragraph.getCTP.getPPr.unsetShd()

    if (paragraph.getCTP.getPPr.getTabs != null)  paragraph.getCTP.getPPr.unsetTabs()
    if (paragraph.getCTP.getPPr.isSetTabs) paragraph.getCTP.getPPr.unsetTabs()

    paragraph
  }

  def appendNameIndex(entry: ModelCfgIndex): Unit = {
    clcnNameIdx.append(entry)
  }

}
