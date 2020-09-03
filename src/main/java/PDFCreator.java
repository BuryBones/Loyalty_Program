import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Stream;

public class PDFCreator {

    private static PDFCreator instance = new PDFCreator();

    private PDFCreator() {}

    public static PDFCreator getInstance() {
        if (instance == null) {
            instance = new PDFCreator();
        }
        return instance;
    }

    // TODO: use savePath!
    private String savePath = "";

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public boolean createDayReport() {
        try {
            Document doc = new Document();
            Date currentDate = new Date();
            SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy - HH-mm");
            PdfWriter.getInstance(doc, new FileOutputStream("Report of the day " + f.format(currentDate) + ".pdf"));

            doc.open();
            String consolasFont = "src\\main\\resources\\fonts\\Consolas.ttf";
            Font font = FontFactory.getFont(consolasFont, "Cp1251", true);
            Chunk pageHeader = new Chunk("Day Report " + f.format(currentDate));
            Chunk newLine = new Chunk("\n");
            PdfPTable table = new PdfPTable(4);
            Stream.of("Имя","Номер телефона","Баллов списано","Чеков за день").
                    forEach(columnTitle -> {
                        PdfPCell header = new PdfPCell();
                        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        header.setPhrase(new Phrase(columnTitle,font));
                        table.addCell(header);
                    });
            String[][] data = DBController.getInstance().pointsUsedDetailedToday();
            if (data == null) return false;
            for (int outer = 0; outer < data.length; outer++) {
                for (int inner = 0; inner < data[outer].length; inner++) {
                    table.addCell(new Phrase(data[outer][inner],font));
                }
            }
            Chunk pointsUsedTotal = new Chunk("Today were used " + DBController.getInstance().totalUsedPointsForToday() + " points.");

            Paragraph p1 = new Paragraph();
            p1.add(pageHeader);
            p1.add(newLine);
            p1.add(table);
            p1.add(newLine);
            p1.add(pointsUsedTotal);

            doc.add(p1);
            doc.close();
            // TODO: exception handling
        } catch (DocumentException de) {
            System.out.println("EXCEPTION!\n" + de.getMessage());
            return false;
        } catch (FileNotFoundException fnfe) {
            System.out.println("File not found!!\n" + fnfe.getMessage());
            return false;
        }
        return true;
    }
    public void createClientReport() {

    }
}
