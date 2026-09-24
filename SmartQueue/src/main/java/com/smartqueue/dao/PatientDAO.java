package com.smartqueue.dao;

import com.smartqueue.db.DatabaseConnection;
import com.smartqueue.model.Patient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    public boolean registerPatient(Patient patient) {
        String sql = "INSERT INTO Patient (name, contact, email, password_hash) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patient.getName());
            stmt.setString(2, patient.getContact());
            stmt.setString(3, patient.getEmail());
            stmt.setString(4, patient.getPasswordHash());
            stmt.executeUpdate();
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Error: Email already registered.");
            return false;
        } catch (SQLException e) {
            System.out.println("Error registering patient: " + e.getMessage());
            return false;
        }
    }


    public Patient getPatientByEmail(String email) {
        String sql = "SELECT * FROM Patient WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Patient p = new Patient();
                p.setPatientId(rs.getInt("patient_id"));
                p.setName(rs.getString("name"));
                p.setContact(rs.getString("contact"));
                p.setEmail(rs.getString("email"));
                p.setPasswordHash(rs.getString("password_hash"));
                return p;
            }

        } catch (SQLException e) {
            System.out.println("Error finding patient: " + e.getMessage());
        }
        return null;
    }


    public Patient getPatientById(int patientId) {
        String sql = "SELECT * FROM Patient WHERE patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Patient p = new Patient();
                p.setPatientId(rs.getInt("patient_id"));
                p.setName(rs.getString("name"));
                p.setContact(rs.getString("contact"));
                p.setEmail(rs.getString("email"));
                p.setPasswordHash(rs.getString("password_hash"));
                return p;
            }

        } catch (SQLException e) {
            System.out.println("Error finding patient: " + e.getMessage());
        }
        return null;
    }


    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM Patient ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Patient p = new Patient();
                p.setPatientId(rs.getInt("patient_id"));
                p.setName(rs.getString("name"));
                p.setContact(rs.getString("contact"));
                p.setEmail(rs.getString("email"));
                p.setPasswordHash(rs.getString("password_hash"));
                patients.add(p);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching patients: " + e.getMessage());
        }
        return patients;
    }


    /**
     * Updates a patient's name, contact, and email (not password).
     * Returns false if the new email is already used by another patient,
     * or if the patient doesn't exist.
     */
    public boolean updatePatient(int patientId, String name, String contact, String email) {
        String sql = "UPDATE Patient SET name = ?, contact = ?, email = ? WHERE patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, contact);
            stmt.setString(3, email);
            stmt.setInt(4, patientId);
            return stmt.executeUpdate() > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Error: Email already in use by another patient.");
            return false;
        } catch (SQLException e) {
            System.out.println("Error updating patient: " + e.getMessage());
            return false;
        }
    }


    /**
     * Updates a patient's password hash.
     */
    public boolean updatePassword(int patientId, String newPasswordHash) {
        String sql = "UPDATE Patient SET password_hash = ? WHERE patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPasswordHash);
            stmt.setInt(2, patientId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating password: " + e.getMessage());
            return false;
        }
    }
}