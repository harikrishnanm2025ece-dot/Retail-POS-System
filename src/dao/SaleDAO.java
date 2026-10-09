package dao;

import model.Payment;
import model.Sale;
import model.SaleItem;
import util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Sales transactions.
 * Enforces atomic database transactions with commit and rollback.
 */
public class SaleDAO {

    private final SaleItemDAO saleItemDAO = new SaleItemDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final ProductDAO productDAO = new ProductDAO();

    /**
     * Executes a complete, atomic sales transaction.
     * Manages setAutoCommit(false), inserts the sale, inserts each sale item,
     * decrements inventory stock with validation, and records the payment.
     * If any operation fails, rolls back the entire transaction.
     *
     * @param sale the sale header information
     * @param items list of products and quantities in this sale
     * @param payment the payment record associated with this sale
     * @return true if the sale was committed successfully, false if rolled back
     */
    public boolean createSale(Sale sale, List<SaleItem> items, Payment payment) {
        if (items == null || items.isEmpty()) {
            System.err.println("[Sale Error] Cannot create a sale without items.");
            return false;
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin database transaction

            // 1. Insert Sale header
            String saleSql = "INSERT INTO sales (invoice_no, customer_id, employee_id, total_amount, status) VALUES (?, ?, ?, ?, ?)";
            int generatedSaleId = -1;

            try (PreparedStatement psSale = conn.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS)) {
                psSale.setString(1, sale.getInvoiceNo());
                if (sale.getCustomerId() != null) {
                    psSale.setInt(2, sale.getCustomerId());
                } else {
                    psSale.setNull(2, Types.INTEGER); // Walk-in customer
                }
                psSale.setInt(3, sale.getEmployeeId());
                psSale.setBigDecimal(4, sale.getTotalAmount());
                psSale.setString(5, (sale.getStatus() != null) ? sale.getStatus() : "COMPLETED");

                int affected = psSale.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("Failed to insert sales header.");
                }

                try (ResultSet rs = psSale.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedSaleId = rs.getInt(1);
                        sale.setSaleId(generatedSaleId);
                    } else {
                        throw new SQLException("Failed to retrieve generated sale_id.");
                    }
                }
            }

            // 2. Insert items and decrement stock
            String itemSql = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";
            String updateStockSql = "UPDATE products SET quantity = quantity - ? WHERE product_id = ? AND quantity >= ?";

            try (PreparedStatement psItem = conn.prepareStatement(itemSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psStock = conn.prepareStatement(updateStockSql)) {

                for (SaleItem item : items) {
                    item.setSaleId(generatedSaleId);

                    // Insert item line
                    psItem.setInt(1, generatedSaleId);
                    psItem.setInt(2, item.getProductId());
                    psItem.setInt(3, item.getQuantity());
                    psItem.setBigDecimal(4, item.getUnitPrice());
                    psItem.setBigDecimal(5, item.getSubtotal());
                    psItem.executeUpdate();

                    try (ResultSet rsItem = psItem.getGeneratedKeys()) {
                        if (rsItem.next()) {
                            item.setItemId(rsItem.getInt(1));
                        }
                    }

                    // Decrement stock conditionally
                    psStock.setInt(1, item.getQuantity());
                    psStock.setInt(2, item.getProductId());
                    psStock.setInt(3, item.getQuantity());
                    int stockRows = psStock.executeUpdate();

                    if (stockRows == 0) {
                        throw new SQLException("Insufficient stock or invalid product ID: " + item.getProductId());
                    }
                }
            }

            // 3. Record Payment
            String paymentSql = "INSERT INTO payments (sale_id, payment_method, amount, payment_status) VALUES (?, ?, ?, ?)";
            try (PreparedStatement psPay = conn.prepareStatement(paymentSql, Statement.RETURN_GENERATED_KEYS)) {
                payment.setSaleId(generatedSaleId);
                psPay.setInt(1, generatedSaleId);
                psPay.setString(2, payment.getPaymentMethod().toUpperCase());
                psPay.setBigDecimal(3, payment.getAmount());
                psPay.setString(4, (payment.getPaymentStatus() != null) ? payment.getPaymentStatus() : "SUCCESS");

                int payRows = psPay.executeUpdate();
                if (payRows == 0) {
                    throw new SQLException("Failed to record payment for sale.");
                }

                try (ResultSet rsPay = psPay.getGeneratedKeys()) {
                    if (rsPay.next()) {
                        payment.setPaymentId(rsPay.getInt(1));
                    }
                }
            }

            // Commit the entire transaction atomically
            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("[Sale Transaction Failed] " + e.getMessage());
            if (conn != null) {
                try {
                    System.err.println("[Rollback] Rolling back database transaction...");
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("[Rollback Error] " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Retrieves all sales records joined with customer and employee names.
     *
     * @return List of sales
     */
    public List<Sale> getAllSales() {
        List<Sale> list = new ArrayList<>();
        String sql = "SELECT s.sale_id, s.invoice_no, s.customer_id, s.employee_id, s.sale_date, s.total_amount, s.status, " +
                     "       COALESCE(c.name, 'Walk-in Customer') AS customer_name, " +
                     "       e.name AS employee_name " +
                     "FROM sales s " +
                     "LEFT JOIN customers c ON s.customer_id = c.customer_id " +
                     "JOIN employees e ON s.employee_id = e.employee_id " +
                     "ORDER BY s.sale_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToSale(rs));
            }
        } catch (SQLException e) {
            System.err.println("[Error in getAllSales] " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves a complete sale by ID, populated with line items and payment details.
     *
     * @param saleId sale ID
     * @return populated Sale object or null
     */
    public Sale getSaleById(int saleId) {
        String sql = "SELECT s.sale_id, s.invoice_no, s.customer_id, s.employee_id, s.sale_date, s.total_amount, s.status, " +
                     "       COALESCE(c.name, 'Walk-in Customer') AS customer_name, " +
                     "       e.name AS employee_name " +
                     "FROM sales s " +
                     "LEFT JOIN customers c ON s.customer_id = c.customer_id " +
                     "JOIN employees e ON s.employee_id = e.employee_id " +
                     "WHERE s.sale_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Sale sale = mapResultSetToSale(rs);
                    sale.setItems(saleItemDAO.getItemsBySaleId(saleId));
                    sale.setPayment(paymentDAO.getPaymentBySaleId(saleId));
                    return sale;
                }
            }
        } catch (SQLException e) {
            System.err.println("[Error in getSaleById] " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves a complete sale by invoice number.
     *
     * @param invoiceNo invoice number string
     * @return populated Sale object or null
     */
    public Sale getSaleByInvoiceNo(String invoiceNo) {
        String sql = "SELECT s.sale_id, s.invoice_no, s.customer_id, s.employee_id, s.sale_date, s.total_amount, s.status, " +
                     "       COALESCE(c.name, 'Walk-in Customer') AS customer_name, " +
                     "       e.name AS employee_name " +
                     "FROM sales s " +
                     "LEFT JOIN customers c ON s.customer_id = c.customer_id " +
                     "JOIN employees e ON s.employee_id = e.employee_id " +
                     "WHERE s.invoice_no = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, invoiceNo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Sale sale = mapResultSetToSale(rs);
                    sale.setItems(saleItemDAO.getItemsBySaleId(sale.getSaleId()));
                    sale.setPayment(paymentDAO.getPaymentBySaleId(sale.getSaleId()));
                    return sale;
                }
            }
        } catch (SQLException e) {
            System.err.println("[Error in getSaleByInvoiceNo] " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all sales processed on a specific calendar date with status 'COMPLETED'.
     *
     * @param date target LocalDate
     * @return List of completed sales on that date
     */
    public List<Sale> getDailySales(LocalDate date) {
        List<Sale> list = new ArrayList<>();
        String sql = "SELECT s.sale_id, s.invoice_no, s.customer_id, s.employee_id, s.sale_date, s.total_amount, s.status, " +
                     "       COALESCE(c.name, 'Walk-in Customer') AS customer_name, " +
                     "       e.name AS employee_name " +
                     "FROM sales s " +
                     "LEFT JOIN customers c ON s.customer_id = c.customer_id " +
                     "JOIN employees e ON s.employee_id = e.employee_id " +
                     "WHERE DATE(s.sale_date) = ? AND s.status = 'COMPLETED' " +
                     "ORDER BY s.sale_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSale(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[Error in getDailySales] " + e.getMessage());
        }
        return list;
    }

    /**
     * Calculates total revenue generated across all completed sales transactions.
     * Failed or cancelled transactions are excluded.
     *
     * @return total revenue as BigDecimal
     */
    public BigDecimal getTotalSalesRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0.00) AS total_revenue FROM sales WHERE status = 'COMPLETED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getBigDecimal("total_revenue");
            }
        } catch (SQLException e) {
            System.err.println("[Error in getTotalSalesRevenue] " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    /**
     * Generates a unique invoice number using date and sequence timestamp.
     *
     * @return formatted invoice number e.g. INV-20261009-1234
     */
    public String generateInvoiceNumber() {
        long timestamp = System.currentTimeMillis() % 1000000;
        String datePart = LocalDate.now().toString().replace("-", "");
        return String.format("INV-%s-%06d", datePart, timestamp);
    }

    private Sale mapResultSetToSale(ResultSet rs) throws SQLException {
        Integer custId = rs.getInt("customer_id");
        if (rs.wasNull()) {
            custId = null;
        }

        Sale sale = new Sale(
                rs.getInt("sale_id"),
                rs.getString("invoice_no"),
                custId,
                rs.getInt("employee_id"),
                rs.getTimestamp("sale_date"),
                rs.getBigDecimal("total_amount"),
                rs.getString("status")
        );
        sale.setCustomerName(rs.getString("customer_name"));
        sale.setEmployeeName(rs.getString("employee_name"));
        return sale;
    }
}
