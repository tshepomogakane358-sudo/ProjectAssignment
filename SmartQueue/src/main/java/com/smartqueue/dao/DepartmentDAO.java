package com.smartqueue.dao;


import com.smartqueue.db.DatabaseConnection;
import com.smartqueue.model.Department;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class DepartmentDAO {

    public List<Department> getAllDepartments() {
        List<Department> departments = new ArrayList<>();
        String sql = "SELECT * FROM Department ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Department d = new Department();
                d.setDepartmentId(rs.getInt("department_id"));
                d.setName(rs.getString("name"));
                d.setDailyLimit(rs.getInt("daily_limit"));
                departments.add(d);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching departments: " + e.getMessage());
        }
        return departments;
    }


    public Department getDepartmentById(int departmentId) {
        String sql = "SELECT * FROM Department WHERE department_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, departmentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Department d = new Department();
                d.setDepartmentId(rs.getInt("department_id"));
                d.setName(rs.getString("name"));
                d.setDailyLimit(rs.getInt("daily_limit"));
                return d;
            }

        } catch (SQLException e) {
            System.out.println("Error finding department: " + e.getMessage());
        }
        return null;
    }


    public int getTodayPatientCount(int departmentId) {
        String sql = "SELECT COUNT(*) FROM Queue WHERE department_id = ? AND queue_date = CURRENT_DATE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, departmentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.out.println("Error counting patients: " + e.getMessage());
        }
        return 0;
    }


    public boolean updateDailyLimit(int departmentId, int newLimit) {
        String sql = "UPDATE Department SET daily_limit = ? WHERE department_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newLimit);
            stmt.setInt(2, departmentId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating limit: " + e.getMessage());
            return false;
        }
    }
}
