package org.example;

import com.smartqueue.service.ApiServer;
import com.smartqueue.service.SyncService;


public class Main {
    public static void main(String[] args) {
        System.out.println("Starting SmartQueue...");
        new SyncService().syncAll();
        new ApiServer().start();
        System.out.println("Open index.html in your browser to use the system.");
    }
}