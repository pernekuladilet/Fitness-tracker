package app.controller;

import app.util.DBUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddUserController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private Runnable onSuccess; // чтобы после добавления обновить таблицу

    public void setOnSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }

    @FXML
    private void onSaveClick() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Введите username и password");
            return;
        }

        boolean ok = DBUtil.addUser(username, password);

        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Успех", "Пользователь добавлен!");
            if (onSuccess != null) onSuccess.run();
            closeWindow();
        } else {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось добавить пользователя");
        }
    }

    @FXML
    private void onCancelClick() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
