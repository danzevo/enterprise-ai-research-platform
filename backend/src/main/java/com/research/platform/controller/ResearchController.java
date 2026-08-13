package com.research.platform.controller;

import com.research.platform.dto.TaskRequest;
import com.research.platform.entity.ResearchTask;
import com.research.platform.repository.ResearchTaskRepository;
import com.research.platform.repository.UserRepository;
import com.research.platform.service.TaskProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/research")
@RequiredArgsConstructor
public class ResearchController {
    private final ResearchTaskRepository repository;
    private final UserRepository userRepository;
    private final TaskProducer taskProducer;
    private final StringRedisTemplate redisTemplate;

    // Endpoint 1: Submit a new research task
    // The Vue frontend will POST to this endpoint
    @PostMapping
    public ResponseEntity<ResearchTask> submitTask(@RequestBody TaskRequest request) {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var user = userRepository.findByUsername(username).orElseThrow();

        // --- REDIS CACHE CHECK ---
        String cachedResult = redisTemplate.opsForValue().get("research:" + request.getTopic());

        if(cachedResult != null) {
            // Instantly return completed cached task!
            ResearchTask cachedTask = ResearchTask.builder()
                .topic(request.getTopic())
                .status("COMPLETED")
                .resultMarkdown(cachedResult)
                .user(user)
                .build();
            
            cachedTask = repository.save(cachedTask);
            return ResponseEntity.ok(cachedTask);
        }

        // --- NORMAL FLOW ---
        // 1. Create the task in the database as PENDING
        ResearchTask task = ResearchTask.builder().topic(request.getTopic()).status("PENDING").user(user).build();
        task = repository.save(task); // Saves to PostgreSQL

        // 2. Publish to RabbitMQ so the Python AI can pick it up
        taskProducer.sendTaskToPython(task);

        // 3. Return HTTP 202 Accepted to the frontend immediately
        return ResponseEntity.accepted().body(task);
    }

    // Endpoint 2: Check the status of a task
    // The Vue frontend will poll this endpoint to see if the report is ready
    @GetMapping("/{id}")
    public ResponseEntity<ResearchTask> getTask(@PathVariable Long id) {
        Optional<ResearchTask> task = repository.findById(id);

        return task.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Endpoint 3: Get ALL tasks for the Dashboard history
    @GetMapping
    public ResponseEntity<List<ResearchTask>> getAllTasks() {
        // Fetch only tasks for the logged in user
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var user = userRepository.findByUsername(username).orElseThrow();
        // Note: You would normally create a custom repository method like `findByUserId`,
        // but for simplicity you can filter here or implement that method.
        return ResponseEntity.ok(repository.findAll().stream().filter(t -> t.getUser() != null && t.getUser().getId().equals(user.getId())).toList());
    }
}