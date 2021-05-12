package com.paterake.lps.address.builder

import com.paterake.lps.address.cfg.model.ModelCfgAddress
import org.apache.poi.xwpf.usermodel.{ParagraphAlignment, TableRowAlign, XWPFDocument, XWPFParagraph}
import org.openxmlformats.schemas.wordprocessingml.x2006.main.{STHeightRule, STJc}

import java.awt.{Font, GraphicsEnvironment}
import java.io.{File, FileInputStream, FileOutputStream}
import java.math.BigInteger

class DocumentBuilder(outputFileName: String, clcnTranslation: Map[String, String]) {

  private val document: XWPFDocument = getNewDocument()

  private var pageCount = 1
  private var lineCount = 0
  private val fontDefault = "Noto Sans"
  private val fontGujarati = "Noto Sans Gujarati"
  //private val fontGujarati = "Gujarati Sangam MN"


  def setPageBreak(): Unit = {
    val break = document.createParagraph()
    break.setPageBreak(true)
    pageCount += 1
  }

  def resetLineCount(): Unit = {
    lineCount = 0
  }

  def incrementLineCount(): Unit = {
    lineCount += 1
  }

  def startNewPage(header: String, blankPageCount: Int): Unit = {
    for (x <- 0 to blankPageCount) {
      setPageBreak()
    }
    val paragraph = document.createParagraph()
    paragraph.setAlignment(ParagraphAlignment.CENTER)
    val run = paragraph.createRun()
    run.setBold(true)
    run.setFontFamily(fontDefault)
    run.setFontSize(12)
    run.setText(header)
    resetLineCount()
    incrementLineCount()
  }


  def getParagragh(fillColor: String, paragraphAlignment: ParagraphAlignment): XWPFParagraph = {
    val paragraph = document.createParagraph()

    if (paragraph.getCTP().getPPr() == null) paragraph.getCTP().addNewPPr()
    if (paragraph.getCTP().getPPr().getShd() != null) paragraph.getCTP().getPPr().unsetShd()

    paragraph.getCTP().getPPr().addNewShd()
    paragraph.getCTP().getPPr().getShd().setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STShd.CLEAR)
    paragraph.getCTP().getPPr().getShd().setColor("auto")
    paragraph.getCTP().getPPr().getShd().setFill(fillColor)

    paragraph.setAlignment(paragraphAlignment)

