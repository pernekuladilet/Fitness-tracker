package app.model;

import java.time.LocalDate;

public class Workout {
    private int id;
    private int userId;
    private LocalDate date;
    private String type;
    private int durationMinutes;
    private int calories;
    private String notes;

    private Integer categoryId;     // NEW
    private String categoryName;    // NEW

    public Workout(int id, int userId, LocalDate date, String type,
                   int durationMinutes, int calories, String notes,
                   Integer categoryId, String categoryName) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.type = type;
        this.durationMinutes = durationMinutes;
        this.calories = calories;
        this.notes = notes;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    // (если где-то используется старый конструктор — оставь и его)
    public Workout(int id, int userId, LocalDate date, String type,
                   int durationMinutes, int calories, String notes) {
        this(id, userId, date, type, durationMinutes, calories, notes, null, null);
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public LocalDate getDate() { return date; }
    public String getType() { return type; }
    public int getDurationMinutes() { return durationMinutes; }
    public int getCalories() { return calories; }
    public String getNotes() { return notes; }

    public Integer getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName == null ? "" : categoryName; }
}
