import org.apache.log4j.Logger;

import javax.swing.*;
import java.sql.Timestamp;
import java.util.Date;
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

    // the following fields hold the values shown to user

    // Client object
    private Client currentClient;
    // client name
    private String name = "";
    // client's points available
    private int points = 0;
    // a phone number searched
    private String phone = "";
    // a sum of purchase if adding points
    private float sumOfPurchaseAdding = 0;
    // a sum of purchase if using points
    private float sumOfPurchaseUsing = 0;
    // a fiscal receipt number, if adding, points, string
    private String receiptAdding = "";
    // a fiscal receipt number, if using points, string
    private String receiptUsing = "";
    // how many points trying to use
    private int pointsUsing = 0;

    public void setClient(Client client) {
        currentClient = client;
        update();
    }
    public Client getCurrentClient() {
        return currentClient;
    }
    public void findClient() {

    }
    public void createClient() {
        currentClient = new Client(phone,name);
        ClientController.getInstance().createClient();
    }
    public void updateClient(boolean add) {
        Purchase purchase;
        if (add) {
            int pointsChange = PointsCalculator.getInstance().convertIntoPoints(getSumOfPurchaseAdding());
            currentClient.setPoints(getPoints() + pointsChange);
            currentClient.setLastAddDate(new Timestamp(System.currentTimeMillis()));
            currentClient.increaseTotalSpent(getSumOfPurchaseAdding());
            purchase = new Purchase(getReceiptAdding(),getSumOfPurchaseAdding(),pointsChange,currentClient);

        } else {
            int usingPoints = getUsingPoints();
            currentClient.setPoints(getPoints() - usingPoints);
            currentClient.setLastUseDate(new Timestamp(System.currentTimeMillis()));
            // Total spent increases by amount payed by money
            currentClient.increaseTotalSpent(getSumOfPurchaseUsing()-usingPoints);
            currentClient.increaseTotalUsed(usingPoints);
            int pointsChange = -usingPoints;
            purchase = new Purchase(getReceiptUsing(),getSumOfPurchaseUsing(),pointsChange,currentClient);
        }
        // TODO: save purchase
        PurchaseController.getInstance().savePurchase(purchase);
        System.out.println(purchase);

        ClientController.getInstance().updateClient();
    }
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
    public int getUsingPoints() {
        int available = getPoints();
        int wanted = pointsUsing;
        float purchaseSum = getSumOfPurchaseUsing();
        if (purchaseSum < wanted) {
            logger.info("Sum of purchase is smaller than using points! Sum: " + purchaseSum + "; points: " + wanted );
            UiController.getInstance().showError("Вы хотите списать баллов больше, чем стоимость покупки!",false);
            return 0;
        }
        if (available < wanted) {
            logger.info("Insufficient points! Available: " + available + "; wanted: " + wanted);
            UiController.getInstance().showError("Недостаточно баллов!",false);
            return 0;
        } else {
            return wanted;
        }
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

    private void update() {
        if (currentClient != null) {
            setName(currentClient.getName());
            setPoints(currentClient.getPoints());
        }
    }
}
