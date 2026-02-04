package app.controller;

import app.model.Session;
import app.model.User;
import app.util.DBUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML
    private void onLoginClick() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Введите логин и пароль");
            return;
        }

        User user = DBUtil.login(username, password);
        if (user == null) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Неверный логин или пароль");
            return;
        }

        // ✅ сохраняем сессию
        Session.currentUser = user;

        try {
            URL url = getClass().getResource("/app/view/main.fxml");
            if (url == null) {
                showAlert(Alert.AlertType.ERROR, "Ошибка",
                        "Файл НЕ найден:\n/app/view/main.fxml\n\nПроверь путь:\nsrc/main/resources/app/view/main.fxml");
                return;
            }

            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Fitness Tracker");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "FXML ошибка",
                    e.getClass().getSimpleName() + ":\n" + (e.getMessage() == null ? "(без сообщения)" : e.getMessage()));
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
