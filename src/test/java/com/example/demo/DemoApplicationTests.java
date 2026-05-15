package com.example.demo;

import com.example.demo.model.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
class DemoApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void employeeCrudFlowWorks() {
        Employee employee = new Employee();
        employee.setName("John");
        employee.setSurname("Doe");
        employee.setCompensation(new BigDecimal("100.00"));
        employee.setHiredAt(Instant.parse("2025-01-01T00:00:00Z"));

        ResponseEntity<Long> createResponse = restTemplate.postForEntity(url("/api/v1/employees"), employee, Long.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long id = createResponse.getBody();
        assertThat(id).isNotNull();

        ResponseEntity<Employee> getResponse = restTemplate.getForEntity(url("/api/v1/employees/" + id), Employee.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getCompensation()).isEqualByComparingTo("115.00");

        Employee update = new Employee();
        update.setName("Jane");
        update.setSurname("Doe");
        update.setCompensation(new BigDecimal("200.00"));
        update.setHiredAt(Instant.parse("2025-01-02T00:00:00Z"));

        ResponseEntity<Employee> updateResponse = restTemplate.exchange(
                url("/api/v1/employees/" + id),
                HttpMethod.PUT,
                new HttpEntity<>(update),
                Employee.class
        );
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().getName()).isEqualTo("Jane");
        assertThat(updateResponse.getBody().getCompensation()).isEqualByComparingTo("230.00");

        restTemplate.delete(url("/api/v1/employees/" + id));
        ResponseEntity<Employee> afterDelete = restTemplate.getForEntity(url("/api/v1/employees/" + id), Employee.class);
        assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
