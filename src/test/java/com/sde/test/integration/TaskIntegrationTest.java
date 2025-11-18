package com.sde.test.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sde.test.dtos.CreateTaskRequest;
import com.sde.test.entites.Task;
import com.sde.test.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    void testCreateAndGetTask() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest("Buy groceries", "Milk and bread");

        // Create task
        String response = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Buy groceries"))
                .andReturn().getResponse().getContentAsString();

        Task createdTask = objectMapper.readValue(response, Task.class);

        // Verify in database
        Task dbTask = taskRepository.findById(createdTask.getId()).orElse(null);
        assertNotNull(dbTask);
        assertEquals("Buy groceries", dbTask.getTitle());
        assertFalse(dbTask.isCompleted());

        // Get task via API
        mockMvc.perform(get("/api/tasks/" + createdTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Buy groceries"));
    }

    @Test
    void testCompleteTask() throws Exception {
        Task task = new Task("Test task", "Description");
        Task saved = taskRepository.save(task);

        // Complete task
        mockMvc.perform(put("/api/tasks/" + saved.getId() + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));

        // Verify in database
        Task updated = taskRepository.findById(saved.getId()).orElse(null);
        assertNotNull(updated);
        assertTrue(updated.isCompleted());
    }

    @Test
    void testDeleteTask() throws Exception {
        Task task = new Task("Delete me", "Test");
        Task saved = taskRepository.save(task);

        // Delete task
        mockMvc.perform(delete("/api/tasks/" + saved.getId()))
                .andExpect(status().isNoContent());

        // Verify deleted from database
        assertFalse(taskRepository.existsById(saved.getId()));
    }

    @Test
    void testGetAllTasks() throws Exception {
        taskRepository.save(new Task("Task 1", "Desc 1"));
        taskRepository.save(new Task("Task 2", "Desc 2"));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetNonExistentTask() throws Exception {
        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Task not found with id: 999"));
    }

    @Test
    void testDeleteNonExistentTask() throws Exception {
        mockMvc.perform(delete("/api/tasks/999"))
                .andExpect(status().isNotFound());
    }
}