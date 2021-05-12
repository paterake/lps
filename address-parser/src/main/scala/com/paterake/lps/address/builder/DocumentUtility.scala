package com.paterake.lps.address.builder

import com.paterake.lps.address.cfg.model.{ModelCfgAddress, ModelCfgIndex}
import com.paterake.lps.address.parse.Location

object DocumentUtility {
  private val clcnNameSuffix = scala.io.Source.fromInputStream(getClass.getResourceAsStream(Location.nameSuffix)).getLines.toList

  def getParagraghFormat(clcnCfgAddress: List[ModelCfgAddress]): (Map[Int, String], Map[Int, Int], Map[Int, String], Map[Int, String], Map[Int, Int], Map[Int, String]) = {
    val clcnFont = scala.collection.mutable.Map[Int, String]()
    val clcnFontSize = scala.collection.mutable.Map[Int, Int]()
    val clcnAlignment = scala.collection.mutable.Map[Int, String]()
    val clcnFontRight = scala.collection.mutable.Map[Int, String]()
    val clcnFontSizeRight = scala.collection.mutable.Map[Int, Int]()
    val clcnAlignmentRight = scala.collection.mutable.Map[Int, String]()

    clcnCfgAddress.sortBy(x => x.lineId).zipWithIndex.foreach(cfg => {
      clcnFont += (cfg._2 -> cfg._1.font)
      clcnFontSize += (cfg._2 -> cfg._1.fontSize)
      clcnAlignment += (cfg._2 -> cfg._1.textAlignment)
      if (cfg._1.fontRight != null) {
        clcnFontRight += (cfg._2 -> cfg._1.fontRight)
      }
      if (cfg._1.fontSizeRight != null && cfg._1.fontSizeRight > 0) {
        clcnFontSizeRight += (cfg._2 -> cfg._1.fontSizeRight)
      }
      if (cfg._1.textAlignmentRight != null) {
        clcnAlignmentRight += (cfg._2 -> cfg._1.textAlignmentRight)
      }
    })
    (clcnFont.toMap, clcnFontSize.toMap, clcnAlignment.toMap, clcnFontRight.toMap, clcnFontSizeRight.toMap, clcnAlignmentRight.toMap)
  }

  def stripSuffix(name: String): String = {
    val newName = clcnNameSuffix.foldLeft(name)((a, b) => a.replaceAll(b, ""))
    //val newName = name.split(" ").map(x => clcnNameSuffix.foldLeft(x)((a, b) => a.stripSuffix(b))).mkString(" ")
    newName
  }

  def getIndexEntry(entryLineNumber: Int, entry: List[(String, String)], header: String, pageCount: Int): ModelCfgIndex = {
    val indexEntry = {
      if (entryLineNumber.equals(1)) {
        val mainName = entry(1)._1.split(" ").filterNot(p => p.equals("(Late)")).filterNot(p => p.equals("Patel")).mkString(" ")
        val spouseName = entry(2)._1.split(" ").filterNot(p => p.equals("(Late)")).filterNot(p => p.equals("Patel")).head
        val village = entry(0)._1
        val spouseVillage = null
        ModelCfgIndex(stripSuffix(mainName), stripSuffix(spouseName), village, spouseVillage, header, pageCount)
      } else {
        val mainName = entry(2)._1.split(" ").filterNot(p => p.equals("(Late)")).filterNot(p => p.equals("Patel")).mkString(" ")
        val spouseName = entry(1)._1.split(" ").filterNot(p => p.equals("(Late)")).filterNot(p => p.equals("Patel")).head
        val village = entry(0)._1
        val spouseVillage = entry(0)._2
        ModelCfgIndex(stripSuffix(mainName.replace(spouseVillage, "").replace("()", "").trim)
          , stripSuffix(spouseName)
          , village
          , spouseVillage
          , header
          , pageCount
        )
      }
    }
    indexEntry
  }


}
