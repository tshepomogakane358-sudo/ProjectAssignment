package com.smartqueue.fx;


import com.smartqueue.model.Patient;
import com.smartqueue.service.PatientService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class LoginScreen {

    private final Stage          stage;
    private final PatientService patientService = new PatientService();

    public LoginScreen(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        stage.setTitle("SmartQueue — Login");


        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + StyleManager.BACKGROUND + ";");


        VBox card = new VBox(16);
        card.setStyle(StyleManager.card());
        card.setMaxWidth(400);
        card.setAlignment(Pos.CENTER);

        Text title = new Text("SmartQueue");
        title.setStyle(StyleManager.label(StyleManager.PRIMARY, 28, true));

        Text subtitle = new Text("Clinic Queue Management System");
        subtitle.setStyle(StyleManager.label(StyleManager.TEXT_SECONDARY, 13, false));


        TextField emailField = new TextField();
        emailField.setPromptText("Email address");
        emailField.setStyle(StyleManager.textField());
        emailField.setMaxWidth(Double.MAX_VALUE);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle(StyleManager.textField());
        passwordField.setMaxWidth(Double.MAX_VALUE);


        Label errorLabel = new Label();
        errorLabel.setStyle(StyleManager.label(StyleManager.DANGER, 13, false));
        errorLabel.setWrapText(true);

        // Login button
        Button loginBtn = new Button("Login");
        loginBtn.setStyle(StyleManager.primaryButton());
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        loginBtn.setOnAction(e -> {
            String email    = emailField.getText().trim();
            String password = passwordField.getText().trim();

            if (email.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please enter your email and password.");
                return;
            }

            Patient patient = patientService.login(email, password);
            if (patient == null) {
                errorLabel.setText("Invalid email or password.");
            } else {
                new PatientDashboard(stage, patient).show();
            }
        });


        Hyperlink registerLink = new Hyperlink("Don't have an account? Register here");
        registerLink.setStyle("-fx-text-fill: " + StyleManager.PRIMARY + ";");
        registerLink.setOnAction(e -> new RegisterScreen(stage).show());


        HBox staffRow = new HBox(16);
        staffRow.setAlignment(Pos.CENTER);
        Hyperlink staffLink = new Hyperlink("Staff Login");
        Hyperlink adminLink = new Hyperlink("Admin Login");
        staffLink.setStyle("-fx-text-fill: " + StyleManager.TEXT_SECONDARY + ";");
        adminLink.setStyle("-fx-text-fill: " + StyleManager.TEXT_SECONDARY + ";");
        staffLink.setOnAction(e -> new StaffDashboard(stage).show());
        adminLink.setOnAction(e -> new AdminDashboard(stage).show());
        staffRow.getChildren().addAll(staffLink, adminLink);


        Separator sep = new Separator();

        card.getChildren().addAll(
                title, subtitle, sep,
                emailField, passwordField,
                errorLabel, loginBtn,
                registerLink, staffRow
        );

        BorderPane.setAlignment(card, Pos.CENTER);
        BorderPane.setMargin(card, new Insets(60));
        root.setCenter(card);

        stage.setScene(new Scene(root, 500, 560));
        stage.setResizable(false);
        stage.show();
    }
}
