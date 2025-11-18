package com.sde.test.controller;

import com.sde.test.controllers.TaskController;
import com.sde.test.dtos.CreateTaskRequest;
import com.sde.test.entites.Task;
import com.sde.test.exceptions.TaskNotFoundException;
import com.sde.test.repositories.TaskRepository;
import com.sde.test.services.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateTask() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest("Buy milk", "From store");
        Task task = new Task(1L, "Buy milk", "From store", false);

        when(taskService.createTask(any(), any())).thenReturn(task);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Buy milk"))
                .andExpect(jsonPath("$.completed").value(false));

        verify(taskService).createTask("Buy milk", "From store");
    }

    @Test
    void testGetTask() throws Exception {
        Task task = new Task(1L, "Test task", "Description", false);
        when(taskService.getTask(1L)).thenReturn(task);

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test task"));

        verify(taskService).getTask(1L);
    }

    @Test
    void testGetTaskNotFound() throws Exception {
        when(taskService.getTask(1L)).thenThrow(new TaskNotFoundException(1L));

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Task not found with id: 1"));

        verify(taskService).getTask(1L);
    }

    @Test
    void testGetAllTasks() throws Exception {
        Task task1 = new Task(1L, "Task 1", "Desc 1", false);
        Task task2 = new Task(2L, "Task 2", "Desc 2", true);
        List<Task> tasks = Arrays.asList(task1, task2);

        when(taskService.getAllTasks()).thenReturn(tasks);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[1].title").value("Task 2"))
                .andExpect(jsonPath("$[1].completed").value(true));

        verify(taskService).getAllTasks();
    }

    @Test
    void testCompleteTask() throws Exception {
        Task task = new Task(1L, "Test task", "Description", true);
        when(taskService.completeTask(1L)).thenReturn(task);

        mockMvc.perform(put("/api/tasks/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));

        verify(taskService).completeTask(1L);
    }

    @Test
    void testDeleteTask() throws Exception {
        doNothing().when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(1L);
    }

    @Test
    void testDeleteTaskNotFound() throws Exception {
        doThrow(new TaskNotFoundException(1L)).when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNotFound());

        verify(taskService).deleteTask(1L);
    }
}