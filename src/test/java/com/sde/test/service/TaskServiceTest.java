package com.sde.test.service;


import com.sde.test.entites.Task;
import com.sde.test.exceptions.TaskNotFoundException;
import com.sde.test.repositories.TaskRepository;
import com.sde.test.services.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void testCreateTask() {
        Task task = new Task("Buy milk", "From store");
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task created = taskService.createTask("Buy milk", "From store");

        assertEquals("Buy milk", created.getTitle());
        assertEquals("From store", created.getDescription());
        assertFalse(created.isCompleted());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void testGetTaskSuccess() {
        Task task = new Task(1L, "Test task", "Description", false);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Task found = taskService.getTask(1L);

        assertEquals("Test task", found.getTitle());
        verify(taskRepository).findById(1L);
    }

    @Test
    void testGetTaskNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.getTask(1L));
        verify(taskRepository).findById(1L);
    }

    @Test
    void testCompleteTask() {
        Task task = new Task(1L, "Test task", "Description", false);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task completed = taskService.completeTask(1L);

        assertTrue(completed.isCompleted());
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(task);
    }

    @Test
    void testDeleteTaskSuccess() {
        when(taskRepository.existsById(1L)).thenReturn(true);

        taskService.deleteTask(1L);

        verify(taskRepository).existsById(1L);
        verify(taskRepository).deleteById(1L);
    }

    @Test
    void testDeleteTaskNotFound() {
        when(taskRepository.existsById(1L)).thenReturn(false);

        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(1L));
        verify(taskRepository).existsById(1L);
        verify(taskRepository, never()).deleteById(1L);
    }
}