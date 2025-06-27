package com.example.api.repository;

import com.example.api.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    List<Task> findByStatus(Task.TaskStatus status);
    
    @Query("SELECT t FROM Task t WHERE t.title LIKE %?1% OR t.description LIKE %?1%")
    List<Task> findByTitleOrDescriptionContaining(String keyword);
    
    List<Task> findByTitleContainingIgnoreCase(String title);
}
