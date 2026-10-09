package dao;

import model.Customer;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Customer entity.
 * Handles customer registration, updates, search, and retrieval.
 */
public class CustomerDAO {

    /**
     * Registers a new customer in the database.
     *
     * @param customer Customer to register
     * @return true if successful, false otherwise
     */
    public boolean registerCustomer(Customer customer) {
        String sql = "INSERT INTO customers (name, phone, email) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, customer.getName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        customer.setCustomerId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[Error in registerCustomer] " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieves all registered customers.
     *
     * @return List of customers
     */
    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT customer_id, name, phone, email, created_at FROM customers ORDER BY customer_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            System.err.println("[Error in getAllCustomers] " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves a customer by their unique ID.
     *
     * @param customerId ID of customer
     * @return Customer if found, null otherwise
     */
    public Customer getCustomerById(int customerId) {
        String sql = "SELECT customer_id, name, phone, email, created_at FROM customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCustomer(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[Error in getCustomerById] " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves a customer by phone number.
     *
     * @param phone Customer phone number
     * @return Customer if found, null otherwise
     */
    public Customer getCustomerByPhone(String phone) {
        String sql = "SELECT customer_id, name, phone, email, created_at FROM customers WHERE phone = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, phone.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCustomer(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[Error in getCustomerByPhone] " + e.getMessage());
        }
        return null;
    }

    /**
     * Searches customers by partial name match.
     *
     * @param name search string
     * @return List of matching customers
     */
    public List<Customer> searchCustomersByName(String name) {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT customer_id, name, phone, email, created_at FROM customers WHERE LOWER(name) LIKE ? ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + name.toLowerCase().trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[Error in searchCustomersByName] " + e.getMessage());
        }
        return list;
    }

    /**
     * Updates an existing customer's information.
     *
     * @param customer Customer with updated information
     * @return true if updated successfully, false otherwise
     */
    public boolean updateCustomer(Customer customer) {
        String sql = "UPDATE customers SET name = ?, phone = ?, email = ? WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customer.getName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setInt(4, customer.getCustomerId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[Error in updateCustomer] " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes a customer by their ID.
     *
     * @param customerId ID of customer to delete
     * @return true if deleted successfully, false otherwise
     */
    public boolean deleteCustomer(int customerId) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[Error in deleteCustomer] " + e.getMessage());
        }
        return false;
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("customer_id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getTimestamp("created_at")
        );
    }
}
