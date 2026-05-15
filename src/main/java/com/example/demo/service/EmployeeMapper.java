package com.example.demo.service;

import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeEntity;

public class EmployeeMapper {


    public static Employee to(EmployeeEntity employeeEntity) {
        if (employeeEntity == null) {
            return null;
        }

        Employee employee = new Employee();
        employee.setId(employeeEntity.getId());
        employee.setName(employeeEntity.getName());
        employee.setSurname(employeeEntity.getSurname());
        employee.setCompensation(employeeEntity.getCompensation());
        employee.setHiredAt(employeeEntity.getHiredAt());

        return employee;
    }

    public static EmployeeEntity toEntity(Employee employee) {
        if (employee == null) {
            return null;
        }

        EmployeeEntity employeeEntity = new EmployeeEntity();
        employeeEntity.setId(employee.getId());
        employeeEntity.setName(employee.getName());
        employeeEntity.setSurname(employee.getSurname());
        employeeEntity.setCompensation(employee.getCompensation());
        employeeEntity.setHiredAt(employee.getHiredAt());

        return employeeEntity;
    }
}
