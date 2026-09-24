package com.smartqueue.model;

public class Patient {

    private int    patientId;
    private String name;
    private String contact;
    private String email;
    private String passwordHash;

    public Patient() {}

    public Patient(String name, String contact, String email, String passwordHash) {
        this.name         = name;
        this.contact      = contact;
        this.email        = email;
        this.passwordHash = passwordHash;
    }

    public int    getPatientId()    { return patientId; }
    public String getName()         { return name; }
    public String getContact()      { return contact; }
    public String getEmail()        { return email; }
    public String getPasswordHash() { return passwordHash; }

    public void setPatientId(int patientId)       { this.patientId    = patientId; }
    public void setName(String name)               { this.name         = name; }
    public void setContact(String contact)         { this.contact      = contact; }
    public void setEmail(String email)             { this.email        = email; }
    public void setPasswordHash(String hash)       { this.passwordHash = hash; }

    @Override
    public String toString() {
        return "Patient [ID=" + patientId + ", Name=" + name + ", Email=" + email + "]";
    }
}
