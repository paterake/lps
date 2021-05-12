package com.paterake.lps.address.builder

import com.paterake.lps.address.parse.Location

import java.awt.image.BufferedImage
import java.awt.{Color, Font, RenderingHints}
import java.io.File
import javax.imageio.ImageIO

class TextToImageParser {

  def textToGraphic(text: String, fontSize: Int): Unit = {
    val font = new Font("Helvetica", Font.PLAIN, fontSize)

    val img0 = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
    val g2d0 = img0.createGraphics()
    g2d0.setFont(font)
    val fm0 = g2d0.getFontMetrics()
    val width = fm0.stringWidth(text)
    val height = fm0.getHeight()
    g2d0.dispose()

    val img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val g2d = img.createGraphics()
    g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
    g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
    g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    g2d.setFont(font);
    val fm = g2d.getFontMetrics();
    g2d.setColor(Color.WHITE);
    g2d.drawString(text, 0, fm.getAscent());
    g2d.dispose()
    try {
      ImageIO.write(img, "png", new File(Location.tmpImageLocation));
    } catch {
      case e: Exception => {
        e.printStackTrace()
      }
    }

    /*
    val raster = img.getRaster()
    val buffer = raster.getDataBuffer().asInstanceOf[DataBufferByte]
    buffer.getData
    */

  }

}

object TextToImageParser extends App {
  val converter = new TextToImageParser
  converter.textToGraphic("આસ્તા", 12)
}