package com.example.demo.service;

import com.example.demo.model.Employee;
import com.example.demo.model.EmployeeCreatedEvent;
import com.example.demo.repository.EmployeeEntity;
import com.example.demo.repository.EmployeeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeJpaRepository employeeJpaRepository;
    private final TaxApplier taxApplier;
    private final EmployeeEventPublisher employeeEventPublisher;

    public List<Employee> getEmployees() {
        return employeeJpaRepository.findAll().stream()
                .map(EmployeeMapper::to)
                .toList();
    }

    public Employee getEmployeeById(Long id) {
        EmployeeEntity entity = employeeJpaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found with id: " + id));
        return EmployeeMapper.to(entity);
    }

    public Long createEmployee(Employee employee) {
        employee.setCompensation(taxApplier.applyTaxes(employee.getCompensation()));
        EmployeeEntity entity = employeeJpaRepository.save(EmployeeMapper.toEntity(employee));

        employeeEventPublisher.publishEmployeeCreated(new EmployeeCreatedEvent(
                entity.getId(),
                entity.getName(),
                entity.getSurname(),
                entity.getCompensation(),
                entity.getHiredAt()
        ));

        return entity.getId();
    }

    public Employee updateEmployee(Long id, Employee employee) {
        EmployeeEntity existing = employeeJpaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found with id: " + id));

        existing.setName(employee.getName());
        existing.setSurname(employee.getSurname());
        existing.setHiredAt(employee.getHiredAt());
        existing.setCompensation(taxApplier.applyTaxes(employee.getCompensation()));

        EmployeeEntity saved = employeeJpaRepository.save(existing);
        return EmployeeMapper.to(saved);
    }

    public void deleteEmployee(Long id) {
        if (!employeeJpaRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found with id: " + id);
        }
        employeeJpaRepository.deleteById(id);
    }
}
