package com.smartqueue.model;

public class Department {
    private int    departmentId;
    private String name;
    private int    dailyLimit;

    public Department() {}

    public Department(String name, int dailyLimit) {
        this.name       = name;
        this.dailyLimit = dailyLimit;
    }

    public int    getDepartmentId() { return departmentId; }
    public String getName()         { return name; }
    public int    getDailyLimit()   { return dailyLimit; }

    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }
    public void setName(String name)               { this.name         = name; }
    public void setDailyLimit(int dailyLimit)      { this.dailyLimit   = dailyLimit; }

    @Override
    public String toString() {
        return "Department [ID=" + departmentId + ", Name=" + name + ", Daily Limit=" + dailyLimit + "]";
    }
}
