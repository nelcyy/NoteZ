package com.example.notez;

public class NoteModel {
    private String id;
    private String title;
    private String subtitle;
    private String note;
    private String firstWord; // New field to store the first word
    private String userId;

    // Required empty constructor for Firestore
    public NoteModel() {}

    NoteModel(String title, String subtitle, String note, String firstWord, String userId) {
        this.title = title;
        this.subtitle = subtitle;
        this.note = note;
        this.firstWord = firstWord; // Initialize the first word
        this.userId = userId;
    }

    public String getFirstWord() {
        return firstWord;
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setId(String id) {
    }
}