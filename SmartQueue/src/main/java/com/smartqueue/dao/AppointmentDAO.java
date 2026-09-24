package com.smartqueue.dao;

import com.smartqueue.db.DatabaseConnection;
import com.smartqueue.model.Appointment;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    /**
     * Books a new appointment.
     *
     * The appointment cannot:
     * - use a time slot that is already booked
     * - have a date before today
     */
    public boolean bookAppointment(Appointment appointment) {

        // Check if the time slot is already taken
        if (isDoubleBooked(appointment)) {
            System.out.println("Error: That time slot is already taken.");
            return false;
        }

        // Check that appointment date is today or in the future
        if (appointment.getAppointmentDate()
                .isBefore(LocalDate.now())) {

            System.out.println(
                    "Error: Appointment date must be today or in the future."
            );

            return false;
        }

        String sql = "INSERT INTO Appointment " +
                "(patient_id, department_id, appointment_date, appointment_time, status) " +
                "VALUES (?, ?, ?, ?, 'Pending')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, appointment.getPatientId());
            stmt.setInt(2, appointment.getDepartmentId());

            stmt.setDate(
                    3,
                    Date.valueOf(appointment.getAppointmentDate())
            );

            stmt.setTime(
                    4,
                    Time.valueOf(appointment.getAppointmentTime())
            );

            stmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            System.out.println(
                    "Error booking appointment: " + e.getMessage()
            );

            return false;
        }
    }


    /**
     * Checks whether the selected appointment date and time
     * are already booked for the department.
     *
     * Cancelled appointments do not count as double bookings.
     */
    private boolean isDoubleBooked(Appointment appointment) {

        String sql = "SELECT COUNT(*) FROM Appointment " +
                "WHERE department_id = ? " +
                "AND appointment_date = ? " +
                "AND appointment_time = ? " +
                "AND status != 'Cancelled'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(
                    1,
                    appointment.getDepartmentId()
            );

            stmt.setDate(
                    2,
                    Date.valueOf(appointment.getAppointmentDate())
            );

            stmt.setTime(
                    3,
                    Time.valueOf(appointment.getAppointmentTime())
            );

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error checking booking: " + e.getMessage()
            );
        }

        return false;
    }


    /**
     * Returns all appointments belonging to a specific patient.
     */
    public List<Appointment> getAppointmentsByPatient(int patientId) {

        List<Appointment> list = new ArrayList<>();

        String sql = "SELECT * FROM Appointment " +
                "WHERE patient_id = ? " +
                "ORDER BY appointment_date, appointment_time";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    Appointment appointment =
                            createAppointmentFromResultSet(rs);

                    list.add(appointment);
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error fetching appointments: " + e.getMessage()
            );
        }

        return list;
    }


    /**
     * Returns all appointments across all patients.
     *
     * Used by staff/admin views.
     *
     * Most recent appointment dates are displayed first.
     */
    public List<Appointment> getAllAppointments() {

        List<Appointment> list = new ArrayList<>();

        String sql = "SELECT * FROM Appointment " +
                "ORDER BY appointment_date DESC, appointment_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                Appointment appointment =
                        createAppointmentFromResultSet(rs);

                list.add(appointment);
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error fetching all appointments: " + e.getMessage()
            );
        }

        return list;
    }


    /**
     * Returns only today's appointments.
     */
    public List<Appointment> getTodayAppointments() {

        List<Appointment> list = new ArrayList<>();

        String sql = "SELECT * FROM Appointment " +
                "WHERE appointment_date = CURRENT_DATE " +
                "ORDER BY appointment_time ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                Appointment appointment =
                        createAppointmentFromResultSet(rs);

                list.add(appointment);
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error fetching today's appointments: "
                            + e.getMessage()
            );
        }

        return list;
    }


    /**
     * Cancels an appointment.
     */
    public boolean cancelAppointment(int appointmentId) {

        String sql = "UPDATE Appointment " +
                "SET status = 'Cancelled' " +
                "WHERE appointment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, appointmentId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(
                    "Error cancelling appointment: "
                            + e.getMessage()
            );

            return false;
        }
    }


    /**
     * Updates the status of a specific appointment.
     *
     * Examples:
     * Pending
     * Confirmed
     * Done
     * Cancelled
     */
    public boolean updateAppointmentStatus(
            int appointmentId,
            String status) {

        String sql = "UPDATE Appointment " +
                "SET status = ? " +
                "WHERE appointment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, appointmentId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(
                    "Error updating appointment status: "
                            + e.getMessage()
            );

            return false;
        }
    }


    /**
     * Converts a ResultSet row into an Appointment object.
     *
     * This prevents duplicate code in the different
     * appointment retrieval methods.
     */
    private Appointment createAppointmentFromResultSet(
            ResultSet rs) throws SQLException {

        Appointment appointment = new Appointment();

        appointment.setAppointmentId(
                rs.getInt("appointment_id")
        );

        appointment.setPatientId(
                rs.getInt("patient_id")
        );

        appointment.setDepartmentId(
                rs.getInt("department_id")
        );

        Date appointmentDate =
                rs.getDate("appointment_date");

        if (appointmentDate != null) {

            appointment.setAppointmentDate(
                    appointmentDate.toLocalDate()
            );
        }

        Time appointmentTime =
                rs.getTime("appointment_time");

        if (appointmentTime != null) {

            appointment.setAppointmentTime(
                    appointmentTime.toLocalTime()
            );
        }

        appointment.setStatus(
                rs.getString("status")
        );

        return appointment;
    }
}