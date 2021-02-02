package com.paterake.lps.address.parse

import java.io.File
import com.paterake.lps.address.cfg.reader.CfgAddress
import org.apache.poi.ss.usermodel.{Workbook, WorkbookFactory}

class AddressParser(cfgName: String, inputFileName: String, outputFileName: String) {
  import scala.collection.JavaConverters._

  private val clcnCfgAddress = new CfgAddress(cfgName).getCfg()

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
        print(SpreadsheetReader.getColumnIndex(colName) + ":")
      })
      println("")
    })
  }

  def getEntry(addressLine: (Int, Seq[(Int, String)]), clcnLineElement: List[String], clcnParenthesis: List[String], elementSeparator: String): String = {
    if (clcnLineElement == null || clcnLineElement.isEmpty) {
      null
    } else {
      val clcnEntry = clcnLineElement.map(columnName => {
        addressLine._2.filter(p => SpreadsheetReader.getColumnIndex(columnName) == p._1).map(x => {
          if (clcnParenthesis != null && clcnParenthesis.contains(columnName) && x._2.nonEmpty) {
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
      val entry = clcnEntry.filter(p => p.nonEmpty).mkString(elementSeparator)
      entry
    }
  }

  def extractAddressLine(addressLine: (Int, Seq[(Int, String)])): List[(String, String)] = {
    val entry = clcnCfgAddress.sortBy(line => line.lineId).map(cfg => {
      val leftEntry = getEntry(addressLine, cfg.clcnLineElement, cfg.clcnParenthesis, cfg.elementSeparator)
      val rightEntry = getEntry(addressLine, cfg.clcnLineElementRight, cfg.clcnParenthesis, cfg.elementSeparatorRight)
      (leftEntry, rightEntry)
    })
    //println(entry)
    entry
  }

  def setAddressBook(clcnData: List[(Int, Seq[(Int, String)])]): List[List[(String, String)]] = {
    val clcnAddressBook = clcnData.map(line => {
      extractAddressLine(line)
    }).filter(p => p(0)._1 != null && p(0)._1 != "").sortBy(x => {
      if (x(0)._1.toLowerCase.startsWith("social") || x(0)._1.toLowerCase.startsWith("overseas")) {
        "zzz" + x(0)._1 + x(1)._1
      } else {
        x(0)._1 + x(1)._1
      }
    })
    clcnAddressBook
  }

  def openWorkbook(clcnArg: Array[String]): Workbook = {
    val workbook =
      if (clcnArg.length == 1) {
        WorkbookFactory.create(new File(inputFileName), clcnArg(0))
      } else {
        WorkbookFactory.create(new File(inputFileName))
      }
    workbook
  }

  def processWorkbook(clcnArg: Array[String]): Unit = {
    val pdfBuilder = new PdfBuilder(outputFileName)
    val workbook = openWorkbook(clcnArg)
    workbook.sheetIterator().asScala.foreach(f => {
      pdfBuilder.startNewPage(f.getSheetName)
      val (clcnHeader, clcnData) = SpreadsheetReader.extractSheet(f)
      val clcnAddressBook = setAddressBook(clcnData)
      //println(clcnAddressBook)
      pdfBuilder.convertToPdf(f.getSheetName, clcnCfgAddress, clcnAddressBook)
    })
    if (clcnArg.length == 1) {
      pdfBuilder.closeDocument(clcnArg(0))
    } else {
      pdfBuilder.closeDocument()
    }
  }

}

object AddressParser extends App {
  val cfgName = "cfgAddress_preston"
  val sourceFileName = "/home/paterake/Downloads/Master sheet V2.2 All regions - For RCP PDF production 31 Jan 2021.xlsx"
  val targetFileName = "/home/paterake/Downloads/lps_draft"
  val parser = new AddressParser(cfgName, sourceFileName, targetFileName)
  parser.processWorkbook(args)
}
