package com.example.demo.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class Employee {
    
    private Long id;
    private String name;
    private String surname;
    private BigDecimal compensation;
    private Instant hiredAt;
 
}
