package com.smartqueue.fx;

import com.smartqueue.dao.DepartmentDAO;
import com.smartqueue.dao.PatientDAO;
import com.smartqueue.model.Department;
import com.smartqueue.model.Patient;
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

public class AdminDashboard {

    private final Stage         stage;
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final PatientDAO    patientDAO    = new PatientDAO();

    public AdminDashboard(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        if (!authenticate()) return;

        stage.setTitle("SmartQueue — Admin Dashboard");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + StyleManager.BACKGROUND + ";");

        // Top bar
        HBox topBar = new HBox();
        topBar.setStyle("-fx-background-color: #333;-fx-padding:14 24;");
        topBar.setAlignment(Pos.CENTER_LEFT);
        Text title = new Text("SmartQueue — Admin");
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

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(
                buildPatientsTab(),
                buildDepartmentsTab()
        );

        root.setCenter(tabs);
        stage.setScene(new Scene(root, 860, 600));
        stage.show();
    }

    private Tab buildPatientsTab() {
        Tab tab = new Tab("All Patients");
        VBox content = new VBox(14);
        content.setPadding(new Insets(24));

        Text heading = new Text("Registered Patients");
        heading.setStyle(StyleManager.label(StyleManager.TEXT_PRIMARY, 18, true));

        TableView<Patient> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Patient, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("patientId"));

        TableColumn<Patient, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Patient, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));

        TableColumn<Patient, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        table.getColumns().addAll(idCol, nameCol, contactCol, emailCol);

        List<Patient> patients = patientDAO.getAllPatients();
        table.setItems(FXCollections.observableArrayList(patients));
        table.setPlaceholder(new Label("No patients registered."));

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle(StyleManager.primaryButton());
        refreshBtn.setOnAction(e -> table.setItems(
                FXCollections.observableArrayList(patientDAO.getAllPatients())));

        content.getChildren().addAll(heading, new Separator(), table, refreshBtn);
        tab.setContent(content);
        return tab;
    }

    private Tab buildDepartmentsTab() {
        Tab tab = new Tab("Departments");
        VBox content = new VBox(14);
        content.setPadding(new Insets(24));

        Text heading = new Text("Department Management");
        heading.setStyle(StyleManager.label(StyleManager.TEXT_PRIMARY, 18, true));

        TableView<Department> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Department, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("departmentId"));

        TableColumn<Department, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Department, Integer> limitCol = new TableColumn<>("Daily Limit");
        limitCol.setCellValueFactory(new PropertyValueFactory<>("dailyLimit"));

        table.getColumns().addAll(idCol, nameCol, limitCol);
        table.setItems(FXCollections.observableArrayList(
                departmentDAO.getAllDepartments()));

        // Update limit controls
        TextField deptIdField = new TextField();
        deptIdField.setPromptText("Department ID");
        deptIdField.setStyle(StyleManager.textField());
        deptIdField.setMaxWidth(140);

        TextField limitField = new TextField();
        limitField.setPromptText("New daily limit");
        limitField.setStyle(StyleManager.textField());
        limitField.setMaxWidth(140);

        Label resultLabel = new Label();

        Button updateBtn = new Button("Update Limit");
        updateBtn.setStyle(StyleManager.primaryButton());
        updateBtn.setOnAction(e -> {
            try {
                int deptId   = Integer.parseInt(deptIdField.getText().trim());
                int newLimit = Integer.parseInt(limitField.getText().trim());
                boolean ok   = departmentDAO.updateDailyLimit(deptId, newLimit);
                if (ok) {
                    resultLabel.setStyle(
                            StyleManager.label(StyleManager.SUCCESS, 13, false));
                    resultLabel.setText("Limit updated successfully.");
                    table.setItems(FXCollections.observableArrayList(
                            departmentDAO.getAllDepartments()));
                } else {
                    resultLabel.setStyle(
                            StyleManager.label(StyleManager.DANGER, 13, false));
                    resultLabel.setText("Update failed. Check department ID.");
                }
            } catch (NumberFormatException ex) {
                resultLabel.setStyle(
                        StyleManager.label(StyleManager.DANGER, 13, false));
                resultLabel.setText("Please enter valid numbers.");
            }
        });

        HBox updateRow = new HBox(10, deptIdField, limitField, updateBtn);
        updateRow.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(
                heading, new Separator(),
                table, new Label("Update Daily Limit:"),
                updateRow, resultLabel
        );

        tab.setContent(content);
        return tab;
    }

    private boolean authenticate() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Admin Login");
        dialog.setHeaderText("Enter admin credentials");

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
                return userField.getText().equals("admin")
                        && passField.getText().equals("admin123");
            }
            return false;
        });

        return dialog.showAndWait().orElse(false);
    }
}
