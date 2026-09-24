package com.smartqueue.ui;

import com.smartqueue.model.Appointment;
import com.smartqueue.model.Department;
import com.smartqueue.model.Patient;
import com.smartqueue.service.AppointmentService;
import com.smartqueue.service.NotificationService;
import com.smartqueue.service.PatientService;
import com.smartqueue.service.QueueService;
import com.smartqueue.dao.DepartmentDAO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class PatientMenu {
    private final Scanner             scanner;
    private final PatientService      patientService      = new PatientService();
    private final AppointmentService  appointmentService  = new AppointmentService();
    private final QueueService        queueService        = new QueueService();
    private final NotificationService notificationService = new NotificationService();
    private final DepartmentDAO       departmentDAO       = new DepartmentDAO();

    public PatientMenu(Scanner scanner) {
        this.scanner = scanner;
    }


    public void show() {
        System.out.println("\n--- Patient Login ---");
        System.out.print("Email   : ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        Patient patient = patientService.login(email, password);
        if (patient == null) return;

        System.out.println("\nWelcome, " + patient.getName() + "!");
        showDashboard(patient);
    }


    public void register() {
        System.out.println("\n--- Register New Patient ---");
        System.out.print("Full Name     : ");
        String name = scanner.nextLine().trim();
        System.out.print("Contact (10 digits): ");
        String contact = scanner.nextLine().trim();
        System.out.print("Email         : ");
        String email = scanner.nextLine().trim();
        System.out.print("Password      : ");
        String password = scanner.nextLine().trim();

        boolean success = patientService.register(name, contact, email, password);
        if (success) {
            System.out.println("Registration successful! You can now log in.");
        }
    }


    private void showDashboard(Patient patient) {
        boolean running = true;

        while (running) {
            System.out.println("\n╔══════════════════════════════════╗");
            System.out.println("║       PATIENT DASHBOARD          ║");
            System.out.println("╠══════════════════════════════════╣");
            System.out.println("║  1. Book Appointment             ║");
            System.out.println("║  2. View My Appointments         ║");
            System.out.println("║  3. Cancel Appointment           ║");
            System.out.println("║  4. Join Queue (Walk-in)         ║");
            System.out.println("║  0. Back to Main Menu            ║");
            System.out.println("╚══════════════════════════════════╝");
            System.out.print("Select option: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> bookAppointment(patient);
                case "2" -> viewAppointments(patient);
                case "3" -> cancelAppointment(patient);
                case "4" -> joinQueue(patient);
                case "0" -> running = false;
                default  -> System.out.println("Invalid option.");
            }
        }
    }

    private void bookAppointment(Patient patient) {
        System.out.println("\n--- Book Appointment ---");
        showDepartments();

        System.out.print("Select department ID: ");
        int deptId = parseIntInput(scanner.nextLine().trim());
        if (deptId == -1) return;

        System.out.print("Date (YYYY-MM-DD)   : ");
        LocalDate date = parseDateInput(scanner.nextLine().trim());
        if (date == null) return;

        System.out.print("Time (HH:MM)        : ");
        LocalTime time = parseTimeInput(scanner.nextLine().trim());
        if (time == null) return;

        AppointmentService.BookingResult result = appointmentService.bookAppointment(
                patient.getPatientId(), deptId, date, time);

        if (result.isSuccess()) {
            String msg = "Your appointment is confirmed for " + date + " at " + time;
            if (result.getQueueNumber() != null) {
                msg += ". Your queue number is " + result.getQueueNumber() + ".";
            }
            notificationService.sendSMS(patient.getPatientId(), msg);
        } else {
            System.out.println("Booking failed: " + result.getMessage());
        }
    }


    private void viewAppointments(Patient patient) {
        System.out.println("\n--- My Appointments ---");
        List<Appointment> appointments =
                appointmentService.getMyAppointments(patient.getPatientId());

        if (appointments.isEmpty()) {
            System.out.println("No appointments found.");
            return;
        }

        System.out.printf("%-5s %-15s %-12s %-10s %-12s%n",
                "ID", "Department", "Date", "Time", "Status");
        System.out.println("-".repeat(55));

        for (Appointment a : appointments) {
            System.out.printf("%-5d %-15d %-12s %-10s %-12s%n",
                    a.getAppointmentId(),
                    a.getDepartmentId(),
                    a.getAppointmentDate(),
                    a.getAppointmentTime(),
                    a.getStatus());
        }
    }


    private void cancelAppointment(Patient patient) {
        viewAppointments(patient);
        System.out.print("\nEnter appointment ID to cancel: ");
        int id = parseIntInput(scanner.nextLine().trim());
        if (id == -1) return;
        appointmentService.cancelAppointment(id);
    }

    private void joinQueue(Patient patient) {
        System.out.println("\n--- Join Queue ---");
        showDepartments();

        System.out.print("Select department ID : ");
        int deptId = parseIntInput(scanner.nextLine().trim());
        if (deptId == -1) return;

        System.out.println("Priority categories:");
        System.out.println("  1. Normal");
        System.out.println("  2. Elderly");
        System.out.println("  3. Pregnant");
        System.out.println("  4. Disabled");
        System.out.println("  5. Emergency");
        System.out.print("Select priority: ");
        int priorityId = parseIntInput(scanner.nextLine().trim());
        if (priorityId == -1) return;

        queueService.joinQueue(patient.getPatientId(), deptId, priorityId);
    }


    private void showDepartments() {
        List<Department> depts = departmentDAO.getAllDepartments();
        System.out.println("\nAvailable Departments:");
        System.out.printf("%-5s %-20s %-12s%n", "ID", "Name", "Daily Limit");
        System.out.println("-".repeat(38));
        for (Department d : depts) {
            System.out.printf("%-5d %-20s %-12d%n",
                    d.getDepartmentId(), d.getName(), d.getDailyLimit());
        }
    }



    private int parseIntInput(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Error: Please enter a valid number.");
            return -1;
        }
    }

    private LocalDate parseDateInput(String input) {
        try {
            return LocalDate.parse(input);
        } catch (DateTimeParseException e) {
            System.out.println("Error: Use format YYYY-MM-DD (e.g. 2025-12-01).");
            return null;
        }
    }

    private LocalTime parseTimeInput(String input) {
        try {
            return LocalTime.parse(input);
        } catch (DateTimeParseException e) {
            System.out.println("Error: Use format HH:MM (e.g. 09:30).");
            return null;
        }
    }
}