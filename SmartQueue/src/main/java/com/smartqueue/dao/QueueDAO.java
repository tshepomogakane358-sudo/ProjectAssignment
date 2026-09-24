package com.smartqueue.dao;

import com.smartqueue.db.DatabaseConnection;
import com.smartqueue.model.QueueEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QueueDAO {

    /**
     * Adds a patient to the queue.
     */
    public boolean addToQueue(QueueEntry entry) {

        String sql = "INSERT INTO Queue " +
                "(patient_id, department_id, priority_id, queue_number, status, estimated_wait_min, queue_date) " +
                "VALUES (?, ?, ?, ?, 'Waiting', ?, CURRENT_DATE)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, entry.getPatientId());
            stmt.setInt(2, entry.getDepartmentId());
            stmt.setInt(3, entry.getPriorityId());
            stmt.setString(4, entry.getQueueNumber());
            stmt.setInt(5, entry.getEstimatedWaitMin());

            stmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            System.out.println("Error adding to queue: " + e.getMessage());
            return false;
        }
    }


    /**
     * Gets all waiting and called patients for today
     * in a specific department.
     */
    public List<QueueEntry> getTodayQueue(int departmentId) {

        List<QueueEntry> queue = new ArrayList<>();

        String sql = "SELECT q.* FROM Queue q " +
                "JOIN Priority p ON q.priority_id = p.priority_id " +
                "WHERE q.department_id = ? " +
                "AND q.queue_date = CURRENT_DATE " +
                "AND q.status IN ('Waiting', 'Called') " +
                "ORDER BY p.rank_order ASC, q.created_at ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, departmentId);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    QueueEntry entry = new QueueEntry();

                    entry.setQueueId(rs.getInt("queue_id"));
                    entry.setPatientId(rs.getInt("patient_id"));
                    entry.setDepartmentId(rs.getInt("department_id"));
                    entry.setPriorityId(rs.getInt("priority_id"));
                    entry.setQueueNumber(rs.getString("queue_number"));
                    entry.setStatus(rs.getString("status"));
                    entry.setEstimatedWaitMin(
                            rs.getInt("estimated_wait_min")
                    );

                    Date queueDate = rs.getDate("queue_date");

                    if (queueDate != null) {
                        entry.setQueueDate(queueDate.toLocalDate());
                    }

                    queue.add(entry);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching queue: " + e.getMessage());
        }

        return queue;
    }


    /**
     * Calls the next patient in the queue.
     *
     * Priority is determined by rank_order.
     * If two patients have the same priority,
     * the patient who joined first is called first.
     */
    public QueueEntry callNextPatient(int departmentId) {

        String sql = "SELECT q.* FROM Queue q " +
                "JOIN Priority p ON q.priority_id = p.priority_id " +
                "WHERE q.department_id = ? " +
                "AND q.queue_date = CURRENT_DATE " +
                "AND q.status = 'Waiting' " +
                "ORDER BY p.rank_order ASC, q.created_at ASC " +
                "LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, departmentId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    QueueEntry next = new QueueEntry();

                    next.setQueueId(rs.getInt("queue_id"));
                    next.setPatientId(rs.getInt("patient_id"));
                    next.setDepartmentId(rs.getInt("department_id"));
                    next.setPriorityId(rs.getInt("priority_id"));
                    next.setQueueNumber(rs.getString("queue_number"));
                    next.setStatus("Called");
                    next.setEstimatedWaitMin(
                            rs.getInt("estimated_wait_min")
                    );

                    Date queueDate = rs.getDate("queue_date");

                    if (queueDate != null) {
                        next.setQueueDate(queueDate.toLocalDate());
                    }

                    // Update the patient's status to Called
                    updateQueueStatus(next.getQueueId(), "Called");

                    return next;
                }
            }

        } catch (SQLException e) {
            System.out.println("Error calling next patient: " + e.getMessage());
        }

        return null;
    }


    /**
     * Updates the status of a queue entry.
     *
     * Examples:
     * Waiting
     * Called
     * Done
     * No-show
     */
    public boolean updateQueueStatus(int queueId, String newStatus) {

        String sql = "UPDATE Queue SET status = ? WHERE queue_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, queueId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating queue status: " + e.getMessage());
            return false;
        }
    }


    /**
     * Generates a queue number for a department.
     *
     * Example:
     * OPD-001
     * OPD-002
     * PHARM-001
     */
    public String generateQueueNumber(String deptPrefix, int departmentId) {

        String sql = "SELECT COUNT(*) FROM Queue " +
                "WHERE department_id = ? " +
                "AND queue_date = CURRENT_DATE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, departmentId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    int count = rs.getInt(1) + 1;

                    return deptPrefix + "-" +
                            String.format("%03d", count);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error generating queue number: " + e.getMessage());
        }

        return deptPrefix + "-001";
    }


    /**
     * Counts patients served today in a department.
     */
    public int countServedToday(int departmentId) {

        String sql = "SELECT COUNT(*) FROM Queue " +
                "WHERE department_id = ? " +
                "AND queue_date = CURRENT_DATE " +
                "AND status = 'Done'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, departmentId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error counting served patients: " + e.getMessage());
        }

        return 0;
    }


    /**
     * Counts no-show patients today in a department.
     */
    public int countNoShowToday(int departmentId) {

        String sql = "SELECT COUNT(*) FROM Queue " +
                "WHERE department_id = ? " +
                "AND queue_date = CURRENT_DATE " +
                "AND status = 'No-show'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, departmentId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error counting no-shows: " + e.getMessage());
        }

        return 0;
    }
}