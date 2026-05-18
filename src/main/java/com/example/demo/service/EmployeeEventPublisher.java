package com.example.demo.service;

import com.example.demo.model.EmployeeCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeEventPublisher {

    private final KafkaTemplate<String, EmployeeCreatedEvent> kafkaTemplate;

    @Value("${app.kafka.employee-created-topic}")
    private String employeeCreatedTopic;

    public void publishEmployeeCreated(EmployeeCreatedEvent event) {
        kafkaTemplate.send(employeeCreatedTopic, event.id().toString(), event);
    }
}
