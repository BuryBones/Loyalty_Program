public class PointsCalculator {

    private static float multiplier = 0.5f;

    public static float getMultiplier() {
        return multiplier;
    }
    public static void setMultiplier(float multiplier) {
        PointsCalculator.multiplier = multiplier;
    }

    private static PointsCalculator instance = new PointsCalculator();

    public static PointsCalculator getInstance() {
        if (instance == null) {
            instance = new PointsCalculator();
        }
        return instance;
    }

    private PointsCalculator() {}

    public int convertIntoPoints(float amount) {
        int result = (int) (amount*multiplier);
        return result;
    }
}
