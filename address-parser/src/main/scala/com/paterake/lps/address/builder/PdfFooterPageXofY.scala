package com.paterake.lps.address.builder

import com.itextpdf.kernel.events.{Event, IEventHandler, PdfDocumentEvent}
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject
import com.itextpdf.layout.Canvas
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.property.TextAlignment

class PdfFooterPageXofY(pdf: PdfDocument) extends IEventHandler {

  protected var side = 20
  protected var x = 300
  protected var y = 25
  protected var space = 4.5f
  protected var descent = 3
  protected var placeholder: PdfFormXObject = new PdfFormXObject(new Rectangle(0, 0, side, side))

  def handleEvent(event: Event): Unit = {
    val docEvent = event.asInstanceOf[PdfDocumentEvent]
    val pdf = docEvent.getDocument
    val page = docEvent.getPage
    val pageNumber = pdf.getPageNumber(page)
    val pageSize = page.getPageSize
    val pdfCanvas = new PdfCanvas(page.getLastContentStream, page.getResources, pdf)
    val canvas = new Canvas(pdfCanvas, pageSize)
    val p = new Paragraph()
      .add("Page ").add(String.valueOf(pageNumber)).add(" of")
    canvas.showTextAligned(p, x, y, TextAlignment.RIGHT)
    pdfCanvas.addXObjectAt(placeholder, x + space, y - descent)
    pdfCanvas.release()
  }

  def writeTotal(pdf: PdfDocument): Unit = {
    val canvas = new Canvas(placeholder, pdf)
    canvas.showTextAligned(String.valueOf(pdf.getNumberOfPages), 0, descent, TextAlignment.LEFT)
  }
}
