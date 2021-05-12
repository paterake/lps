package com.paterake.lps.address.builder

import com.itextpdf.kernel.events.{Event, IEventHandler, PdfDocumentEvent}
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.layout.Canvas
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.property.TextAlignment

class PdfFooter extends IEventHandler {

  def getParagraph(alignment: TextAlignment, fontSize: Int): Paragraph = {
    val paragraph = new Paragraph()
    paragraph.setTextAlignment(alignment)
    paragraph.setFontSize(fontSize)
    paragraph
  }

  def handleEvent(event: Event): Unit = {
    val docEvent = event.asInstanceOf[PdfDocumentEvent]
    val pdf = docEvent.getDocument
    val page = docEvent.getPage
    val pageSize = page.getPageSize
    val pdfCanvas = new PdfCanvas(page.getLastContentStream, page.getResources, pdf)
    val canvas = new Canvas(pdfCanvas, pdf, pageSize)
    val x = (pageSize.getLeft + pageSize.getRight) - 37
    val y = pageSize.getBottom + 15
    val p = getParagraph(TextAlignment.RIGHT, 10)
    p.add(String.valueOf(pdf.getPageNumber(page)))
    canvas.showTextAligned(p, x, y, TextAlignment.RIGHT)
  }
}
