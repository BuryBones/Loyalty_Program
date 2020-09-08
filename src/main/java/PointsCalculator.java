public class PointsCalculator {

    private float multiplier = 0.5f;

    public float getMultiplier() {
        return multiplier;
    }
    public void setMultiplier(float multiplier) {
        this.multiplier = multiplier;
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
