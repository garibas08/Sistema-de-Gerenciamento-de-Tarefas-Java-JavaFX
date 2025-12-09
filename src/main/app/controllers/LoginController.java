package app.controllers;

import app.models.User;
import app.storage.UserStorage;
import app.storage.TaskStorage;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.util.List;

public class LoginController {

    @FXML private TextField UsernameField;
    @FXML private PasswordField PasswordField;

    @FXML
    private void handleLogin() {
        String user = UsernameField.getText().trim();
        String pass = PasswordField.getText().trim();

        List<User> users = UserStorage.loadUsers();

        boolean exists = users.stream()
                .anyMatch(u -> u.getUsuario().equals(user) && u.getSenha().equals(pass));

        if (!exists) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Usuário ou senha incorretos.");
            alert.show();
            return;
        }

        TaskStorage.setCurrentUser(user);
        openMainWindow();
    }

    @FXML
    private void openRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/views/RegisterView.fxml"));
            Stage stage = (Stage) UsernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/views/MainView.fxml"));
            Stage stage = (Stage) UsernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
