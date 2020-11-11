package com.paterake.lps.address.parser

import java.io.FileInputStream

import org.apache.poi.ss.usermodel.{Cell, CellType, Row, Sheet}
import org.apache.poi.ss.util.CellReference
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class AddressParser(inputFileName: String) {

  import scala.collection.JavaConverters._

  val inputFileStream = new FileInputStream(inputFileName)
  val clcnAddressModel: Seq[(Int, Array[String])] = new AddressModel().getAddressCfg()

  def getColumnIndex(columnName: String): Int = {
    CellReference.convertColStringToIndex(columnName)
  }

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

  def extractSheet(sheet: Sheet): (Seq[(Int, String)], List[(Int, Seq[(Int, String)])]) = {
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
    (clcnHeader, clcnData.toList)
  }

  def processWorkbook(): Unit = {
    val workbook = new XSSFWorkbook(inputFileStream)
    workbook.sheetIterator().asScala.foreach(f => {
      val (clcnHeader, clcnData) = extractSheet(f)
    })
    clcnAddressModel.foreach(line => {
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
    //println(getColumnIndex("A"))
  }
}

object AddressParser extends App {
  val parser = new AddressParser("/Users/rrpate/Downloads/Template4Coder.xlsx")
  parser.processWorkbook()
}
