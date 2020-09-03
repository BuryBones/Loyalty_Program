import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Model {

    Logger logger = Logger.getLogger(Model.class);

    private static Model instance = new Model();

    private Model() {

    }

    public static Model getInstance() {
        if (instance == null) {
            instance = new Model();
        }
        return instance;
    }

    private String name = "";
    private int points = 0;
    private String phone = "";
    private float sumOfPurchaseAdding = 0;
    private float sumOfPurchaseUsing = 0;
    private String receiptAdding = "";
    private String receiptUsing = "";
    private int pointsUsing = 0;

    public String getName() {
        return name;
    }
    public int getPoints() {
        return points;
    }
    public String getPhone() {
        return phone;
    }
    public float getSumOfPurchaseAdding() {
        return sumOfPurchaseAdding;
    }
    public float getSumOfPurchaseUsing() {
        return sumOfPurchaseUsing;
    }
    public String getReceiptAdding() {
        return receiptAdding;
    }
    public String getReceiptUsing() {
        return receiptUsing;
    }
    public int getPointsUsing() {
        return pointsUsing;
    }

    public boolean setName(String name) {
        if (name.length() <= 30) {
            this.name = name;
            return true;
        }
        return false;
    }
    public void setPoints(int points) {
        this.points = points;
    }
    public boolean setPhone(String phone) {
        // accepting only numbers with no symbols, 11 to 13
        Pattern pattern = Pattern.compile("(\\d){11,13}");
        Matcher matcher = pattern.matcher(phone);
        boolean result = matcher.matches();
        logger.info("phone number validity check: '" + phone + "' -" + result);
        if (result) {
            this.phone = phone;
            logger.info("Phone set: " + phone);
        }
        return result;
    }
    public boolean setSumOfPurchaseAdding(String strSum) {
        try {
            sumOfPurchaseAdding = Float.parseFloat(strSum.trim());
            return true;
        } catch (NumberFormatException nfe) {
            logger.info("Wrong input. " + strSum + " cannot be converted into float.\n" + nfe.getMessage());
        }
        return false;
    }
    public boolean setSumOfPurchaseUsing(String strSum) {
        try {
            sumOfPurchaseUsing = Float.parseFloat(strSum.trim());
            return true;
        } catch (NumberFormatException nfe) {
            logger.info("Wrong input. " + strSum + " cannot be converted into float.\n" + nfe.getMessage());
        }
        return false;
    }
    public boolean setReceiptAdding(String receiptAdding) {
        if (receiptAdding.trim().length() <= 10) {
            this.receiptAdding = receiptAdding.trim();
            return true;
        }
        return false;
    }
    public boolean setReceiptUsing(String receiptUsing) {
        if (receiptUsing.trim().length() <= 10) {
            this.receiptUsing = receiptUsing.trim();
            return true;
        }
        return false;
    }
    public boolean setPointsUsing(String pointsStr) {
        try {
            pointsUsing = Integer.parseInt(pointsStr.trim());
            return true;
        } catch (NumberFormatException nfe) {
            logger.info("Wrong input. " + pointsStr + " cannot be converted into integer.\n" + nfe.getMessage());
        }
        return false;
    }

    public void resetName() {
        name = "";
    }
    public void resetPoints() {
        points = 0;
    }
    public void resetPhone() {
        phone = "";
    }
    public void resetSumOfPurchaseAdding() {
        sumOfPurchaseAdding = 0;
    }
    public void resetSumOfPurchaseUsing() {
        sumOfPurchaseUsing = 0;
    }
    public void resetReceiptUsing() {
        receiptUsing = "";
    }
    public void resetReceiptAdding() {
        receiptAdding = "";
    }
    public void resetPointsUsing() {
        pointsUsing = 0;
    }

}
