package com.smartqueue.fx;

import com.smartqueue.service.SyncService;
import javafx.application.Application;
import javafx.stage.Stage;


public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        new SyncService().syncAll();
        new LoginScreen(primaryStage).show();
    }
}