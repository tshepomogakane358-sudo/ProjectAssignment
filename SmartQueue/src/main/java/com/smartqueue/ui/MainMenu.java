package com.smartqueue.ui;

import java.util.Scanner;

public class MainMenu {


    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            System.out.println("\n╔══════════════════════════════════╗");
            System.out.println("║       SMARTQUEUE SYSTEM          ║");
            System.out.println("╠══════════════════════════════════╣");
            System.out.println("║  1. Patient Login                ║");
            System.out.println("║  2. Register New Patient         ║");
            System.out.println("║  3. Staff Login                  ║");
            System.out.println("║  4. Admin Login                  ║");
            System.out.println("║  5. View Queue Display           ║");
            System.out.println("║  0. Exit                         ║");
            System.out.println("╚══════════════════════════════════╝");
            System.out.print("Select option: ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> new PatientMenu(scanner).show();
                case "2" -> new PatientMenu(scanner).register();
                case "3" -> new StaffMenu(scanner).show();
                case "4" -> new AdminMenu(scanner).show();
                case "5" -> new QueueDisplayMenu(scanner).show();
                case "0" -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }
}
