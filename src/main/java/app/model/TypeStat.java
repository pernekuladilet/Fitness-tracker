package app.model;

public class TypeStat {

    private final String type;
    private final int count;
    private final int minutes;
    private final int calories;

    public TypeStat(String type, int count, int minutes, int calories) {
        this.type = type;
        this.count = count;
        this.minutes = minutes;
        this.calories = calories;
    }

    public String getType() {
        return type;
    }

    public int getCount() {
        return count;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getCalories() {
        return calories;
    }
}
