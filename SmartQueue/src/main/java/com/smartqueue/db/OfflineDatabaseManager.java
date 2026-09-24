package com.smartqueue.db;

import java.sql.*;

public class OfflineDatabaseManager {

    private static final String H2_URL  = "jdbc:h2:~/smartqueue_offline;AUTO_SERVER=TRUE";
    private static final String H2_USER = "sa";
    private static final String H2_PASS = "";

    private static Connection offlineConnection = null;


    public static Connection getOfflineConnection() throws SQLException {
        if (offlineConnection == null || offlineConnection.isClosed()) {
            offlineConnection = DriverManager.getConnection(H2_URL, H2_USER, H2_PASS);
            initOfflineTables();
        }
        return offlineConnection;
    }


    private static void initOfflineTables() {
        String createPatient =
                "CREATE TABLE IF NOT EXISTS OfflinePatient (" +
                        "  patient_id    INT AUTO_INCREMENT PRIMARY KEY," +
                        "  name          VARCHAR(100) NOT NULL," +
                        "  contact       VARCHAR(20)  NOT NULL," +
                        "  email         VARCHAR(100) NOT NULL," +
                        "  password_hash VARCHAR(255) NOT NULL," +
                        "  synced        BOOLEAN DEFAULT FALSE" +
                        ")";

        String createQueue =
                "CREATE TABLE IF NOT EXISTS OfflineQueue (" +
                        "  queue_id      INT AUTO_INCREMENT PRIMARY KEY," +
                        "  patient_id    INT NOT NULL," +
                        "  department_id INT NOT NULL," +
                        "  priority_id   INT NOT NULL," +
                        "  queue_number  VARCHAR(20) NOT NULL," +
                        "  queue_date    DATE NOT NULL," +
                        "  status        VARCHAR(20) DEFAULT 'Waiting'," +
                        "  synced        BOOLEAN DEFAULT FALSE" +
                        ")";

        String createAppointment =
                "CREATE TABLE IF NOT EXISTS OfflineAppointment (" +
                        "  appointment_id   INT AUTO_INCREMENT PRIMARY KEY," +
                        "  patient_id       INT NOT NULL," +
                        "  department_id    INT NOT NULL," +
                        "  appointment_date DATE NOT NULL," +
                        "  appointment_time TIME NOT NULL," +
                        "  status           VARCHAR(20) DEFAULT 'Pending'," +
                        "  synced           BOOLEAN DEFAULT FALSE" +
                        ")";

        try (Connection conn = getOfflineConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createPatient);
            stmt.execute(createQueue);
            stmt.execute(createAppointment);
            System.out.println("Offline tables ready.");
        } catch (SQLException e) {
            System.out.println("Error initialising offline tables: " + e.getMessage());
        }
    }


    public static boolean isMySQLAvailable() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }


    public static void closeOfflineConnection() {
        try {
            if (offlineConnection != null && !offlineConnection.isClosed()) {
                offlineConnection.close();
            }
        } catch (SQLException e) {
            System.out.println("Error closing offline connection: " + e.getMessage());
        }
    }
}
