package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {

    private static final String CSS_PATH = "/app/view/style/style.css";

    @Override
    public void start(Stage stage) throws Exception {

        // ✅ Load fonts that you реально have
        loadFont("/app/view/fonts/Inter-VariableFont_opsz,wght.ttf");
        loadFont("/app/view/fonts/Inter-Italic-VariableFont_opsz,wght.ttf");

        Parent root = FXMLLoader.load(getClass().getResource("/app/view/login.fxml"));
        Scene scene = new Scene(root, 1200, 780);

        // ✅ Apply theme globally to first scene
        applyTheme(scene);

        stage.setTitle("Fitness Tracker");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.centerOnScreen();
        stage.show();
    }

    // ✅ Call this for any new Scene you create (charts/stats windows)
    public static void applyTheme(Scene scene) {
        if (scene == null) return;

        String css = Main.class.getResource(CSS_PATH).toExternalForm();
        if (!scene.getStylesheets().contains(css)) scene.getStylesheets().add(css);

        if (scene.getRoot() != null && !scene.getRoot().getStyleClass().contains("root")) {
            scene.getRoot().getStyleClass().add("root");
        }
    }

    // ✅ Call this for Dialogs (Add workout, Add user, alerts etc.)
    public static void applyTheme(Dialog<?> dialog) {
        if (dialog == null) return;

        String css = Main.class.getResource(CSS_PATH).toExternalForm();
        DialogPane pane = dialog.getDialogPane();
        if (!pane.getStylesheets().contains(css)) pane.getStylesheets().add(css);

        if (!pane.getStyleClass().contains("root")) {
            pane.getStyleClass().add("root");
        }
    }

    private void loadFont(String resourcePath) {
        try {
            Font f = Font.loadFont(getClass().getResourceAsStream(resourcePath), 14);
            if (f == null) System.out.println("⚠ Font not loaded: " + resourcePath);
            else System.out.println("✅ Font loaded: " + f.getName() + " (" + resourcePath + ")");
        } catch (Exception e) {
            System.out.println("⚠ Font load error: " + resourcePath);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
