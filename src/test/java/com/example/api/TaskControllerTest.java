package com.example.api;

import com.example.api.controller.TaskController;
import com.example.api.model.Task;
import com.example.api.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetAllTasks() throws Exception {
        Task task1 = new Task("Task 1", "Description 1");
        task1.setId(1L);
        task1.setCreatedAt(LocalDateTime.now());
        task1.setUpdatedAt(LocalDateTime.now());
        
        Task task2 = new Task("Task 2", "Description 2");
        task2.setId(2L);
        task2.setCreatedAt(LocalDateTime.now());
        task2.setUpdatedAt(LocalDateTime.now());

        when(taskService.getAllTasks()).thenReturn(Arrays.asList(task1, task2));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[1].title").value("Task 2"));
    }

    @Test
    public void testGetTaskById() throws Exception {
        Task task = new Task("Test Task", "Test Description");
        task.setId(1L);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        when(taskService.getTaskById(1L)).thenReturn(Optional.of(task));

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"));
    }

    @Test
    public void testGetTaskByIdNotFound() throws Exception {
        when(taskService.getTaskById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateTask() throws Exception {
        Task inputTask = new Task("New Task", "New Description");
        Task savedTask = new Task("New Task", "New Description");
        savedTask.setId(1L);
        savedTask.setCreatedAt(LocalDateTime.now());
        savedTask.setUpdatedAt(LocalDateTime.now());

        when(taskService.createTask(any(Task.class))).thenReturn(savedTask);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputTask)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.description").value("New Description"));
    }

    @Test
    public void testCreateTaskWithInvalidData() throws Exception {
        Task invalidTask = new Task("", ""); // Empty title should fail validation

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateTask() throws Exception {
        Task updatedTask = new Task("Updated Task", "Updated Description");
        updatedTask.setId(1L);
        updatedTask.setStatus(Task.TaskStatus.COMPLETED);
        updatedTask.setCreatedAt(LocalDateTime.now());
        updatedTask.setUpdatedAt(LocalDateTime.now());

        when(taskService.updateTask(anyLong(), any(Task.class))).thenReturn(updatedTask);

        mockMvc.perform(put("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedTask)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    public void testDeleteTask() throws Exception {
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());
    }
}
