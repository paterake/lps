package com.paterake.lps.address.parse

import com.itextpdf.kernel.events.{Event, IEventHandler, PdfDocumentEvent}
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfPage
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.layout.Canvas
import com.itextpdf.layout.property.TextAlignment

class PdfFooter extends IEventHandler {
  def handleEvent(event: Event): Unit = {
    val docEvent = event.asInstanceOf[PdfDocumentEvent]
    val pdf = docEvent.getDocument
    val page = docEvent.getPage
    val pageSize = page.getPageSize
    val pdfCanvas = new PdfCanvas(page.getLastContentStream, page.getResources, pdf)
    val canvas = new Canvas(pdfCanvas, pdf, pageSize)
    val x = (pageSize.getLeft + pageSize.getRight) / 2
    val y = pageSize.getBottom + 15
    canvas.showTextAligned(String.valueOf(pdf.getPageNumber(page)), x, y, TextAlignment.CENTER)
  }
}
