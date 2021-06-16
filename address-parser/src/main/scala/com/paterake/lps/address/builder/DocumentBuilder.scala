package com.paterake.lps.address.builder

import com.paterake.lps.address.cfg.model.{ModelCfgAddress, ModelCfgIndex}
import com.paterake.lps.address.cfg.reader.CfgBlank
import com.paterake.lps.address.parse.Location
import org.apache.poi.xwpf.usermodel.{ParagraphAlignment, TableRowAlign, XWPFDocument, XWPFParagraph}
import org.openxmlformats.schemas.wordprocessingml.x2006.main.{STHeightRule, STTabJc}

import java.io.{File, FileOutputStream}
import java.math.BigInteger
import scala.collection.mutable.ListBuffer

class DocumentBuilder(outputFileName: String, clcnTranslation: Map[String, String]) {

  private val documentMain: XWPFDocument = DocumentBuilderUtility.getNewDocument()
  private val cfgBlank = new CfgBlank()
  private var blankPageCount: Int = 0

  def getDocument(): XWPFDocument = {
    documentMain
  }

  def insertPageBreak(): Unit = {
    val break = DocumentBuilderUtility.getParagraph(documentMain)
    break.setPageBreak(true)
    DocumentBuilderUtility.pageCount += 1
  }

  def setPageBreak(): Unit = {
    insertPageBreak()
    if (blankPageCount > 0) {
      for (x <- 1 to blankPageCount) {
        insertPageBreak()
      }
      blankPageCount = 0
    }
  }

  def resetLineCount(): Unit = {
    DocumentBuilderUtility.lineCount = 0
  }

  def incrementLineCount(): Unit = {
    DocumentBuilderUtility.lineCount += 1
  }

  def startNewPage(header: String, emptyPageCount: Int): Unit = {
    for (x <- 0 to emptyPageCount) {
      setPageBreak()
    }
    val paragraph = DocumentBuilderUtility.getParagraph(documentMain)
    paragraph.setAlignment(ParagraphAlignment.CENTER)
    val run = paragraph.createRun()
    run.setBold(true)
    run.setFontFamily(DocumentBuilderUtility.fontDefault)
    run.setFontSize(12)
    run.setText(header)
    resetLineCount()
    incrementLineCount()
  }

  def getParagragh(fillColor: String, paragraphAlignment: ParagraphAlignment): XWPFParagraph = {
    val paragraph = DocumentBuilderUtility.getParagraph(documentMain)

    paragraph.getCTP().getPPr().addNewShd()
    paragraph.getCTP().getPPr().getShd().setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STShd.CLEAR)
    paragraph.getCTP().getPPr().getShd().setColor("auto")
    paragraph.getCTP().getPPr().getShd().setFill(fillColor)

    paragraph.setAlignment(paragraphAlignment)

    paragraph
  }

  def addGap(): Unit = {
    val paragraph = DocumentBuilderUtility.getParagraph(documentMain)
    val run = paragraph.createRun()
    run.setFontSize(4)
    run.setFontFamily(DocumentBuilderUtility.fontDefault)
    run.setText("-")
    run.setColor("FFFFFF")
  }

  def setSubHeaderText(paragraph: XWPFParagraph, text: String, font: String, fontSize: Int): Unit = {
    val spacing = if (paragraph.getCTP.getPPr.isSetSpacing) {
      paragraph.getCTP.getPPr.getSpacing
    } else {
      paragraph.getCTP.getPPr.addNewSpacing()
    }
    //spacing.setAfter(200)
    //spacing.setBefore(200)
    paragraph.setSpacingBetween(1)


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
    setSubHeaderText(paragraph, clcnText(0), DocumentBuilderUtility.fontDefault, fontSize)
    setSubHeaderText(paragraph, " " + clcnText(1), DocumentBuilderUtility.fontGujarati, fontSize)
    addGap()
    incrementLineCount()
    incrementLineCount()
  }

  def setSubHeaderLine(): Unit = {
    addGap()

    val table = documentMain.createTable(1, 1)
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
    if (!line0.equals(line._1._1) || (DocumentBuilderUtility.lineCount == 1)) {
      val line0Return = line._1._1
      setSubHeader(clcnTxt, 12)
      line0Return
    } else {
      setSubHeaderLine()
      line0
    }
  }

  def addTextTab(clcnText: Seq[String], clcnFontSize: Seq[Int], clcnFont: Seq[String]): Unit = {
    val paragraph = DocumentBuilderUtility.getParagraph(documentMain)

    val spacing = if (paragraph.getCTP.getPPr.isSetSpacing) {
      paragraph.getCTP.getPPr.getSpacing
    } else {
      paragraph.getCTP.getPPr.addNewSpacing()
    }
    //spacing.setAfter(200)
    //spacing.setBefore(200)
    paragraph.setSpacingBetween(1.3)

    val tabStop = paragraph.getCTP().getPPr().addNewTabs().addNewTab();
    tabStop.setPos(BigInteger.valueOf(9000))
    tabStop.setVal(STTabJc.RIGHT)

    clcnText.zipWithIndex.foreach(x => {
      val run = paragraph.createRun()
      run.setFontFamily(DocumentBuilderUtility.fontDefault)
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

  def convertToDoc(header: String, clcnCfgAddress: List[ModelCfgAddress], clcnAddressBook: List[List[(String, String)]]): Unit = {
    val (clcnFont, clcnFontSize, clcnAlignment, clcnFontRight, clcnFontSizeRight, clcnAlignmentRight) = DocumentUtility.getParagraghFormat(clcnCfgAddress)

    var line0 = ""
    clcnAddressBook.foreach(entry => {
      if (DocumentBuilderUtility.lineCount >= Location.maxLineCount) {
        startNewPage(header, 0)
      }
      if (blankPageCount == 0) {
        blankPageCount = cfgBlank.getBlankPageCount(header, entry(0)._1, entry(1)._1)
      }
      entry.zipWithIndex.foreach(line => {
        if (line._1._1.nonEmpty) {
          val font = clcnFont(line._2)
          val fontSize = clcnFontSize(line._2)
          val fontRight = clcnFontRight.getOrElse(line._2, DocumentBuilderUtility.fontDefault)
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
            DocumentBuilderUtility.appendNameIndex(DocumentUtility.getIndexEntry(line._2, entry, header, DocumentBuilderUtility.pageCount))
          }
        }
      })
    })
  }

  def closeDocument(): Unit = {
    val out = new FileOutputStream(new File(outputFileName + ".docx"))
    documentMain.write(out)
    out.close()
  }

  def closeDocument(password: String): Unit = {
    closeDocument()
  }

}
