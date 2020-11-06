import java.util.*;

public class DayReportRow {

    // TODO: make a method to reset a map??

    private static TreeMap<Integer,DayReportRow> map = new TreeMap<>();
    // ("Имя","Номер телефона","Баллов списано","Чеков за день")
    private final String name;
    private final String phone;
    private int pointsUsed;
    private int purchases;

    private DayReportRow(String name, String phone, int pointsUsed, int purchases) {
        this.name = name;
        this.phone = phone;
        this.pointsUsed = pointsUsed;
        this.purchases = purchases;
    }

    public static void startNewReport() {
        map = new TreeMap<>();
    }

    public static void addNewRow(Purchase p) {
        // grouping by clients' IDs
        Client client = p.getClient();
        Integer id = client.getId();
        if (map.containsKey(id)) {
            map.get(id).purchases++;
            map.get(id).pointsUsed += p.getPointsChange();
        } else {
            DayReportRow row = new DayReportRow(client.getName(),client.getPhone(),p.getPointsChange(),1);
            map.put(id,row);
        }
    }
    public static Collection<DayReportRow> getRows() {
        return map.values();
    }
    public String toString() {
        return String.format("%s | %s | %d | %d",name,phone,pointsUsed,purchases);
    }
}
