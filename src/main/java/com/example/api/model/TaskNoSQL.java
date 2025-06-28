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
    
    // Legacy compatibility fields (for old data in Firestore)
    @JsonIgnore
    private Object createdAt; // Can be LocalDateTime or Timestamp
    
    @JsonIgnore
    private Object updatedAt; // Can be LocalDateTime or Timestamp
    
    @JsonIgnore
    private Object dueDate; // Can be LocalDateTime or Timestamp
    
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
        // Try new format first
        if (firestoreCreatedAt != null) {
            return LocalDateTime.ofEpochSecond(firestoreCreatedAt.getSeconds(), 
                                             (int) firestoreCreatedAt.getNanos(), 
                                             ZoneOffset.UTC);
        }
        // Fall back to legacy format for backwards compatibility
        if (createdAt instanceof Timestamp) {
            Timestamp ts = (Timestamp) createdAt;
            return LocalDateTime.ofEpochSecond(ts.getSeconds(), 
                                             (int) ts.getNanos(), 
                                             ZoneOffset.UTC);
        }
        // If it's already LocalDateTime (old format), return as is
        if (createdAt instanceof LocalDateTime) {
            return (LocalDateTime) createdAt;
        }
        return null;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.firestoreCreatedAt = createdAt != null ? 
            Timestamp.ofTimeSecondsAndNanos(createdAt.toEpochSecond(ZoneOffset.UTC), 
                                           createdAt.getNano()) : null;
    }
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime getUpdatedAt() {
        // Try new format first
        if (firestoreUpdatedAt != null) {
            return LocalDateTime.ofEpochSecond(firestoreUpdatedAt.getSeconds(), 
                                             (int) firestoreUpdatedAt.getNanos(), 
                                             ZoneOffset.UTC);
        }
        // Fall back to legacy format for backwards compatibility
        if (updatedAt instanceof Timestamp) {
            Timestamp ts = (Timestamp) updatedAt;
            return LocalDateTime.ofEpochSecond(ts.getSeconds(), 
                                             (int) ts.getNanos(), 
                                             ZoneOffset.UTC);
        }
        // If it's already LocalDateTime (old format), return as is
        if (updatedAt instanceof LocalDateTime) {
            return (LocalDateTime) updatedAt;
        }
        return null;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.firestoreUpdatedAt = updatedAt != null ? 
            Timestamp.ofTimeSecondsAndNanos(updatedAt.toEpochSecond(ZoneOffset.UTC), 
                                           updatedAt.getNano()) : null;
    }
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime getDueDate() {
        // Try new format first
        if (firestoreDueDate != null) {
            return LocalDateTime.ofEpochSecond(firestoreDueDate.getSeconds(), 
                                             (int) firestoreDueDate.getNanos(), 
                                             ZoneOffset.UTC);
        }
        // Fall back to legacy format for backwards compatibility
        if (dueDate instanceof Timestamp) {
            Timestamp ts = (Timestamp) dueDate;
            return LocalDateTime.ofEpochSecond(ts.getSeconds(), 
                                             (int) ts.getNanos(), 
                                             ZoneOffset.UTC);
        }
        // If it's already LocalDateTime (old format), return as is
        if (dueDate instanceof LocalDateTime) {
            return (LocalDateTime) dueDate;
        }
        return null;
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
    
    // Legacy field getters/setters for backwards compatibility
    public Object getLegacyCreatedAt() {
        return createdAt;
    }
    
    public void setLegacyCreatedAt(Object createdAt) {
        this.createdAt = createdAt;
    }
    
    public Object getLegacyUpdatedAt() {
        return updatedAt;
    }
    
    public void setLegacyUpdatedAt(Object updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Object getLegacyDueDate() {
        return dueDate;
    }
    
    public void setLegacyDueDate(Object dueDate) {
        this.dueDate = dueDate;
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
