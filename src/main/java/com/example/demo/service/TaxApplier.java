package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class TaxApplier {

    @Value("${compensation.taxes.number}")
    private BigDecimal taxRate;

    public BigDecimal applyTaxes(BigDecimal compensation) {
        var tax = compensation.multiply(taxRate).divide(BigDecimal.valueOf(100), RoundingMode.HALF_DOWN);
        return compensation.add(tax);
    }
}
