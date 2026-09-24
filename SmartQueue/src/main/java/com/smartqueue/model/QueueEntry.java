package com.smartqueue.model;

import java.time.LocalDate;

public class QueueEntry {

    private int       queueId;
    private int       patientId;
    private int       departmentId;
    private int       priorityId;
    private String    queueNumber;
    private String    status;
    private int       estimatedWaitMin;
    private LocalDate queueDate;

    public QueueEntry() {}

    public QueueEntry(int patientId, int departmentId, int priorityId,
                      String queueNumber, LocalDate queueDate) {
        this.patientId    = patientId;
        this.departmentId = departmentId;
        this.priorityId   = priorityId;
        this.queueNumber  = queueNumber;
        this.status       = "Waiting";
        this.queueDate    = queueDate;
    }

    public int       getQueueId()         { return queueId; }
    public int       getPatientId()       { return patientId; }
    public int       getDepartmentId()    { return departmentId; }
    public int       getPriorityId()      { return priorityId; }
    public String    getQueueNumber()     { return queueNumber; }
    public String    getStatus()          { return status; }
    public int       getEstimatedWaitMin(){ return estimatedWaitMin; }
    public LocalDate getQueueDate()       { return queueDate; }

    public void setQueueId(int queueId)               { this.queueId          = queueId; }
    public void setPatientId(int patientId)           { this.patientId        = patientId; }
    public void setDepartmentId(int departmentId)     { this.departmentId     = departmentId; }
    public void setPriorityId(int priorityId)         { this.priorityId       = priorityId; }
    public void setQueueNumber(String queueNumber)    { this.queueNumber      = queueNumber; }
    public void setStatus(String status)              { this.status           = status; }
    public void setEstimatedWaitMin(int mins)         { this.estimatedWaitMin = mins; }
    public void setQueueDate(LocalDate queueDate)     { this.queueDate        = queueDate; }

    @Override
    public String toString() {
        return "Queue [#" + queueNumber + " | Patient=" + patientId +
                " | Dept=" + departmentId + " | Status=" + status +
                " | Wait=" + estimatedWaitMin + " mins]";
    }
}
