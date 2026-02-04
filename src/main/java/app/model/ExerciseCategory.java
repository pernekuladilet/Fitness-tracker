package app.model;

public class ExerciseCategory {
    private int id;
    private String name;

    public ExerciseCategory(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return name; // удобно для ComboBox
    }
}
