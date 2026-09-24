package com.smartqueue.service;

import com.smartqueue.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class NotificationService {
    public void sendSMS(int patientId, String message) {
        System.out.println("------ SMS NOTIFICATION ------");
        System.out.println("To Patient ID : " + patientId);
        System.out.println("Message       : " + message);
        System.out.println("------------------------------");
        saveNotification(patientId, "SMS", message);
    }


    public void sendSystemNotification(int patientId, String message) {
        System.out.println("[SYSTEM] Patient " + patientId + ": " + message);
        saveNotification(patientId, "System", message);
    }


    private void saveNotification(int patientId, String type, String message) {
        String sql = "INSERT INTO Notification (patient_id, type, message, status) " +
                "VALUES (?, ?, ?, 'Sent')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            stmt.setString(2, type);
            stmt.setString(3, message);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error saving notification: " + e.getMessage());
        }
    }
}
