package com.research.platform.service;

import com.research.platform.entity.ResearchTask;
import com.research.platform.repository.ResearchTaskRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResultConsumer {
    private final ResearchTaskRepository repository;

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

            log.info("Task {} successfully updated to COMPLETED!", task.getId());
        } else {
            log.error("Received result for Task ID {}, but it doesn't exist in the database!", payload.getId());
        }
    }
}