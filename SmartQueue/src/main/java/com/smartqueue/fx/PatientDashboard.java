package com.smartqueue.fx;


import com.smartqueue.dao.DepartmentDAO;
import com.smartqueue.model.Appointment;
import com.smartqueue.model.Department;
import com.smartqueue.model.Patient;
import com.smartqueue.service.AppointmentService;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


public class PatientDashboard {


    private final Stage              stage;
    private final Patient            patient;
    private final AppointmentService appointmentService  = new AppointmentService();
    private final QueueService       queueService        = new QueueService();
    private final NotificationService notificationService = new NotificationService();
    private final DepartmentDAO      departmentDAO       = new DepartmentDAO();

    public PatientDashboard(Stage stage, Patient patient) {
        this.stage   = stage;
        this.patient = patient;
    }

    public void show() {
        stage.setTitle("SmartQueue — " + patient.getName());

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + StyleManager.BACKGROUND + ";");

        // Top bar
        HBox topBar = buildTopBar();
        root.setTop(topBar);

        // Tab pane
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-background-color: " + StyleManager.BACKGROUND + ";");

        tabs.getTabs().addAll(
                buildBookTab(),
                buildMyAppointmentsTab(),
                buildQueueTab()
        );

        root.setCenter(tabs);

        stage.setScene(new Scene(root, 860, 600));
        stage.setResizable(true);
        stage.show();
    }

    // --- Top bar ---

    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setStyle("-fx-background-color: " + StyleManager.PRIMARY + ";-fx-padding: 14 24;");
        bar.setAlignment(Pos.CENTER_LEFT);

        Text title = new Text("SmartQueue");
        title.setStyle("-fx-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Text welcome = new Text("Welcome, " + patient.getName());
        welcome.setStyle("-fx-fill: white; -fx-font-size: 13px;");

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2);" +
                "-fx-text-fill: white;-fx-cursor: hand;" +
                "-fx-background-radius: 4;-fx-padding: 6 14;");
        logoutBtn.setOnAction(e -> new LoginScreen(stage).show());

        bar.getChildren().addAll(title, spacer, welcome,
                new Label("  "), logoutBtn);
        return bar;
    }

    // --- Book Appointment Tab ---

    private Tab buildBookTab() {
        Tab tab = new Tab("Book Appointment");

        VBox content = new VBox(14);
        content.setPadding(new Insets(24));
        content.setMaxWidth(480);

        Text heading = new Text("Book an Appointment");
        heading.setStyle(StyleManager.label(StyleManager.TEXT_PRIMARY, 18, true));

        // Department picker
        List<Department> depts = departmentDAO.getAllDepartments();
        ComboBox<String> deptBox = new ComboBox<>();
        for (Department d : depts) {
            deptBox.getItems().add(d.getDepartmentId() + " — " + d.getName());
        }
        deptBox.setPromptText("Select department");
        deptBox.setMaxWidth(Double.MAX_VALUE);
        deptBox.setStyle(StyleManager.textField());

        // Date picker
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Select date");
        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.setValue(LocalDate.now().plusDays(1));

        // Time field
        TextField timeField = new TextField("09:00");
        timeField.setPromptText("Time (HH:MM)");
        timeField.setStyle(StyleManager.textField());
        timeField.setMaxWidth(Double.MAX_VALUE);

        Label resultLabel = new Label();
        resultLabel.setWrapText(true);

        Button bookBtn = new Button("Book Appointment");
        bookBtn.setStyle(StyleManager.primaryButton());
        bookBtn.setMaxWidth(Double.MAX_VALUE);

        bookBtn.setOnAction(e -> {
            if (deptBox.getValue() == null || datePicker.getValue() == null
                    || timeField.getText().isEmpty()) {
                resultLabel.setStyle(
                        StyleManager.label(StyleManager.DANGER, 13, false));
                resultLabel.setText("Please fill in all fields.");
                return;
            }

            try {
                int deptId = Integer.parseInt(
                        deptBox.getValue().split(" — ")[0]);
                LocalDate date = datePicker.getValue();
                LocalTime time = LocalTime.parse(timeField.getText().trim());

                AppointmentService.BookingResult result = appointmentService.bookAppointment(
                        patient.getPatientId(), deptId, date, time);

                if (result.isSuccess()) {
                    String confirmMsg = "Appointment confirmed for " + date + " at " + time;
                    if (result.getQueueNumber() != null) {
                        confirmMsg += ". Queue number: " + result.getQueueNumber();
                    }
                    notificationService.sendSMS(patient.getPatientId(), confirmMsg);
                    resultLabel.setStyle(
                            StyleManager.label(StyleManager.SUCCESS, 13, false));
                    resultLabel.setText(confirmMsg);
                } else {
                    resultLabel.setStyle(
                            StyleManager.label(StyleManager.DANGER, 13, false));
                    resultLabel.setText(result.getMessage());
                }
            } catch (Exception ex) {
                resultLabel.setStyle(
                        StyleManager.label(StyleManager.DANGER, 13, false));
                resultLabel.setText("Invalid time format. Use HH:MM e.g. 09:30");
            }
        });

        content.getChildren().addAll(
                heading, new Separator(),
                fieldLabel("Department"), deptBox,
                fieldLabel("Date"), datePicker,
                fieldLabel("Time (07:00 — 17:00)"), timeField,
                resultLabel, bookBtn
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        tab.setContent(scroll);
        return tab;
    }

    // --- My Appointments Tab ---

    private Tab buildMyAppointmentsTab() {
        Tab tab = new Tab("My Appointments");

        VBox content = new VBox(14);
        content.setPadding(new Insets(24));

        Text heading = new Text("My Appointments");
        heading.setStyle(StyleManager.label(StyleManager.TEXT_PRIMARY, 18, true));

        TableView<Appointment> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Appointment, Integer> idCol =
                new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));

        TableColumn<Appointment, Integer> deptCol =
                new TableColumn<>("Dept ID");
        deptCol.setCellValueFactory(new PropertyValueFactory<>("departmentId"));

        TableColumn<Appointment, String> dateCol =
                new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("appointmentDate"));

        TableColumn<Appointment, String> timeCol =
                new TableColumn<>("Time");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("appointmentTime"));

        TableColumn<Appointment, String> statusCol =
                new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(idCol, deptCol, dateCol, timeCol, statusCol);

        List<Appointment> appointments =
                appointmentService.getMyAppointments(patient.getPatientId());
        table.setItems(FXCollections.observableArrayList(appointments));
        table.setPlaceholder(new Label("No appointments found."));

        // Cancel button
        Label cancelLabel = new Label();
        TextField cancelField = new TextField();
        cancelField.setPromptText("Enter appointment ID to cancel");
        cancelField.setStyle(StyleManager.textField());
        cancelField.setMaxWidth(250);

        Button cancelBtn = new Button("Cancel Appointment");
        cancelBtn.setStyle(StyleManager.dangerButton());

        cancelBtn.setOnAction(e -> {
            try {
                int id = Integer.parseInt(cancelField.getText().trim());
                boolean ok = appointmentService.cancelAppointment(id);
                if (ok) {
                    cancelLabel.setStyle(
                            StyleManager.label(StyleManager.SUCCESS, 13, false));
                    cancelLabel.setText("Appointment " + id + " cancelled.");
                    table.setItems(FXCollections.observableArrayList(
                            appointmentService.getMyAppointments(
                                    patient.getPatientId())));
                } else {
                    cancelLabel.setStyle(
                            StyleManager.label(StyleManager.DANGER, 13, false));
                    cancelLabel.setText("Could not cancel. Check the ID.");
                }
            } catch (NumberFormatException ex) {
                cancelLabel.setStyle(
                        StyleManager.label(StyleManager.DANGER, 13, false));
                cancelLabel.setText("Please enter a valid appointment ID.");
            }
        });

        HBox cancelRow = new HBox(10, cancelField, cancelBtn);
        cancelRow.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(
                heading, new Separator(),
                table, cancelLabel, cancelRow
        );

        tab.setContent(content);
        return tab;
    }

    // --- Join Queue Tab ---

    private Tab buildQueueTab() {
        Tab tab = new Tab("Join Queue");

        VBox content = new VBox(14);
        content.setPadding(new Insets(24));
        content.setMaxWidth(480);

        Text heading = new Text("Join Walk-in Queue");
        heading.setStyle(StyleManager.label(StyleManager.TEXT_PRIMARY, 18, true));

        List<Department> depts = departmentDAO.getAllDepartments();
        ComboBox<String> deptBox = new ComboBox<>();
        for (Department d : depts) {
            deptBox.getItems().add(d.getDepartmentId() + " — " + d.getName());
        }
        deptBox.setPromptText("Select department");
        deptBox.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll(
                "5 — Normal",
                "2 — Elderly",
                "3 — Pregnant",
                "4 — Disabled",
                "1 — Emergency"
        );
        priorityBox.setPromptText("Select priority");
        priorityBox.setMaxWidth(Double.MAX_VALUE);
        priorityBox.setValue("5 — Normal");

        Label resultLabel = new Label();
        resultLabel.setWrapText(true);

        Button joinBtn = new Button("Join Queue");
        joinBtn.setStyle(StyleManager.successButton());
        joinBtn.setMaxWidth(Double.MAX_VALUE);

        joinBtn.setOnAction(e -> {
            if (deptBox.getValue() == null || priorityBox.getValue() == null) {
                resultLabel.setStyle(
                        StyleManager.label(StyleManager.DANGER, 13, false));
                resultLabel.setText("Please select a department and priority.");
                return;
            }

            int deptId     = Integer.parseInt(deptBox.getValue().split(" — ")[0]);
            int priorityId = Integer.parseInt(priorityBox.getValue().split(" — ")[0]);

            boolean ok = queueService.joinQueue(
                    patient.getPatientId(), deptId, priorityId);

            if (ok) {
                resultLabel.setStyle(
                        StyleManager.label(StyleManager.SUCCESS, 13, false));
                resultLabel.setText(
                        "You have joined the queue. Check the queue display for your number.");
            } else {
                resultLabel.setStyle(
                        StyleManager.label(StyleManager.DANGER, 13, false));
                resultLabel.setText(
                        "Could not join queue. Department may be full.");
            }
        });

        content.getChildren().addAll(
                heading, new Separator(),
                fieldLabel("Department"), deptBox,
                fieldLabel("Priority Category"), priorityBox,
                resultLabel, joinBtn
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        tab.setContent(scroll);
        return tab;
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle(StyleManager.label(StyleManager.TEXT_SECONDARY, 12, false));
        return l;
    }
}
