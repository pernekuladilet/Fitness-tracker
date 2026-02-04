package app.model;

public class WorkoutResult {
    private int id;
    private int workoutId;
    private int exerciseId;

    private String exerciseName;     // для отображения
    private String categoryName;     // для отображения

    private int sets;
    private int reps;
    private double weightKg;
    private int durationMin;
    private double distanceKm;
    private String notes;

    public WorkoutResult(int id, int workoutId, int exerciseId,
                         String exerciseName, String categoryName,
                         int sets, int reps, double weightKg,
                         int durationMin, double distanceKm, String notes) {
        this.id = id;
        this.workoutId = workoutId;
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
        this.categoryName = categoryName;
        this.sets = sets;
        this.reps = reps;
        this.weightKg = weightKg;
        this.durationMin = durationMin;
        this.distanceKm = distanceKm;
        this.notes = notes;
    }

    public int getId() { return id; }
    public int getWorkoutId() { return workoutId; }
    public int getExerciseId() { return exerciseId; }
    public String getExerciseName() { return exerciseName; }
    public String getCategoryName() { return categoryName; }
    public int getSets() { return sets; }
    public int getReps() { return reps; }
    public double getWeightKg() { return weightKg; }
    public int getDurationMin() { return durationMin; }
    public double getDistanceKm() { return distanceKm; }
    public String getNotes() { return notes; }

    public void setSets(int sets) { this.sets = sets; }
    public void setReps(int reps) { this.reps = reps; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }
    public void setDurationMin(int durationMin) { this.durationMin = durationMin; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    public void setNotes(String notes) { this.notes = notes; }
}
