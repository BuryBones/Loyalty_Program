import org.apache.log4j.Logger;

import java.sql.*;
import java.util.HashMap;
import java.util.Random;

public class DBController {

    Logger logger = Logger.getLogger(DBController.class);

    private static DBController instance = new DBController();

    private DBController() {
    }

    public static DBController getInstance() {
        if (instance == null) {
            instance = new DBController();
        }
        return instance;
    }

    private boolean tryConnect() {
        logger.info("Connecting... User: " + user + "; Password: " + password);
        try {
            connection = DriverManager.getConnection(url, user, password);
            logger.info("Connection established!");
        } catch (SQLException sqlex) {
            logger.error("DB connection failed! " + sqlex.getMessage());
            System.out.println("CONNECTION FAILED");
            return false;
        }
        return true;
    }

    private Model model = Model.getInstance();

    private String user = "";
    private String password = "";
    private String url = "jdbc:postgresql://localhost:5432/testdb";
    private Connection connection;

    public boolean connect(String user, String password) {
        this.user = user;
        this.password = password;
        return tryConnect();
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public boolean findClient(String phone) {
        try {
            String query = "SELECT name, points FROM client WHERE phone = ?;";
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
            logger.error("FIND ERROR! Phone: " + phone + ";\n message: " + sqlEx.getMessage());
            UiController.getInstance().showError("Ошибка выполнения запроса к базе данных!",false);
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
            logger.info("NEW CLIENT Name: " + name + "; phone: " + phone);
            return true;
        } catch (SQLException sqlEx) {
            logger.info("CREATE ERROR! Phone: " + phone + "; name: " + name + "\n" + sqlEx.getMessage());
            UiController.getInstance().showError("Ошибка выполнения запроса к базе данных!",false);
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
            logger.info("Phone: " + phone + " ADDED: " + points + " points");

            recordPurchase(receipt,amount,points,phone);
            return true;
        } catch (SQLException sqlEx) {
            logger.error("ADDING FAILED! Phone: " + phone + " points: " + points + "\n" + sqlEx.getMessage());
            UiController.getInstance().showError("Ошибка выполнения запроса к базе данных!",false);
        }
        return false;
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
            logger.info("Phone: " + phone + " USED: " + points + " points");

            int negativePoints = ~points + 1;
            recordPurchase(receipt,amount,negativePoints,phone);
            return true;
        } catch (SQLException sqlEx) {
            logger.error("SUBTRACTING FAILED! Phone: " + phone + " points: " + points + "\n" + sqlEx.getMessage());
            UiController.getInstance().showError("Ошибка выполнения запроса к базе данных!",false);
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
                result = rs.getInt("result");
            }
            logger.info("TOTAL USED POINTS TODAY REPORT result = " + result);
        } catch (SQLException e) {
            logger.error("ERROR TOTAL USED TODAY\n" + e.getMessage());
            UiController.getInstance().showError("Ошибка выполнения запроса к базе данных!",false);
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
            String[][] result = new String[rsSize][4];

            int i = 0;
            while (rs.next()) {
                result[i][0] = rs.getString("name");
                result[i][1] = rs.getString("phone");
                result[i][2] = String.valueOf(rs.getInt("total_used"));
                result[i][3] = String.valueOf(rs.getInt("purchases"));
                i++;
            }
            logger.info("POINTS USED DETAILED REPORT " + result.length + " result rows");
            return result;
        } catch (SQLException e) {
            logger.error("ERROR DETAILED USED TODAY\n" + e.getMessage());
            UiController.getInstance().showError("Ошибка выполнения запроса к базе данных!",false);
        }
        return null;
    }
    public String[][] clientDetailed(String phone, Interval interval) {
        int id = findId(phone);
        if (id == -1) return null;
        try {
            String query =
                    "SELECT fiscal, amount, points_change, date_trunc('second', time_n_date) " +
                    "FROM purchase WHERE client_id = ? AND " +
                    "time_n_date > now() - CAST (? AS INTERVAL);";
            PreparedStatement ps = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
            ps.setInt(1, id);
            ps.setString(2, interval.getDbValue());

            ResultSet rs = ps.executeQuery();
            int rsSize = 0;
            if (rs != null) {
                rs.last();
                rsSize = rs.getRow();
                rs.beforeFirst();
            }
            String[][] result = new String[rsSize][4];
            int i = 0;
            while (rs.next()) {
                result[i][0] = rs.getString("fiscal");
                result[i][1] = String.valueOf(rs.getFloat("amount"));
                result[i][2] = String.valueOf(rs.getInt("points_change"));
                result[i][3] = rs.getTimestamp("date_trunc").toString();
                i++;
            }
            logger.info("CLIENT DETAILED REPORT id: " + id + " " + result.length + " result rows");
            return result;
        } catch (SQLException e) {
            logger.error(
                    "ERROR CLIENT DETAILED for phone: " + phone + " id: " + id + " interval: " + interval.toString() +
                    "\n" + e.getMessage());
            UiController.getInstance().showError("Ошибка выполнения запроса к базе данных!",false);
        }
        return null;
    }
    public String[][] getAggregateClientData(String phone, Interval interval) {
        int id = findId(phone);
        if (id == -1) return null;
        try {
            String query =
                    "SELECT " +
                    "COUNT(fiscal) AS total_purchases, " +
                    "SUM(amount) AS total_spent, " +
                    "SUM(points_change) FILTER (WHERE points_change > 0 ) AS points_gained, " +
                    "SUM(points_change) FILTER (WHERE points_change < 0 ) AS points_spent " +
                    "FROM purchase WHERE client_id = ? AND " +
                    "time_n_date > now() - CAST (? AS INTERVAL);";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1,id);
            ps.setString(2,interval.getDbValue());

            ResultSet rs = ps.executeQuery();
            String[][] result = new String [1][4];
            rs.next();
            result[0][0] = String.valueOf(rs.getInt("total_purchases"));
            result[0][1] = String.valueOf(rs.getDouble("total_spent"));
            result[0][2] = String.valueOf(rs.getInt("points_gained"));
            result[0][3] = String.valueOf(rs.getInt("points_spent"));

            logger.info("CLIENT DETAILED REPORT id: " + id + " " + result.length + " result rows");
            return result;
        } catch (SQLException e) {
            logger.error(
                    "ERROR CLIENT DETAILED AGGREGATE for phone: " + phone + " id: " + id + " interval: " + interval.toString() +
                            "\n" + e.getMessage());
            UiController.getInstance().showError("Ошибка выполнения запроса к базе данных!",false);
        }
        return null;
    }
    public Double getTotalExpendituresOrNull(String phone) {
        int id = findId(phone);
        if (id == -1) return null;
        try {
            String query =
                    "SELECT SUM(amount) " +
                    "FROM purchase WHERE client_id = ?;";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1,id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            Double result = rs.getDouble(1);
            logger.info("TOTAL EXPENDITURES REPORT for phone: " + phone + " id:" + id + " result = " + result);
            return result;
        } catch (SQLException e) {
            logger.error(
                    "ERROR TOTAL EXPENDITURES for phone: " + phone + " id: " + id + "\n" + e.getMessage());
            UiController.getInstance().showError("Ошибка выполнения запроса к базе данных!",false);
        }
        return null;
    }
    public Integer getTotalPointsCollectedOrNull(String phone) {
        int id = findId(phone);
        if (id == -1) return null;
        try {
            String query =
                    "SELECT SUM(points_change) FROM purchase " +
                    "WHERE client_id = ? AND points_change > 0;";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1,id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            Integer result = rs.getInt(1);
            logger.info("TOTAL POINTS COLLECTED REPORT for phone: " + phone + " id:" + id + " result = " + result);
            return result;
        } catch (SQLException e) {
            logger.error(
                    "ERROR TOTAL POINTS COLLECTED for phone: " + phone + " id: " + id + "\n" + e.getMessage());
            UiController.getInstance().showError("Ошибка выполнения запроса к базе данных!",false);
        }
        return null;
    }
    public Integer getTotalPointsUsedOrNull(String phone) {
        int id = findId(phone);
        if (id == -1) return null;
        try {
            String query =
                    "SELECT SUM(points_change) FROM purchase " +
                    "WHERE client_id = ? AND points_change < 0;";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1,id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            Integer result = Math.abs(rs.getInt(1));
            logger.info("TOTAL POINTS USED REPORT for phone: " + phone + " id:" + id + " result = " + result);
            return result;
        } catch (SQLException e) {
            logger.error(
                    "ERROR TOTAL POINTS USED for phone: " + phone + " id: " + id + "\n" + e.getMessage());
            UiController.getInstance().showError("Ошибка выполнения запроса к базе данных!",false);
        }
        return null;
    }
    public Double getAveragePurchaseOrNull(String phone) {
        int id = findId(phone);
        if (id == -1) return null;
        try {
            String query =
                    "SELECT AVG(amount) FROM purchase " +
                    "WHERE client_id = ?;";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1,id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            Double result = Math.abs(rs.getDouble(1));
            logger.info("AVG PURCHASE REPORT for phone: " + phone + " id:" + id + " result = " + result);
            return result;
        } catch (SQLException e) {
            logger.error(
                    "ERROR AVG PURCHASE for phone: " + phone + " id: " + id + "\n" + e.getMessage());
            UiController.getInstance().showError("Ошибка выполнения запроса к базе данных!",false);
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
        } catch (SQLException sqlEx) {
            logger.error("ID ERROR" + phone + "\n" + sqlEx.getMessage());
            UiController.getInstance().showError("Ошибка выполнения запроса к базе данных!",false);
        }
        logger.info("ID for phone: " + phone + " is " + id);
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
            logger.error("Failed to insert a purchase; phone: " + phone + "; id: " +
                    id + "; receipt: " + fiscalNumber + "\n" + sqlEx.getMessage());
            UiController.getInstance().showError("Ошибка выполнения запроса к базе данных!",false);
        }
    }

    // method for random generation
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
            UiController.getInstance().showError("Ошибка выполнения запроса к базе данных!",false);
        }
        return new HashMap<>();
    }
}
