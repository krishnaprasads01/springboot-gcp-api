package com.example.api.repository;

import com.example.api.config.TaskConverter;
import com.example.api.model.TaskNoSQL;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Repository
public class TaskRepositoryImpl implements TaskRepositoryNoSQL {
    
    private static final String COLLECTION_NAME = "tasks";
    
    @Autowired
    private Firestore firestore;
    
    @Autowired
    private TaskConverter taskConverter;
    
    @Override
    public List<TaskNoSQL> findAll() throws ExecutionException, InterruptedException {
        List<TaskNoSQL> tasks = new ArrayList<>();
        firestore.collection(COLLECTION_NAME)
                .get()
                .get()
                .getDocuments()
                .forEach(document -> {
                    try {
                        TaskNoSQL task = taskConverter.convertFromFirestore(document);
                        if (task != null) {
                            tasks.add(task);
                        }
                    } catch (Exception e) {
                        // Log error but continue processing other documents
                        System.err.println("Error converting document " + document.getId() + ": " + e.getMessage());
                    }
                });
        return tasks;
    }
    
    @Override
    public Optional<TaskNoSQL> findById(String id) throws ExecutionException, InterruptedException {
        var document = firestore.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .get();
        
        if (document.exists()) {
            try {
                TaskNoSQL task = taskConverter.convertFromFirestore(document);
                return Optional.ofNullable(task);
            } catch (Exception e) {
                System.err.println("Error converting document " + id + ": " + e.getMessage());
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
    
    @Override
    public TaskNoSQL save(TaskNoSQL task) throws ExecutionException, InterruptedException {
        if (task.getId() == null || task.getId().isEmpty()) {
            // Create new task
            task.setId(UUID.randomUUID().toString());
            task.setFirestoreCreatedAt(Timestamp.now());
        }
        task.setFirestoreUpdatedAt(Timestamp.now());
        
        firestore.collection(COLLECTION_NAME)
                .document(task.getId())
                .set(task)
                .get();
        
        return task;
    }
    
    @Override
    public void deleteById(String id) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION_NAME)
                .document(id)
                .delete()
                .get();
    }
    
    @Override
    public List<TaskNoSQL> findByStatus(TaskNoSQL.TaskStatus status) throws ExecutionException, InterruptedException {
        List<TaskNoSQL> tasks = new ArrayList<>();
        firestore.collection(COLLECTION_NAME)
                .whereEqualTo("status", status.toString())
                .get()
                .get()
                .getDocuments()
                .forEach(document -> {
                    try {
                        TaskNoSQL task = taskConverter.convertFromFirestore(document);
                        if (task != null) {
                            tasks.add(task);
                        }
                    } catch (Exception e) {
                        System.err.println("Error converting document " + document.getId() + ": " + e.getMessage());
                    }
                });
        return tasks;
    }
    
    @Override
    public List<TaskNoSQL> findByTitleOrDescriptionContaining(String keyword) throws ExecutionException, InterruptedException {
        List<TaskNoSQL> tasks = new ArrayList<>();
        String searchKeyword = keyword.toLowerCase();
        
        // Note: Firestore doesn't support case-insensitive text search directly
        // This is a basic implementation - for production, consider using Algolia or similar
        firestore.collection(COLLECTION_NAME)
                .get()
                .get()
                .getDocuments()
                .forEach(document -> {
                    TaskNoSQL task = document.toObject(TaskNoSQL.class);
                    task.setId(document.getId());
                    
                    if ((task.getTitle() != null && task.getTitle().toLowerCase().contains(searchKeyword)) ||
                        (task.getDescription() != null && task.getDescription().toLowerCase().contains(searchKeyword))) {
                        tasks.add(task);
                    }
                });
        
        return tasks;
    }
    
    @Override
    public boolean existsById(String id) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .get()
                .exists();
    }
}
