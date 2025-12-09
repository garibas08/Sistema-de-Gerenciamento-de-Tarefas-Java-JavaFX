package app.controllers;

import app.storage.TaskStorage;
import app.models.Task;

import java.util.List;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;

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
    //  CELL FACTORY COMPLETA (CHECKBOX + EDIT + DRAG START)
    // =======================================================
    private void configureCellFactory() {

        taskListView.setCellFactory(lv -> {

            ListCell<Task> cell = new ListCell<>() {

                private final CheckBox checkBox = new CheckBox();
                private double pressY;

                {
                    // registrar Y ao pressionar (para distinguir click de drag)
                    setOnMousePressed(e -> pressY = e.getScreenY());

                    // DRAG_DETECTED para iniciar drag (evita conflito com clicks)
                    setOnDragDetected(e -> {
                        if (getItem() == null) return;

                        // não começar drag se o clique foi no checkbox
                        Node target = e.getPickResult().getIntersectedNode();
                        if (isNodeOrParentCheckBox(target)) {
                            return;
                        }

                        // Inicia drag (só se tiver havido movimento pregresso)
                        Dragboard db = startDragAndDrop(TransferMode.MOVE);
                        ClipboardContent cc = new ClipboardContent();
                        cc.putString(String.valueOf(getIndex()));
                        db.setContent(cc);

                        e.consume();
                    });

                    // Duplo clique para editar (não consome eventos simples)
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
                protected void updateItem(Task task, boolean empty) {
                    super.updateItem(task, empty);

                    if (empty || task == null) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    CheckBox checkBox = new CheckBox();
                    checkBox.setSelected(task.isDone());

                    Label label = new Label(task.getTitle());
                    label.setStyle(task.isDone()
                             ? "-fx-text-fill: #777; -fx-text-decoration: line-through;"
                            : "-fx-text-fill: black;"
                    );

                    checkBox.setOnAction(evt -> {
                    task.setDone(checkBox.isSelected());
                    TaskStorage.saveTasks(tasks);

                    FadeTransition ft = new FadeTransition(Duration.millis(150), label);
                    ft.setFromValue(0.3);
                    ft.setToValue(1.0);
                    ft.play();


                    getListView().refresh();


                 });

                HBox box = new HBox(10, checkBox, label);
                setGraphic(box);
            }

            };

            return cell;
        });
    }

    // =======================================================
    // RECEBIMENTO DO DRAG & DROP (corrige índices e posicionamento)
    // =======================================================
    private void configureDragReceiver() {

        // aceitar o drag (aceita tanto vindo de fora quanto de dentro da mesma lista)
        taskListView.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        // drop: calcula índice alvo a partir do node sob o mouse e metade da célula
        taskListView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString()) {
                try {
                    int fromIndex = Integer.parseInt(db.getString());
                    Node targetNode = event.getPickResult().getIntersectedNode();
                    ListCell<?> targetCell = findParentListCell(targetNode);

                    int toIndex;
                    double sceneY = event.getSceneY();

                    if (targetCell != null) {
                        Bounds cellBounds = targetCell.localToScene(targetCell.getBoundsInLocal());
                        double midY = cellBounds.getMinY() + cellBounds.getHeight() / 2.0;

                        if (sceneY > midY) {
                            toIndex = targetCell.getIndex() + 1; // depois da célula
                        } else {
                            toIndex = targetCell.getIndex(); // antes da célula
                        }
                    } else {
                        toIndex = tasks.size();
                    }

                    Task moved = tasks.remove(fromIndex);
                    if (fromIndex < toIndex) {
                        toIndex = Math.max(0, toIndex - 1);
                    }

                    if (toIndex > tasks.size()) toIndex = tasks.size();
                    if (toIndex < 0) toIndex = 0;

                    tasks.add(toIndex, moved);
                    taskListView.getSelectionModel().select(toIndex);

                    TaskStorage.saveTasks(tasks);
                    success = true;
                } catch (NumberFormatException ex) {
                    success = false;
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    // =======================================================
    // AUX: sobe a hierarquia até achar ListCell
    // =======================================================
    private ListCell<?> findParentListCell(Node node) {
        while (node != null && !(node instanceof ListCell)) {
            node = node.getParent();
        }
        return (ListCell<?>) node;
    }

    // =======================================================
    // AUX: detecta se nó (ou pai) é CheckBox
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
