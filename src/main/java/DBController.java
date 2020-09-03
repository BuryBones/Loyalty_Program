import org.apache.log4j.Logger;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class DBController {

    Logger logger = Logger.getLogger(DBController.class);

    private static DBController instance = new DBController();

    private DBController(){
        String[] loginInfo = UI.getInstance().login();
        setUser(loginInfo[1]);
        setPassword(loginInfo[2]);
        logger.info("Connecting... User: " + user + "; Password: " + password);
        // TODO: while cycle if login info is invalid
        try {
            connection = DriverManager.getConnection(url, user, password);
            logger.info("Connection established!");
        } catch (SQLException sqlex) {
            logger.error("DB connection failed! " + sqlex.getMessage());
            System.out.println("CONNECTION FAILED");
        }
    }

    public static DBController getInstance() {
        if (instance == null) {
            instance = new DBController();
        }
        return instance;
    }

    private Model model = Model.getInstance();

    private String user = "";
    private String password = "";
    private String url = "jdbc:postgresql://localhost:5432/testdb";
    private Connection connection;

    public void setUser(String user) {
        this.user = user;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    // maybe to use in reports
    public boolean findClient(String phone) {
        try {
            String query = "SELECT name, points FROM client WHERE phone = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, phone);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String name = resultSet.getString("name");
                int points = resultSet.getInt("points");
                model.setName(name);
                model.setPoints(points);
                logger.info("Selected name: " + name);
                logger.info("Points: " + points);
                return true;
            }
        } catch (SQLException sqlEx) {
            logger.error("Find client error! Phone: " + phone + ";\n message: " + sqlEx.getMessage());
        }
        return false;
    }
    public boolean createClient(String name, String phone) {
        try {
            String query = "INSERT into client (name,phone) VALUES (?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1,name);
            preparedStatement.setString(2,phone);
            preparedStatement.execute();
            logger.info("New client created. Name: " + name + "; phone: " + phone);
            return true;
        } catch (SQLException sqlEx) {
            logger.info("Failed to create client! Phone: " + phone + "; name: " + name + "\n" + sqlEx.getMessage());
        }
        return false;
    }
    public boolean addPoints(String phone, float amount, String receipt) {
        // getting the points from the given sum of purchase
        int points = PointsCalculator.getInstance().convertIntoPoints(amount);

        try {
            String query =
                    "UPDATE client SET " +
                    "points = points + ?, " +
                    "last_add_date = NOW(), " +
                    "total_spent = total_spent + ? " +
                    "WHERE phone = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, points);
            preparedStatement.setFloat(2,amount);
            preparedStatement.setString(3, phone);
            preparedStatement.execute();
            logger.info("Phone: " + phone + " points added: " + points);

            recordPurchase(receipt,amount,points,phone);
            return true;
        } catch (SQLException sqlEx) {
            logger.error("Phone: " + phone + " points: " + points + " ADDING FAILED!\n" + sqlEx.getMessage());
        }
        return false;
        // TODO: USE RETURNING, return not boolean but actual points!
    }
    public boolean subtractPoints (String phone, float amount, int points, String receipt) {
        if (points <= 0) return false;
        try {
            String query =
                    "UPDATE client SET " +
                    "points = points - ?, " +
                    "last_use_date = NOW(), " +
                    "total_spent = total_spent + ? - ?, " +
                    "total_used = total_used + ? " +
                    "WHERE phone = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, points);
            preparedStatement.setFloat(2,amount);
            preparedStatement.setInt(3,points);
            preparedStatement.setInt(4,points);
            preparedStatement.setString(5, phone);
            preparedStatement.execute();
            logger.info("Phone: " + phone + " points used: " + points);

            int negativePoints = ~points + 1;
            recordPurchase(receipt,amount,negativePoints,phone);
            return true;
        } catch (SQLException sqlEx) {
            logger.error("Phone: " + phone + " points: " + points + " SUBTRACTING FAILED!\n" + sqlEx.getMessage());
        }
        return false;
    }
    public void generateRandomPurchases() {
        HashMap<String, Integer> map = getPhonesAndAvailablePoints();
        Object[] clients = map.keySet().toArray();

        Random randomPurchases = new Random();
        int numberOfPurchases = 10 + randomPurchases.nextInt(11);

        Random randomSum = new Random();
        Random randomReceipt = new Random();
        Random randomPoints;

        float sum;
        int receipt;
        int pointsAvailable;

        for (int i = 0; i < numberOfPurchases; i++) {
            Random randomClient = new Random();
            int client = randomClient.nextInt(clients.length);
            String phone = (String) clients[client];
            sum = 50 + randomSum.nextFloat() * 450;
            receipt = randomReceipt.nextInt(1000);

            pointsAvailable = map.get(phone);
            boolean usePoints = pointsAvailable >= sum/3;
            if (usePoints) {
                randomPoints = new Random();
                int points = pointsAvailable == 0 ? 0 : randomPoints.nextInt(pointsAvailable);
                logger.info("Trying... Phone: " + phone + "; Points available: " + pointsAvailable + "; Points using: " + points);
                subtractPoints(phone,sum,points,String.valueOf(receipt));
            } else {
                addPoints(phone,sum,String.valueOf(receipt));
            }
        }
    }
    public int totalUsedPointsForToday() {
        int result = 0;
        try {
            String query = "SELECT SUM(points_change) AS result FROM purchase\n" +
                    "WHERE points_change < 0 AND time_n_date::timestamp::date = now()::date;";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result = (Integer) rs.getInt("result");
            }
        } catch (SQLException e) {
            // TODO: exception handling!
            System.out.println("EXCEPTION!\n" + e.getMessage());
        }
        return result;
    }
    public String[][] pointsUsedDetailedToday() {
        try {
            String query =
                    "SELECT name, phone, SUM(points_change) AS total_used, COUNT(purchase.id) AS purchases FROM client\n" +
                    "INNER JOIN purchase ON purchase.client_id = client.id\n" +
                    "WHERE points_change < 0 AND time_n_date::timestamp::date = now()::date\n" +
                    "GROUP BY client.id\n" +
                    "ORDER BY client.id;";
            PreparedStatement ps = connection.prepareStatement(query,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = ps.executeQuery();


            int rsSize = 0;
            if (rs != null) {
                rs.last();
                rsSize = rs.getRow();
                rs.beforeFirst();
            }
            logger.info("Query got " + rsSize + " rows.");
            String[][] result = new String[rsSize][4];

            int i = 0;
            while (rs.next()) {
                result[i][0] = rs.getString("name");
                result[i][1] = rs.getString("phone");
                result[i][2] = String.valueOf(rs.getInt("total_used"));
                result[i][3] = String.valueOf(rs.getInt("purchases"));
                i++;
            }
            return result;
        } catch (SQLException e) {
            // TODO: exception handling!
            System.out.println("EXCEPTION!");
            System.out.println(e.getMessage());
        }
        return null;
    }

    private int findId(String phone) {
        int id = -1;
        try {
            String query = "SELECT id FROM client WHERE phone = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1,phone);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                id = rs.getInt("id");
            }
            logger.info("ID for phone: " + phone + " is " + id);
        } catch (SQLException sqlEx) {
            // TODO: exception handling!
            logger.error("Failed to find ID of a client " + phone + "\n" + sqlEx.getMessage());
        }
        return id;
    }
    private void recordPurchase(String fiscalNumber, float amount, int points, String phone) {
        int id = findId(phone);
        try {
            String query = "INSERT INTO purchase (fiscal,amount,points_change,client_id) VALUES (?,?,?,?);";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1,fiscalNumber);
            ps.setFloat(2, amount);
            ps.setInt(3,points);
            ps.setInt(4,id);
            ps.execute();
            logger.info("Receipt " + fiscalNumber + "; sum: " + amount + "; recorded.");
        } catch (SQLException sqlEx) {
            // TODO: exception handling!
            logger.error("Failed to insert a purchase; phone: " + phone + "; id: " +
                    id + "; receipt: " + fiscalNumber + "\n" + sqlEx.getMessage());
        }
    }

    // methods for random generation
    private HashMap<String,Integer> getPhonesAndAvailablePoints() {
        try {
            String query = "SELECT phone, points FROM client;";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();
            HashMap<String, Integer> result = new HashMap<>();
            while (rs.next()) {
                result.put(rs.getString("phone"),rs.getInt("points"));
            }
            return result;
        } catch (SQLException e) {
            // TODO: exception handling!
            System.out.println("EXCEPTION!\n" + e.getMessage());
        }
        return new HashMap<>();
    }
}
