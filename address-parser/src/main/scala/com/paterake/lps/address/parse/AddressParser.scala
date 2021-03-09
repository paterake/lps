package com.paterake.lps.address.parse

import com.paterake.lps.address.cfg.reader.{CfgAddress, CfgRegion}

class AddressParser(cfgAddressName: String, inputFileName: String, outputFileName: String) {

  import scala.collection.JavaConverters._

  private val clcnCfgAddress = new CfgAddress(cfgAddressName).getCfg()
  private val cfgRegion = new CfgRegion()
  private val mainNameIndex = 2

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

  def getEntry(addressLine: (Int, Seq[(Int, String)]), clcnLineElement: List[String], clcnParenthesis: List[String], elementSeparator: String, fontCase: String, dropSurname: Boolean, surname: String): String = {
    if (clcnLineElement == null || clcnLineElement.isEmpty) {
      null
    } else {
      val clcnEntry = clcnLineElement.map(columnName => {
        addressLine._2.filter(p => SpreadsheetReader.getColumnIndex(columnName) == p._1).map(x => {
          if (clcnParenthesis != null && clcnParenthesis.contains(columnName) && x._2.nonEmpty) {
            if (x._2.startsWith("(") && x._2.endsWith(")")) {
              if (fontCase != null && fontCase.equals("lower")) {
                x._2.toLowerCase
              } else {
                x._2
              }
            } else {
              if (fontCase != null && fontCase.equals("lower")) {
                "(" + x._2.toLowerCase + ")"
              } else {
                "(" + x._2 + ")"
              }
            }
          } else {
            if (fontCase != null && fontCase.equals("lower")) {
              x._2.toLowerCase
            } else {
              if (dropSurname) {
                x._2.replace(" " + surname, "")
              } else {
                x._2
              }
            }
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
    val surname = addressLine._2(mainNameIndex)._2.split(" ").filterNot(p => p.equals("(Late)")).reverse.head
    val entry = clcnCfgAddress.sortBy(line => line.lineId).map(cfg => {
      val leftEntry = getEntry(addressLine, cfg.clcnLineElement, cfg.clcnParenthesis, cfg.elementSeparator, cfg.fontCase, cfg.dropSurname, surname)
      val rightEntry = getEntry(addressLine, cfg.clcnLineElementRight, cfg.clcnParenthesis, cfg.elementSeparatorRight, cfg.fontCase, cfg.dropSurname, surname)
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

  def processWorkbook(clcnArg: Array[String]): Unit = {
    val clcnTranslation = new LanguageParser().getTranslation(null)
    val pdfBuilder = new PdfBuilder(outputFileName, clcnTranslation)
    val workbook = SpreadsheetReader.openWorkbook(inputFileName, clcnArg)
    workbook.sheetIterator().asScala.foreach(f => {
      val region = cfgRegion.getCfg(f.getSheetName)
      pdfBuilder.startNewPage(f.getSheetName, region.blankPageCount)
      val (clcnHeader, clcnData) = SpreadsheetReader.extractSheet(f)
      val clcnAddressBook = setAddressBook(clcnData)
      //println(clcnAddressBook)
      pdfBuilder.convertToPdf(f.getSheetName, clcnCfgAddress, clcnAddressBook)
    })
    pdfBuilder.startNewPage("Index", 0)
    pdfBuilder.addNameIndex()
    if (clcnArg.length == 1) {
      pdfBuilder.closeDocument(clcnArg(0))
    } else {
      pdfBuilder.closeDocument()
    }
  }

}

object AddressParser extends App {
  val cfgAddressName = "cfgAddress"
  val sourceFileName = "/home/paterake/Downloads/Master sheet V3 All regions Updated - For PDF production.xlsx"
  val targetFileName = "/home/paterake/Downloads/lps_draft"
  val parser = new AddressParser(cfgAddressName, sourceFileName, targetFileName)
  parser.processWorkbook(args)
}
