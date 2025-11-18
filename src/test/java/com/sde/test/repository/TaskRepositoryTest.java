package com.sde.test.repository;


import com.sde.test.entites.Task;
import com.sde.test.repositories.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void testSaveTask() {
        Task task = new Task("Buy groceries", "Milk, eggs, bread");

        Task saved = taskRepository.save(task);

        assertNotNull(saved.getId());
        assertEquals("Buy groceries", saved.getTitle());
        assertFalse(saved.isCompleted());
    }

    @Test
    void testFindById() {
        Task task = new Task("Clean room", "Vacuum and dust");
        Task saved = taskRepository.save(task);

        Optional<Task> found = taskRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Clean room", found.get().getTitle());
    }

    @Test
    void testFindByCompleted() {
        Task task1 = new Task("Task 1", "Description 1");
        task1.setCompleted(true);
        taskRepository.save(task1);

        Task task2 = new Task("Task 2", "Description 2");
        task2.setCompleted(false);
        taskRepository.save(task2);

        List<Task> completedTasks = taskRepository.findByCompleted(true);

        assertEquals(1, completedTasks.size());
        assertEquals("Task 1", completedTasks.get(0).getTitle());
    }

    @Test
    void testDeleteTask() {
        Task task = new Task("Delete me", "Test deletion");
        Task saved = taskRepository.save(task);

        taskRepository.deleteById(saved.getId());

        Optional<Task> found = taskRepository.findById(saved.getId());
        assertFalse(found.isPresent());
    }
}