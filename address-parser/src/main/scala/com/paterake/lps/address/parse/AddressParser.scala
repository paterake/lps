package com.paterake.lps.address.parse

import com.paterake.lps.address.builder.{DocumentBuilder, DocumentBuilderIdx}
import com.paterake.lps.address.cfg.reader.{CfgAddress, CfgRegion}

class AddressParser(cfgAddressName: String, inputFileName: String, outputFileName: String) {

  import scala.collection.JavaConverters._

  private val clcnCfgAddress = new CfgAddress(cfgAddressName).getCfg()
  private val cfgRegion = new CfgRegion()
  private val mainNameIndex = 2
  private val clcnAddressDrop = scala.io.Source.fromInputStream(getClass.getResourceAsStream(Location.addressDrop)).getLines.toList.sorted


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

  def getFormattedEntry(clcnParenthesis: List[String], fontCase: String, dropSurname: Boolean, surname: String, columnName: String, entry: (Int, String)): String = {
    var formattedEntry = entry._2
    if (formattedEntry.startsWith("(") && formattedEntry.endsWith(")")) {
      formattedEntry = formattedEntry.stripPrefix("(").stripSuffix(")")
    }
    if (dropSurname) {
      formattedEntry = formattedEntry.replace(" " + surname, "")
    }
    if (fontCase != null) {
      if (fontCase.equals("lower")) {
        formattedEntry = formattedEntry.toLowerCase
      } else if (fontCase.equals("initcap")) {
        formattedEntry = formattedEntry.toLowerCase.split(' ').map(_.capitalize).mkString(" ")
        if (formattedEntry.contains("(")) {
          formattedEntry = formattedEntry.split('(').map(_.capitalize).mkString("(")
        }
        if (formattedEntry.contains("-")) {
          formattedEntry = formattedEntry.split('-').map(_.capitalize).mkString("-")
        }
        if (formattedEntry.split(" ").contains("Uk")) {
          formattedEntry = formattedEntry.split(" ").map(x => {
            if (x.equals("Uk")) {
              x.toUpperCase
            } else {
              x
            }
          }).mkString(" ")
        }
      }
    }
    if (clcnParenthesis != null && clcnParenthesis.contains(columnName) && entry._2.nonEmpty) {
      formattedEntry = "(" + formattedEntry + ")"
    }
    formattedEntry
  }

  def getEntry(lineId: Int, addressLine: (Int, Seq[(Int, String)]), clcnLineElement: List[String], clcnParenthesis: List[String], elementSeparator: String, fontCase: String, dropSurname: Boolean, surname: String, singleInd: Boolean, singleIdx: Int): String = {
    if (clcnLineElement == null || clcnLineElement.isEmpty) {
      null
    } else {
      val clcnEntry = clcnLineElement.map(columnName => {
        addressLine._2.filter(p => SpreadsheetReader.getColumnIndex(columnName) == p._1).map(x => {
          getFormattedEntry(clcnParenthesis, fontCase, dropSurname, surname, columnName, x)
        })
      }).map(x => {
        x.mkString(" ")
      })
      val entry = if (singleInd) {
        if (clcnEntry.count(p => p.nonEmpty) == 1 && singleIdx == 1) {
          clcnEntry.filter(p => p.nonEmpty).mkString(elementSeparator)
        } else if (clcnEntry.count(p => p.nonEmpty) == 1 && singleIdx != 1) {
          ""
        } else {
          clcnEntry(singleIdx-1)
        }
      } else {
        clcnEntry.filter(p => p.nonEmpty).mkString(elementSeparator)
      }
      if (lineId != 4) {
        entry
      } else if (lineId == 4 && entry.length <= 70) {
        entry
      } else if (lineId == 4 && entry.length > 70) {
        val clcnAddress = entry.split(",")
        var replacement = clcnAddress.map(x => x.trim).filter(p => !clcnAddressDrop.contains(p.trim)).mkString(", ")
        replacement = replacement.replaceAll("Uxbridge, Middlesex", "Uxbridge")
        replacement = replacement.replaceAll("Benfleet, Essex", "Benfleet")
        replacement = replacement.replaceAll("Buckinghamshire", "Bucks")
        replacement = replacement.stripPrefix("Plot ")
        replacement = replacement.split(",").filterNot(p => p.trim.equals("")).map(x => x.trim).mkString(", ")
        //println("replaced: " + entry)
        //println("    with: " + replacement)
        replacement
      } else {
        entry
      }
    }
  }

