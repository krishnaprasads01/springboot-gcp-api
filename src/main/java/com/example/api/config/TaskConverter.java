package com.example.api.config;

import com.example.api.model.TaskNoSQL;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TaskConverter {

    public TaskNoSQL convertFromFirestore(DocumentSnapshot document) {
        if (!document.exists()) {
            return null;
        }

        TaskNoSQL task = new TaskNoSQL();
        Map<String, Object> data = document.getData();
        
        // Basic fields
        task.setId(document.getId());
        task.setTitle((String) data.get("title"));
        task.setDescription((String) data.get("description"));
        task.setAssignee((String) data.get("assignee"));
        
        // Handle status
        Object statusObj = data.get("status");
        if (statusObj instanceof String) {
            try {
                task.setStatus(TaskNoSQL.TaskStatus.valueOf((String) statusObj));
            } catch (IllegalArgumentException e) {
                task.setStatus(TaskNoSQL.TaskStatus.PENDING);
            }
        }
        
        // Handle timestamps with backwards compatibility
        handleTimestamp(data, "createdAt", "firestoreCreatedAt", task::setFirestoreCreatedAt);
        handleTimestamp(data, "updatedAt", "firestoreUpdatedAt", task::setFirestoreUpdatedAt);
        handleTimestamp(data, "dueDate", "firestoreDueDate", task::setFirestoreDueDate);
        
        return task;
    }
    
    private void handleTimestamp(Map<String, Object> data, String legacyField, String newField, TimestampSetter setter) {
        // Try new field first
        Object newValue = data.get(newField);
        if (newValue instanceof Timestamp) {
            setter.set((Timestamp) newValue);
            return;
        }
        
        // Try legacy field
        Object legacyValue = data.get(legacyField);
        if (legacyValue instanceof Timestamp) {
            setter.set((Timestamp) legacyValue);
        } else if (legacyValue instanceof Map) {
            // Handle LocalDateTime stored as map (Firestore sometimes does this)
            // This is a fallback - create a timestamp from current time if we can't parse
            setter.set(Timestamp.now());
        } else {
            // Default to current time for new objects
            if (legacyField.equals("createdAt") || legacyField.equals("updatedAt")) {
                setter.set(Timestamp.now());
            }
            // dueDate can be null
        }
    }
    
    @FunctionalInterface
    private interface TimestampSetter {
        void set(Timestamp timestamp);
    }
}
