package com.example.api.controller;

import com.example.api.model.TaskNoSQL;
import com.example.api.service.TaskNoSQLService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/tasks") // Unified endpoint
public class TaskNoSQLController {
    
    @Autowired
    private TaskNoSQLService taskService;
    
    @GetMapping
    public ResponseEntity<List<TaskNoSQL>> getAllTasks() {
        try {
            List<TaskNoSQL> tasks = taskService.getAllTasks();
            return ResponseEntity.ok(tasks);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TaskNoSQL> getTaskById(@PathVariable String id) {
        try {
            Optional<TaskNoSQL> task = taskService.getTaskById(id);
            return task.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping
    public ResponseEntity<TaskNoSQL> createTask(@Valid @RequestBody TaskNoSQL task) {
        try {
            TaskNoSQL createdTask = taskService.createTask(task);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TaskNoSQL> updateTask(@PathVariable String id, @Valid @RequestBody TaskNoSQL taskDetails) {
        try {
            TaskNoSQL updatedTask = taskService.updateTask(id, taskDetails);
            return ResponseEntity.ok(updatedTask);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        try {
            taskService.deleteTask(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskNoSQL>> getTasksByStatus(@PathVariable String status) {
        try {
            TaskNoSQL.TaskStatus taskStatus = TaskNoSQL.TaskStatus.valueOf(status.toUpperCase());
            List<TaskNoSQL> tasks = taskService.getTasksByStatus(taskStatus);
            return ResponseEntity.ok(tasks);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<TaskNoSQL>> searchTasks(@RequestParam String keyword) {
        try {
            List<TaskNoSQL> tasks = taskService.searchTasks(keyword);
            return ResponseEntity.ok(tasks);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
