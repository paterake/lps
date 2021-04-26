package com.paterake.lps.address.parse

import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.events.PdfDocumentEvent
import com.itextpdf.kernel.font.{PdfFont, PdfFontFactory}
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine
import com.itextpdf.kernel.pdf.{EncryptionConstants, PdfDocument, PdfReader, PdfWriter, WriterProperties}
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.{AreaBreak, Cell, LineSeparator, Paragraph, Table, Text}
import com.itextpdf.layout.property.{AreaBreakType, TextAlignment}
import com.paterake.lps.address.cfg.model.{ModelCfgAddress, ModelCfgIndex}

import java.io.FileOutputStream
import scala.collection.mutable.ListBuffer

class PdfBuilder(outputFileName: String, clcnTranslation: Map[String, String]) {

  private val pdfDocument = getPdfDocument()
  private val document = getNewDocument()

  private val font_gujarati = FontBuilder.getFont()


  private var lineCount = 0
  private val clcnNameSuffix = scala.io.Source.fromInputStream(getClass.getResourceAsStream(Location.nameSuffix)).getLines.toList
  private val clcnNameIdx = new ListBuffer[ModelCfgIndex]()
  private val clcnFailedTranslation = new ListBuffer[String]()


  def getPdfDocument(): PdfDocument = {
    val pdf = new PdfDocument(new PdfWriter(outputFileName + ".pdf"))
    pdf.setDefaultPageSize(PageSize.A5)
    pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new PdfFooter());
    pdf
  }

  def getNewDocument(): Document = {
    val document = new Document(pdfDocument)
    document
  }

  def passwordProtect(password: String): Unit = {
    val userPwd = password.getBytes()
    val ownerPwd = password.getBytes()
    val writerProperties = new WriterProperties()
    writerProperties.setStandardEncryption(userPwd, ownerPwd, EncryptionConstants.ALLOW_PRINTING, EncryptionConstants.ENCRYPTION_AES_128);
    val pdfReader = new PdfReader(outputFileName + ".pdf")
    val pdfWriter = new PdfWriter(new FileOutputStream(outputFileName + "_protected.pdf"), writerProperties)
    val pdfDocument = new PdfDocument(pdfReader, pdfWriter)
    pdfDocument.close()
  }

  def closeDocument(password: String): Unit = {
    document.close()
    passwordProtect(password)
  }

  def closeDocument(): Unit = {
    document.close()
  }

  def resetLineCount(): Unit = {
    lineCount = 0
  }

  def incrementLineCount(): Unit = {
    lineCount += 1
  }

  def getParagraph(alignment: TextAlignment, fontSize: Int): Paragraph = {
    val paragraph = new Paragraph()
    paragraph.setTextAlignment(alignment)
    paragraph.setFontSize(fontSize)
    paragraph
  }

  def startNewPage(header: String, blankPageCount: Int): Unit = {
    for (x <- 0 to blankPageCount) {
      document.add(new AreaBreak(AreaBreakType.NEXT_PAGE))
    }
    val paragraph = getParagraph(TextAlignment.CENTER, 12)
    val txt = new Text(header).setFont(PdfFontFactory.createFont("Helvetica-Bold"))
    paragraph.add(txt)
    document.add(paragraph)
    //println(pdfDocument.getNumberOfPages + ":" + header)
    resetLineCount()
    incrementLineCount()
  }

  def setSubHeader(clcnTxt: List[Text], alignment: TextAlignment, fontSize: Int): Unit = {
    val paragraph = getParagraph(alignment, 0)
    paragraph.setHeight(Location.subHeaderParagraphHeight)
    clcnTxt.foreach(x => paragraph.add(x))
    paragraph.setBackgroundColor(ColorConstants.LIGHT_GRAY)
    paragraph.setFontColor(ColorConstants.WHITE)
    document.add(paragraph)
    incrementLineCount()
    incrementLineCount()
  }

  def setSubHeaderAsLine(): Unit = {
    val paragraph = new Paragraph("")
    paragraph.setFixedLeading(1f)
    val line = new SolidLine(1f)
    line.setColor(ColorConstants.LIGHT_GRAY)
    val lineSeparator = new LineSeparator(line)
    document.add(lineSeparator)
    document.add(paragraph)
    //incrementLineCount()
  }

  def addSeparator(clcnTxt: List[Text], line0: String, line: ((String, String), Int), alignment: TextAlignment, fontSize: Int): String = {
    if (!line0.equals(line._1._1) || (lineCount == 1)) {
      val line0Return = line._1._1
      setSubHeader(clcnTxt, alignment, fontSize)
      line0Return
    } else {
      setSubHeaderAsLine()
      line0
    }
  }

  def getParagraghFormat(clcnCfgAddress: List[ModelCfgAddress]): (Map[Int, PdfFont], Map[Int, Int], Map[Int, TextAlignment], Map[Int, PdfFont], Map[Int, Int], Map[Int, TextAlignment]) = {
    val clcnFont = scala.collection.mutable.Map[Int, PdfFont]()
    val clcnFontSize = scala.collection.mutable.Map[Int, Int]()
    val clcnAlignment = scala.collection.mutable.Map[Int, TextAlignment]()
    val clcnFontRight = scala.collection.mutable.Map[Int, PdfFont]()
    val clcnFontSizeRight = scala.collection.mutable.Map[Int, Int]()
    val clcnAlignmentRight = scala.collection.mutable.Map[Int, TextAlignment]()

    clcnCfgAddress.sortBy(x => x.lineId).zipWithIndex.foreach(cfg => {
      clcnFont += (cfg._2 -> PdfFontFactory.createFont(cfg._1.font))
      clcnFontSize += (cfg._2 -> cfg._1.fontSize)
      clcnAlignment += (cfg._2 -> TextAlignment.valueOf(cfg._1.textAlignment))
      if (cfg._1.fontRight != null) {
        clcnFontRight += (cfg._2 -> PdfFontFactory.createFont(cfg._1.fontRight))
      }
      if (cfg._1.fontSizeRight != null && cfg._1.fontSizeRight > 0) {
        clcnFontSizeRight += (cfg._2 -> cfg._1.fontSizeRight)
      }
      if (cfg._1.textAlignmentRight != null) {
        clcnAlignmentRight += (cfg._2 -> TextAlignment.valueOf(cfg._1.textAlignmentRight))
      }
    })
    (clcnFont.toMap, clcnFontSize.toMap, clcnAlignment.toMap, clcnFontRight.toMap, clcnFontSizeRight.toMap, clcnAlignmentRight.toMap)
  }

  def getCell(text: Text, alignment: TextAlignment, fontSize: Int): Cell = {
    val paragraph = getParagraph(alignment, fontSize)
    paragraph.add(text)
    val cell = new Cell().add(paragraph)
    cell.setPadding(0)
    cell.setTextAlignment(alignment)
    cell.setBorder(Border.NO_BORDER)
    cell
  }

  def getSeparatorText(textToTranslate: String, font: PdfFont, fontSize: Int): List[Text] = {
    val clcnTxt = if ("Overseas Members".equalsIgnoreCase(textToTranslate)) {
      ListBuffer(
        new Text(textToTranslate).setFont(font).setFontSize(fontSize)
      )
    } else {
      val translation = try {
        "(" + clcnTranslation(textToTranslate) + ")"
      } catch {
        case _: Exception => clcnTranslation("(" + textToTranslate + ")")
      }
      ListBuffer(
        new Text(textToTranslate).setFont(font).setFontSize(fontSize),
        new Text(" " + translation).setFont(font_gujarati).setFontSize(fontSize)
      )
    }
    clcnTxt.toList
  }

  def setDocument(txt: Text, alignment: TextAlignment, fontSize: Int): Table = {
    val table = new Table(1).useAllAvailableWidth()
    table.addCell(getCell(txt, TextAlignment.LEFT, fontSize))
    document.add(table)
    incrementLineCount()
    table
  }

  def setDocument(txt: Text, alignment: TextAlignment, fontSize: Int, rightString: String, fontRight: PdfFont, fontSizeRight: Int, alignmentRight: TextAlignment): Table = {
    val txtRight = new Text(rightString).setFont(fontRight)
    val table = new Table(2).useAllAvailableWidth()
    table.addCell(getCell(txt, TextAlignment.LEFT, fontSize))
    table.addCell(getCell(txtRight, TextAlignment.RIGHT, fontSizeRight))
    document.add(table)
    incrementLineCount()
    table
  }

  def stripSuffix(name: String): String = {
    val newName = clcnNameSuffix.foldLeft(name)((a, b) => a.replaceAll(b, ""))
    //val newName = name.split(" ").map(x => clcnNameSuffix.foldLeft(x)((a, b) => a.stripSuffix(b))).mkString(" ")
    newName
  }

  def getIndexEntry(entryLineNumber: Int, entry: List[(String, String)], header: String): ModelCfgIndex = {
    val indexEntry = {
      if (entryLineNumber.equals(1)) {
        val mainName = entry(1)._1.split(" ").filterNot(p => p.equals("(Late)")).filterNot(p => p.equals("Patel")).mkString(" ")
        val spouseName = entry(2)._1.split(" ").filterNot(p => p.equals("(Late)")).filterNot(p => p.equals("Patel")).head
        val village = entry(0)._1
        val spouseVillage = null
        ModelCfgIndex(stripSuffix(mainName), stripSuffix(spouseName), village, spouseVillage, header, pdfDocument.getNumberOfPages)
      } else {
        val mainName = entry(2)._1.split(" ").filterNot(p => p.equals("(Late)")).filterNot(p => p.equals("Patel")).mkString(" ")
        val spouseName = entry(1)._1.split(" ").filterNot(p => p.equals("(Late)")).filterNot(p => p.equals("Patel")).head
        val village = entry(0)._1
        val spouseVillage =
          if (entry(2)._1.split(" ").reverse.head.startsWith("(") && entry(2)._1.split(" ").reverse.head.endsWith(")")) {
            entry(2)._1.split(" ").reverse.head
          } else {
            ""
          }

        if (mainName.toLowerCase.contains("umadsihn")) {
          //println(mainName)
          null
        }
        ModelCfgIndex(stripSuffix(mainName.replace(spouseVillage, "").trim), stripSuffix(spouseName), village, spouseVillage, header, pdfDocument.getNumberOfPages)
      }
    }
    indexEntry
  }

  def convertToPdf(header: String, clcnCfgAddress: List[ModelCfgAddress], clcnAddressBook: List[List[(String, String)]]): Unit = {

    val (clcnFont, clcnFontSize, clcnAlignment, clcnFontRight, clcnFontSizeRight, clcnAlignmentRight) = getParagraghFormat(clcnCfgAddress)

    var line0 = ""
    clcnAddressBook.foreach(entry => {
      if (lineCount >= Location.maxLineCount) {
        startNewPage(header, 0)
      }
      entry.zipWithIndex.foreach(line => {
        if (line._1._1.nonEmpty) {
          val font = clcnFont(line._2)
          val fontSize = clcnFontSize(line._2)
          val alignment = clcnAlignment(line._2)
          if (line._2 == 0) {
            val clcnTxt = getSeparatorText(line._1._1, font, fontSize)
            line0 = addSeparator(clcnTxt, line0, line, alignment, fontSize)
          } else {
            val txt = new Text(line._1._1).setFont(font)
            val element = if (line._1._2 == null) {
              setDocument(txt, TextAlignment.LEFT, fontSize)
            } else {
              setDocument(txt, TextAlignment.LEFT, fontSize, line._1._2, clcnFontRight(line._2), clcnFontSizeRight(line._2), clcnAlignmentRight(line._2))
            }
          }
          if (clcnCfgAddress(line._2).indexInd) {
            clcnNameIdx.append(getIndexEntry(line._2, entry, header))
          }
          if (line._2 == 1) {
            //println("Position: " + line._1._1 + ":" + line._1._2 + ":" + lineCount)
          }
        }
      })
    })
  }

  def getPartTranslation(part: String): String = {
    if (part.startsWith("(") || part.endsWith(")")) {
      val partTranslation = try {
        clcnTranslation(part.replace("(", "").replace(")", ""))
      } catch {
        case _: Exception => {
          clcnFailedTranslation.append(part.replace("(", "").replace(")", ""))
          //println("Failed to translate name part: " + part.replace("(", "").replace(")", ""))
          ""
        }
      }
      val newPart = StringBuilder.newBuilder
      if (part.startsWith("(")) {
        newPart.append("(")
      }
      newPart.append(partTranslation)
      if (part.endsWith(")")) {
        newPart.append(")")
      }
      newPart.mkString
    } else {
      try {
        clcnTranslation(part)
      } catch {
        case _: Exception => {
          clcnFailedTranslation.append(part)
          //println("Failed to translate name part: " + part)
          ""
        }
      }
    }
  }

  def getIndexName(mainName: String, spouseName: String): (String, String) = {
    val memberName = if (mainName.split(" ").size > 3) {
      val clcnName = mainName.split(" ")
      clcnName(0) + " " + clcnName(1) + " " + clcnName.reverse.head
    } else {
      mainName
    }
    val indexName = if (spouseName == null || spouseName.length < 1) {
      memberName
    } else {
      memberName + " (" + spouseName + ")"
    }
    val translation = memberName.replaceAll("\\(.*?\\)", "").split(" ").filter(p => p.nonEmpty).map(part => {
      getPartTranslation(part)
    }).mkString(" ")
    (indexName, translation)
  }

  def getIndexVillageName(mainVillageName: String, spouseVillageName: String): String = {
    val villageName =
      if (spouseVillageName == null || spouseVillageName.length < 1) {
        mainVillageName
      } else {
        mainVillageName + " (" + spouseVillageName.replaceAll("[\\[\\](){}]", "") + ")"
      }
    villageName
  }

  def addNameIndex(): Unit = {
    val table = new Table(5).useAllAvailableWidth()
    val font = PdfFontFactory.createFont("Helvetica")
    val fontSize = 5

    clcnNameIdx.sortBy(index => index.mainName).foreach(x => {
      //println(x)
      val regionName = if (x.region.toLowerCase.startsWith("rest")) {
        x.region
      }
      else {
        x.region.split(" ").head.stripSuffix(",")
      }
      //val indexName = getIndexName(x.mainName, x.spouseName)
      val indexName = getIndexName(x.mainName.replaceAll("\\(.*?\\)", ""), x.spouseName.replaceAll("\\(.*?\\)", ""))
      val villageName = getIndexVillageName(x.mainVillageName, x.spouseVillageName)
      val txtMainName = new Text(indexName._1).setFont(font)
      val txtTranslation = new Text(indexName._2).setFont(font_gujarati)
      val txtVillage = new Text(villageName).setFont(font)
      val txtRegion = new Text(regionName).setFont(font)
      val txtPageNumber = new Text(x.pageNumber.toString).setFont(font)
      table.addCell(getCell(txtMainName, TextAlignment.LEFT, fontSize))
      table.addCell(getCell(txtTranslation, TextAlignment.LEFT, fontSize))
      table.addCell(getCell(txtVillage, TextAlignment.LEFT, fontSize))
      table.addCell(getCell(txtRegion, TextAlignment.LEFT, fontSize))
      table.addCell(getCell(txtPageNumber, TextAlignment.LEFT, fontSize))
    })
    document.add(table)
    clcnFailedTranslation.toList.distinct.sorted.foreach(x => println("Failed to translate name part: " + x))

  }


}
