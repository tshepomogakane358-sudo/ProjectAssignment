package com.smartqueue.fx;

import com.smartqueue.service.PatientService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class RegisterScreen {
    private final Stage          stage;
    private final PatientService patientService = new PatientService();

    public RegisterScreen(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        stage.setTitle("SmartQueue — Register");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + StyleManager.BACKGROUND + ";");

        VBox card = new VBox(14);
        card.setStyle(StyleManager.card());
        card.setMaxWidth(420);
        card.setAlignment(Pos.CENTER_LEFT);

        Text title = new Text("Create Account");
        title.setStyle(StyleManager.label(StyleManager.PRIMARY, 24, true));

        TextField nameField = new TextField();
        nameField.setPromptText("Full name");
        nameField.setStyle(StyleManager.textField());
        nameField.setMaxWidth(Double.MAX_VALUE);

        TextField contactField = new TextField();
        contactField.setPromptText("Contact number (10 digits)");
        contactField.setStyle(StyleManager.textField());
        contactField.setMaxWidth(Double.MAX_VALUE);

        TextField emailField = new TextField();
        emailField.setPromptText("Email address");
        emailField.setStyle(StyleManager.textField());
        emailField.setMaxWidth(Double.MAX_VALUE);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password (min 6 characters)");
        passwordField.setStyle(StyleManager.textField());
        passwordField.setMaxWidth(Double.MAX_VALUE);

        Label messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(Double.MAX_VALUE);

        Button registerBtn = new Button("Create Account");
        registerBtn.setStyle(StyleManager.primaryButton());
        registerBtn.setMaxWidth(Double.MAX_VALUE);

        registerBtn.setOnAction(e -> {
            String name     = nameField.getText().trim();
            String contact  = contactField.getText().trim();
            String email    = emailField.getText().trim();
            String password = passwordField.getText().trim();

            boolean success = patientService.register(name, contact, email, password);

            if (success) {
                messageLabel.setStyle(
                        StyleManager.label(StyleManager.SUCCESS, 13, false));
                messageLabel.setText(
                        "Account created! You can now log in.");
            } else {
                messageLabel.setStyle(
                        StyleManager.label(StyleManager.DANGER, 13, false));
                messageLabel.setText(
                        "Registration failed. Check your details and try again.");
            }
        });

        Hyperlink backLink = new Hyperlink("Already have an account? Login");
        backLink.setStyle("-fx-text-fill: " + StyleManager.PRIMARY + ";");
        backLink.setOnAction(e -> new LoginScreen(stage).show());

        card.getChildren().addAll(
                title,
                new Separator(),
                label("Full Name"), nameField,
                label("Contact Number"), contactField,
                label("Email"), emailField,
                label("Password"), passwordField,
                messageLabel, registerBtn, backLink
        );

        BorderPane.setAlignment(card, Pos.CENTER);
        BorderPane.setMargin(card, new Insets(40));
        root.setCenter(card);

        stage.setScene(new Scene(root, 500, 600));
        stage.show();
    }

    private Label label(String text) {
        Label l = new Label(text);
        l.setStyle(StyleManager.label(StyleManager.TEXT_SECONDARY, 12, false));
        return l;
    }
}
