package com.smartqueue.service;

import com.smartqueue.dao.DepartmentDAO;
import com.smartqueue.dao.PatientDAO;
import com.smartqueue.model.Appointment;
import com.smartqueue.model.Department;
import com.smartqueue.model.Patient;
import com.smartqueue.model.QueueEntry;
import io.javalin.Javalin;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * REST API server for the SmartQueue web frontend.
 * Runs on http://localhost:7070
 */
public class ApiServer {

    private final PatientService     patientService     = new PatientService();
    private final AppointmentService appointmentService = new AppointmentService();
    private final QueueService       queueService       = new QueueService();
    private final DepartmentDAO      departmentDAO      = new DepartmentDAO();
    private final PatientDAO         patientDAO         = new PatientDAO();
    private final com.smartqueue.dao.QueueDAO queueDAO = new com.smartqueue.dao.QueueDAO();

    public void start() {
        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(rule -> {
                    rule.anyHost();
                    rule.allowCredentials = false;
                });
            });
            config.staticFiles.add("web",
                    io.javalin.http.staticfiles.Location.EXTERNAL);
        }).start(7070);

        System.out.println("SmartQueue API running at http://localhost:7070");

        // ═══════════════════════════════════════════════════
        // PATIENT ROUTES
        // ═══════════════════════════════════════════════════

        // ── REGISTER (THIS WAS MISSING) ──────────────────────
        app.post("/api/register", ctx -> {
            try {
                Map body        = ctx.bodyAsClass(Map.class);
                String name     = String.valueOf(body.get("name"));
                String contact  = String.valueOf(body.get("contact"));
                String email    = String.valueOf(body.get("email"));
                String password = String.valueOf(body.get("password"));

                if (name.equals("null") || contact.equals("null") ||
                        email.equals("null") || password.equals("null") ||
                        name.isEmpty() || contact.isEmpty() ||
                        email.isEmpty() || password.isEmpty()) {
                    ctx.status(400).result("{\"error\":\"All fields are required\"}");
                    return;
                }

                boolean ok = patientService.register(name, contact, email, password);
                if (ok) {
                    ctx.status(201).result("{\"message\":\"Registered successfully\"}");
                } else {
                    ctx.status(400).result(
                            "{\"error\":\"Registration failed. Email may already be registered.\"}");
                }
            } catch (Exception e) {
                System.out.println("Register error: " + e.getMessage());
                ctx.status(500).result("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        // ── LOGIN ────────────────────────────────────────────
        app.post("/api/login", ctx -> {
            try {
                Map body        = ctx.bodyAsClass(Map.class);
                String email    = String.valueOf(body.get("email"));
                String password = String.valueOf(body.get("password"));

                Patient patient = patientService.login(email, password);
                if (patient != null) {
                    String json = "{"
                            + "\"patientId\":"  + patient.getPatientId() + ","
                            + "\"name\":\""     + patient.getName()      + "\","
                            + "\"contact\":\"" + patient.getContact()   + "\","
                            + "\"email\":\""    + patient.getEmail()     + "\""
                            + "}";
                    ctx.status(200).result(json);
                } else {
                    ctx.status(401).result("{\"error\":\"Invalid email or password\"}");
                }
            } catch (Exception e) {
                ctx.status(500).result("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        // ── GET ALL PATIENTS ─────────────────────────────────
        app.get("/api/patients", ctx -> {
            try {
                List<Patient> patients = patientDAO.getAllPatients();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < patients.size(); i++) {
                    Patient p = patients.get(i);
                    sb.append("{")
                            .append("\"patientId\":").append(p.getPatientId()).append(",")
                            .append("\"name\":\"").append(escape(p.getName())).append("\",")
                            .append("\"contact\":\"").append(escape(p.getContact())).append("\",")
                            .append("\"email\":\"").append(escape(p.getEmail())).append("\"")
                            .append("}");
                    if (i < patients.size() - 1) sb.append(",");
                }
                sb.append("]");
                ctx.contentType("application/json").result(sb.toString());
            } catch (Exception e) {
                ctx.status(500).result("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        // ── UPDATE PATIENT PROFILE ───────────────────────────
        app.put("/api/patients/{id}", ctx -> {
            try {
                int patientId  = Integer.parseInt(ctx.pathParam("id"));
                Map body       = ctx.bodyAsClass(Map.class);
                String name    = String.valueOf(body.get("name"));
                String contact = String.valueOf(body.get("contact"));
                String email   = String.valueOf(body.get("email"));

                PatientService.UpdateResult result =
                        patientService.updateProfile(patientId, name, contact, email);

                if (result.isSuccess()) {
                    Patient p = result.getPatient();
                    String json = "{"
                            + "\"patientId\":"  + p.getPatientId()  + ","
                            + "\"name\":\""     + escape(p.getName())    + "\","
                            + "\"contact\":\"" + escape(p.getContact()) + "\","
                            + "\"email\":\""    + escape(p.getEmail())   + "\""
                            + "}";
                    ctx.status(200).result(json);
                } else {
                    ctx.status(400).result("{\"error\":\"" + result.getMessage() + "\"}");
                }
            } catch (Exception e) {
                ctx.status(500).result("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        // ── CHANGE PASSWORD ──────────────────────────────────
        app.put("/api/patients/{id}/password", ctx -> {
            try {
                int patientId      = Integer.parseInt(ctx.pathParam("id"));
                Map body           = ctx.bodyAsClass(Map.class);
                String currentPass = String.valueOf(body.get("currentPassword"));
                String newPass     = String.valueOf(body.get("newPassword"));

                PatientService.PasswordChangeResult result =
                        patientService.changePassword(patientId, currentPass, newPass);

                if (result.isSuccess()) {
                    ctx.status(200).result("{\"message\":\"" + result.getMessage() + "\"}");
                } else {
                    ctx.status(400).result("{\"error\":\"" + result.getMessage() + "\"}");
                }
            } catch (Exception e) {
                ctx.status(500).result("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        // ═══════════════════════════════════════════════════
        // DEPARTMENT ROUTES
        // ═══════════════════════════════════════════════════

        app.get("/api/departments", ctx -> {
            try {
                List<Department> depts = departmentDAO.getAllDepartments();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < depts.size(); i++) {
                    Department d = depts.get(i);
                    sb.append("{")
                            .append("\"departmentId\":").append(d.getDepartmentId()).append(",")
                            .append("\"name\":\"").append(escape(d.getName())).append("\",")
                            .append("\"dailyLimit\":").append(d.getDailyLimit())
                            .append("}");
                    if (i < depts.size() - 1) sb.append(",");
                }
                sb.append("]");
                ctx.contentType("application/json").result(sb.toString());
            } catch (Exception e) {
                ctx.status(500).result("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        app.put("/api/departments/{id}/limit", ctx -> {
            try {
                int deptId = Integer.parseInt(ctx.pathParam("id"));
                Map body   = ctx.bodyAsClass(Map.class);
                int limit  = Integer.parseInt(String.valueOf(body.get("limit")));
                boolean ok = departmentDAO.updateDailyLimit(deptId, limit);
                if (ok) {
                    ctx.result("{\"message\":\"Limit updated\"}");
                } else {
                    ctx.status(400).result("{\"error\":\"Update failed\"}");
                }
            } catch (Exception e) {
                ctx.status(500).result("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        // ═══════════════════════════════════════════════════
        // STATS ROUTE
        // ═══════════════════════════════════════════════════

        app.get("/api/stats/today", ctx -> {
            try {
                List<Department> depts = departmentDAO.getAllDepartments();
                int waiting = 0;
                int served  = 0;

                for (Department dept : depts) {
                    List<QueueEntry> todayQueue =
                            queueDAO.getTodayQueue(dept.getDepartmentId());
                    waiting += (int) todayQueue.stream()
                            .filter(q -> "Waiting".equals(q.getStatus())).count();
                    served  += queueDAO.countServedToday(dept.getDepartmentId());
                }

                String json = "{"
                        + "\"patientsToday\":" + (waiting + served) + ","
                        + "\"waiting\":"       + waiting             + ","
                        + "\"served\":"        + served
                        + "}";
                ctx.contentType("application/json").result(json);
            } catch (Exception e) {
                ctx.status(500).result("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        // ═══════════════════════════════════════════════════
        // APPOINTMENT ROUTES
        // ═══════════════════════════════════════════════════

        // ── BOOK APPOINTMENT ─────────────────────────────────
        app.post("/api/appointments", ctx -> {
            try {
                Map body         = ctx.bodyAsClass(Map.class);
                int patientId    = Integer.parseInt(String.valueOf(body.get("patientId")));
                int departmentId = Integer.parseInt(String.valueOf(body.get("departmentId")));
                LocalDate date   = LocalDate.parse(String.valueOf(body.get("date")));
                LocalTime time   = LocalTime.parse(String.valueOf(body.get("time")));

                AppointmentService.BookingResult result =
                        appointmentService.bookAppointment(patientId, departmentId, date, time);

                if (result.isSuccess()) {
                    String queueNum = result.getQueueNumber() != null
                            ? "\"" + result.getQueueNumber() + "\"" : "null";
                    ctx.status(201).result("{"
                            + "\"message\":\"Appointment booked\","
                            + "\"queueNumber\":" + queueNum
                            + "}");
                } else {
                    ctx.status(400).result("{\"error\":\"" + result.getMessage() + "\"}");
                }
            } catch (Exception e) {
                ctx.status(500).result("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        // ── GET APPOINTMENTS FOR A PATIENT ───────────────────
        app.get("/api/appointments/{patientId}", ctx -> {
            try {
                int patientId         = Integer.parseInt(ctx.pathParam("patientId"));
                List<Appointment> list =
                        appointmentService.getMyAppointments(patientId);
                ctx.contentType("application/json")
                        .result(appointmentsToJson(list));
            } catch (Exception e) {
                ctx.status(500).result("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        // ── GET ALL APPOINTMENTS (staff/admin) ───────────────
        app.get("/api/appointments", ctx -> {
            try {
                String scope = ctx.queryParam("scope");
                List<Appointment> list = "today".equals(scope)
                        ? appointmentService.getTodayAppointments()
                        : appointmentService.getAllAppointments();
                ctx.contentType("application/json")
                        .result(appointmentsToJson(list));
            } catch (Exception e) {
                ctx.status(500).result("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        // ── CANCEL APPOINTMENT ───────────────────────────────
        app.delete("/api/appointments/{id}", ctx -> {
            try {
                int id     = Integer.parseInt(ctx.pathParam("id"));
                boolean ok = appointmentService.cancelAppointment(id);
                if (ok) {
                    ctx.result("{\"message\":\"Cancelled\"}");
                } else {
                    ctx.status(400).result("{\"error\":\"Cancel failed\"}");
                }
            } catch (Exception e) {
                ctx.status(500).result("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        // ── UPDATE APPOINTMENT STATUS ────────────────────────
        app.put("/api/appointments/{id}/status", ctx -> {
            try {
                int id        = Integer.parseInt(ctx.pathParam("id"));
                Map body      = ctx.bodyAsClass(Map.class);
                String status = String.valueOf(body.get("status"));
                boolean ok    = appointmentService.updateAppointmentStatus(id, status);
                if (ok) {
                    ctx.result("{\"message\":\"Status updated\"}");
                } else {
                    ctx.status(400).result("{\"error\":\"Update failed\"}");
                }
            } catch (Exception e) {
                ctx.status(500).result("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        // ═══════════════════════════════════════════════════
        // QUEUE ROUTES
        // ═══════════════════════════════════════════════════

        app.post("/api/queue/join", ctx -> {
            try {
                Map body         = ctx.bodyAsClass(Map.class);
                int patientId    = Integer.parseInt(String.valueOf(body.get("patientId")));
                int departmentId = Integer.parseInt(String.valueOf(body.get("departmentId")));
                int priorityId   = Integer.parseInt(String.valueOf(body.get("priorityId")));
                boolean ok       = queueService.joinQueue(patientId, departmentId, priorityId);
                if (ok) {
                    ctx.status(201).result("{\"message\":\"Joined queue\"}");
                } else {
                    ctx.status(400).result("{\"error\":\"Could not join queue\"}");
                }
            } catch (Exception e) {
                ctx.status(500).result("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        app.get("/api/queue/{departmentId}", ctx -> {
            try {
                int deptId           = Integer.parseInt(ctx.pathParam("departmentId"));
                List<QueueEntry> queue = queueService.viewQueue(deptId);
                StringBuilder sb     = new StringBuilder("[");
                for (int i = 0; i < queue.size(); i++) {
                    QueueEntry q = queue.get(i);
                    sb.append("{")
                            .append("\"queueId\":").append(q.getQueueId()).append(",")
                            .append("\"patientId\":").append(q.getPatientId()).append(",")
                            .append("\"departmentId\":").append(q.getDepartmentId()).append(",")
                            .append("\"priorityId\":").append(q.getPriorityId()).append(",")
                            .append("\"queueNumber\":\"").append(q.getQueueNumber()).append("\",")
                            .append("\"status\":\"").append(q.getStatus()).append("\",")
                            .append("\"estimatedWaitMin\":").append(q.getEstimatedWaitMin())
                            .append("}");
                    if (i < queue.size() - 1) sb.append(",");
                }
                sb.append("]");
                ctx.contentType("application/json").result(sb.toString());
            } catch (Exception e) {
                ctx.status(500).result("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        app.post("/api/queue/{departmentId}/next", ctx -> {
            try {
                int deptId      = Integer.parseInt(ctx.pathParam("departmentId"));
                QueueEntry next = queueService.callNext(deptId);
                if (next != null) {
                    ctx.result("{"
                            + "\"queueNumber\":\"" + next.getQueueNumber() + "\","
                            + "\"patientId\":"      + next.getPatientId()
                            + "}");
                } else {
                    ctx.status(404).result("{\"error\":\"No patients waiting\"}");
                }
            } catch (Exception e) {
                ctx.status(500).result("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        app.put("/api/queue/{queueId}/done", ctx -> {
            try {
                int queueId = Integer.parseInt(ctx.pathParam("queueId"));
                queueService.markDone(queueId);
                ctx.result("{\"message\":\"Marked as done\"}");
            } catch (Exception e) {
                ctx.status(500).result("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        app.put("/api/queue/{queueId}/noshow", ctx -> {
            try {
                int queueId = Integer.parseInt(ctx.pathParam("queueId"));
                queueService.markNoShow(queueId);
                ctx.result("{\"message\":\"Marked as no-show\"}");
            } catch (Exception e) {
                ctx.status(500).result("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        // ═══════════════════════════════════════════════════
        // REPORTS ROUTE
        // ═══════════════════════════════════════════════════

        app.get("/api/reports/staff-summary", ctx -> {
            try {
                List<Appointment> allAppts = appointmentService.getAllAppointments();
                int totalAppointments = allAppts.size();
                int completed = (int) allAppts.stream()
                        .filter(a -> "Done".equals(a.getStatus())
                                || "Confirmed".equals(a.getStatus()))
                        .count();
                int cancelled = (int) allAppts.stream()
                        .filter(a -> "Cancelled".equals(a.getStatus()))
                        .count();

                List<Department> depts = departmentDAO.getAllDepartments();
                StringBuilder deptJson = new StringBuilder("[");
                int totalNoShow = 0;

                for (int i = 0; i < depts.size(); i++) {
                    Department d        = depts.get(i);
                    List<QueueEntry> tq = queueDAO.getTodayQueue(d.getDepartmentId());
                    int waiting = (int) tq.stream()
                            .filter(q -> "Waiting".equals(q.getStatus())).count();
                    int served  = queueDAO.countServedToday(d.getDepartmentId());
                    int noShow  = queueDAO.countNoShowToday(d.getDepartmentId());
                    totalNoShow += noShow;

                    deptJson.append("{")
                            .append("\"departmentId\":").append(d.getDepartmentId()).append(",")
                            .append("\"name\":\"").append(escape(d.getName())).append("\",")
                            .append("\"waiting\":").append(waiting).append(",")
                            .append("\"served\":").append(served).append(",")
                            .append("\"noShow\":").append(noShow)
                            .append("}");
                    if (i < depts.size() - 1) deptJson.append(",");
                }
                deptJson.append("]");

                String json = "{"
                        + "\"totalAppointments\":" + totalAppointments + ","
                        + "\"completed\":"          + completed         + ","
                        + "\"cancelled\":"          + cancelled         + ","
                        + "\"noShow\":"             + totalNoShow       + ","
                        + "\"departments\":"        + deptJson
                        + "}";
                ctx.contentType("application/json").result(json);
            } catch (Exception e) {
                ctx.status(500).result("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });
    }

    // ═══════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════

    /**
     * Escapes special characters for safe JSON string embedding.
     */
    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    /**
     * Converts a list of Appointment objects to a JSON array string.
     */
    private String appointmentsToJson(List<Appointment> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Appointment a = list.get(i);
            sb.append("{")
                    .append("\"appointmentId\":").append(a.getAppointmentId()).append(",")
                    .append("\"patientId\":").append(a.getPatientId()).append(",")
                    .append("\"departmentId\":").append(a.getDepartmentId()).append(",")
                    .append("\"appointmentDate\":\"").append(a.getAppointmentDate()).append("\",")
                    .append("\"appointmentTime\":\"").append(a.getAppointmentTime()).append("\",")
                    .append("\"status\":\"").append(a.getStatus()).append("\"")
                    .append("}");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}