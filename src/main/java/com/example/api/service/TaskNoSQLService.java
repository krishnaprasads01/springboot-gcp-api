package com.example.api.service;

import com.example.api.model.TaskNoSQL;
import com.example.api.repository.TaskRepositoryNoSQL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class TaskNoSQLService {
    
    @Autowired
    private TaskRepositoryNoSQL taskRepository;
    
    public List<TaskNoSQL> getAllTasks() throws ExecutionException, InterruptedException {
        return taskRepository.findAll();
    }
    
    public Optional<TaskNoSQL> getTaskById(String id) throws ExecutionException, InterruptedException {
        return taskRepository.findById(id);
    }
    
    public TaskNoSQL createTask(TaskNoSQL task) throws ExecutionException, InterruptedException {
        return taskRepository.save(task);
    }
    
    public TaskNoSQL updateTask(String id, TaskNoSQL taskDetails) throws ExecutionException, InterruptedException {
        Optional<TaskNoSQL> existingTaskOpt = taskRepository.findById(id);
        if (existingTaskOpt.isEmpty()) {
            throw new RuntimeException("Task not found with id: " + id);
        }
        
        TaskNoSQL existingTask = existingTaskOpt.get();
        existingTask.setTitle(taskDetails.getTitle());
        existingTask.setDescription(taskDetails.getDescription());
        existingTask.setStatus(taskDetails.getStatus());
        if (taskDetails.getDueDate() != null) {
            existingTask.setDueDate(taskDetails.getDueDate());
        }
        if (taskDetails.getAssignee() != null) {
            existingTask.setAssignee(taskDetails.getAssignee());
        }
        
        return taskRepository.save(existingTask);
    }
    
    public void deleteTask(String id) throws ExecutionException, InterruptedException {
        if (!taskRepository.existsById(id)) {
            throw new RuntimeException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }
    
    public List<TaskNoSQL> getTasksByStatus(TaskNoSQL.TaskStatus status) throws ExecutionException, InterruptedException {
        return taskRepository.findByStatus(status);
    }
    
    public List<TaskNoSQL> searchTasks(String keyword) throws ExecutionException, InterruptedException {
        return taskRepository.findByTitleOrDescriptionContaining(keyword);
    }
}
