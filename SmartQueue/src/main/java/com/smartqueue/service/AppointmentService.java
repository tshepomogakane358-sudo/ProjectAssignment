package com.smartqueue.service;

import com.smartqueue.dao.AppointmentDAO;
import com.smartqueue.dao.DepartmentDAO;
import com.smartqueue.dao.QueueDAO;
import com.smartqueue.model.Appointment;
import com.smartqueue.model.Department;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Business logic for appointment booking.
 */
public class AppointmentService {

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final DepartmentDAO  departmentDAO  = new DepartmentDAO();
    private final QueueDAO       queueDAO       = new QueueDAO();

    /**
     * Books an appointment and automatically joins the queue.
     * Returns a BookingResult with queue number.
     */
    public BookingResult bookAppointment(int patientId, int departmentId,
                                         LocalDate date, LocalTime time) {
        // Check department exists
        Department dept = departmentDAO.getDepartmentById(departmentId);
        if (dept == null) {
            return BookingResult.fail("Department not found.");
        }

        // Date must not be in the past
        if (date.isBefore(LocalDate.now())) {
            return BookingResult.fail("Cannot book an appointment in the past.");
        }

        // Time must be within clinic hours 07:00 - 17:00
        LocalTime openTime  = LocalTime.of(7, 0);
        LocalTime closeTime = LocalTime.of(17, 0);
        if (time.isBefore(openTime) || time.isAfter(closeTime)) {
            return BookingResult.fail("Appointments must be between 07:00 and 17:00.");
        }

        // Book the appointment
        Appointment appointment = new Appointment(patientId, departmentId, date, time);
        boolean booked = appointmentDAO.bookAppointment(appointment);
        if (!booked) {
            return BookingResult.fail("That time slot may already be taken.");
        }

        // Automatically join the queue with Normal priority (5)
        String prefix      = dept.getName().substring(0, Math.min(3, dept.getName().length())).toUpperCase();
        String queueNumber = queueDAO.generateQueueNumber(prefix, departmentId);

        // Add to queue
        com.smartqueue.model.QueueEntry entry = new com.smartqueue.model.QueueEntry(
                patientId, departmentId, 5, queueNumber, date
        );
        int todayCount = departmentDAO.getTodayPatientCount(departmentId);
        entry.setEstimatedWaitMin(todayCount * 10);
        queueDAO.addToQueue(entry);

        System.out.println("Appointment booked. Queue number: " + queueNumber);
        return BookingResult.success(queueNumber);
    }

    /**
     * Returns all appointments for a patient.
     */
    public List<Appointment> getMyAppointments(int patientId) {
        return appointmentDAO.getAppointmentsByPatient(patientId);
    }

    /**
     * Returns today's appointments across all patients.
     */
    public List<Appointment> getTodayAppointments() {
        return appointmentDAO.getTodayAppointments();
    }

    /**
     * Returns all appointments across all patients.
     */
    public List<Appointment> getAllAppointments() {
        return appointmentDAO.getAllAppointments();
    }

    /**
     * Cancels an appointment.
     */
    public boolean cancelAppointment(int appointmentId) {
        return appointmentDAO.cancelAppointment(appointmentId);
    }

    /**
     * Updates the status of an appointment.
     */
    public boolean updateAppointmentStatus(int appointmentId, String status) {
        return appointmentDAO.updateAppointmentStatus(appointmentId, status);
    }

    // ── Result classes ────────────────────────────────────

    public static class BookingResult {
        private final boolean success;
        private final String  message;
        private final String  queueNumber;

        private BookingResult(boolean success, String message, String queueNumber) {
            this.success     = success;
            this.message     = message;
            this.queueNumber = queueNumber;
        }

        public static BookingResult success(String queueNumber) {
            return new BookingResult(true, "Appointment booked successfully.", queueNumber);
        }

        public static BookingResult fail(String message) {
            return new BookingResult(false, message, null);
        }

        public boolean isSuccess()    { return success; }
        public String  getMessage()   { return message; }
        public String  getQueueNumber() { return queueNumber; }
    }
}