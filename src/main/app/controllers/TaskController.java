package app.controllers;

import app.models.Task;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;


public class TaskController {

    @FXML
    private TextField taskInput;

    @FXML
    private ListView<Task> taskListView;

    private ObservableList<Task> tasks;

    @FXML
    public void initialize() {
        tasks = FXCollections.observableArrayList();
        taskListView.setItems(tasks);

        // duplo clique para marcar/desmarcar tarefa
        taskListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Task selected = taskListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    selected.setDone(!selected.isDone());
                    // atualiza view
                    taskListView.refresh();
                }
            }
        });
    }

    @FXML
    private void handleAddTask() {
        String title = taskInput.getText();
        if (title != null && !title.trim().isEmpty()) {
            tasks.add(new Task(title.trim()));
            Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Sucesso");
                success.setHeaderText(null);
                success.setContentText("A tarefa foi adicionada com sucesso!");
                success.showAndWait();

            taskInput.clear();
        }
    }

    @FXML
    private void handleDeleteTask() {
        Task selected = taskListView.getSelectionModel().getSelectedItem();
        if (selected != null){
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);    
            confirm.setTitle("Confirmar exclusão");
            confirm.setHeaderText(null);
            confirm.setContentText("Você realmente deseja deletar essa tarefa?");
            if (confirm.showAndWait().get() != ButtonType.OK) {
                return;
            }

            tasks.remove(selected);

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Sucesso");
            success.setHeaderText(null);
            success.setContentText("A tarefa foi deletada com sucesso!");
            success.showAndWait();

        }
        else {
            Alert vazio = new Alert(Alert.AlertType.INFORMATION);
                vazio.setTitle("Aviso");
                vazio.setHeaderText(null);
                vazio.setContentText("Escolha uma tarefa para ser deletada!");
                vazio.showAndWait();
        }
    }
}
