package com.digitalhelper.controller;

import com.digitalhelper.entity.Task;
import com.digitalhelper.repository.TaskRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskRepository taskRepository;
    private final ObjectMapper objectMapper;

    public TaskController(TaskRepository taskRepository, ObjectMapper objectMapper) {
        this.taskRepository = taskRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public List<Map<String, Object>> getTasks() {
        return taskRepository.findAll().stream()
                .map(task -> {
                    List<String> keywords;
                    try {
                        keywords = objectMapper.readValue(task.getKeywords(), new TypeReference<>() {});
                    } catch (Exception e) {
                        keywords = List.of();
                    }
                    return Map.<String, Object>of(
                            "name", task.getName(),
                            "display_name", task.getDisplayName(),
                            "keywords", keywords
                    );
                })
                .toList();
    }
}
