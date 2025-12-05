package app.controllers;

import app.storage.TaskStorage;
import app.models.Task;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;

public class TaskController {

    @FXML
    private TextField taskInput;

    @FXML
    private ListView<Task> taskListView;

    private ObservableList<Task> tasks;

    @FXML
    public void initialize() {

        List<Task> loaded = TaskStorage.loadTasks();
        tasks = (loaded != null)
                ? FXCollections.observableArrayList(loaded)
                : FXCollections.observableArrayList();

        taskListView.setItems(tasks);

        configureCellFactory();
        configureDragReceiver();
    }

    // =======================================================
    //  CELL FACTORY COMPLETA (CHECKBOX + EDIT + DRAG)
    // =======================================================
    private void configureCellFactory() {

        taskListView.setCellFactory(lv -> {

            ListCell<Task> cell = new ListCell<>() {

                private final CheckBox checkBox = new CheckBox();
                private double pressY;

                {
                    // Registrar Y ao pressionar
                    setOnMousePressed(e -> pressY = e.getScreenY());

                    // Drag **deve** iniciar aqui
                    setOnDragDetected(e -> {
                        if (getItem() == null) return;

                        Node target = e.getPickResult().getIntersectedNode();
                        if (isNodeOrParentCheckBox(target)) return; // evita drag no checkbox

                        // inicia drag somente se houve movimento antes
                        if (Math.abs(e.getScreenY() - pressY) < 4) return;

                        Dragboard db = startDragAndDrop(TransferMode.MOVE);
                        ClipboardContent cc = new ClipboardContent();
                        cc.putString(String.valueOf(getIndex()));
                        db.setContent(cc);
                        e.consume();
                    });

                    // Duplo clique para editar
                    setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2 && !isEmpty()) {
                            Task t = getItem();
                            TextInputDialog dialog = new TextInputDialog(t.getTitle());
                            dialog.setTitle("Editar Tarefa");
                            dialog.setHeaderText(null);
                            dialog.setContentText("Novo texto:");

                            dialog.showAndWait().ifPresent(newText -> {
                                if (!newText.isBlank()) {
                                    t.setTitle(newText.trim());
                                    taskListView.refresh();
                                    TaskStorage.saveTasks(tasks);
                                }
                            });
                        }
                    });
                }

                @Override
                protected void updateItem(Task item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setGraphic(null);
                        return;
                    }

                    // CheckBox da célula
                    checkBox.setText(item.getTitle());
                    checkBox.setSelected(item.isDone());

                    // impedir click no checkbox de interferir com drag
                    checkBox.setOnMousePressed(e -> e.consume());

                    checkBox.setOnAction(e -> {
                        item.setDone(checkBox.isSelected());
                        TaskStorage.saveTasks(tasks);
                        taskListView.refresh();
                    });

                    setGraphic(checkBox);
                }
            };

            return cell;
        });
    }

    // =======================================================
    // RECEBIMENTO DO DRAG & DROP
    // =======================================================
    private void configureDragReceiver() {

        taskListView.setOnDragOver(event -> {
            if (event.getDragboard().hasString())
                event.acceptTransferModes(TransferMode.MOVE);
        });

        taskListView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasString()) {

                int fromIndex = Integer.parseInt(db.getString());
                Task dragged = tasks.remove(fromIndex);

                int toIndex = taskListView.getSelectionModel().getSelectedIndex();
                if (toIndex < 0) toIndex = tasks.size();

                tasks.add(toIndex, dragged);
                TaskStorage.saveTasks(tasks);

                event.setDropCompleted(true);
            }
        });
    }

    // =======================================================
    // AUXILIAR: identificar se clicou no CheckBox
    // =======================================================
    private boolean isNodeOrParentCheckBox(Node node) {
        while (node != null) {
            if (node instanceof CheckBox) return true;
            node = node.getParent();
        }
        return false;
    }

    // =======================================================
    // ADICIONAR TAREFA
    // =======================================================
    @FXML
    private void handleAddTask() {
        String title = taskInput.getText();

        if (title != null && !title.trim().isEmpty()) {

            tasks.add(new Task(title.trim()));
            TaskStorage.saveTasks(tasks);

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Sucesso");
            success.setHeaderText(null);
            success.setContentText("A tarefa foi adicionada com sucesso!");
            success.showAndWait();

            taskInput.clear();
        }
    }

    // =======================================================
    // DELETAR TAREFA
    // =======================================================
    @FXML
    private void handleDeleteTask() {

        Task selected = taskListView.getSelectionModel().getSelectedItem();

        if (selected == null) {

            Alert warn = new Alert(Alert.AlertType.INFORMATION);
            warn.setTitle("Aviso");
            warn.setHeaderText(null);
            warn.setContentText("Escolha uma tarefa para ser deletada!");
            warn.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar exclusão");
        confirm.setHeaderText(null);
        confirm.setContentText("Você realmente deseja deletar essa tarefa?");

        if (confirm.showAndWait().get() != ButtonType.OK) return;

        tasks.remove(selected);
        TaskStorage.saveTasks(tasks);

        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Sucesso");
        success.setHeaderText(null);
        success.setContentText("A tarefa foi deletada com sucesso!");
        success.showAndWait();
    }
}
