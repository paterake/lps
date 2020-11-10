package com.paterake.lps.address.parser

import java.io.FileInputStream

import org.apache.poi.ss.usermodel.{Cell, CellType, Row, Sheet}
import org.apache.poi.xssf.usermodel.{XSSFCell, XSSFWorkbook}

import scala.collection.mutable

class AddressParser(inputFileName: String) {
  import scala.collection.JavaConverters._
  val inputFileStream = new FileInputStream(inputFileName)
  val clcnHeader = new mutable.HashMap[Integer, String]()
  val clcnTable = new mutable.HashMap[Integer, mutable.HashMap[String, String]]()

  def getCellValue(cell: Cell): String = {
    cell.getCellType match {
      case CellType.NUMERIC => cell.getNumericCellValue.toString
      case _ => cell.getStringCellValue
    }
  }

  def setHeader(row: Row): Unit = {
    row.iterator().asScala.zipWithIndex.foreach(cell => {
      clcnHeader.put(cell._2, cell._1.getStringCellValue)
    })
  }

  def getDataRow(row: Row): mutable.HashMap[String, String] = {
    val clcnData = new mutable.HashMap[String, String]()
    row.iterator().asScala.zipWithIndex.foreach(cell => {
      clcnData.put(clcnHeader(cell._2), getCellValue(cell._1))
    })
    clcnData
  }

  def processSheet(sheet: Sheet): Unit = {
    println(sheet.getSheetName)
    sheet.iterator().asScala.zipWithIndex.foreach(row => {
      if (row._2 == 0) {
        setHeader(row._1)
      } else {
        clcnTable.put(row._2, getDataRow(row._1))
      }
    })
    clcnHeader.foreach(f => println(f._1 + ":" + f._2))
    clcnTable.foreach(row => {
      println(row._1 + ":" + row._2.mkString("=>"))
    })
  }

  def processWorkbook() : Unit = {
    val workbook = new XSSFWorkbook(inputFileStream)
    workbook.sheetIterator().asScala.foreach(f => {
      processSheet(f)
    })
  }
}

object AddressParser extends App {
  val parser = new AddressParser("/Users/rrpate/Downloads/Template4Coder.xlsx")
  parser.processWorkbook()
}
