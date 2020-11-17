package com.paterake.lps.address.parser

import java.io.{FileInputStream, FileOutputStream}

import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.font.{PdfFont, PdfFontFactory}
import com.itextpdf.layout.element.Paragraph
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

  def getDocumentFontSize(): Map[Int, Int] = {
    val clcnFontSize = scala.collection.mutable.Map[Int, Int]()
    clcnFontSize += (clcnFontSize.size -> 18)
    clcnFontSize += (clcnFontSize.size -> 12)
    clcnFontSize += (clcnFontSize.size -> 8)
    clcnFontSize += (clcnFontSize.size -> 8)
    clcnFontSize += (clcnFontSize.size -> 8)
    clcnFontSize += (clcnFontSize.size -> 12)
    clcnFontSize.toMap
  }

  def getDocumentFont(): Map[Int, PdfFont] = {
    val clcnFont = scala.collection.mutable.Map[Int, PdfFont]()
    clcnFont += (clcnFont.size -> PdfFontFactory.createFont(StandardFonts.TIMES_BOLD))
    clcnFont += (clcnFont.size -> PdfFontFactory.createFont(StandardFonts.TIMES_BOLD))
    clcnFont += (clcnFont.size -> PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN))
    clcnFont += (clcnFont.size -> PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN))
    clcnFont += (clcnFont.size -> PdfFontFactory.createFont(StandardFonts.TIMES_ITALIC))
    clcnFont += (clcnFont.size -> PdfFontFactory.createFont(StandardFonts.TIMES_ITALIC))
    clcnFont.toMap
  }

  def getAlignedParagraph(text: String, alignment: Int): Paragraph = {
    val paragraph = new Paragraph(text)
    paragraph.setAlignment(alignment)
    paragraph
  }

  def getDocumentAlignment(): Map[Int, Int] = {
    val clcnAlignment = scala.collection.mutable.Map[Int, Int]()
    clcnAlignment += (clcnAlignment.size -> Element.ALIGN_LEFT)
    clcnAlignment += (clcnAlignment.size -> Element.ALIGN_LEFT)
    clcnAlignment += (clcnAlignment.size -> Element.ALIGN_LEFT)
    clcnAlignment += (clcnAlignment.size -> Element.ALIGN_LEFT)
    clcnAlignment += (clcnAlignment.size -> Element.ALIGN_LEFT)
    clcnAlignment += (clcnAlignment.size -> Element.ALIGN_CENTER)
    clcnAlignment.toMap
  }

  def convertToPdf(clcnAddressBook: List[Seq[String]]): Unit = {
    val clcnFont = getDocumentFont()
    val clcnAlignment = getDocumentAlignment()
    val document = new Document(PageSize.A5)
    val writer = PdfWriter.getInstance(document, new FileOutputStream(outputFileName))
    document.open()
    clcnAddressBook.foreach(entry => {
      entry.zipWithIndex.foreach(line => {
        val font = clcnFont(line._2)
        val alignment = clcnAlignment(line._2)
        if (line._1.size > 0) {
          val paragraph = getAlignedParagraph(line._1, alignment)
        }
      })
    })
    document.add(new Paragraph("First Page."))
    document.setPageSize(PageSize.A5)
    document.newPage()
    document.add(new Paragraph("This PageSize is DIN A5."))
    document.close()
    writer.close()
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
