package model;

import java.sql.Timestamp;

/**
 * Model representing a retail Employee.
 */
public class Employee {
    private int employeeId;
    private String name;
    private String role;
    private String phone;
    private Timestamp createdAt;

    public Employee() {
    }

    public Employee(String name, String role, String phone) {
        this.name = name;
        this.role = role;
        this.phone = phone;
    }

    public Employee(int employeeId, String name, String role, String phone, Timestamp createdAt) {
        this.employeeId = employeeId;
        this.name = name;
        this.role = role;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("Employee [ID=%d, Name='%s', Role='%s', Phone='%s']",
                employeeId, name, role, phone);
    }
}
