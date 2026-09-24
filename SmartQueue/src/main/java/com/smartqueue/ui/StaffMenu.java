package com.smartqueue.ui;

import com.smartqueue.dao.DepartmentDAO;
import com.smartqueue.model.Department;
import com.smartqueue.model.QueueEntry;
import com.smartqueue.service.QueueService;
import com.smartqueue.service.NotificationService;

import java.util.List;
import java.util.Scanner;

public class StaffMenu {

    private final Scanner             scanner;
    private final QueueService        queueService        = new QueueService();
    private final NotificationService notificationService = new NotificationService();
    private final DepartmentDAO       departmentDAO       = new DepartmentDAO();

    public StaffMenu(Scanner scanner) {
        this.scanner = scanner;
    }


    public void show() {
        System.out.println("\n--- Staff Login ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        // Placeholder check — replace with StaffDAO lookup later
        if (!username.equals("staff") || !password.equals("staff123")) {
            System.out.println("Invalid staff credentials.");
            return;
        }

        showDashboard();
    }

    private void showDashboard() {
        boolean running = true;

        while (running) {
            System.out.println("\n╔══════════════════════════════════╗");
            System.out.println("║        STAFF DASHBOARD           ║");
            System.out.println("╠══════════════════════════════════╣");
            System.out.println("║  1. View Department Queue        ║");
            System.out.println("║  2. Call Next Patient            ║");
            System.out.println("║  3. Mark Patient as Done         ║");
            System.out.println("║  4. Mark Patient as No-show      ║");
            System.out.println("║  0. Back to Main Menu            ║");
            System.out.println("╚══════════════════════════════════╝");
            System.out.print("Select option: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> viewQueue();
                case "2" -> callNext();
                case "3" -> markDone();
                case "4" -> markNoShow();
                case "0" -> running = false;
                default  -> System.out.println("Invalid option.");
            }
        }
    }


    private void viewQueue() {
        int deptId = selectDepartment();
        if (deptId == -1) return;

        List<QueueEntry> queue = queueService.viewQueue(deptId);
        if (queue.isEmpty()) {
            System.out.println("No patients in queue.");
            return;
        }

        System.out.printf("%n%-10s %-12s %-10s %-15s%n",
                "Queue #", "Patient ID", "Priority", "Status");
        System.out.println("-".repeat(48));

        for (QueueEntry e : queue) {
            System.out.printf("%-10s %-12d %-10d %-15s%n",
                    e.getQueueNumber(),
                    e.getPatientId(),
                    e.getPriorityId(),
                    e.getStatus());
        }
    }


    private void callNext() {
        int deptId = selectDepartment();
        if (deptId == -1) return;

        QueueEntry next = queueService.callNext(deptId);
        if (next != null) {
            notificationService.sendSystemNotification(
                    next.getPatientId(),
                    "Queue #" + next.getQueueNumber() + " — please proceed to the consultation room."
            );
        }
    }


    private void markDone() {
        System.out.print("Enter queue ID to mark as done: ");
        int queueId = parseIntInput(scanner.nextLine().trim());
        if (queueId == -1) return;
        queueService.markDone(queueId);
        System.out.println("Marked as done.");
    }


    private void markNoShow() {
        System.out.print("Enter queue ID to mark as no-show: ");
        int queueId = parseIntInput(scanner.nextLine().trim());
        if (queueId == -1) return;
        queueService.markNoShow(queueId);
        System.out.println("Marked as no-show.");
    }


    private int selectDepartment() {
        List<Department> depts = departmentDAO.getAllDepartments();
        System.out.println("\nDepartments:");
        for (Department d : depts) {
            System.out.println("  " + d.getDepartmentId() + ". " + d.getName());
        }
        System.out.print("Select department ID: ");
        return parseIntInput(scanner.nextLine().trim());
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
