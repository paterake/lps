package com.paterake.lps.address.builder

import com.paterake.lps.address.cfg.model.{ModelCfgAddress, ModelCfgIndex}
import com.paterake.lps.address.parse.Location
import org.apache.poi.xwpf.usermodel.{ParagraphAlignment, TableRowAlign, XWPFDocument, XWPFParagraph, XWPFTable, XWPFTableCell, XWPFTableRow}
import org.openxmlformats.schemas.wordprocessingml.x2006.main.{STHeightRule, STJc, STTabJc}

import java.awt.{Font, GraphicsEnvironment}
import java.io.{File, FileInputStream, FileOutputStream}
import java.math.BigInteger
import scala.collection.mutable.ListBuffer

class DocumentBuilder(outputFileName: String, clcnTranslation: Map[String, String]) {

  private val document: XWPFDocument = getNewDocument()
  private val documentIdx: XWPFDocument = getNewDocument()

  private val fontDefault = "Noto Sans"
  private val fontGujarati = "Noto Sans Gujarati"
  //private val fontGujarati = "Gujarati Sangam MN"

  private var pageCount = 1
  private var lineCount = 0
  private val clcnNameIdx = new ListBuffer[ModelCfgIndex]()

  def getDocument(): XWPFDocument = {
    document
  }


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

