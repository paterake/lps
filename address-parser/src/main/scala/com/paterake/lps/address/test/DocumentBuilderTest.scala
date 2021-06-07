package com.paterake.lps.address.test

import com.paterake.lps.address.builder.DocumentBuilder
import org.apache.poi.xwpf.usermodel.ParagraphAlignment

import java.io.{File, FileOutputStream}

class DocumentBuilderTest {

  def test(): Unit = {
    val clcnFontSize = Seq(12, 10)
    val clcnFont = Seq("Helvetica", "Helvetica-Bold")
    val builder = new DocumentBuilder("test.docx", null)
    val document = builder.getDocument()
    //registerFont

    val paragraph = document.createParagraph()
    paragraph.setAlignment(ParagraphAlignment.CENTER)
    val run = paragraph.createRun()
    run.setText("Hello Word")

    val p2 = document.createParagraph()
    p2.setAlignment(ParagraphAlignment.LEFT)
    val r2 = p2.createRun()
    r2.setText("Text body....")

    builder.setSubHeader(Seq("Asta", " (આસ્તા)"), 12)

    builder.addText(Seq("val1", "val2"), clcnFontSize, clcnFont)
    builder.addText(Seq("val3"), clcnFontSize, clcnFont)

    builder.setSubHeaderLine()

    builder.addText(Seq("val4", "val5"), clcnFontSize, clcnFont)
    builder.addText(Seq("val6"), clcnFontSize, clcnFont)
    builder.addText(Seq("abcd", "01999 999-6665"), clcnFontSize, clcnFont)


    val out = new FileOutputStream(new File("test.docx"))
    document.write(out)
    out.close()
  }
}


object DocumentBuilderTest extends App {
  val doc = new DocumentBuilderTest
  doc.test()
}