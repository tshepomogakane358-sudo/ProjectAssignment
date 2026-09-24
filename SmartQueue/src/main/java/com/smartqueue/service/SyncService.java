package com.smartqueue.service;

import com.smartqueue.db.DatabaseConnection;
import com.smartqueue.db.OfflineDatabaseManager;

import java.sql.*;

/**
 * Syncs locally cached offline data to MySQL when connection is restored.
 */
public class SyncService {

    public void syncAll() {
        if (!OfflineDatabaseManager.isMySQLAvailable()) {
            System.out.println("MySQL not available. Sync skipped.");
            return;
        }
        System.out.println("MySQL detected. Starting sync...");
        try { syncPatients();     } catch (Exception e) { System.out.println("Patients sync skipped: " + e.getMessage()); }
        try { syncQueues();       } catch (Exception e) { System.out.println("Queue sync skipped: "    + e.getMessage()); }
        try { syncAppointments(); } catch (Exception e) { System.out.println("Appt sync skipped: "    + e.getMessage()); }
        System.out.println("Sync complete.");
    }

    private void syncPatients() {
        String selectSQL = "SELECT * FROM OfflinePatient WHERE synced = FALSE";
        String insertSQL = "INSERT IGNORE INTO Patient (name, contact, email, password_hash) VALUES (?, ?, ?, ?)";
        String updateSQL = "UPDATE OfflinePatient SET synced = TRUE WHERE patient_id = ?";

        try {
            Connection h2    = OfflineDatabaseManager.getOfflineConnection();
            Connection mysql = DatabaseConnection.getConnection();

            PreparedStatement sel = h2.prepareStatement(selectSQL);
            ResultSet rs = sel.executeQuery();

            int count = 0;
            while (rs.next()) {
                try {
                    PreparedStatement ins = mysql.prepareStatement(insertSQL);
                    ins.setString(1, rs.getString("name"));
                    ins.setString(2, rs.getString("contact"));
                    ins.setString(3, rs.getString("email"));
                    ins.setString(4, rs.getString("password_hash"));
                    ins.executeUpdate();
                    ins.close();

                    PreparedStatement upd = h2.prepareStatement(updateSQL);
                    upd.setInt(1, rs.getInt("patient_id"));
                    upd.executeUpdate();
                    upd.close();
                    count++;
                } catch (SQLException inner) {
                    System.out.println("Row sync error: " + inner.getMessage());
                }
            }
            rs.close();
            sel.close();
            if (count > 0) System.out.println("Synced " + count + " patient(s).");

        } catch (SQLException e) {
            System.out.println("Error syncing patients: " + e.getMessage());
        }
    }

    private void syncQueues() {
        String selectSQL = "SELECT * FROM OfflineQueue WHERE synced = FALSE";
        String insertSQL = "INSERT IGNORE INTO Queue (patient_id, department_id, priority_id, queue_number, status, queue_date) VALUES (?, ?, ?, ?, ?, ?)";
        String updateSQL = "UPDATE OfflineQueue SET synced = TRUE WHERE queue_id = ?";

        try {
            Connection h2    = OfflineDatabaseManager.getOfflineConnection();
            Connection mysql = DatabaseConnection.getConnection();

            PreparedStatement sel = h2.prepareStatement(selectSQL);
            ResultSet rs = sel.executeQuery();

            int count = 0;
            while (rs.next()) {
                try {
                    PreparedStatement ins = mysql.prepareStatement(insertSQL);
                    ins.setInt(1,    rs.getInt("patient_id"));
                    ins.setInt(2,    rs.getInt("department_id"));
                    ins.setInt(3,    rs.getInt("priority_id"));
                    ins.setString(4, rs.getString("queue_number"));
                    ins.setString(5, rs.getString("status"));
                    ins.setDate(6,   rs.getDate("queue_date"));
                    ins.executeUpdate();
                    ins.close();

                    PreparedStatement upd = h2.prepareStatement(updateSQL);
                    upd.setInt(1, rs.getInt("queue_id"));
                    upd.executeUpdate();
                    upd.close();
                    count++;
                } catch (SQLException inner) {
                    System.out.println("Row sync error: " + inner.getMessage());
                }
            }
            rs.close();
            sel.close();
            if (count > 0) System.out.println("Synced " + count + " queue entry/entries.");

        } catch (SQLException e) {
            System.out.println("Error syncing queue: " + e.getMessage());
        }
    }

    private void syncAppointments() {
        String selectSQL = "SELECT * FROM OfflineAppointment WHERE synced = FALSE";
        String insertSQL = "INSERT IGNORE INTO Appointment (patient_id, department_id, appointment_date, appointment_time, status) VALUES (?, ?, ?, ?, ?)";
        String updateSQL = "UPDATE OfflineAppointment SET synced = TRUE WHERE appointment_id = ?";

        try {
            Connection h2    = OfflineDatabaseManager.getOfflineConnection();
            Connection mysql = DatabaseConnection.getConnection();

            PreparedStatement sel = h2.prepareStatement(selectSQL);
            ResultSet rs = sel.executeQuery();

            int count = 0;
            while (rs.next()) {
                try {
                    PreparedStatement ins = mysql.prepareStatement(insertSQL);
                    ins.setInt(1,    rs.getInt("patient_id"));
                    ins.setInt(2,    rs.getInt("department_id"));
                    ins.setDate(3,   rs.getDate("appointment_date"));
                    ins.setTime(4,   rs.getTime("appointment_time"));
                    ins.setString(5, rs.getString("status"));
                    ins.executeUpdate();
                    ins.close();

                    PreparedStatement upd = h2.prepareStatement(updateSQL);
                    upd.setInt(1, rs.getInt("appointment_id"));
                    upd.executeUpdate();
                    upd.close();
                    count++;
                } catch (SQLException inner) {
                    System.out.println("Row sync error: " + inner.getMessage());
                }
            }
            rs.close();
            sel.close();
            if (count > 0) System.out.println("Synced " + count + " appointment(s).");

        } catch (SQLException e) {
            System.out.println("Error syncing appointments: " + e.getMessage());
        }
    }
}