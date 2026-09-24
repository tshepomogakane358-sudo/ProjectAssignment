package com.smartqueue.ui;

import com.smartqueue.dao.DepartmentDAO;
import com.smartqueue.dao.PatientDAO;
import com.smartqueue.model.Department;
import com.smartqueue.model.Patient;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final Scanner       scanner;
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final PatientDAO    patientDAO    = new PatientDAO();

    public AdminMenu(Scanner scanner) {
        this.scanner = scanner;
    }


    public void show() {
        System.out.println("\n--- Admin Login ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        // Placeholder — replace with AdminDAO lookup later
        if (!username.equals("admin") || !password.equals("admin123")) {
            System.out.println("Invalid admin credentials.");
            return;
        }

        showDashboard();
    }


    private void showDashboard() {
        boolean running = true;

        while (running) {
            System.out.println("\n╔══════════════════════════════════╗");
            System.out.println("║        ADMIN DASHBOARD           ║");
            System.out.println("╠══════════════════════════════════╣");
            System.out.println("║  1. View All Patients            ║");
            System.out.println("║  2. View All Departments         ║");
            System.out.println("║  3. Update Department Limit      ║");
            System.out.println("║  0. Back to Main Menu            ║");
            System.out.println("╚══════════════════════════════════╝");
            System.out.print("Select option: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> viewAllPatients();
                case "2" -> viewAllDepartments();
                case "3" -> updateDepartmentLimit();
                case "0" -> running = false;
                default  -> System.out.println("Invalid option.");
            }
        }
    }


    private void viewAllPatients() {
        List<Patient> patients = patientDAO.getAllPatients();
        if (patients.isEmpty()) {
            System.out.println("No patients registered.");
            return;
        }

        System.out.printf("%n%-5s %-25s %-15s %-25s%n",
                "ID", "Name", "Contact", "Email");
        System.out.println("-".repeat(70));

        for (Patient p : patients) {
            System.out.printf("%-5d %-25s %-15s %-25s%n",
                    p.getPatientId(), p.getName(),
                    p.getContact(), p.getEmail());
        }
    }


    private void viewAllDepartments() {
        List<Department> depts = departmentDAO.getAllDepartments();
        System.out.printf("%n%-5s %-20s %-12s%n", "ID", "Name", "Daily Limit");
        System.out.println("-".repeat(38));
        for (Department d : depts) {
            System.out.printf("%-5d %-20s %-12d%n",
                    d.getDepartmentId(), d.getName(), d.getDailyLimit());
        }
    }


    private void updateDepartmentLimit() {
        viewAllDepartments();
        System.out.print("\nEnter department ID : ");
        int deptId = parseIntInput(scanner.nextLine().trim());
        if (deptId == -1) return;

        System.out.print("Enter new daily limit: ");
        int newLimit = parseIntInput(scanner.nextLine().trim());
        if (newLimit == -1) return;

        boolean success = departmentDAO.updateDailyLimit(deptId, newLimit);
        if (success) System.out.println("Daily limit updated successfully.");
    }

    private int parseIntInput(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Error: Please enter a valid number.");
            return -1;
        }
    }
}
