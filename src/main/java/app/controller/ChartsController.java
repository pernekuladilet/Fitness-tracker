package app.controller;

import app.model.ExerciseCategory;
import app.model.TypeStat;
import app.model.Workout;
import app.util.DBUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ChartsController {

    @FXML private TabPane chartsTabs;

    @FXML private LineChart<String, Number> caloriesChart;
    @FXML private LineChart<String, Number> minutesChart;
    @FXML private PieChart typePie;

    // NEW UI
    @FXML private ComboBox<ExerciseCategory> categoryBox;
    @FXML private Label infoLabel;

    private int userId;
    private LocalDate from;
    private LocalDate to;

    // keeps workouts already loaded for the user/date range
    private List<Workout> cached = new ArrayList<>();

    public void init(int userId, LocalDate from, LocalDate to) {
        this.userId = userId;
        this.from = from;
        this.to = to;

        setupCategoryBox();
        reloadDataAndRender();
    }

    private void setupCategoryBox() {
        if (categoryBox == null) return;

        // "All categories" as null-like item
        ExerciseCategory all = new ExerciseCategory(-1, "All categories"); // ✅
        all.setId(-1);
        all.setName("All categories");

        List<ExerciseCategory> cats = new ArrayList<>();
        cats.add(all);
        cats.addAll(DBUtil.getAllCategories());

        categoryBox.setItems(FXCollections.observableArrayList(cats));
        categoryBox.getSelectionModel().selectFirst();

        // display text
        categoryBox.setCellFactory(cb -> new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(ExerciseCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        categoryBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(ExerciseCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
    }

    @FXML
    private void onApplyClick() {
        // just re-render with selected category, without reloading dates/users
        render();
    }

    private void reloadDataAndRender() {
        cached = DBUtil.getWorkoutsByUserId(userId, from, to);
        render();
    }

    private void render() {
        Integer categoryId = null;
        if (categoryBox != null && categoryBox.getValue() != null && categoryBox.getValue().getId() != -1) {
            categoryId = categoryBox.getValue().getId();
        }

        List<Workout> data = cached;
        if (categoryId != null) {
            int cid = categoryId;
            data = cached.stream()
                    .filter(w -> w.getCategoryId() != null && w.getCategoryId() == cid)
                    .collect(Collectors.toList());
        }

        if (infoLabel != null) {
            infoLabel.setText("Records: " + data.size());
        }

        buildLine(caloriesChart, data, true);
        buildLine(minutesChart, data, false);
        buildPie(typePie, data);
    }

    private void buildLine(LineChart<String, Number> chart, List<Workout> data, boolean calories) {
        if (chart == null) return;

        chart.getData().clear();

        // sum by date
        Map<LocalDate, Integer> sum = new TreeMap<>();
        for (Workout w : data) {
            if (w.getDate() == null) continue;
            int v = calories ? w.getCalories() : w.getDurationMinutes();
            sum.put(w.getDate(), sum.getOrDefault(w.getDate(), 0) + v);
        }

        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName(calories ? "Calories" : "Minutes");

        for (Map.Entry<LocalDate, Integer> e : sum.entrySet()) {
            s.getData().add(new XYChart.Data<>(e.getKey().toString(), e.getValue()));
        }

        chart.getData().add(s);
    }

    private void buildPie(PieChart pie, List<Workout> data) {
        if (pie == null) return;

        Map<String, Integer> count = new HashMap<>();
        for (Workout w : data) {
            String t = (w.getType() == null || w.getType().isBlank()) ? "Unknown" : w.getType().trim();
            count.put(t, count.getOrDefault(t, 0) + 1);
        }

        var items = FXCollections.<PieChart.Data>observableArrayList();
        count.entrySet().stream()
                .sorted((a,b) -> Integer.compare(b.getValue(), a.getValue()))
                .forEach(e -> items.add(new PieChart.Data(e.getKey(), e.getValue())));

        pie.setData(items);
    }

    @FXML
    private void onCloseClick() {
        Stage st = (Stage) typePie.getScene().getWindow();
        st.close();
    }
}
