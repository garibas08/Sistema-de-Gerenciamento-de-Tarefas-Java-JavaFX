package app.controllers;

import app.models.User;
import app.storage.UserStorage;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

public class RegisterController {

    @FXML private TextField UsernameField;
    @FXML private PasswordField PasswordField;

    @FXML
    private void handleRegister() {
        String user = UsernameField.getText().trim();
        String pass = PasswordField.getText().trim();

        if (user.isBlank() || pass.isBlank()) {
            new Alert(Alert.AlertType.ERROR, "Preencha todos os campos!").show();
            return;
        }

        List<User> users = UserStorage.loadUsers();

        boolean exists = users.stream()
                .anyMatch(u -> u.getUsuario().equals(user));

        if (exists) {
            new Alert(Alert.AlertType.ERROR, "Usuário já existe!").show();
            return;
        }

        users.add(new User(user, pass));
        UserStorage.saveUsers(users);

        new Alert(Alert.AlertType.INFORMATION, "Conta criada com sucesso!").show();

        goBack();
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/views/LoginView.fxml"));
            Stage stage = (Stage) UsernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
