import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.UnitValue;
import com.paterake.lps.address.parse.Location;

import java.io.File;

public class GujaratiExample {
    public static final String DEST = "/home/paterake/Downloads/gujarati_example.pdf";

    public static final String FONT = Location.font_gujarati_location();

    public static void main(String[] args) throws Exception {
        File file = new File(DEST);
        file.getParentFile().mkdirs();

        new GujaratiExample().manipulatePdf(DEST);
    }

    protected void manipulatePdf(String dest) throws Exception {
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(dest));
        Document doc = new Document(pdfDoc);
        PdfFont f = PdfFontFactory.createFont(FONT, PdfEncodings.IDENTITY_H);

        Text t1 = new Text("Rakesh");
        Text t2 = new Text("Patel");
        Text t3 = new Text("Asta");

        Text gt1 = new Text(" (રાકેશ)").setFont(f);
        Text gt2 = new Text(" (પટેલ)").setFont(f);
        Text gt3 = new Text(" (આસ્તા)").setFont(f);

        Paragraph p1 = new Paragraph();
        p1.add(t1);
        p1.add(gt1);
        doc.add(p1);

        Paragraph p2 = new Paragraph();
        p2.add(t2);
        p2.add(gt2);
        doc.add(p2);

        Paragraph p3 = new Paragraph();
        p3.add(t3);
        p3.add(gt3);
        doc.add(p3);

        Table table = new Table(UnitValue.createPercentArray(new float[] {10, 60, 30})).useAllAvailableWidth();
        Cell customerLblCell = new Cell().add(new Paragraph("CUSTOMERS"));
        table.addCell(customerLblCell);

        // "कारपार्किंग"
        p2 = new Paragraph("ગ્રાહકો")
                .setFont(f)
                .setFontColor(new DeviceRgb(50, 205, 50));
        Cell balanceLblCell = new Cell().add(p2);
        table.addCell(balanceLblCell);

        table.setMarginTop(10);
        doc.add(table);

        doc.close();
    }
}