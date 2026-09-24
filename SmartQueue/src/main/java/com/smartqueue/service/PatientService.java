package com.smartqueue.service;

import com.smartqueue.dao.PatientDAO;
import com.smartqueue.model.Patient;
import com.smartqueue.util.PasswordUtil;


public class PatientService {

    private final PatientDAO patientDAO = new PatientDAO();


    public boolean register(String name, String contact, String email, String password) {


        if (name == null || name.trim().isEmpty()) {
            System.out.println("Error: Name cannot be empty.");
            return false;
        }


        if (!contact.matches("\\d{10}")) {
            System.out.println("Error: Contact must be a 10-digit phone number.");
            return false;
        }

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            System.out.println("Error: Invalid email format.");
            return false;
        }


        if (password.length() < 6) {
            System.out.println("Error: Password must be at least 6 characters.");
            return false;
        }


        if (patientDAO.getPatientByEmail(email) != null) {
            System.out.println("Error: Email already registered.");
            return false;
        }


        String hashedPassword = PasswordUtil.hashPassword(password);
        Patient patient = new Patient(name.trim(), contact, email.toLowerCase(), hashedPassword);
        return patientDAO.registerPatient(patient);
    }


    public Patient login(String email, String password) {
        if (email == null || password == null) {
            System.out.println("Error: Email and password are required.");
            return null;
        }

        Patient patient = patientDAO.getPatientByEmail(email.toLowerCase());

        if (patient == null) {
            System.out.println("Error: No account found with that email.");
            return null;
        }

        if (!PasswordUtil.verifyPassword(password, patient.getPasswordHash())) {
            System.out.println("Error: Incorrect password.");
            return null;
        }

        return patient;
    }


    public Patient getPatientById(int patientId) {
        return patientDAO.getPatientById(patientId);
    }


    /**
     * Result of a profile update attempt.
     */
    public static class UpdateResult {
        private final boolean success;
        private final String  message;
        private final Patient patient;

        public UpdateResult(boolean success, String message, Patient patient) {
            this.success = success;
            this.message = message;
            this.patient = patient;
        }

        public boolean isSuccess()    { return success; }
        public String  getMessage()   { return message; }
        public Patient getPatient()   { return patient; }
    }

    /**
     * Updates a patient's name, contact, and email, reusing the same
     * validation rules as registration. Password is not changed here.
     */
    public UpdateResult updateProfile(int patientId, String name, String contact, String email) {

        if (name == null || name.trim().isEmpty()) {
            return new UpdateResult(false, "Name cannot be empty.", null);
        }

        if (contact == null || !contact.matches("\\d{10}")) {
            return new UpdateResult(false, "Contact must be a 10-digit phone number.", null);
        }

        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            return new UpdateResult(false, "Invalid email format.", null);
        }

        String normalizedEmail = email.toLowerCase();

        // If the email changed, make sure it's not already used by another patient
        Patient existing = patientDAO.getPatientByEmail(normalizedEmail);
        if (existing != null && existing.getPatientId() != patientId) {
            return new UpdateResult(false, "Email already in use by another account.", null);
        }

        boolean ok = patientDAO.updatePatient(patientId, name.trim(), contact, normalizedEmail);
        if (!ok) {
            return new UpdateResult(false, "Could not update profile.", null);
        }

        Patient updated = patientDAO.getPatientById(patientId);
        return new UpdateResult(true, "Profile updated successfully.", updated);
    }


    /**
     * Result of a password change attempt. No patient data is returned
     * here since the password itself is never sent back to the client.
     */
    public static class PasswordChangeResult {
        private final boolean success;
        private final String  message;

        public PasswordChangeResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess()  { return success; }
        public String  getMessage() { return message; }
    }

    /**
     * Changes a patient's password after verifying their current password.
     */
    public PasswordChangeResult changePassword(int patientId, String currentPassword, String newPassword) {

        Patient patient = patientDAO.getPatientById(patientId);
        if (patient == null) {
            return new PasswordChangeResult(false, "Patient not found.");
        }

        if (currentPassword == null || !PasswordUtil.verifyPassword(currentPassword, patient.getPasswordHash())) {
            return new PasswordChangeResult(false, "Current password is incorrect.");
        }

        if (newPassword == null || newPassword.length() < 6) {
            return new PasswordChangeResult(false, "New password must be at least 6 characters.");
        }

        if (currentPassword.equals(newPassword)) {
            return new PasswordChangeResult(false, "New password must be different from the current password.");
        }

        String newHash = PasswordUtil.hashPassword(newPassword);
        boolean ok = patientDAO.updatePassword(patientId, newHash);

        if (!ok) {
            return new PasswordChangeResult(false, "Could not update password.");
        }
        return new PasswordChangeResult(true, "Password updated successfully.");
    }
}