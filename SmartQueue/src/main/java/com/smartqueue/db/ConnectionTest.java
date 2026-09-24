package com.smartqueue.db;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionTest {
    public static void main(String[] args) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                System.out.println("SUCCESS - SmartQueue is connected to MySQL!");
            }
        } catch (SQLException e) {
            System.out.println("FAILED - Check your password and MySQL server.");
            System.out.println("Error: " + e.getMessage());
        } finally {
            DatabaseConnection.closeConnection();
        }
    }

}
