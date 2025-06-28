package com.example.api.repository;

import com.example.api.model.TaskNoSQL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public interface TaskRepositoryNoSQL {
    
    List<TaskNoSQL> findAll() throws ExecutionException, InterruptedException;
    
    Optional<TaskNoSQL> findById(String id) throws ExecutionException, InterruptedException;
    
    TaskNoSQL save(TaskNoSQL task) throws ExecutionException, InterruptedException;
    
    void deleteById(String id) throws ExecutionException, InterruptedException;
    
    List<TaskNoSQL> findByStatus(TaskNoSQL.TaskStatus status) throws ExecutionException, InterruptedException;
    
    List<TaskNoSQL> findByTitleOrDescriptionContaining(String keyword) throws ExecutionException, InterruptedException;
    
    boolean existsById(String id) throws ExecutionException, InterruptedException;
}
