package com.paterake.lps.address.translate

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths, StandardOpenOption}
import scala.collection.mutable
import scala.io.Source

object TranslationMap {
  private val mappingFileName = "/home/paterak/lsp_language_mapping.txt"
  private val mappingFileWriter = Files.newBufferedWriter(Paths.get(mappingFileName), StandardCharsets.UTF_16, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)
  private val clcnTranslation: mutable.Map[String, String] = getLanguageMapping(mappingFileName)
  private val translationService = new TranslationService

  private def getLanguageMapping(fileName: String): mutable.Map[String, String] = {
    val clcnTranslation = mutable.Map[String, String]()
    val src = Source.fromFile(fileName, "UTF-16")
    src.getLines().filter(p => p.size > 1).foreach(x => {
      val kv = x.split("~")
      clcnTranslation += (kv.head.trim -> kv.tail.head.trim)
    })
    clcnTranslation
  }

  def getTranslationMap(): mutable.Map[String, String] = {
    clcnTranslation
  }

  def getTranslation(srcText: String, tgtLanguageCode: String): String = {
    val key = String.format("%s_%s", tgtLanguageCode, srcText)
    if (!clcnTranslation.contains(key)) {
      println("calling translation service for: " + srcText)
      val translatedText = translationService.getTranslation(srcText, tgtLanguageCode)
      clcnTranslation += (key -> translatedText)
      mappingFileWriter.newLine()
      mappingFileWriter.append(key + "~" + translatedText)
      mappingFileWriter.newLine()
      mappingFileWriter.flush()
    }
    clcnTranslation(key)
  }

  def persistTranslationMap(): Unit = {
    val path = Paths.get(mappingFileName)
    val writer = Files.newBufferedWriter(path, StandardCharsets.UTF_16, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
    clcnTranslation.foreach(x => {
      writer.append(x._1 + "~" + x._2)
      writer.newLine()
    })
    writer.flush()
    writer.close()
  }

  def close(): Unit = {
    mappingFileWriter.flush()
    mappingFileWriter.close()
  }

}
