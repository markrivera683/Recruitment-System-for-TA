package com.bupt.ta.model;

public class User {
    public String id;
    public String name;
    public String studentId;
    public String email;
    public String passwordHash; // for coursework: OK to store plain during early prototype, but document risk
    public String role = "TA";

    public User() {}

    public User(String id, String name, String studentId, String email, String passwordHash) {
        this.id = id;
        this.name = name;
        this.studentId = studentId;
        this.email = email;
        this.passwordHash = passwordHash;
    }
}
