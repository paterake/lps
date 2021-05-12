package com.paterake.lps.address.builder

import org.apache.poi.xwpf.usermodel.{ParagraphAlignment, XWPFDocument}

import java.io.{File, FileOutputStream}

class DocumentBuilderTest {

  def test(): Unit = {
    val builder = new DocumentBuilder(null, null)
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

    val p3 = document.createParagraph()
    p3.setAlignment(ParagraphAlignment.LEFT)
    val r3 = p3.createRun()
    r3.setText("આસ્તા")
    r3.setFontFamily("Noto Sans Gujarati")

    builder.setPageBreak()

    val p4 = document.createParagraph()
    p4.setAlignment(ParagraphAlignment.LEFT)
    val r4 = p4.createRun()
    r4.setText("આસ્તા")
    r4.setFontFamily("Gujarati Sangam MN")
    r4.setBold(true)

    builder.setSubHeader(Seq("Asta", " (આસ્તા)"))

    val p5 = document.createParagraph()
    p5.setAlignment(ParagraphAlignment.LEFT)
    val r5 = p5.createRun()
    r5.setText("Next")

    builder.setSubHeaderLine()

    val p6 = document.createParagraph()
    p6.setAlignment(ParagraphAlignment.LEFT)
    val r6 = p6.createRun()
    r6.setText("Next")

    builder.addText(Seq("val1", "val2"))
    builder.addText(Seq("val3"))

    val out = new FileOutputStream(new File("test.docx"))
    document.write(out)
    out.close()
  }
}


object DocumentBuilderTest extends App {
  val doc = new DocumentBuilderTest
  doc.test()
}