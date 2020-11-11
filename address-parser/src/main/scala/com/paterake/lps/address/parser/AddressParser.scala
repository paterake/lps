package com.paterake.lps.address.parser

import java.io.FileInputStream

import org.apache.poi.ss.usermodel.{Cell, CellType, Row, Sheet}
import org.apache.poi.xssf.usermodel.{XSSFCell, XSSFWorkbook}

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

class AddressParser(inputFileName: String) {
  import scala.collection.JavaConverters._
  val inputFileStream = new FileInputStream(inputFileName)

  def getCellValue(cell: Cell): String = {
    cell.getCellType match {
      case CellType.NUMERIC => cell.getNumericCellValue.toString
      case _ => cell.getStringCellValue
    }
  }

  def getHeaderRow(row: Row): Seq[(Int, String)] = {
    val clcnData = scala.collection.mutable.ListBuffer.empty[(Int, String)]
    row.iterator().asScala.zipWithIndex.foreach(cell => {
      clcnData.append((cell._2, cell._1.getStringCellValue))
    })
    clcnData.sortBy(_._1)
  }

  def getDataRow(row: Row): Seq[(Int, String)] = {
    val clcnData = scala.collection.mutable.ListBuffer.empty[(Int, String)]
    row.iterator().asScala.zipWithIndex.foreach(cell => {
      clcnData.append((cell._2, getCellValue(cell._1)))
    })
    clcnData.sortBy(_._1)
  }

  def processSheet(sheet: Sheet): Unit = {
    println(sheet.getSheetName)
    var clcnHeader: Seq[(Int, String)] = null
    val clcnData = scala.collection.mutable.ListBuffer.empty[(Int, Seq[(Int, String)])]

    sheet.iterator().asScala.zipWithIndex.foreach(row => {
      if (row._2 == 0) {
        clcnHeader = getHeaderRow(row._1)
      } else {
        clcnData.append((row._2, getDataRow(row._1)))
      }
    })
    clcnHeader.foreach(f => println(f._1 + ":" + f._2))
    clcnData.foreach(row => {
      println(row._1 + ":" + row._2.mkString(";"))
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
