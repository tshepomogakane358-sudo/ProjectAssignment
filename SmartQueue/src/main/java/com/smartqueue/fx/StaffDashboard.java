package com.smartqueue.fx;

import com.smartqueue.dao.DepartmentDAO;
import com.smartqueue.model.Department;
import com.smartqueue.model.QueueEntry;
import com.smartqueue.service.NotificationService;
import com.smartqueue.service.QueueService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.List;

public class StaffDashboard {

    private final Stage               stage;
    private final QueueService        queueService        = new QueueService();
    private final NotificationService notificationService = new NotificationService();
    private final DepartmentDAO       departmentDAO       = new DepartmentDAO();

    private TableView<QueueEntry> queueTable;
    private ComboBox<String>      deptBox;

    public StaffDashboard(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        // Simple credential check
        if (!authenticate()) return;

        stage.setTitle("SmartQueue — Staff Dashboard");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + StyleManager.BACKGROUND + ";");

        // Top bar
        HBox topBar = new HBox();
        topBar.setStyle("-fx-background-color: " + StyleManager.PRIMARY_DARK +
                ";-fx-padding: 14 24;");
        topBar.setAlignment(Pos.CENTER_LEFT);
        Text title = new Text("SmartQueue — Staff");
        title.setStyle("-fx-fill:white;-fx-font-size:18px;-fx-font-weight:bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color:rgba(255,255,255,0.2);" +
                "-fx-text-fill:white;-fx-cursor:hand;" +
                "-fx-background-radius:4;-fx-padding:6 14;");
        logoutBtn.setOnAction(e -> new LoginScreen(stage).show());
        topBar.getChildren().addAll(title, spacer, logoutBtn);
        root.setTop(topBar);

        // Main content
        VBox content = new VBox(16);
        content.setPadding(new Insets(24));

        Text heading = new Text("Queue Management");
        heading.setStyle(StyleManager.label(StyleManager.TEXT_PRIMARY, 18, true));

        // Department selector
        List<Department> depts = departmentDAO.getAllDepartments();
        deptBox = new ComboBox<>();
        for (Department d : depts) {
            deptBox.getItems().add(d.getDepartmentId() + " — " + d.getName());
        }
        deptBox.setPromptText("Select department");
        deptBox.setMaxWidth(300);

        Button refreshBtn = new Button("Load Queue");
        refreshBtn.setStyle(StyleManager.primaryButton());
        refreshBtn.setOnAction(e -> loadQueue());

        HBox deptRow = new HBox(12, deptBox, refreshBtn);
        deptRow.setAlignment(Pos.CENTER_LEFT);

        // Queue table
        queueTable = new TableView<>();
        queueTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        queueTable.setPlaceholder(new Label("Select a department and click Load Queue."));

        TableColumn<QueueEntry, String> numCol = new TableColumn<>("Queue #");
        numCol.setCellValueFactory(new PropertyValueFactory<>("queueNumber"));

        TableColumn<QueueEntry, Integer> patCol = new TableColumn<>("Patient ID");
        patCol.setCellValueFactory(new PropertyValueFactory<>("patientId"));

        TableColumn<QueueEntry, Integer> priCol = new TableColumn<>("Priority");
        priCol.setCellValueFactory(new PropertyValueFactory<>("priorityId"));

        TableColumn<QueueEntry, String> statCol = new TableColumn<>("Status");
        statCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<QueueEntry, Integer> waitCol = new TableColumn<>("Wait (min)");
        waitCol.setCellValueFactory(new PropertyValueFactory<>("estimatedWaitMin"));

        queueTable.getColumns().addAll(numCol, patCol, priCol, statCol, waitCol);

        // Action buttons
        Button callNextBtn = new Button("Call Next Patient");
        callNextBtn.setStyle(StyleManager.primaryButton());

        Button markDoneBtn = new Button("Mark Selected Done");
        markDoneBtn.setStyle(StyleManager.successButton());

        Button noShowBtn = new Button("Mark No-show");
        noShowBtn.setStyle(StyleManager.dangerButton());

        Label actionLabel = new Label();
        actionLabel.setWrapText(true);

        callNextBtn.setOnAction(e -> {
            if (deptBox.getValue() == null) {
                actionLabel.setText("Please select a department first.");
                return;
            }
            int deptId = Integer.parseInt(deptBox.getValue().split(" — ")[0]);
            QueueEntry next = queueService.callNext(deptId);
            if (next != null) {
                notificationService.sendSystemNotification(next.getPatientId(),
                        "Queue #" + next.getQueueNumber() + " — please proceed.");
                actionLabel.setStyle(
                        StyleManager.label(StyleManager.SUCCESS, 13, false));
                actionLabel.setText("Now calling: Queue #" + next.getQueueNumber());
                loadQueue();
            } else {
                actionLabel.setStyle(
                        StyleManager.label(StyleManager.DANGER, 13, false));
                actionLabel.setText("No patients waiting.");
            }
        });

        markDoneBtn.setOnAction(e -> {
            QueueEntry selected = queueTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                actionLabel.setText("Please select a queue entry first.");
                return;
            }
            queueService.markDone(selected.getQueueId());
            actionLabel.setStyle(StyleManager.label(StyleManager.SUCCESS, 13, false));
            actionLabel.setText("Marked as done.");
            loadQueue();
        });

        noShowBtn.setOnAction(e -> {
            QueueEntry selected = queueTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                actionLabel.setText("Please select a queue entry first.");
                return;
            }
            queueService.markNoShow(selected.getQueueId());
            actionLabel.setStyle(StyleManager.label(StyleManager.DANGER, 13, false));
            actionLabel.setText("Marked as no-show.");
            loadQueue();
        });

        HBox actionRow = new HBox(12, callNextBtn, markDoneBtn, noShowBtn);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(
                heading, new Separator(),
                deptRow, queueTable,
                actionRow, actionLabel
        );

        root.setCenter(content);
        stage.setScene(new Scene(root, 860, 600));
        stage.show();
    }

    private void loadQueue() {
        if (deptBox.getValue() == null) return;
        int deptId = Integer.parseInt(deptBox.getValue().split(" — ")[0]);
        List<QueueEntry> queue = queueService.viewQueue(deptId);
        queueTable.setItems(FXCollections.observableArrayList(queue));
    }

    private boolean authenticate() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Staff Login");
        dialog.setHeaderText("Enter staff credentials");

        TextField userField = new TextField();
        userField.setPromptText("Username");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");

        VBox box = new VBox(10, new Label("Username:"), userField,
                new Label("Password:"), passField);
        box.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return userField.getText().equals("staff")
                        && passField.getText().equals("staff123");
            }
            return false;
        });

        return dialog.showAndWait().orElse(false);
    }
}
