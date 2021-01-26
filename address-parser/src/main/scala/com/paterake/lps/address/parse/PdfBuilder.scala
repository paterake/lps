package com.paterake.lps.address.parse

import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.{PdfFont, PdfFontFactory}
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine
import com.itextpdf.kernel.pdf.{PdfDocument, PdfWriter}
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.{Cell, LineSeparator, Paragraph, Table, Text}
import com.itextpdf.layout.property.TextAlignment
import com.paterake.lps.address.cfg.model.ModelCfgAddress

class PdfBuilder(outputFileName: String) {

  private val document = getDocument()

  def getDocument(): Document = {
    val pdf = new PdfDocument(new PdfWriter(outputFileName))
    pdf.setDefaultPageSize(PageSize.A5)
    val document = new Document(pdf)
    document
  }

  def closeDocument(): Unit = {
    document.close()
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

  def getParagraph(alignment: TextAlignment, fontSize: Int): Paragraph = {
    val paragraph = new Paragraph()
    paragraph.setTextAlignment(alignment)
    paragraph.setFontSize(fontSize)
    paragraph
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

  def convertToPdf(clcnCfgAddress: List[ModelCfgAddress], clcnAddressBook: List[List[(String, String)]]): Unit = {
    val (clcnFont, clcnFontSize, clcnAlignment, clcnFontRight, clcnFontSizeRight, clcnAlignmentRight) = getParagraghFormat(clcnCfgAddress)

    var line0 = ""
    clcnAddressBook.foreach(entry => {
      entry.zipWithIndex.foreach(line => {
        if (line._1._1.nonEmpty) {
          val font = clcnFont(line._2)
          val fontSize = clcnFontSize(line._2)
          val alignment = clcnAlignment(line._2)

          val paragraph = getParagraph(alignment, fontSize)
          val txt = new Text(line._1._1).setFont(font)
          if (line._2 == 0) {
            if (!line0.equals(line._1._1)) {
              line0 = line._1._1
              paragraph.add(txt)
              paragraph.setBackgroundColor(ColorConstants.LIGHT_GRAY)
              paragraph.setFontColor(ColorConstants.WHITE)
              document.add(paragraph)
            } else {
              val line = new SolidLine(1f)
              line.setColor(ColorConstants.LIGHT_GRAY)
              val lineSeparator = new LineSeparator(line)
              document.add(lineSeparator)
            }
          } else {
            if (line._1._2 == null) {
              paragraph.add(txt)
              document.add(paragraph)
            } else {
              val fontRight = clcnFontRight(line._2)
              val fontSizeRight = clcnFontSizeRight(line._2)
              val alignmentRight = clcnAlignmentRight(line._2)
              val txtRight = new Text(line._1._2).setFont(fontRight)
              val table = new Table(2).useAllAvailableWidth()
              table.addCell(getCell(txt, TextAlignment.LEFT, fontSize))
              table.addCell(getCell(txtRight, TextAlignment.RIGHT, fontSizeRight))
              document.add(table)
            }
          }
        }
      })
    })
  }


}
