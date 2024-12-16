package com.example.notez;

public class UsersModel {
    private String email;
    private String password; // For security reasons, storing passwords in Firestore is not recommended

    // Empty constructor required for Firestore serialization
    public UsersModel() {}

    public UsersModel(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters and setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}