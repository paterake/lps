package com.paterake.lps.address.builder

import org.apache.poi.xwpf.usermodel.{Borders, ParagraphAlignment, TableRowAlign, XWPFDocument, XWPFParagraph}
import org.openxmlformats.schemas.wordprocessingml.x2006.main.{STHeightRule, STTabJc}

import java.io.{File, FileOutputStream}
import java.math.BigInteger

class DocumentBuilderIdx(outputFileName: String, clcnTranslation: Map[String, String]) {
  private val documentIdx: XWPFDocument = DocumentBuilderUtility.getNewDocument()

  def setPageBreak(): Unit = {
    val break = DocumentBuilderUtility.getParagraph(documentIdx)
    break.setPageBreak(true)
  }

  def startNewIndexPage(header: String, blankPageCount: Int): Unit = {
    for (x <- 0 to blankPageCount) {
      setPageBreak()
    }
    val paragraph = DocumentBuilderUtility.getParagraph(documentIdx)
    paragraph.setAlignment(ParagraphAlignment.CENTER)
    val run = paragraph.createRun()
    run.setBold(true)
    run.setFontFamily(DocumentBuilderUtility.fontDefault)
    run.setFontSize(12)
    run.setText(header)
  }

  def setIndexEntryLine(): Unit = {
    val table = documentIdx.createTable(1, 1)
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
    row.setHeight(1)
    row.getCtRow().getTrPr().getTrHeightArray(0).setHRule(STHeightRule.EXACT)
  }

  def getTabbedParagrah(): XWPFParagraph = {
    val paragraph = DocumentBuilderUtility.getParagraph(documentIdx)

    val clcnTab = paragraph.getCTP().getPPr().addNewTabs()

    val tab1 = clcnTab.addNewTab()
    val tab2 = clcnTab.addNewTab()
    val tab3 = clcnTab.addNewTab()
    val tab4 = clcnTab.addNewTab()

    tab1.setPos(BigInteger.valueOf(3000))
    tab2.setPos(BigInteger.valueOf(5000))
    tab3.setPos(BigInteger.valueOf(7800))
    tab4.setPos(BigInteger.valueOf(9500))

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

  def addNameIndexTabHeader(): Unit = {
    val fontSize = 8
    val paragraph = getTabbedParagrah()
    addTabbedIndexEnty(paragraph, "Name (Spouse)", DocumentBuilderUtility.fontDefault, fontSize, false)
    addTabbedIndexEnty(paragraph, "Name (in Gujarati)", DocumentBuilderUtility.fontGujarati, fontSize, true)
    addTabbedIndexEnty(paragraph, "Gaam (Original Gaam)", DocumentBuilderUtility.fontDefault, fontSize, true)
    addTabbedIndexEnty(paragraph, "Region", DocumentBuilderUtility.fontDefault, fontSize, true)
    addTabbedIndexEnty(paragraph, "Page", DocumentBuilderUtility.fontDefault, fontSize, true)
  }

  def addNameIndexTab(): Unit = {
    addNameIndexTabHeader()
    setIndexEntryLine
    val fontSize = 8
    val clcnVillage = DocumentBuilderUtility.getClcnVillage()
    DocumentBuilderUtility.clcnNameIdx.sortBy(index => index.mainName).zipWithIndex.foreach(x => {
      val regionName = DocumentUtility.getIndexRegionName(x._1.region)
      val indexName = DocumentUtility.getIndexName(clcnTranslation
        , x._1.mainName.replaceAll("\\(.*?\\)", "").split(" ").filterNot(p => p.equals("")).mkString(" ")
        , x._1.spouseName.replaceAll("\\(.*?\\)", "").split(" ").filterNot(p => p.equals("")).mkString(" ")
      )
      val villageName = DocumentUtility.getIndexVillageName(x._1.mainVillageName, x._1.spouseVillageName, clcnVillage)

      val paragraph = getTabbedParagrah()
      addTabbedIndexEnty(paragraph, indexName._1, DocumentBuilderUtility.fontDefault, fontSize, false)
      addTabbedIndexEnty(paragraph, indexName._2, DocumentBuilderUtility.fontGujarati, fontSize, true)
      addTabbedIndexEnty(paragraph, villageName, DocumentBuilderUtility.fontDefault, fontSize, true)
      addTabbedIndexEnty(paragraph, regionName, DocumentBuilderUtility.fontDefault, fontSize, true)
      addTabbedIndexEnty(paragraph, x._1.pageNumber.toString, DocumentBuilderUtility.fontDefault, fontSize, true)
      setIndexEntryLine
    })
    DocumentUtility.outputFailedTranslation()
  }

  def addNameIndex(header: String, blankCount: Int): Unit = {
    startNewIndexPage(header, blankCount)
    addNameIndexTab
  }

  def closeDocument(): Unit = {
    val outIdx = new FileOutputStream(new File(outputFileName + "_idx.docx"))
    documentIdx.write(outIdx)
    outIdx.close()
  }

  def closeDocument(password: String): Unit = {
    closeDocument()
  }

}
