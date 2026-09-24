package com.smartqueue.ui;

import com.smartqueue.dao.DepartmentDAO;
import com.smartqueue.model.Department;
import com.smartqueue.model.QueueEntry;
import com.smartqueue.service.QueueService;

import java.util.List;
import java.util.Scanner;

public class QueueDisplayMenu {
    private final Scanner       scanner;
    private final QueueService  queueService  = new QueueService();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();

    public QueueDisplayMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void show() {
        List<Department> depts = departmentDAO.getAllDepartments();

        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║         LIVE QUEUE DISPLAY               ║");
        System.out.println("╚══════════════════════════════════════════╝");

        for (Department dept : depts) {
            List<QueueEntry> queue = queueService.viewQueue(dept.getDepartmentId());

            System.out.println("\n  Department: " + dept.getName());
            System.out.println("  " + "-".repeat(44));

            if (queue.isEmpty()) {
                System.out.println("  No patients currently waiting.");
                continue;
            }

            System.out.printf("  %-10s %-12s %-10s %-12s%n",
                    "Queue #", "Wait (mins)", "Priority", "Status");

            for (QueueEntry e : queue) {
                System.out.printf("  %-10s %-12d %-10d %-12s%n",
                        e.getQueueNumber(),
                        e.getEstimatedWaitMin(),
                        e.getPriorityId(),
                        e.getStatus());
            }
        }

        System.out.println("\nPress Enter to return...");
        scanner.nextLine();
    }
}
