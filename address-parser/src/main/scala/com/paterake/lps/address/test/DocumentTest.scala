package com.paterake.lps.address.test

import org.apache.poi.xwpf.usermodel.{ParagraphAlignment, XWPFDocument}

import java.awt.{Font, GraphicsEnvironment}
import java.io.{File, FileInputStream, FileOutputStream, InputStream}
import java.math.BigInteger

class DocumentTest {

  def outputDocument(document: XWPFDocument): Unit = {
    val out = new FileOutputStream(new File("test.docx"))
    document.write(out)
    out.close()
  }

  def test() : Unit = {
    val ge: GraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment
    val is: InputStream = new FileInputStream("/home/paterake/.fonts/NotoSansGujarati-Regular.ttf")
    val font: Font = Font.createFont(Font.TRUETYPE_FONT, is)
    is.close()

    val document = new XWPFDocument()
    val paragraph = document.createParagraph()

    if (paragraph.getCTP().getPPr() == null) paragraph.getCTP().addNewPPr()
    if (paragraph.getCTP().getPPr().getShd() != null) paragraph.getCTP().getPPr().unsetShd()

    paragraph.getCTP.getPPr.addNewRPr()
    //paragraph.getCTP.getPPr.getRPr.addNewSz().setVal(BigInteger.valueOf(24))
    //paragraph.getCTP.getPPr.getRPr.addNewSzCs().setVal(BigInteger.valueOf(24))

    paragraph.setAlignment(ParagraphAlignment.CENTER)
    val run = paragraph.createRun()
    run.setText("આસ્તા")
    run.setFontSize(24)
    run.setFontFamily("Noto Sans Gujarati")

    outputDocument(document)
    println(run.getFontFamily)

  }

}

object DocumentTest extends App {
  val doc = new DocumentTest
  doc.test()
}