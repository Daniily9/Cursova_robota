
package com.company;

import java.math.BigDecimal;

public class Employee {
    private int id;
    private String firstName;
    private String lastName;
    private String position;
    private BigDecimal salary;
    private int departmentId;

    public Employee(int id, String firstName,
                    String lastName, String position,
                    BigDecimal salary, int departmentId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.position = position;
        this.salary = salary;
        this.departmentId = departmentId;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPosition() {
        return position;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }
}
