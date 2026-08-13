package com.research.platform.service;

import com.research.platform.entity.ResearchTask;
import com.research.platform.repository.ResearchTaskRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResultConsumer {
    private final ResearchTaskRepository repository;
    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate; // Inject WebSocket template
    
    // A small internal DTO to catch the JSON that Python sends
    @Data
    public static class ResultPayload{
        private Long id;
        private String resultMarkdown;
    }

    // Spring Boot will automatically run this method whenever a message enters the result queue!
    @RabbitListener(queues = "${app.rabbitmq.result_queue}")
    public void receiveResult(ResultPayload payload) {
        log.info("Received completed report from Python for Task ID: {}", payload.getId());

        // 1. Find the PENDING task in PostgreSQL
        Optional<ResearchTask> optionalTask = repository.findById(payload.getId());

        if (optionalTask.isPresent()) {
            ResearchTask task = optionalTask.get();

            // 🌟 The Idempotency Check! 🌟
            if("COMPLETED".equals(task.getStatus())) {
                log.info("Task {} is already completed! Skipping duplicate message.", task.getId());
                return; // Exit early, do not waste database CPU!
            }

            task.setStatus("COMPLETED");
            task.setResultMarkdown(payload.getResultMarkdown());
            repository.save(task);

            // 1. Cache the result in Redis for 24 hours
            redisTemplate.opsForValue().set(
                "research:" + task.getTopic(),
                payload.getResultMarkdown(),
                Duration.ofHours(24)
            );

            // 2. Broadcast via WebSocket
            // If we linked tasks to users, we would send this to "/topic/tasks/" + task.getUser().getId()
            messagingTemplate.convertAndSend("/topic/tasks", task);
            
            log.info("Task {} successfully updated to COMPLETED!", task.getId());
        } else {
            log.error("Received result for Task ID {}, but it doesn't exist in the database!", payload.getId());
        }
    }
}