package com.example.api.repository;

import com.example.api.model.TaskNoSQL;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
    
    @Override
    public List<TaskNoSQL> findAll() throws ExecutionException, InterruptedException {
        List<TaskNoSQL> tasks = new ArrayList<>();
        firestore.collection(COLLECTION_NAME)
                .get()
                .get()
                .getDocuments()
                .forEach(document -> {
                    TaskNoSQL task = document.toObject(TaskNoSQL.class);
                    task.setId(document.getId());
                    tasks.add(task);
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
            TaskNoSQL task = document.toObject(TaskNoSQL.class);
            task.setId(document.getId());
            return Optional.of(task);
        }
        return Optional.empty();
    }
    
    @Override
    public TaskNoSQL save(TaskNoSQL task) throws ExecutionException, InterruptedException {
        if (task.getId() == null || task.getId().isEmpty()) {
            // Create new task
            task.setId(UUID.randomUUID().toString());
            task.setCreatedAt(LocalDateTime.now());
        }
        task.setUpdatedAt(LocalDateTime.now());
        
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
                    TaskNoSQL task = document.toObject(TaskNoSQL.class);
                    task.setId(document.getId());
                    tasks.add(task);
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
