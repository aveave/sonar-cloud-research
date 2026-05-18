package com.example.demo;

import com.example.demo.model.Employee;
import com.example.demo.model.EmployeeCreatedEvent;
import com.example.demo.repository.EmployeeJpaRepository;
import com.example.demo.support.TestKafkaEventsListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
class DemoApplicationTests {

    private static final String TOPIC = "employee-created";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("app.kafka.employee-created-topic", () -> TOPIC);
        registry.add("kafka.topics", () -> TOPIC);
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    EmployeeJpaRepository employeeJpaRepository;

    @Autowired
    TestKafkaEventsListener kafkaEventsListener;

    @BeforeEach
    void setUp() {
        kafkaEventsListener.clear();
    }

    @Test
    void createEmployeePersistsToPostgresAndPublishesKafkaEvent() {
        Employee employee = new Employee();
        employee.setName("John");
        employee.setSurname("Doe");
        employee.setCompensation(new BigDecimal("100.00"));
        employee.setHiredAt(Instant.parse("2025-01-01T00:00:00Z"));

        ResponseEntity<Long> createResponse = restTemplate.postForEntity(url("/api/v1/employees"), employee, Long.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long id = createResponse.getBody();
        assertThat(id).isNotNull();

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            assertThat(employeeJpaRepository.findById(id)).isPresent();
            assertThat(employeeJpaRepository.findById(id).orElseThrow().getCompensation())
                    .isEqualByComparingTo("115.00");
        });

        kafkaEventsListener.awaitAndAssertMessageCount(1);
        kafkaEventsListener.assertKeysContainExactlyInAnyOrder(id.toString());

        EmployeeCreatedEvent actualEvent = kafkaEventsListener.payloads().getFirst();
        EmployeeCreatedEvent expectedEvent = new EmployeeCreatedEvent(
                id,
                "John",
                "Doe",
                new BigDecimal("115.00"),
                Instant.parse("2025-01-01T00:00:00Z")
        );

        assertThat(actualEvent)
                .usingRecursiveComparison()
                .isEqualTo(expectedEvent);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
