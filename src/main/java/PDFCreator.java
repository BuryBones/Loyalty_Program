import com.itextpdf.text.*;
import com.itextpdf.text.Font;
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

    public boolean createDayReport() {
        // TODO: get data here, in case of getting null return false and probably and error message to ui
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
                        header.setBackgroundColor(BaseColor.GRAY);
                        header.setPhrase(new Phrase(columnTitle,font));
                        table.addCell(header);
                    });
            String[][] data = DBController.getInstance().pointsUsedDetailedToday();
            if (data == null) return false;
            for (int outer = 0; outer < data.length; outer++) {
                BaseColor color = outer%2 == 0 ? BaseColor.LIGHT_GRAY : BaseColor.WHITE;
                for (int inner = 0; inner < data[outer].length; inner++) {
                    PdfPCell cell = new PdfPCell(new Phrase(data[outer][inner],font));
                    cell.setBackgroundColor(color);
                    table.addCell(cell);
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
    public boolean createClientReport(DBController.Interval interval) {
        // TODO: move out?
        DBController controller = DBController.getInstance();
        Model model = Model.getInstance();
        String phone = model.getPhone();
        String name = model.getName();
        // get data from DB
        String[][] data = controller.clientDetailed(phone,interval);
        String[][] aggregateData = controller.getAggregateClientData(phone,interval);
        if (data == null) return false;
        try {
            Document doc = new Document();
            Date currentDate = new Date();
            SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy - HH-mm");
            PdfWriter.getInstance(doc, new FileOutputStream(
                    "Report " + interval.toString() + " " + phone + " " + name + " " + f.format(currentDate) + ".pdf"));
            String consolasFont = "src\\main\\resources\\fonts\\Consolas.ttf";
            Font font = FontFactory.getFont(consolasFont, "Cp1251", true);

            doc.open();
            Chunk newLine = new Chunk("\n");

            Paragraph headerParagraph = new Paragraph();
            Chunk pageHeader = new Chunk("Report for client " + name + " " + phone + " " + f.format(currentDate));
            pageHeader.setFont(font);
            headerParagraph.add(pageHeader);
            headerParagraph.add(newLine);

            Paragraph tableParagraph = new Paragraph();
            PdfPTable table = new PdfPTable(4);
            Stream.of("Номер чека","Сумма чека","Баллы","Дата").forEach(
                    columnTitle -> {
                        PdfPCell header = new PdfPCell();
                        header.setBackgroundColor(BaseColor.GRAY);
                        header.setPhrase(new Phrase(columnTitle,font));
                        table.addCell(header);
                    }
            );

            for (int outer = 0; outer < data.length; outer++) {
                BaseColor color = outer%2 == 0 ? BaseColor.LIGHT_GRAY : BaseColor.WHITE;
                for (int inner = 0; inner < data[outer].length; inner++) {
                    PdfPCell cell = new PdfPCell(new Phrase(data[outer][inner],font));
                    cell.setBackgroundColor(color);
                    table.addCell(cell);
                }
            }
            tableParagraph.add(newLine);
            tableParagraph.add(table);
            tableParagraph.add(newLine);

            Paragraph sumsParagraph = new Paragraph();
            PdfPTable sumsTable = new PdfPTable(4);
            Stream.of("Кол-во чеков","Сумма покупок","Начислено баллов","Списано баллов").forEach(
                    columnTitle -> {
                        PdfPCell header = new PdfPCell();
                        header.setBackgroundColor(BaseColor.GRAY);
                        header.setPhrase(new Phrase(columnTitle,font));
                        sumsTable.addCell(header);
                    }
            );
            for (int outer = 0; outer < aggregateData.length; outer++) {
                for (int inner = 0; inner < aggregateData[outer].length; inner++) {
                    sumsTable.addCell(new PdfPCell(new Phrase(aggregateData[outer][inner],font)));
                }
            }
            sumsParagraph.add(sumsTable);
            sumsParagraph.add(newLine);

            Paragraph statsParagraph = new Paragraph();
            Chunk totalExpenditures = new Chunk("Total spent: " + controller.getTotalExpendituresOrNull(phone), font);
            Chunk totalPointsCollected = new Chunk("Total points gained: " + controller.getTotalPointsCollectedOrNull(phone), font);
            Chunk totalPointsUsed = new Chunk("Total points used: " + controller.getTotalPointsUsedOrNull(phone), font);
            Chunk averagePurchase = new Chunk("Average sum of purchase: " + controller.getAveragePurchaseOrNull(phone), font);
            Chunk pointsLeft = new Chunk("Available Points: " + model.getPoints(), font);

            statsParagraph.add(totalExpenditures);
            statsParagraph.add(newLine);
            statsParagraph.add(totalPointsCollected);
            statsParagraph.add(newLine);
            statsParagraph.add(totalPointsUsed);
            statsParagraph.add(newLine);
            statsParagraph.add(averagePurchase);
            statsParagraph.add(newLine);
            statsParagraph.add(pointsLeft);
            statsParagraph.add(newLine);

            doc.add(headerParagraph);
            doc.add(tableParagraph);
            doc.add(sumsParagraph);
            doc.add(statsParagraph);
            doc.close();
        } catch (DocumentException de) {
            // TODO: Exceptions handling
            System.out.println("EXCEPTION!\n" + de.getMessage());
            return false;
        } catch (FileNotFoundException fnfe) {
            System.out.println("File not found!!\n" + fnfe.getMessage());
            return false;
        }
        return true;
    }
}
