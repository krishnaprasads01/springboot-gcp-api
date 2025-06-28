package com.example.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.cloud.Timestamp;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TaskNoSQL {
    
    private String id;
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    private TaskStatus status = TaskStatus.PENDING;
    
    // Firestore storage fields (Timestamp objects for Firestore compatibility)
    @JsonIgnore
    private Timestamp firestoreCreatedAt;
    
    @JsonIgnore
    private Timestamp firestoreUpdatedAt;
    
    @JsonIgnore
    private Timestamp firestoreDueDate;
    
    private String assignee;
    
    public enum TaskStatus {
        PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    }
    
    // Constructors
    public TaskNoSQL() {}
    
    public TaskNoSQL(String title, String description) {
        this.title = title;
        this.description = description;
        Timestamp now = Timestamp.now();
        this.firestoreCreatedAt = now;
        this.firestoreUpdatedAt = now;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
        this.firestoreUpdatedAt = Timestamp.now();
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        this.firestoreUpdatedAt = Timestamp.now();
    }
    
    public TaskStatus getStatus() {
        return status;
    }
    
    public void setStatus(TaskStatus status) {
        this.status = status;
        this.firestoreUpdatedAt = Timestamp.now();
    }
    
    // JSON getters/setters for LocalDateTime (for API responses)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime getCreatedAt() {
        return firestoreCreatedAt != null ? 
            LocalDateTime.ofEpochSecond(firestoreCreatedAt.getSeconds(), 
                                       (int) firestoreCreatedAt.getNanos(), 
                                       ZoneOffset.UTC) : null;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.firestoreCreatedAt = createdAt != null ? 
            Timestamp.ofTimeSecondsAndNanos(createdAt.toEpochSecond(ZoneOffset.UTC), 
                                           createdAt.getNano()) : null;
    }
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime getUpdatedAt() {
        return firestoreUpdatedAt != null ? 
            LocalDateTime.ofEpochSecond(firestoreUpdatedAt.getSeconds(), 
                                       (int) firestoreUpdatedAt.getNanos(), 
                                       ZoneOffset.UTC) : null;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.firestoreUpdatedAt = updatedAt != null ? 
            Timestamp.ofTimeSecondsAndNanos(updatedAt.toEpochSecond(ZoneOffset.UTC), 
                                           updatedAt.getNano()) : null;
    }
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime getDueDate() {
        return firestoreDueDate != null ? 
            LocalDateTime.ofEpochSecond(firestoreDueDate.getSeconds(), 
                                       (int) firestoreDueDate.getNanos(), 
                                       ZoneOffset.UTC) : null;
    }
    
    public void setDueDate(LocalDateTime dueDate) {
        this.firestoreDueDate = dueDate != null ? 
            Timestamp.ofTimeSecondsAndNanos(dueDate.toEpochSecond(ZoneOffset.UTC), 
                                           dueDate.getNano()) : null;
        this.firestoreUpdatedAt = Timestamp.now();
    }
    
    // Firestore getters/setters (for database storage)
    public Timestamp getFirestoreCreatedAt() {
        return firestoreCreatedAt;
    }
    
    public void setFirestoreCreatedAt(Timestamp firestoreCreatedAt) {
        this.firestoreCreatedAt = firestoreCreatedAt;
    }
    
    public Timestamp getFirestoreUpdatedAt() {
        return firestoreUpdatedAt;
    }
    
    public void setFirestoreUpdatedAt(Timestamp firestoreUpdatedAt) {
        this.firestoreUpdatedAt = firestoreUpdatedAt;
    }
    
    public Timestamp getFirestoreDueDate() {
        return firestoreDueDate;
    }
    
    public void setFirestoreDueDate(Timestamp firestoreDueDate) {
        this.firestoreDueDate = firestoreDueDate;
    }
    
    public String getAssignee() {
        return assignee;
    }
    
    public void setAssignee(String assignee) {
        this.assignee = assignee;
        this.firestoreUpdatedAt = Timestamp.now();
    }
    
    @Override
    public String toString() {
        return "TaskNoSQL{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                ", dueDate=" + getDueDate() +
                ", assignee='" + assignee + '\'' +
                '}';
    }
}
