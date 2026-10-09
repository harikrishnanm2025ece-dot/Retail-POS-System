package dao;

import model.Payment;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Payment entity.
 * Handles recording and querying payments.
 */
public class PaymentDAO {

    /**
     * Records a payment inside an active transactional connection.
     *
     * @param conn active transactional Connection
     * @param payment Payment details to persist
     * @return true if successful
     * @throws SQLException on database error
     */
    public boolean recordPayment(Connection conn, Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (sale_id, payment_method, amount, payment_status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, payment.getSaleId());
            ps.setString(2, payment.getPaymentMethod().toUpperCase());
            ps.setBigDecimal(3, payment.getAmount());
            ps.setString(4, (payment.getPaymentStatus() != null) ? payment.getPaymentStatus() : "SUCCESS");

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        payment.setPaymentId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Standalone method to record a payment with auto-commit.
     *
     * @param payment Payment to record
     * @return true if successful, false otherwise
     */
    public boolean recordPayment(Payment payment) {
        try (Connection conn = DBConnection.getConnection()) {
            return recordPayment(conn, payment);
        } catch (SQLException e) {
            System.err.println("[Error in recordPayment] " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all recorded payments.
     *
     * @return List of payments
     */
    public List<Payment> getAllPayments() {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT p.payment_id, p.sale_id, p.payment_method, p.amount, p.payment_date, p.payment_status, s.invoice_no " +
                     "FROM payments p " +
                     "JOIN sales s ON p.sale_id = s.sale_id " +
                     "ORDER BY p.payment_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Payment payment = mapResultSetToPayment(rs);
                payment.setInvoiceNo(rs.getString("invoice_no"));
                list.add(payment);
            }
        } catch (SQLException e) {
            System.err.println("[Error in getAllPayments] " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves a payment by payment ID.
     *
     * @param paymentId payment identifier
     * @return Payment if found, null otherwise
     */
    public Payment getPaymentById(int paymentId) {
        String sql = "SELECT p.payment_id, p.sale_id, p.payment_method, p.amount, p.payment_date, p.payment_status, s.invoice_no " +
                     "FROM payments p " +
                     "JOIN sales s ON p.sale_id = s.sale_id " +
                     "WHERE p.payment_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Payment payment = mapResultSetToPayment(rs);
                    payment.setInvoiceNo(rs.getString("invoice_no"));
                    return payment;
                }
            }
        } catch (SQLException e) {
            System.err.println("[Error in getPaymentById] " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves the payment for a specific sale.
     *
     * @param saleId sale identifier
     * @return Payment if found, null otherwise
     */
    public Payment getPaymentBySaleId(int saleId) {
        String sql = "SELECT p.payment_id, p.sale_id, p.payment_method, p.amount, p.payment_date, p.payment_status, s.invoice_no " +
                     "FROM payments p " +
                     "JOIN sales s ON p.sale_id = s.sale_id " +
                     "WHERE p.sale_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Payment payment = mapResultSetToPayment(rs);
                    payment.setInvoiceNo(rs.getString("invoice_no"));
                    return payment;
                }
            }
        } catch (SQLException e) {
            System.err.println("[Error in getPaymentBySaleId] " + e.getMessage());
        }
        return null;
    }

    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        return new Payment(
                rs.getInt("payment_id"),
                rs.getInt("sale_id"),
                rs.getString("payment_method"),
                rs.getBigDecimal("amount"),
                rs.getTimestamp("payment_date"),
                rs.getString("payment_status")
        );
    }
}