  def extractAddressLine(addressLine: (Int, Seq[(Int, String)])): List[(String, String)] = {
    val surname = addressLine._2(mainNameIndex)._2
      .replaceAll("\\(.*?\\)", "")
      .split(" ").filterNot(p => p.equals("(Late)")).reverse.head
    val entry = clcnCfgAddress.sortBy(line => line.lineId).map(cfg => {
      val leftEntry = getEntry(cfg.lineId, addressLine, cfg.clcnLineElement, cfg.clcnParenthesis, cfg.elementSeparator, cfg.fontCase, cfg.dropSurname, surname, cfg.singleInd, cfg.singleIdx)
      val rightEntry = getEntry(cfg.lineId, addressLine, cfg.clcnLineElementRight, cfg.clcnParenthesis, cfg.elementSeparatorRight, cfg.fontCase, cfg.dropSurname, surname, cfg.singleIndRight, cfg.singleIdxRight)
      (leftEntry, rightEntry)
    })
    //println(entry)
    entry
  }

  def setAddressBook(clcnData: List[(Int, Seq[(Int, String)])]): List[List[(String, String)]] = {
    val clcnAddressBook = clcnData.map(line => {
      extractAddressLine(line)
    }).filter(p => p(0)._1 != null && p(0)._1 != "").sortBy(x => {
      val sortKey1 = x(0)._1.replaceAll("[^A-Za-z0-9]", "")
      val sortKey2 = x(1)._1.replaceAll("[^A-Za-z0-9]", "")
      val sortPrefix = {
        if (x(0)._1.toLowerCase.startsWith("social") || x(0)._1.toLowerCase.startsWith("overseas")) {
          "zzz"
        } else {
          ""
        }
      }
      (sortPrefix + sortKey1, sortKey2)
    })
    clcnAddressBook
  }

  def processWorkbook(clcnArg: Array[String]): Unit = {
    val clcnTranslation = new LanguageParser().getTranslation(null)
    val docBuilder = new DocumentBuilder(outputFileName, clcnTranslation)
    val docBuilderIdx = new DocumentBuilderIdx(outputFileName, clcnTranslation)
    val workbook = SpreadsheetReader.openWorkbook(inputFileName, clcnArg)
    workbook.sheetIterator().asScala.foreach(f => {
      val blankPageCount = cfgRegion.getCfgBlankPages(f.getSheetName)
      val regionName = cfgRegion.getCfgRename(f.getSheetName)
      docBuilder.startNewPage(regionName, blankPageCount)
      val (clcnHeader, clcnData) = SpreadsheetReader.extractSheet(f)
      val clcnAddressBook = setAddressBook(clcnData)
      //println(clcnAddressBook)
      docBuilder.convertToDoc(regionName, clcnCfgAddress, clcnAddressBook)
    })
    docBuilderIdx.addNameIndex("Index", 1)
    if (clcnArg.length == 1) {
      docBuilder.closeDocument(clcnArg(0))
      docBuilderIdx.closeDocument(clcnArg(0))
    } else {
      docBuilder.closeDocument()
      docBuilderIdx.closeDocument()
    }
  }

}

object AddressParser extends App {
  val cfgAddressName = Location.cfgAddress
  val sourceFileName = Location.sourceFileName
  val targetFileName = Location.targetFileName
  val parser = new AddressParser(cfgAddressName, sourceFileName, targetFileName)
  parser.processWorkbook(args)
}

