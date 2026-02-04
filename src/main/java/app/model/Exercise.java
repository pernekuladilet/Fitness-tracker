package app.model;

public class Exercise {
    private int id;
    private Integer categoryId; // может быть null
    private String categoryName; // удобно для таблицы/ComboBox
    private String name;
    private String description;

    public Exercise(int id, Integer categoryId, String categoryName, String name, String description) {
        this.id = id;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.name = name;
        this.description = description;
    }

    public Exercise(Integer categoryId, String name, String description) {
        this(0, categoryId, null, name, description);
    }

    public int getId() { return id; }
    public Integer getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getName() { return name; }
    public String getDescription() { return description; }

    public void setId(int id) { this.id = id; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return name;
    }
}
