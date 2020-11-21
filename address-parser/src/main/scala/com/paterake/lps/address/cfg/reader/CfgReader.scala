package com.paterake.lps.address.cfg.reader

import java.io.{PrintWriter, StringWriter}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

class CfgReader {

  def readConfiguration[T: Manifest](fileName: String): T = {
    try{
      val json = scala.io.Source.fromInputStream(this.getClass.getResourceAsStream(String.format("/%s.json", fileName))).mkString
      val objectMapper = new ObjectMapper() with ScalaObjectMapper
      objectMapper.registerModule(DefaultScalaModule)
      val parsedJson = objectMapper.readValue[T](json)
      parsedJson
    } catch {
      case ex: Throwable => {
        val sw = new StringWriter
        ex.printStackTrace(new PrintWriter(sw))
        println(ex.toString)
        null.asInstanceOf[T]
      }
    }
  }
}