    paragraph
  }

  def setSubHeaderText(paragraph: XWPFParagraph, text: String, font: String, fontSize: Int) : Unit = {
    val run = paragraph.createRun()
    run.setBold(true)
    run.setColor("FFFFFF")
    run.setFontFamily(font)
    run.setText(text)
    run.setFontSize(fontSize)
  }

  def setSubHeader(clcnText: Seq[String]): Unit = {
    val paragraph = getParagragh("D3D3D3", ParagraphAlignment.CENTER)
    setSubHeaderText(paragraph, clcnText(0), fontDefault, 12)
    setSubHeaderText(paragraph, clcnText(1), fontGujarati, 12)
  }

  def setSubHeaderLine(): Unit = {
    val table = document.createTable(1,1)
    table.setWidth("100%")

    table.setTableAlignment(TableRowAlign.CENTER)
    table.getCTTbl().getTblPr().getTblBorders().getTop().setColor("D3D3D3")
    table.getCTTbl().getTblPr().getTblBorders().getLeft().setColor("FFFFFF")
    table.getCTTbl().getTblPr().getTblBorders().getRight().setColor("FFFFFF")
    table.getCTTbl().getTblPr().getTblBorders().getBottom.setColor("FFFFFF")
    table.getCTTbl().getTblPr().getTblBorders().getInsideH().setColor("FFFFFF")
    table.getCTTbl().getTblPr().getTblBorders().getInsideV().setColor("FFFFFF")

    table.getCTTbl().getTblPr().getTblBorders().getTop().setSz(BigInteger.valueOf(10))
    table.getCTTbl().getTblPr().getTblBorders().getRight().setSz(BigInteger.valueOf(0))
    table.getCTTbl().getTblPr().getTblBorders().getLeft().setSz(BigInteger.valueOf(0))
    table.getCTTbl().getTblPr().getTblBorders().getBottom().setSz(BigInteger.valueOf(0))
    table.getCTTbl().getTblPr().getTblBorders().getInsideH().setSz(BigInteger.valueOf(0))
    table.getCTTbl().getTblPr().getTblBorders().getInsideV().setSz(BigInteger.valueOf(0))

    val row = table.getRow(0)
    row.setHeight(100)
    row.getCtRow().getTrPr().getTrHeightArray(0).setHRule(STHeightRule.EXACT)
  }

  def addText(clcnText: Seq[String]): Unit = {
    val table = document.createTable(1, clcnText.size)
    table.setWidth("100%")
    table.setCellMargins(0, 0, 0, 0)

    table.setTableAlignment(TableRowAlign.CENTER)
    table.getCTTbl().getTblPr().getTblBorders().getTop().setSz(BigInteger.valueOf(10))
    table.getCTTbl().getTblPr().getTblBorders().getRight().setSz(BigInteger.valueOf(0))
    table.getCTTbl().getTblPr().getTblBorders().getLeft().setSz(BigInteger.valueOf(0))
    table.getCTTbl().getTblPr().getTblBorders().getBottom().setSz(BigInteger.valueOf(0))
    table.getCTTbl().getTblPr().getTblBorders().getInsideH().setSz(BigInteger.valueOf(0))
    table.getCTTbl().getTblPr().getTblBorders().getInsideV().setSz(BigInteger.valueOf(0))

    val row = table.getRow(0)

    clcnText.zipWithIndex.foreach(x => {
      val cell = row.getCell(x._2)
      cell.setText(x._1)

      if (x._2 == 0) {
        val cttc = cell.getCTTc
        val ctp = cttc.getPList.get(0)
        var ctppr = ctp.getPPr
        if (ctppr == null) ctppr = ctp.addNewPPr
        var ctjc = ctppr.getJc
        if (ctjc == null) ctjc = ctppr.addNewJc
        ctjc.setVal(STJc.LEFT) // horizontally centered
      } else {
        val cttc = cell.getCTTc
        val ctp = cttc.getPList.get(0)
        var ctppr = ctp.getPPr
        if (ctppr == null) ctppr = ctp.addNewPPr
        var ctjc = ctppr.getJc
        if (ctjc == null) ctjc = ctppr.addNewJc
        ctjc.setVal(STJc.RIGHT) // horizontally centered
      }
    })

  }

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

  def convertToDoc(header: String, clcnCfgAddress: List[ModelCfgAddress], clcnAddressBook: List[List[(String, String)]]): Unit = {


  }

  def addNameIndex(): Unit = {

  }

  def closeDocument(): Unit = {
    val out = new FileOutputStream(new File(outputFileName + ".docx"))
    document.write(out)
    out.close()
  }

  def closeDocument(password: String): Unit = {
    closeDocument()
  }


  def test(): Unit = {
    registerFont

    val paragraph = document.createParagraph()
    paragraph.setAlignment(ParagraphAlignment.CENTER)
    val run = paragraph.createRun()
    run.setText("Hello Word")

    val p2 = document.createParagraph()
    p2.setAlignment(ParagraphAlignment.LEFT)
    val r2 = p2.createRun()
    r2.setText("Text body....")

    val p3 = document.createParagraph()
    p3.setAlignment(ParagraphAlignment.LEFT)
    val r3 = p3.createRun()
    r3.setText("આસ્તા")
    r3.setFontFamily("Noto Sans Gujarati")

    setPageBreak()

    val p4 = document.createParagraph()
    p4.setAlignment(ParagraphAlignment.LEFT)
    val r4 = p4.createRun()
    r4.setText("આસ્તા")
    r4.setFontFamily("Gujarati Sangam MN")
    r4.setBold(true)

    setSubHeader(Seq("Asta", " (આસ્તા)"))

    val p5 = document.createParagraph()
    p5.setAlignment(ParagraphAlignment.LEFT)
    val r5 = p5.createRun()
    r5.setText("Next")

    setSubHeaderLine()

    val p6 = document.createParagraph()
    p6.setAlignment(ParagraphAlignment.LEFT)
    val r6 = p6.createRun()
    r6.setText("Next")

    addText(Seq("val1", "val2"))
    addText(Seq("val3"))

    println(pageCount)

    val out = new FileOutputStream(new File("test.docx"))
    document.write(out)
    out.close()
  }

}


object DocumentBuilder extends App {
  val doc = new DocumentBuilder(null, null)
  doc.test()
}