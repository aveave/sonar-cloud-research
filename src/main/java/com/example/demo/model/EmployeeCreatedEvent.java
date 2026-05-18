package com.example.demo.model;

import java.math.BigDecimal;
import java.time.Instant;

public record EmployeeCreatedEvent(
        Long id,
        String name,
        String surname,
        BigDecimal compensation,
        Instant hiredAt
) {
}
