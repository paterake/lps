package com.paterake.lps.address.parse

import org.apache.poi.ss.usermodel.{Cell, CellType, Row, Sheet}
import org.apache.poi.ss.util.CellReference

object SpreadsheetReader {
  import scala.collection.JavaConverters._

  def outputSheet(clcnHeader: Seq[(Int, String)], clcnData: List[(Int, Seq[(Int, String)])]): Unit = {
    clcnHeader.foreach(f => println(f._1 + ":" + f._2))
    clcnData.foreach(row => {
      println(row._1 + ":" + row._2.mkString(";"))
    })
  }

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

  def extractSheet(sheet: Sheet): (Seq[(Int, String)], List[(Int, Seq[(Int, String)])]) = {
    println(sheet.getSheetName)
    var clcnHeader: Seq[(Int, String)] = null
    val clcnData = scala.collection.mutable.ListBuffer.empty[(Int, Seq[(Int, String)])]

    sheet.iterator().asScala.zipWithIndex.foreach(row => {
      if (row._2 == 0) {
        clcnHeader = SpreadsheetReader.getHeaderRow(row._1)
      } else {
        clcnData.append((row._2, SpreadsheetReader.getDataRow(row._1, clcnHeader.size)))
      }
    })
    (clcnHeader, clcnData.toList)
  }


}
