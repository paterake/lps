package com.paterake.lps.address.builder

import com.paterake.lps.address.cfg.model.{ModelCfgAddress, ModelCfgIndex}
import com.paterake.lps.address.parse.Location

import scala.collection.mutable.ListBuffer

object DocumentUtility {
  private val clcnNameSuffix = scala.io.Source.fromInputStream(getClass.getResourceAsStream(Location.nameSuffix)).getLines.toList
  private val clcnFailedTranslation = new ListBuffer[String]()

  def outputFailedTranslation(): Unit = {
    clcnFailedTranslation.toList.distinct.sorted.foreach(x => println("Failed to translate name part: " + x))
  }


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
    val clcnDrop = Seq("(late)", "patel", "das-patel", "vara-patel", "patel-surdhar")
    val indexEntry = {
      if (entryLineNumber.equals(1)) {
        val mainName = entry(1)._1.split(" ").filterNot(p => clcnDrop.contains(p.toLowerCase)).mkString(" ")
        val spouseName = entry(2)._1.split(" ").filterNot(p => clcnDrop.contains(p.toLowerCase)).head
        val village = entry(0)._1
        val spouseVillage = null
        ModelCfgIndex(stripSuffix(mainName), stripSuffix(spouseName), village, spouseVillage, header, pageCount)
      } else {
        val mainName = entry(2)._1.split(" ").filterNot(p => clcnDrop.contains(p.toLowerCase)).mkString(" ")
        val spouseName = entry(1)._1.split(" ").filterNot(p => clcnDrop.contains(p.toLowerCase)).head
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

  def getIndexRegionName(regionName: String): String = {
    if (regionName.toLowerCase.startsWith("rest")) {
      regionName
    }
    else {
      regionName.split(" ").head.stripSuffix(",")
    }
  }

  def getPartTranslation(part: String, clcnTranslation: Map[String, String]): String = {
    if (part.startsWith("(") || part.endsWith(")")) {
      val partTranslation = try {
        clcnTranslation(part.replace("(", "").replace(")", ""))
      } catch {
        case _: Exception => {
          clcnFailedTranslation.append(part.replace("(", "").replace(")", ""))
          //println("Failed to translate name part: " + part.replace("(", "").replace(")", ""))
          ""
        }
      }
      val newPart = StringBuilder.newBuilder
      if (part.startsWith("(")) {
        newPart.append("(")
      }
      newPart.append(partTranslation)
      if (part.endsWith(")")) {
        newPart.append(")")
      }
      newPart.mkString
    } else {
      try {
        clcnTranslation(part)
      } catch {
        case _: Exception => {
          clcnFailedTranslation.append(part)
          //println("Failed to translate name part: " + part)
          ""
        }
      }
    }
  }


  def getIndexName(clcnTranslation: Map[String, String], mainName: String, spouseName: String): (String, String) = {
    val memberName = if (mainName.split(" ").size > 3) {
      val clcnName = mainName.split(" ")
      clcnName(0) + " " + clcnName(1) + " " + clcnName.reverse.head
      //clcnName(0) + " " + clcnName(1)
    } else {
      mainName
    }
    val indexName = if (spouseName == null || spouseName.length < 1) {
      memberName
    } else {
      memberName + " (" + spouseName + ")"
    }
    val translation = memberName.replaceAll("\\(.*?\\)", "").split(" ").filter(p => p.trim.nonEmpty).filter(p => p.length > 1).map(part => {
      getPartTranslation(part, clcnTranslation)
    }).mkString(" ")
    (indexName, translation)
  }

  def getIndexVillageName(mainVillageName: String, spouseVillageName: String): String = {
    val villageName =
      if (spouseVillageName == null || spouseVillageName.length < 1) {
        mainVillageName
      } else {
        mainVillageName + " (" + spouseVillageName.replaceAll("[\\[\\](){}]", "") + ")"
      }
    villageName
  }




}
