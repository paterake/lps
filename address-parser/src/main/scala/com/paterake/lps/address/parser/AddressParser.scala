package com.paterake.lps.address.parser

import java.io.FileInputStream

import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.{PdfFont, PdfFontFactory}
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine
import com.itextpdf.kernel.pdf.{PdfDocument, PdfWriter}
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.{LineSeparator, Paragraph, Text}
import com.itextpdf.layout.property.TextAlignment
import com.paterake.lps.address.cfg.reader.CfgAddress
import org.apache.poi.ss.usermodel.{Cell, CellType, Row, Sheet}
import org.apache.poi.ss.util.CellReference
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class AddressParser(inputFileName: String, outputFileName: String) {

  import scala.collection.JavaConverters._

  private val inputFileStream = new FileInputStream(inputFileName)
  private val clcnCfgAddress = new CfgAddress().getCfg()

  def getColumnIndex(columnName: String): Int = {
    CellReference.convertColStringToIndex(columnName)
  }

  def getCellValue(cell: Cell): String = {
    cell.getCellType match {
      case CellType.NUMERIC => cell.getNumericCellValue.toString
      case _ => cell.getStringCellValue.trim
    }
  }

  def getHeaderRow(row: Row): Seq[(Int, String)] = {
    println(row.getLastCellNum)
    val clcnData = scala.collection.mutable.ListBuffer.empty[(Int, String)]
    (0 to row.getLastCellNum).foreach(idx => {
      val cell = row.getCell(idx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
      clcnData.append((idx, getCellValue(cell)))
    })
    clcnData.sortBy(_._1)
  }

  def getDataRow(row: Row, columnCount: Int): Seq[(Int, String)] = {
    val clcnData = scala.collection.mutable.ListBuffer.empty[(Int, String)]
    (0 to columnCount).foreach(idx => {
      val cell = row.getCell(idx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
      clcnData.append((idx, getCellValue(cell)))
    })
    clcnData.sortBy(_._1)
  }

  def outputSheet(clcnHeader: Seq[(Int, String)], clcnData: List[(Int, Seq[(Int, String)])]): Unit = {
    clcnHeader.foreach(f => println(f._1 + ":" + f._2))
    clcnData.foreach(row => {
      println(row._1 + ":" + row._2.mkString(";"))
    })
  }

  def outputAddressCfg(): Unit = {
    clcnCfgAddress.foreach(line => {
      print(line.lineId + "...")
      line.clcnLineElement.foreach(col => {
        val colName =
          if (col.startsWith("(")) {
            col.replaceAll("^.|.$", "")
          } else {
            col
          }
        print(getColumnIndex(colName) + ":")
      })
      println("")
    })
  }

  def extractSheet(sheet: Sheet): (Seq[(Int, String)], List[(Int, Seq[(Int, String)])]) = {
    println(sheet.getSheetName)
    var clcnHeader: Seq[(Int, String)] = null
    val clcnData = scala.collection.mutable.ListBuffer.empty[(Int, Seq[(Int, String)])]

    sheet.iterator().asScala.zipWithIndex.foreach(row => {
      if (row._2 == 0) {
        clcnHeader = getHeaderRow(row._1)
      } else {
        clcnData.append((row._2, getDataRow(row._1, clcnHeader.size)))
      }
    })
    (clcnHeader, clcnData.toList)
  }

  def extractAddressLine(addressLine: (Int, Seq[(Int, String)])): Seq[String] = {
    val entry = clcnCfgAddress.sortBy(line => line.lineId).map(cfg => {
      cfg.clcnLineElement.map(columnName => {
        addressLine._2.filter(p => getColumnIndex(columnName) == p._1).map(x => {
          if (cfg.clcnParenthesis != null && cfg.clcnParenthesis.contains(columnName) && x._2.length > 0) {
            if (x._2.startsWith("(") && x._2.endsWith(")")) {
              x._2
            } else {
              "(" + x._2 + ")"
            }
          } else {
            x._2
          }
        })
      }).map(x => {
        x.mkString(" ")
      })
    }).zipWithIndex.map(x => {
      val sep = clcnCfgAddress(x._2).elementSeparator
      (sep, x._1.mkString(sep))
    }).map(x => {
      x._2.trim.replaceAll(x._1 + "+", x._1).stripSuffix(x._1)
    })
    println(entry)
    entry
  }

  def setAddressBook(clcnData: List[(Int, Seq[(Int, String)])]): List[Seq[String]] = {
    val clcnAddressBook = clcnData.map(line => {
      extractAddressLine(line)
    }).sortBy(x => x(0) + x(1))
    clcnAddressBook
  }

  def getParagraghFormat(): (Map[Int, PdfFont], Map[Int, Int], Map[Int, TextAlignment]) = {
    val clcnFont = scala.collection.mutable.Map[Int, PdfFont]()
    val clcnFontSize = scala.collection.mutable.Map[Int, Int]()
    val clcnAlignment = scala.collection.mutable.Map[Int, TextAlignment]()
    clcnCfgAddress.sortBy(x => x.lineId).foreach(cfg => {
      clcnFont += (clcnFont.size -> PdfFontFactory.createFont(cfg.font))
      clcnFontSize += (clcnFontSize.size -> cfg.fontSize)
      clcnAlignment += (clcnAlignment.size -> TextAlignment.valueOf(cfg.textAlignment))
    })
    (clcnFont.toMap, clcnFontSize.toMap, clcnAlignment.toMap)
  }

  def getParagraph(alignment: TextAlignment, fontSize: Int): Paragraph = {
    val paragraph = new Paragraph()
    paragraph.setTextAlignment(alignment)
    paragraph.setFontSize(fontSize)
    paragraph
  }

  def convertToPdf(clcnAddressBook: List[Seq[String]]): Unit = {
    val (clcnFont, clcnFontSize, clcnAlignment) = getParagraghFormat()
    val pdf = new PdfDocument(new PdfWriter(outputFileName))
    pdf.setDefaultPageSize(PageSize.A5)
    val document = new Document(pdf)

    var line0 = ""
    clcnAddressBook.foreach(entry => {
      entry.zipWithIndex.foreach(line => {
        if (line._1.size > 0) {
          val font = clcnFont(line._2)
          val fontSize = clcnFontSize(line._2)
          val alignment = clcnAlignment(line._2)
          val paragraph = getParagraph(alignment, fontSize)
          val txt = new Text(line._1).setFont(font)
          if (line._2 == 0) {
            if (!line0.equals(line._1)) {
              line0 = line._1
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
            paragraph.add(txt)
            document.add(paragraph)
          }
        }
      })
    })
    document.close()
  }

  def processWorkbook(): Unit = {
    val workbook = new XSSFWorkbook(inputFileStream)
    workbook.sheetIterator().asScala.foreach(f => {
      val (clcnHeader, clcnData) = extractSheet(f)
      val clcnAddressBook = setAddressBook(clcnData)
      println(clcnAddressBook)
      convertToPdf(clcnAddressBook)
    })
  }
}

object AddressParser extends App {
  val sourceFileName = "/Users/rrpate/Downloads/Template4Coder.xlsx"
  val targetFileName = "/Users/rrpate/Documents/samplePdf.pdf"
  val parser = new AddressParser(sourceFileName, targetFileName)
  parser.processWorkbook()
}
