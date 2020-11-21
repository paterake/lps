package com.paterake.lps.address.parser

import java.io.FileInputStream

import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.{PdfFont, PdfFontFactory}
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine
import com.itextpdf.kernel.pdf.{PdfDocument, PdfWriter}
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.{LineSeparator, Paragraph, Text}
import com.itextpdf.layout.property.TextAlignment
import com.paterake.lps.address.model.{AddressModel, FormatModel}
import org.apache.poi.ss.usermodel.{Cell, CellType, Row, Sheet}
import org.apache.poi.ss.util.CellReference
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class AddressParser(inputFileName: String, outputFileName: String) {

  import scala.collection.JavaConverters._

  val inputFileStream = new FileInputStream(inputFileName)
  val clcnAddressCfg: Seq[(Int, Array[String])] = new AddressModel().getAddressCfg()
  val clcnFormatCfg: Seq[String] = new FormatModel().getFormatCfg()

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
    /*
    -- Skips empty cells
    row.iterator().asScala.zipWithIndex.foreach(cell => {
      clcnData.append((cell._2, cell._1.getStringCellValue.trim))
    })
    */
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
    /*
    clcnHeader.foreach(f => println(f._1 + ":" + f._2))
    clcnData.foreach(row => {
      println(row._1 + ":" + row._2.mkString(";"))
    })
    */
    (clcnHeader, clcnData.toList)
  }

  def outputAddressCfg(): Unit = {
    clcnAddressCfg.foreach(line => {
      print(line._1 + "...")
      line._2.foreach(col => {
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

  def outputFormatCfg(): Unit = {
    clcnFormatCfg.foreach(x => {
      println(x)
    })
  }

  def extractAddressLine(clcnHeader: Seq[(Int, String)], addressLine: (Int, Seq[(Int, String)])): Seq[String] = {
    val entry = clcnAddressCfg.map(cfg => {
      cfg._2.map(columnName => {
        addressLine._2.filter(p => getColumnIndex(columnName) == p._1).map(x => x._2)
      }).map(x => {
        x.mkString(" ")
      })
    }).map(x => {
      x.mkString(" ")
    }).map(x => {
      x.trim.replaceAll(" +", " ")
    }) //.filter(x => x.length > 0)
    println(entry)
    entry
  }

  def setAddressBook(clcnHeader: Seq[(Int, String)], clcnData: List[(Int, Seq[(Int, String)])]): List[Seq[String]] = {
    val clcnAddressBook = clcnData.map(line => {
      extractAddressLine(clcnHeader, line)
    }).sortBy(x => x(0) + x(1))
    clcnAddressBook
  }

  def getDocumentFont(): Map[Int, PdfFont] = {
    val clcnFont = scala.collection.mutable.Map[Int, PdfFont]()
    clcnFont += (clcnFont.size -> PdfFontFactory.createFont(StandardFonts.TIMES_BOLD))
    clcnFont += (clcnFont.size -> PdfFontFactory.createFont(StandardFonts.TIMES_BOLD))
    clcnFont += (clcnFont.size -> PdfFontFactory.createFont(StandardFonts.TIMES_BOLD))
    clcnFont += (clcnFont.size -> PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN))
    clcnFont += (clcnFont.size -> PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN))
    clcnFont += (clcnFont.size -> PdfFontFactory.createFont(StandardFonts.TIMES_ITALIC))
    clcnFont += (clcnFont.size -> PdfFontFactory.createFont(StandardFonts.TIMES_ITALIC))
    clcnFont.toMap
  }

  def getDocumentFontSize(): Map[Int, Int] = {
    val clcnFontSize = scala.collection.mutable.Map[Int, Int]()
    clcnFontSize += (clcnFontSize.size -> 18)
    clcnFontSize += (clcnFontSize.size -> 18)
    clcnFontSize += (clcnFontSize.size -> 12)
    clcnFontSize += (clcnFontSize.size -> 8)
    clcnFontSize += (clcnFontSize.size -> 8)
    clcnFontSize += (clcnFontSize.size -> 8)
    clcnFontSize += (clcnFontSize.size -> 12)
    clcnFontSize.toMap
  }

  def getDocumentAlignment(): Map[Int, TextAlignment] = {
    val clcnAlignment = scala.collection.mutable.Map[Int, TextAlignment]()
    clcnAlignment += (clcnAlignment.size -> TextAlignment.CENTER)
    clcnAlignment += (clcnAlignment.size -> TextAlignment.LEFT)
    clcnAlignment += (clcnAlignment.size -> TextAlignment.LEFT)
    clcnAlignment += (clcnAlignment.size -> TextAlignment.LEFT)
    clcnAlignment += (clcnAlignment.size -> TextAlignment.LEFT)
    clcnAlignment += (clcnAlignment.size -> TextAlignment.LEFT)
    clcnAlignment += (clcnAlignment.size -> TextAlignment.RIGHT)
    clcnAlignment.toMap
  }

  def getParagraph(alignment: TextAlignment, fontSize: Int): Paragraph = {
    val paragraph = new Paragraph()
    paragraph.setTextAlignment(alignment)
    paragraph.setFontSize(fontSize)
    paragraph
  }

  def convertToPdf(clcnAddressBook: List[Seq[String]]): Unit = {
    val clcnFont = getDocumentFont()
    val clcnFontSize = getDocumentFontSize()
    val clcnAlignment = getDocumentAlignment()
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
      val clcnAddressBook = setAddressBook(clcnHeader, clcnData)
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
