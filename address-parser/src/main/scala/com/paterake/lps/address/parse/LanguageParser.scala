package com.paterake.lps.address.parse

class LanguageParser(sourceFileName: String, sheetName: String) {
  import scala.collection.JavaConverters._

  def this() {
    this("/home/paterake/Downloads/Master list Guju-Name-Gaam- 8 Feb 2021.xlsx", "Corrected 23-02-21")
  }

  def normaliseData(clcnData: List[(Int, Seq[(Int, String)])]): Map[String, String] = {
    val clcn = clcnData
      //.map(x => (x._2(1)._2.replaceAll("[()]", ""), x._2(2)._2.replaceAll("[()]", "")))
      .map(x => (x._2(1)._2, x._2(2)._2))
      .filter(p => p._1 != null && p._2 != null && !p._1.equals("") && !p._2.equals(""))
      .foldLeft(Map[String, String]()) { (m, s) => m + s }
    clcn
  }

  def processWorkbook(clcnArg: Array[String]): Map[String, String] = {
    val workbook = SpreadsheetReader.openWorkbook(sourceFileName, clcnArg)
    var clcnTranslation:Map[String, String] = null
    workbook.sheetIterator().asScala.filter(p => p.getSheetName.equals(sheetName)).foreach(f => {
      println(f.getSheetName)
      val (clcnHeader, clcnData) = SpreadsheetReader.extractSheet(f)
      clcnTranslation = normaliseData(clcnData)
    })
    clcnTranslation
  }

  def getTranslation(clcnArg: Array[String]): Map[String, String] = {
    val clcnTranslation = processWorkbook(clcnArg)
    clcnTranslation
  }

}

object LanguageParser extends App {
  val parser = new LanguageParser()
  val clcnTranslation = parser.getTranslation(args)
  clcnTranslation.foreach(x => println(x))
}