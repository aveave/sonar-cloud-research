package com.example.demo.support;

import com.example.demo.model.EmployeeCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@ConditionalOnExpression("'${kafka.topics:}'.trim() != ''")
public class TestKafkaEventsListener {

    private final Queue<ConsumerRecord<String, EmployeeCreatedEvent>> consumedMessages = new ConcurrentLinkedQueue<>();

    @KafkaListener(topics = {"#{'${kafka.topics}'.split(',')}"}, groupId = "test-consumer-group")
    public void listen(ConsumerRecord<String, EmployeeCreatedEvent> message) {
        consumedMessages.add(message);
    }

    public void clear() {
        consumedMessages.clear();
    }

    public void awaitAndAssertMessageCount(int expectedCount) {
        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(
                () -> Assertions.assertThat(consumedMessages).hasSize(expectedCount)
        );
    }

    public void assertKeysContainExactlyInAnyOrder(String... keys) {
        Assertions.assertThat(consumedMessages.stream().map(ConsumerRecord::key).toList())
                .containsExactlyInAnyOrder(keys);
    }

    public List<EmployeeCreatedEvent> payloads() {
        return consumedMessages.stream()
                .map(ConsumerRecord::value)
                .toList();
    }
}
