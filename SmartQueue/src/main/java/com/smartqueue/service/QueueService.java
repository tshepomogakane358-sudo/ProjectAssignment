package com.smartqueue.service;

import com.smartqueue.dao.DepartmentDAO;
import com.smartqueue.dao.QueueDAO;
import com.smartqueue.model.Department;
import com.smartqueue.model.QueueEntry;

import java.time.LocalDate;
import java.util.List;


public class QueueService {


    private final QueueDAO       queueDAO      = new QueueDAO();
    private final DepartmentDAO  departmentDAO = new DepartmentDAO();


    private static final int AVG_WAIT_PER_PATIENT = 10;


    public boolean joinQueue(int patientId, int departmentId, int priorityId) {


        Department dept = departmentDAO.getDepartmentById(departmentId);
        if (dept == null) {
            System.out.println("Error: Department not found.");
            return false;
        }


        int todayCount = departmentDAO.getTodayPatientCount(departmentId);
        if (todayCount >= dept.getDailyLimit()) {
            System.out.println("Error: Department has reached its daily patient limit.");
            return false;
        }


        String prefix = dept.getName().substring(0, 3).toUpperCase();
        String queueNumber = queueDAO.generateQueueNumber(prefix, departmentId);


        int estimatedWait = todayCount * AVG_WAIT_PER_PATIENT;


        QueueEntry entry = new QueueEntry(
                patientId, departmentId, priorityId, queueNumber, LocalDate.now()
        );
        entry.setEstimatedWaitMin(estimatedWait);

        boolean success = queueDAO.addToQueue(entry);
        if (success) {
            System.out.println("Success! Your queue number is: " + queueNumber);
            System.out.println("Estimated wait time: " + estimatedWait + " minutes");
        }
        return success;
    }


    public QueueEntry callNext(int departmentId) {
        QueueEntry next = queueDAO.callNextPatient(departmentId);
        if (next == null) {
            System.out.println("No patients waiting in this department.");
        } else {
            System.out.println("Now calling: Queue #" + next.getQueueNumber());
        }
        return next;
    }


    public List<QueueEntry> viewQueue(int departmentId) {
        return queueDAO.getTodayQueue(departmentId);
    }


    public boolean markDone(int queueId) {
        return queueDAO.updateQueueStatus(queueId, "Done");
    }


    public boolean markNoShow(int queueId) {
        return queueDAO.updateQueueStatus(queueId, "No-show");
    }
}
