package dao;

import model.Employee;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Employee entity.
 * Handles adding, updating, viewing, searching, and deleting employees.
 */
public class EmployeeDAO {

    /**
     * Adds a new employee to the database.
     *
     * @param employee Employee to insert
     * @return true if successful, false otherwise
     */
    public boolean addEmployee(Employee employee) {
        String sql = "INSERT INTO employees (name, role, phone) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, employee.getName());
            ps.setString(2, employee.getRole());
            ps.setString(3, employee.getPhone());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        employee.setEmployeeId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[Error in addEmployee] " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieves all employees.
     *
     * @return List of employees
     */
    public List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT employee_id, name, role, phone, created_at FROM employees ORDER BY employee_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToEmployee(rs));
            }
        } catch (SQLException e) {
            System.err.println("[Error in getAllEmployees] " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves an employee by their ID.
     *
     * @param employeeId ID of employee
     * @return Employee if found, null otherwise
     */
    public Employee getEmployeeById(int employeeId) {
        String sql = "SELECT employee_id, name, role, phone, created_at FROM employees WHERE employee_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmployee(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[Error in getEmployeeById] " + e.getMessage());
        }
        return null;
    }

    /**
     * Searches employees by name match.
     *
     * @param name search string
     * @return List of matching employees
     */
    public List<Employee> searchEmployeesByName(String name) {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT employee_id, name, role, phone, created_at FROM employees WHERE LOWER(name) LIKE ? ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + name.toLowerCase().trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEmployee(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[Error in searchEmployeesByName] " + e.getMessage());
        }
        return list;
    }

    /**
     * Updates an employee's details.
     *
     * @param employee Employee with updated info
     * @return true if updated, false otherwise
     */
    public boolean updateEmployee(Employee employee) {
        String sql = "UPDATE employees SET name = ?, role = ?, phone = ? WHERE employee_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, employee.getName());
            ps.setString(2, employee.getRole());
            ps.setString(3, employee.getPhone());
            ps.setInt(4, employee.getEmployeeId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[Error in updateEmployee] " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes an employee by ID.
     *
     * @param employeeId ID of employee to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteEmployee(int employeeId) {
        String sql = "DELETE FROM employees WHERE employee_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, employeeId);
            return ps.executeUpdate() > 0;
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            System.err.println("[Integrity Notice] Cannot delete employee ID " + employeeId + " as they are referenced in processed sales records.");
        } catch (SQLException e) {
            System.err.println("[Error in deleteEmployee] " + e.getMessage());
        }
        return false;
    }

    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        return new Employee(
                rs.getInt("employee_id"),
                rs.getString("name"),
                rs.getString("role"),
                rs.getString("phone"),
                rs.getTimestamp("created_at")
        );
    }
}