  def startNewIndexPage(header: String, blankPageCount: Int): Unit = {
    for (x <- 0 to blankPageCount) {
      setPageBreak()
    }
    val paragraph = documentIdx.createParagraph()
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

  def addGap(): Unit = {
    val paragraph = document.createParagraph()
    val run = paragraph.createRun()
    run.setFontSize(4)
    run.setFontFamily(fontDefault)
    run.setText("-")
    run.setColor("FFFFFF")
  }

  def setSubHeaderText(paragraph: XWPFParagraph, text: String, font: String, fontSize: Int): Unit = {
    val run = paragraph.createRun()
    run.setText(text)
    run.setBold(true)
    run.setColor("FFFFFF")
    run.setFontFamily(font)
    run.setFontSize(fontSize)
  }

  def setSubHeader(clcnText: Seq[String], fontSize: Int): Unit = {
    //addGap()
    addGap()
    val paragraph = getParagragh("D3D3D3", ParagraphAlignment.CENTER)
    setSubHeaderText(paragraph, clcnText(0), fontDefault, fontSize)
    setSubHeaderText(paragraph, clcnText(1), fontGujarati, fontSize)
    addGap()
    incrementLineCount()
    incrementLineCount()
  }

  def setSubHeaderLine(): Unit = {
    addGap()

    val table = document.createTable(1, 1)
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
    /*
    val cell = row.getCell(0)
    cell.setText("abc")
    */

    //addGap()

  }

  def addSeparator(clcnTxt: List[String], line0: String, line: ((String, String), Int), alignment: String, fontSize: Int): String = {
    if (!line0.equals(line._1._1) || (lineCount == 1)) {
      val line0Return = line._1._1
      setSubHeader(clcnTxt, 12)
      line0Return
    } else {
      setSubHeaderLine()
      line0
    }
  }

  def addTextTable(clcnText: Seq[String]): Unit = {
    val table = document.createTable(1, clcnText.size)
    table.setWidth("100%")
    table.setCellMargins(0, 0, 0, 0)

    table.getCTTbl().getTblPr().getTblBorders().getTop().setColor("FFFFFF")
    table.getCTTbl().getTblPr().getTblBorders().getLeft().setColor("FFFFFF")
    table.getCTTbl().getTblPr().getTblBorders().getRight().setColor("FFFFFF")
    table.getCTTbl().getTblPr().getTblBorders().getBottom.setColor("FFFFFF")
    table.getCTTbl().getTblPr().getTblBorders().getInsideH().setColor("FFFFFF")
    table.getCTTbl().getTblPr().getTblBorders().getInsideV().setColor("FFFFFF")

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
        ctjc.setVal(STJc.LEFT)
      } else {
        val cttc = cell.getCTTc
        val ctp = cttc.getPList.get(0)
        var ctppr = ctp.getPPr
        if (ctppr == null) ctppr = ctp.addNewPPr
        var ctjc = ctppr.getJc
        if (ctjc == null) ctjc = ctppr.addNewJc
        ctjc.setVal(STJc.RIGHT)
      }
    })

    val paragraph = document.createParagraph()
    paragraph.setAlignment(ParagraphAlignment.CENTER)
    paragraph.setSpacingBetween(2)
    //val run = paragraph.createRun()
  }

  def addTextTab(clcnText: Seq[String], clcnFontSize: Seq[Int], clcnFont: Seq[String]): Unit = {
    val paragraph = document.createParagraph()

    if (paragraph.getCTP().getPPr() == null) paragraph.getCTP().addNewPPr()
    if (paragraph.getCTP().getPPr().getShd() != null) paragraph.getCTP().getPPr().unsetShd()

    val tabStop = paragraph.getCTP().getPPr().addNewTabs().addNewTab();
    tabStop.setPos("9000")
    tabStop.setVal(STTabJc.RIGHT)

    clcnText.zipWithIndex.foreach(x => {
      val run = paragraph.createRun()
      run.setFontFamily(fontDefault)
      run.setFontSize(clcnFontSize(x._2))
      if (clcnFont(x._2).toLowerCase.contains("bold")) {
        run.setBold(true)
      }
      if (x._2.equals(0)) {
        run.setText(x._1)
      } else {
        run.addTab()
        run.setText(x._1)
      }
    })
  }


  def addText(clcnText: Seq[String], clcnFontSize: Seq[Int], clcnFont: Seq[String]): Unit = {
    addTextTab(clcnText, clcnFontSize, clcnFont)
    incrementLineCount()
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

  def getSeparatorText(textToTranslate: String, font: String, fontSize: Int): List[String] = {
    val clcnTxt = if ("Overseas Members".equalsIgnoreCase(textToTranslate)) {
      ListBuffer(
        textToTranslate
      )
    } else {
      val translation = try {
        "(" + clcnTranslation(textToTranslate) + ")"
      } catch {
        case _: Exception => clcnTranslation("(" + textToTranslate + ")")
      }
      ListBuffer(textToTranslate, translation)
    }
    clcnTxt.toList
  }

  def appendNameIndex(entry: ModelCfgIndex): Unit = {
    clcnNameIdx.append(entry)
  }


  def convertToDoc(header: String, clcnCfgAddress: List[ModelCfgAddress], clcnAddressBook: List[List[(String, String)]]): Unit = {
    val (clcnFont, clcnFontSize, clcnAlignment, clcnFontRight, clcnFontSizeRight, clcnAlignmentRight) = DocumentUtility.getParagraghFormat(clcnCfgAddress)

    var line0 = ""
    clcnAddressBook.foreach(entry => {
      if (lineCount >= Location.maxLineCount) {
        startNewPage(header, 0)
      }
      entry.zipWithIndex.foreach(line => {
        if (line._1._1.nonEmpty) {
          val font = clcnFont(line._2)
          val fontSize = clcnFontSize(line._2)
          val fontRight = clcnFontRight.getOrElse(line._2, fontDefault)
          val fontRightSize = clcnFontSizeRight.getOrElse(line._2, 8)
          val alignment = clcnAlignment(line._2)
          if (line._2 == 0) {
            val clcnTxt = getSeparatorText(line._1._1, font, fontSize)
            line0 = addSeparator(clcnTxt, line0, line, alignment, fontSize)
          } else {
            val element = if (line._1._2 == null) {
              addText(Seq(line._1._1), Seq(fontSize), Seq(font))
            } else {
              addText(Seq(line._1._1, line._1._2), Seq(fontSize, fontRightSize), Seq(font, fontRight))
            }
          }
          if (clcnCfgAddress(line._2).indexInd) {
            appendNameIndex(DocumentUtility.getIndexEntry(line._2, entry, header, pageCount))
          }
        }
      })
    })
  }

  def getTabbedParagrah(): XWPFParagraph = {
    val paragraph = documentIdx.createParagraph()

    if (paragraph.getCTP().getPPr() == null) paragraph.getCTP().addNewPPr()
    if (paragraph.getCTP().getPPr().getShd() != null) paragraph.getCTP().getPPr().unsetShd()

    if (paragraph.getCTP.getPPr.getTabs != null)  {
      paragraph.getCTP.getPPr.unsetTabs()
    }

    if (paragraph.getCTP.getPPr.isSetTabs) {
      paragraph.getCTP.getPPr.unsetTabs()
    }

    val clcnTab = paragraph.getCTP().getPPr().addNewTabs()

    val tab1 = clcnTab.addNewTab()
    val tab2 = clcnTab.addNewTab()
    val tab3 = clcnTab.addNewTab()
    val tab4 = clcnTab.addNewTab()

    tab1.setPos("3000")
    tab2.setPos("5000")
    tab3.setPos("7800")
    tab4.setPos("9500")

    tab1.setVal(STTabJc.LEFT)
    tab2.setVal(STTabJc.LEFT)
    tab3.setVal(STTabJc.LEFT)
    tab4.setVal(STTabJc.RIGHT)

    paragraph
  }

  def addTabbedIndexEnty(paragraph: XWPFParagraph, text: String, font: String, fontSize: Int, addTab: Boolean): Unit = {
    val run = paragraph.createRun()
    if (addTab) {
      run.addTab()
    }
    run.setFontFamily(font)
    run.setFontSize(fontSize)
    run.setText(text)
  }

  def addNameIndexTab(): Unit = {
    val fontSize = 8
    clcnNameIdx.sortBy(index => index.mainName).zipWithIndex.foreach(x => {
      val regionName = DocumentUtility.getIndexRegionName(x._1.region)
      val indexName = DocumentUtility.getIndexName(clcnTranslation, x._1.mainName.replaceAll("\\(.*?\\)", ""), x._1.spouseName.replaceAll("\\(.*?\\)", ""))
      val villageName = DocumentUtility.getIndexVillageName(x._1.mainVillageName, x._1.spouseVillageName)

      val paragraph = getTabbedParagrah()
      addTabbedIndexEnty(paragraph, indexName._1, fontDefault, fontSize, false)
      addTabbedIndexEnty(paragraph, indexName._2, fontGujarati, fontSize, true)
      addTabbedIndexEnty(paragraph, villageName, fontDefault, fontSize, true)
      addTabbedIndexEnty(paragraph, regionName, fontDefault, fontSize, true)
      addTabbedIndexEnty(paragraph, x._1.pageNumber.toString, fontDefault, fontSize, true)

    })
    DocumentUtility.outputFailedTranslation()
  }

  def addNameIndex(header: String, blankCount: Int): Unit = {
    startNewIndexPage(header, blankCount)
    addNameIndexTab
  }

  def closeDocument(): Unit = {
    val out = new FileOutputStream(new File(outputFileName + ".docx"))
    document.write(out)
    out.close()

    val outIdx = new FileOutputStream(new File(outputFileName + "_idx.docx"))
    documentIdx.write(outIdx)
    outIdx.close()
  }

  def closeDocument(password: String): Unit = {
    closeDocument()
  }

}
