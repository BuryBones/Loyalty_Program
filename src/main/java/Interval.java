public enum Interval {

    ONE_DAY("One Day","1 day"),
    ONE_WEEK("One Week","1 week"),
    ONE_MONTH("One Month","1 month"),
    ONE_YEAR("One Year","1 year");

    private String desc;
    private String dbValue;
    private static Interval[] values = values();

    Interval(String desc,String dbValue) {
        this.desc = desc;
        this.dbValue = dbValue;
    }

    public static Interval[] getValues() {
        return values;
    }

    public String toString() {
        return desc;
    }

    public String getDbValue() {
        return dbValue;
    }
}
