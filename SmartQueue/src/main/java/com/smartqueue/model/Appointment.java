package com.smartqueue.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Appointment {

    private int       appointmentId;
    private int       patientId;
    private int       departmentId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String    status;

    public Appointment() {}

    public Appointment(int patientId, int departmentId,
                       LocalDate appointmentDate, LocalTime appointmentTime) {
        this.patientId       = patientId;
        this.departmentId    = departmentId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status          = "Pending";
    }

    public int       getAppointmentId()   { return appointmentId; }
    public int       getPatientId()       { return patientId; }
    public int       getDepartmentId()    { return departmentId; }
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public LocalTime getAppointmentTime() { return appointmentTime; }
    public String    getStatus()          { return status; }

    public void setAppointmentId(int appointmentId)       { this.appointmentId   = appointmentId; }
    public void setPatientId(int patientId)               { this.patientId       = patientId; }
    public void setDepartmentId(int departmentId)         { this.departmentId    = departmentId; }
    public void setAppointmentDate(LocalDate date)        { this.appointmentDate = date; }
    public void setAppointmentTime(LocalTime time)        { this.appointmentTime = time; }
    public void setStatus(String status)                  { this.status          = status; }

    @Override
    public String toString() {
        return "Appointment [ID=" + appointmentId + " | Patient=" + patientId +
                " | Dept=" + departmentId + " | Date=" + appointmentDate +
                " | Time=" + appointmentTime + " | Status=" + status + "]";
    }


}
