import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.FileInputStream;

public class PdfBoxExample {

    public static void main(String[] args) throws Exception {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage( page );

        PDFont font = PDType1Font.HELVETICA_BOLD;

        String font_gujarati_location = "/home/paterake/Documents/__cfg/fonts/noto_gujarati/NotoSansGujarati-Regular.ttf";


        //PDFont fontGujarati = PDTrueTypeFont.loadTTF(document, new File(font_gujarati_location));

        PDFont fontGujarati = PDType0Font.load(document, new FileInputStream(font_gujarati_location), false);


        // Start a new content stream which will "hold" the to be created content
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        // Define a text content stream using the selected font, moving the cursor and drawing the text "Hello World"
        contentStream.beginText();
        contentStream.setFont( fontGujarati, 12 );
        contentStream.moveTextPositionByAmount( 100, 700 );
        contentStream.drawString( "(આસ્તા)" );
        contentStream.endText();

        // Make sure that the content stream is closed:
        contentStream.close();

        // Save the results and ensure that the document is properly closed:
        document.save( "Hello World.pdf");
        document.close();

    }


}
