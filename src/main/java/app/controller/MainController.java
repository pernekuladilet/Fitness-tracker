package app.controller;

import app.model.ExerciseCategory;
import app.model.Session;
import app.model.TypeStat;
import app.model.User;
import app.model.Workout;
import app.util.DBUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public class MainController {

    // ===== TABS =====
    @FXML private TabPane mainTabs;
    @FXML private Tab usersTab;
    @FXML private Tab categoriesTab;

    // ===== USERS (Dashboard tab picker) =====
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUsername;

    // ===== USERS (Admin tab) =====
    @FXML private TableView<User> adminUsersTable;
    @FXML private TableColumn<User, Integer> adminColId;
    @FXML private TableColumn<User, String> adminColUsername;
    @FXML private TableColumn<User, String> adminColRole;

    // ===== CATEGORIES =====
    @FXML private TableView<ExerciseCategory> categoryTable;
    @FXML private TableColumn<ExerciseCategory, Integer> catColId;
    @FXML private TableColumn<ExerciseCategory, String> catColName;

    // ===== PERIOD FILTER =====
    @FXML private ComboBox<String> periodBox;
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;

    // ===== WORKOUTS =====
    @FXML private TableView<Workout> workoutTable;
    @FXML private TableColumn<Workout, LocalDate> wColDate;
    @FXML private TableColumn<Workout, String> wColCategory;
    @FXML private TableColumn<Workout, String> wColType;
    @FXML private TableColumn<Workout, Integer> wColDuration;
    @FXML private TableColumn<Workout, Integer> wColCalories;
    @FXML private TableColumn<Workout, String> wColNotes;

    // ===== ADMIN BUTTONS (Users tab) =====
    @FXML private Button usersBtn;
    @FXML private Button addUserBtn;
    @FXML private Button deleteUserBtn;

    // ===== DASHBOARD =====
    @FXML private Label dashWorkouts;
    @FXML private Label dashMinutes;
    @FXML private Label dashCalories;
    @FXML private Label dashLastDate;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {

        // Dashboard users picker columns
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colUsername != null) colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));

        // Admin users table columns
        if (adminColId != null) adminColId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (adminColUsername != null) adminColUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        if (adminColRole != null) adminColRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Categories
        if (catColId != null) catColId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (catColName != null) catColName.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Workouts
        if (wColDate != null) wColDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        if (wColCategory != null) wColCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        if (wColType != null) wColType.setCellValueFactory(new PropertyValueFactory<>("type"));
        if (wColDuration != null) wColDuration.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));
        if (wColCalories != null) wColCalories.setCellValueFactory(new PropertyValueFactory<>("calories"));
        if (wColNotes != null) wColNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));

        // Notes wrap
        if (wColNotes != null) {
            wColNotes.setCellFactory(col -> new TableCell<Workout, String>() {
                private final Label lbl = new Label();
                {
                    lbl.setWrapText(true);
                    lbl.setMaxWidth(420);
                }
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) setGraphic(null);
                    else {
                        lbl.setText(item == null ? "" : item);
                        setGraphic(lbl);
                    }
                }
            });
        }

        // PeriodBox
        if (periodBox != null) {
            periodBox.setItems(FXCollections.observableArrayList(
                    "All time", "Last 7 days", "Last 30 days", "This month"
            ));
            periodBox.getSelectionModel().select("All time");
            periodBox.setOnAction(e -> {
                if (fromDate != null) fromDate.setValue(null);
                if (toDate != null) toDate.setValue(null);
                onRefreshClick();
            });
        }

        if (fromDate != null) fromDate.setOnAction(e -> onRefreshClick());
        if (toDate != null) toDate.setOnAction(e -> onRefreshClick());

        // Roles + tabs
        applyRoleUI();

        // Load base data
        onUsersClick();
        onLoadCategoriesClick();

        // Important: select user so workouts can load
        ensureUserSelected();

        // refresh when pick user (admin)
        if (userTable != null) {
            userTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> onRefreshClick());
        }

        onRefreshClick();
        setStatus("Ready");
    }

    // ===== ROLE UI =====
    private void applyRoleUI() {
        if (Session.currentUser == null) return;
        boolean admin = Session.currentUser.isAdmin();

        // admin buttons only for admin
        if (usersBtn != null) { usersBtn.setVisible(admin); usersBtn.setManaged(admin); }
        if (addUserBtn != null) { addUserBtn.setVisible(admin); addUserBtn.setManaged(admin); }
        if (deleteUserBtn != null) { deleteUserBtn.setVisible(admin); deleteUserBtn.setManaged(admin); }

        // dashboard user picker is only meaningful for admin
        if (userTable != null) { userTable.setVisible(admin); userTable.setManaged(admin); }

        // Tabs: Tab has no setVisible -> remove for USER
        if (!admin && mainTabs != null) {
            if (usersTab != null) mainTabs.getTabs().remove(usersTab);
            if (categoriesTab != null) mainTabs.getTabs().remove(categoriesTab);
        }
    }

    private void ensureUserSelected() {
        if (Session.currentUser == null) return;

        if (!Session.currentUser.isAdmin()) {
            // USER: always himself
            if (userTable != null) {
                userTable.setItems(FXCollections.observableArrayList(Session.currentUser));
                userTable.getSelectionModel().selectFirst();
            }
            return;
        }

        // ADMIN: select first if none
        if (userTable != null && !userTable.getItems().isEmpty()
                && userTable.getSelectionModel().getSelectedItem() == null) {
            userTable.getSelectionModel().selectFirst();
        }
    }

    private User getSelectedUserForWorkouts() {
        if (Session.currentUser == null) return null;

        if (Session.currentUser.isAdmin()) {
            return (userTable == null) ? null : userTable.getSelectionModel().getSelectedItem();
        }
        return Session.currentUser;
    }

    // ================= USERS =================

    @FXML
    private void onUsersClick() {
        List<User> users = DBUtil.getAllUsers();

        if (Session.currentUser != null && !Session.currentUser.isAdmin()) {
            if (userTable != null) {
                userTable.setItems(FXCollections.observableArrayList(Session.currentUser));
                userTable.getSelectionModel().selectFirst();
            }
        } else {
            if (userTable != null) userTable.setItems(FXCollections.observableArrayList(users));
        }

        if (adminUsersTable != null) adminUsersTable.setItems(FXCollections.observableArrayList(users));

        ensureUserSelected();
        setStatus("Users loaded: " + users.size());
    }

    @FXML
    private void onAddUserClick() {
        if (Session.currentUser == null || !Session.currentUser.isAdmin()) {
            showAlert(Alert.AlertType.ERROR, "Доступ запрещен", "Только ADMIN может управлять пользователями");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        applyTheme(dialog); // ✅

        dialog.setTitle("Add user");
        dialog.setHeaderText("Create new user");

        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);

        TextField username = new TextField();
        username.setPromptText("username");
        PasswordField password = new PasswordField();
        password.setPromptText("password");

        VBox box = new VBox(10,
                new Label("Username:"), username,
                new Label("Password:"), password
        );
        dialog.getDialogPane().setContent(box);

        dialog.getDialogPane().lookupButton(addBtn).setDisable(true);
        username.textProperty().addListener((obs, ov, nv) ->
                dialog.getDialogPane().lookupButton(addBtn).setDisable(nv == null || nv.isBlank())
        );

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isEmpty() || res.get() != addBtn) return;

        String u = safeTrim(username.getText());
        String p = safeTrim(password.getText());

        if (u.isBlank() || p.isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Username и password не могут быть пустыми");
            return;
        }

        boolean ok = DBUtil.addUser(u, p);
        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Успех", "Пользователь добавлен!");
            onUsersClick();
        } else {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось добавить пользователя");
        }
    }

    @FXML
    private void onDeleteUserClick() {
        if (Session.currentUser == null || !Session.currentUser.isAdmin()) {
            showAlert(Alert.AlertType.ERROR, "Доступ запрещен", "Только ADMIN может управлять пользователями");
            return;
        }

        User selected = (userTable == null) ? null : userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Внимание", "Выбери пользователя в таблице");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        applyTheme(confirm); // ✅
        confirm.setTitle("Delete user");
        confirm.setHeaderText(null);
        confirm.setContentText("Удалить пользователя ID=" + selected.getId() + " ?");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            boolean ok = DBUtil.deleteUserById(selected.getId());
            if (ok) {
                onUsersClick();
                if (workoutTable != null) workoutTable.getItems().clear();
                setDashboard(0, 0, 0, null);
                setStatus("Deleted user id=" + selected.getId());
            } else {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось удалить пользователя");
            }
        }
    }

    @FXML
    private void onLogoutClick() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/app/view/login.fxml"));
            Stage stage = (Stage) statusLabel.getScene().getWindow();

            Scene sc = new Scene(root);
            applyTheme(sc); // ✅ чтобы login тоже был в теме
            stage.setScene(sc);
            stage.setTitle("Fitness Tracker - Login");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "login.fxml не найден или ошибка в FXML");
        }
    }

    // ================= WORKOUTS =================

    @FXML
    private void onLoadWorkoutsClick() {
        onRefreshClick();
    }

    @FXML
    public void onRefreshClick() {
        User u = getSelectedUserForWorkouts();

        // DEBUG
        System.out.println("Selected user = " + (u == null ? "NULL" : (u.getId() + " (" + u.getUsername() + ")")));

        if (u == null) {
            if (workoutTable != null) workoutTable.getItems().clear();
            setDashboard(0, 0, 0, null);
            setStatus("Select a user to see stats");
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate from = null;
        LocalDate to = null;

        if (fromDate != null && fromDate.getValue() != null) from = fromDate.getValue();
        if (toDate != null && toDate.getValue() != null) to = toDate.getValue();

        if (from == null && to == null) {
            String period = (periodBox == null) ? "All time" : periodBox.getSelectionModel().getSelectedItem();
            if (period == null) period = "All time";

            if ("Last 7 days".equals(period)) {
                from = today.minusDays(6);
                to = today;
            } else if ("Last 30 days".equals(period)) {
                from = today.minusDays(29);
                to = today;
            } else if ("This month".equals(period)) {
                YearMonth ym = YearMonth.from(today);
                from = ym.atDay(1);
                to = ym.atEndOfMonth();
            }
        }

        if (from != null && to != null && from.isAfter(to)) {
            LocalDate tmp = from;
            from = to;
            to = tmp;
        }

        System.out.println("Filter from=" + (from == null ? "NULL" : from) + ", to=" + (to == null ? "NULL" : to));

        List<Workout> filtered = DBUtil.getWorkoutsByUserId(u.getId(), from, to);
        System.out.println("Loaded workouts = " + filtered.size() + " for user_id=" + u.getId());

        ObservableList<Workout> data = FXCollections.observableArrayList(filtered);
        if (workoutTable != null) workoutTable.setItems(data);

        updateDashboardFromTable();

        if (filtered.isEmpty()) {
            setStatus("No workouts for user_id=" + u.getId() + " (check DB: workouts.user_id must match selected user)");
        } else {
            setStatus("Workouts: " + data.size()
                    + " | User: " + u.getUsername()
                    + " | From: " + (from == null ? "-" : from)
                    + " | To: " + (to == null ? "-" : to));
        }
    }

    private void updateDashboardFromTable() {
        ObservableList<Workout> items = (workoutTable == null) ? null : workoutTable.getItems();
        int workouts = (items == null) ? 0 : items.size();

        int minutes = 0;
        int calories = 0;
        LocalDate last = null;

        if (items != null) {
            for (Workout w : items) {
                minutes += w.getDurationMinutes();
                calories += w.getCalories();
                if (w.getDate() != null && (last == null || w.getDate().isAfter(last))) last = w.getDate();
            }
        }

        setDashboard(workouts, minutes, calories, last);
    }

    private void setDashboard(int workouts, int minutes, int calories, LocalDate last) {
        if (dashWorkouts != null) dashWorkouts.setText(String.valueOf(workouts));
        if (dashMinutes != null) dashMinutes.setText(String.valueOf(minutes));
        if (dashCalories != null) dashCalories.setText(String.valueOf(calories));
        if (dashLastDate != null) dashLastDate.setText(last == null ? "-" : last.toString());
    }

    // ===== WORKOUT CRUD =====

    @FXML
    private void onAddWorkoutClick() {
        User selected = getSelectedUserForWorkouts();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Внимание", "Сначала выбери пользователя");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        applyTheme(dialog); // ✅

        dialog.setTitle("Add workout");
        dialog.setHeaderText("Add workout for user: " + selected.getUsername());

        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField typeField = new TextField();
        typeField.setPromptText("type (e.g. Running)");
        TextField durationField = new TextField();
        durationField.setPromptText("duration minutes (e.g. 30)");
        TextField caloriesField = new TextField();
        caloriesField.setPromptText("calories (e.g. 250)");
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("notes (optional)");
        notesArea.setPrefRowCount(3);

        ComboBox<ExerciseCategory> catBox = new ComboBox<>();
        catBox.setItems(FXCollections.observableArrayList(DBUtil.getAllCategories()));
        catBox.setPromptText("Select category");

        VBox box = new VBox(10,
                new Label("Date:"), datePicker,
                new Label("Category:"), catBox,
                new Label("Type:"), typeField,
                new Label("Duration minutes:"), durationField,
                new Label("Calories:"), caloriesField,
                new Label("Notes:"), notesArea
        );
        dialog.getDialogPane().setContent(box);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isEmpty() || res.get() != addBtn) return;

        LocalDate d = datePicker.getValue();
        String type = safeTrim(typeField.getText());
        String notes = safeTrim(notesArea.getText());
        Integer duration = parseIntOrNull(durationField.getText());
        Integer calories = parseIntOrNull(caloriesField.getText());

        ExerciseCategory cat = catBox.getValue();
        Integer categoryId = (cat == null) ? null : cat.getId();

        if (d == null) { showAlert(Alert.AlertType.ERROR, "Ошибка", "Выбери дату"); return; }
        if (type.isBlank()) { showAlert(Alert.AlertType.ERROR, "Ошибка", "Type не может быть пустым"); return; }
        if (duration == null || duration <= 0) { showAlert(Alert.AlertType.ERROR, "Ошибка", "Duration должен быть > 0"); return; }
        if (calories == null || calories < 0) { showAlert(Alert.AlertType.ERROR, "Ошибка", "Calories должен быть >= 0"); return; }

        boolean ok = DBUtil.addWorkout(selected.getId(), d, type, duration, calories, notes, categoryId);
        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Успех", "Workout добавлен!");
            onRefreshClick();
        } else {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось добавить workout");
        }
    }

    @FXML
    private void onEditWorkoutClick() {
        User selectedUser = getSelectedUserForWorkouts();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Внимание", "Сначала выбери пользователя");
            return;
        }

        Workout w = (workoutTable == null) ? null : workoutTable.getSelectionModel().getSelectedItem();
        if (w == null) {
            showAlert(Alert.AlertType.WARNING, "Внимание", "Выбери workout в таблице Workouts");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        applyTheme(dialog); // ✅

        dialog.setTitle("Edit workout");
        dialog.setHeaderText("Edit workout id=" + w.getId() + " for user: " + selectedUser.getUsername());

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        DatePicker datePicker = new DatePicker(w.getDate());
        TextField typeField = new TextField(w.getType());
        TextField durationField = new TextField(String.valueOf(w.getDurationMinutes()));
        TextField caloriesField = new TextField(String.valueOf(w.getCalories()));
        TextArea notesArea = new TextArea(w.getNotes() == null ? "" : w.getNotes());
        notesArea.setPrefRowCount(3);

        ComboBox<ExerciseCategory> catBox = new ComboBox<>();
        catBox.setItems(FXCollections.observableArrayList(DBUtil.getAllCategories()));
        catBox.setPromptText("Select category");

        if (w.getCategoryId() != null) {
            for (ExerciseCategory c : catBox.getItems()) {
                if (c.getId() == w.getCategoryId()) { catBox.setValue(c); break; }
            }
        }

        VBox box = new VBox(10,
                new Label("Date:"), datePicker,
                new Label("Category:"), catBox,
                new Label("Type:"), typeField,
                new Label("Duration minutes:"), durationField,
                new Label("Calories:"), caloriesField,
                new Label("Notes:"), notesArea
        );
        dialog.getDialogPane().setContent(box);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isEmpty() || res.get() != saveBtn) return;

        LocalDate d = datePicker.getValue();
        String type = safeTrim(typeField.getText());
        String notes = safeTrim(notesArea.getText());
        Integer duration = parseIntOrNull(durationField.getText());
        Integer calories = parseIntOrNull(caloriesField.getText());

        ExerciseCategory cat = catBox.getValue();
        Integer categoryId = (cat == null) ? null : cat.getId();

        if (d == null) { showAlert(Alert.AlertType.ERROR, "Ошибка", "Выбери дату"); return; }
        if (type.isBlank()) { showAlert(Alert.AlertType.ERROR, "Ошибка", "Type не может быть пустым"); return; }
        if (duration == null || duration <= 0) { showAlert(Alert.AlertType.ERROR, "Ошибка", "Duration должен быть > 0"); return; }
        if (calories == null || calories < 0) { showAlert(Alert.AlertType.ERROR, "Ошибка", "Calories должен быть >= 0"); return; }

        boolean ok = DBUtil.updateWorkout(w.getId(), d, type, duration, calories, notes, categoryId);
        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Успех", "Workout обновлен!");
            onRefreshClick();
        } else {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось обновить workout");
        }
    }

    @FXML
    private void onDeleteWorkoutClick() {
        Workout selectedWorkout = (workoutTable == null) ? null : workoutTable.getSelectionModel().getSelectedItem();
        if (selectedWorkout == null) {
            showAlert(Alert.AlertType.WARNING, "Внимание", "Выбери workout в таблице Workouts");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        applyTheme(confirm); // ✅
        confirm.setTitle("Delete workout");
        confirm.setHeaderText(null);
        confirm.setContentText("Удалить workout ID=" + selectedWorkout.getId() + " ?");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            boolean ok = DBUtil.deleteWorkoutById(selectedWorkout.getId());
            if (ok) {
                onRefreshClick();
                setStatus("Deleted workout id=" + selectedWorkout.getId());
            } else {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось удалить workout");
            }
        }
    }

    // ================= CSV IMPORT/EXPORT =================

    @FXML
    private void onImportCsvClick() {
        User u = getSelectedUserForWorkouts();
        if (u == null) {
            showAlert(Alert.AlertType.WARNING, "Внимание", "Сначала выбери пользователя");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Open CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));

        File file = fc.showOpenDialog(statusLabel.getScene().getWindow());
        if (file == null) return;

        int inserted = DBUtil.importWorkoutsFromCsv(u.getId(), file);

        if (inserted > 0) {
            showAlert(Alert.AlertType.INFORMATION, "Import", "Импортировано записей: " + inserted);
            onRefreshClick();
        } else {
            showAlert(Alert.AlertType.ERROR, "Import error", "Не удалось импортировать CSV (проверь формат файла)");
        }
    }

    @FXML
    private void onExportCsvClick() {
        User u = getSelectedUserForWorkouts();
        if (u == null) {
            showAlert(Alert.AlertType.WARNING, "Внимание", "Сначала выбери пользователя");
            return;
        }

        ObservableList<Workout> items = (workoutTable == null) ? null : workoutTable.getItems();
        if (items == null || items.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Export", "Таблица Workouts пустая — сначала нажми Refresh");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Save CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        fc.setInitialFileName("workouts_user_" + u.getUsername() + ".csv");

        File file = fc.showSaveDialog(statusLabel.getScene().getWindow());
        if (file == null) return;

        try (FileWriter w = new FileWriter(file, StandardCharsets.UTF_8)) {
            w.write("id,date,category,type,duration_minutes,calories,notes\n");
            for (Workout wo : items) {
                String notes = (wo.getNotes() == null) ? "" : wo.getNotes().replace("\n", " ").replace("\r", " ");
                notes = notes.replace("\"", "\"\"");

                w.write(wo.getId() + ","
                        + wo.getDate() + ","
                        + safeCsv(wo.getCategoryName()) + ","
                        + safeCsv(wo.getType()) + ","
                        + wo.getDurationMinutes() + ","
                        + wo.getCalories() + ","
                        + "\"" + notes + "\""
                        + "\n");
            }

            showAlert(Alert.AlertType.INFORMATION, "Export", "CSV сохранён:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Export error", e.getMessage() == null ? "Ошибка" : e.getMessage());
        }
    }

    // ================= STATS/CHARTS =================

    @FXML
    private void onStatsByTypeClick() {
        User u = getSelectedUserForWorkouts();
        if (u == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Select user first");
            return;
        }

        LocalDate from = (fromDate != null) ? fromDate.getValue() : null;
        LocalDate to = (toDate != null) ? toDate.getValue() : null;

        List<TypeStat> stats = DBUtil.getWorkoutTypeStats(u.getId(), from, to);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/view/stats.fxml"));
            Parent root = loader.load();

            StatsController c = loader.getController();
            c.setData(stats);

            Stage stage = new Stage();
            stage.setTitle("Statistics");

            Scene sc = new Scene(root);
            applyTheme(sc);              // ✅
            stage.setScene(sc);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось открыть окно Statistics");
        }
    }

    @FXML
    private void onChartsClick() {
        User u = getSelectedUserForWorkouts();
        if (u == null) {
            showAlert(Alert.AlertType.WARNING, "Внимание", "Сначала выбери пользователя");
            return;
        }

        LocalDate from = (fromDate != null) ? fromDate.getValue() : null;
        LocalDate to = (toDate != null) ? toDate.getValue() : null;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/view/charts.fxml"));
            Parent root = loader.load();

            ChartsController c = loader.getController();
            c.init(u.getId(), from, to);

            Stage stage = new Stage();
            stage.setTitle("Charts - " + u.getUsername());

            Scene sc = new Scene(root);
            applyTheme(sc);              // ✅
            stage.setScene(sc);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось открыть окно Charts");
        }
    }

    // ================= CATEGORIES =================

    @FXML
    private void onLoadCategoriesClick() {
        if (categoryTable == null) return;
        categoryTable.setItems(FXCollections.observableArrayList(DBUtil.getAllCategories()));
        setStatus("Categories loaded: " + categoryTable.getItems().size());
    }

    @FXML
    private void onAddCategoryClick() {
        TextInputDialog dialog = new TextInputDialog();
        applyTheme(dialog); // ✅
        dialog.setTitle("Add category");
        dialog.setHeaderText(null);
        dialog.setContentText("Category name:");

        Optional<String> res = dialog.showAndWait();
        if (res.isEmpty()) return;

        String name = safeTrim(res.get());
        if (name.isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Name не может быть пустым");
            return;
        }

        boolean ok = DBUtil.addCategory(name);
        if (ok) {
            onLoadCategoriesClick();
            showAlert(Alert.AlertType.INFORMATION, "Успех", "Категория добавлена!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось добавить (возможно такая категория уже есть)");
        }
    }

    @FXML
    private void onEditCategoryClick() {
        if (categoryTable == null) return;

        ExerciseCategory c = categoryTable.getSelectionModel().getSelectedItem();
        if (c == null) {
            showAlert(Alert.AlertType.WARNING, "Внимание", "Выбери категорию");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(c.getName());
        applyTheme(dialog); // ✅
        dialog.setTitle("Edit category");
        dialog.setHeaderText(null);
        dialog.setContentText("New name:");

        Optional<String> res = dialog.showAndWait();
        if (res.isEmpty()) return;

        String name = safeTrim(res.get());
        if (name.isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Name не может быть пустым");
            return;
        }

        boolean ok = DBUtil.updateCategory(c.getId(), name);
        if (ok) {
            onLoadCategoriesClick();
            showAlert(Alert.AlertType.INFORMATION, "Успех", "Категория обновлена!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось обновить");
        }
    }

    @FXML
    private void onDeleteCategoryClick() {
        if (categoryTable == null) return;

        ExerciseCategory c = categoryTable.getSelectionModel().getSelectedItem();
        if (c == null) {
            showAlert(Alert.AlertType.WARNING, "Внимание", "Выбери категорию");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        applyTheme(confirm); // ✅
        confirm.setTitle("Delete category");
        confirm.setHeaderText(null);
        confirm.setContentText("Удалить категорию ID=" + c.getId() + " (" + c.getName() + ")?");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            boolean ok = DBUtil.deleteCategoryById(c.getId());
            if (ok) {
                onLoadCategoriesClick();
                setStatus("Deleted category id=" + c.getId());
            } else {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось удалить (возможно есть зависимости)");
            }
        }
    }

    // ================= helpers =================

    private Integer parseIntOrNull(String s) {
        try {
            if (s == null) return null;
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private void setStatus(String s) {
        if (statusLabel != null) statusLabel.setText(s);
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private String safeCsv(String s) {
        if (s == null) return "";
        String v = s.replace("\n", " ").replace("\r", " ");
        if (v.contains(",") || v.contains("\"")) {
            v = v.replace("\"", "\"\"");
            return "\"" + v + "\"";
        }
        return v;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        applyTheme(alert); // ✅
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ===== THEME =====

    private void applyTheme(Scene scene) {
        if (scene == null) return;
        String css = getClass().getResource("/app/view/style/style.css").toExternalForm();
        if (!scene.getStylesheets().contains(css)) scene.getStylesheets().add(css);
        if (scene.getRoot() != null && !scene.getRoot().getStyleClass().contains("root")) {
            scene.getRoot().getStyleClass().add("root");
        }
    }

    private void applyTheme(Dialog<?> dialog) {
        if (dialog == null) return;
        String css = getClass().getResource("/app/view/style/style.css").toExternalForm();
        DialogPane pane = dialog.getDialogPane();
        if (!pane.getStylesheets().contains(css)) pane.getStylesheets().add(css);
        if (!pane.getStyleClass().contains("root")) pane.getStyleClass().add("root");
    }

    private void applyTheme(Alert alert) {
        if (alert == null) return;
        String css = getClass().getResource("/app/view/style/style.css").toExternalForm();
        DialogPane pane = alert.getDialogPane();
        if (!pane.getStylesheets().contains(css)) pane.getStylesheets().add(css);
        if (!pane.getStyleClass().contains("root")) pane.getStyleClass().add("root");
    }
}
